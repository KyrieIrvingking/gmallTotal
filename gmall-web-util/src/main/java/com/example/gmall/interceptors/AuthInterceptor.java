package com.example.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.util.CookieUtil;
import com.example.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handle){
          HandlerMethod hm = (HandlerMethod) handle;
          LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
          if(methodAnnotation==null){
              return true;
          }
          String token="";
          String oldToken=CookieUtil.getCookieValue(request,"oldToken",true);
          if(StringUtils.isNotBlank(oldToken)){
              token=oldToken;
          }
          String newToken=request.getParameter("token");
          if(StringUtils.isNotBlank(newToken)){
              token=newToken;
          }
          boolean b = methodAnnotation.loginSuccess();
          //调用验证中心验证
         String success="fail";
          Map<String,String> successMap=new HashMap<>();
         if(StringUtils.isNotBlank(token)) {
             String remoteAddr = request.getHeader("x-forwarded-for");
             if(StringUtils.isBlank(remoteAddr)){
                 remoteAddr = request.getRemoteAddr();
                 if(StringUtils.isBlank(remoteAddr)){
                     remoteAddr="10.0.6.126";
                 }
             }
           String successJson=HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token+"&currentIp="+remoteAddr);
           successMap= JSON.parseObject(successJson,Map.class);
           success=successMap.get("status");
         }
          if (b){
              //必须登陆才能使用
             if(!success.equals("success")){
                 //重定向到登陆
                 try {
                     response.sendRedirect("http://localhost:8085/index?ReturnUrl="+request.getRequestURL());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return false;
             }
                 //验证通过，覆盖cookie中的token
                 request.setAttribute("memberId",successMap.get("memberId"));
                 request.setAttribute("nickname",successMap.get("nickname"));
              if(StringUtils.isNotBlank(token))
                  CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
          }else {
                 //没有登录也能用，但是必须验证
              if(success.equals("success")){
                 //需要将token携带的信息写入
                  request.setAttribute("memberId",successMap.get("memberId"));
                  request.setAttribute("nickname",successMap.get("nickname"));
                  if(StringUtils.isNotBlank(token))
                      CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
              }
                  return true;
          }
          return true;
      }
}
