package com.example.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.util.HttpclientUtil;
import com.example.gmall.util.JwtUtil;
import com.gmall.bean.UmsMember;
import com.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;
    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){
       //用code换取access_token
            String s3 = "https://api.weibo.com/oauth2/access_token?";
            Map<String, String> map = new HashMap<>();
            map.put("client_id", "2432222636");
            map.put("client_secret", "aa1a707e1bb80621a010edd476ea63bb");
            map.put("grant_type", "authorization_code");
            map.put("redirect_uri", "http://127.0.01:8085/vlogin");
            map.put("code", code);
            String access_token = HttpclientUtil.doPost(s3, map);
            Map<String, Object> map1 = JSON.parseObject(access_token, Map.class);
            String uid=(String)map1.get("uid");
            access_token=(String) map1.get("access_token");
        //根据access_token获取用户信息
        String s4="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json=HttpclientUtil.doGet(s4);
        Map<String,String> map2 = JSON.parseObject(user_json, Map.class);
        //保存用户信息
        //生成jwt的token,并且携带token重定向到页面，
        UmsMember umsMember=new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setCity((String) map2.get("location"));
        umsMember.setNickname(map2.get("screen_name"));
        String s= map2.get("idstr");
        System.out.println(s);
        umsMember.setSourceUid(s);
        umsMember.setGender(map2.get("gender"));
        UmsMember umsCheck=new UmsMember();
        UmsMember umsMember1=userService.checkOauthUser(s);//判断用户是否登陆过
        if(umsMember1==null) {
            umsCheck=userService.addOauthUser(umsMember);
        }else {
            umsCheck=umsMember1;
        }
        String memberId=umsCheck.getId();
        String nickName=umsCheck.getNickname();
        String token=getToken(request,memberId,nickName);
        return "redirect:http://localhost:8083/index?token="+token;
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String token="";
        UmsMember umsMemberLogin=userService.login(umsMember);
        if(umsMemberLogin!=null){
            //登陆成功
            //用jwt制作token
            String id = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String,Object> map=new HashMap<>();
//            map.put("memberId",id);
//            map.put("nickname",nickname);
//            String remoteAddr = request.getHeader("x-forwarded-for");
//            if(StringUtils.isBlank(remoteAddr)){
//                remoteAddr = request.getRemoteAddr();
//                if(StringUtils.isBlank(remoteAddr)){
//                    remoteAddr="10.0.6.126";
//                }
//            }
//            //需要加密生成token
//            token = JwtUtil.encode("2019gmall",map,remoteAddr);
            //将token存入redis一份
            token=getToken(request,nickname,id);
            userService.addUserToken(token,umsMemberLogin.getId());
        }else {
           token="fail";
        }
        return token;
    }
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String  token,String currentIp){
        Map<String,String> map=new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token,"2019gmall",  currentIp);
        if(decode!=null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        }else {
            map.put("status","fail");
        }
        return JSON.toJSONString(map);
    }
    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(String ReturnUrl, ModelMap modelMap)
    {
        if(StringUtils.isNotBlank(ReturnUrl)){
            modelMap.put("ReturnUrl",ReturnUrl);
        }
        return "index";
    }
    public String getToken(HttpServletRequest request,String nickname,String id){
        Map<String,Object> map=new HashMap<>();
        map.put("memberId",id);
        map.put("nickname",nickname);
        String remoteAddr = request.getHeader("x-forwarded-for");
        if(StringUtils.isBlank(remoteAddr)){
            remoteAddr = request.getRemoteAddr();
            if(StringUtils.isBlank(remoteAddr)){
                remoteAddr="10.0.6.126";
            }
        }
        //需要加密生成token
        String  token = JwtUtil.encode("2019gmall",map,remoteAddr);
        return token;
    }
}
