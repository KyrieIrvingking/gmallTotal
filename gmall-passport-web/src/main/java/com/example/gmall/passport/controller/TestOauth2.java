package com.example.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.example.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {


    public static   String getCode(){
        //App Key：
        //    2432222636
        //http://127.0.01:8085/vlogin
        //http://127.0.0.1:8085/vlogin?code=1d78a56bb7c5c8ce3afb4c48dd654ccd
         String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2432222636&response_type=code&redirect_uri=http://127.0.01:8085/vlogin");
         System.out.println(s1);
         String s2="http://127.0.0.1:8085/vlogin?code=8b0365c30447bcbcdecf4c9e4f98c410";
         return null;
    }
    public static  String getAccess_token(){
        //App Key：
        //    2432222636
        //App Secret：
        //    aa1a707e1bb80621a010edd476ea63bb
        String s3="https://api.weibo.com/oauth2/access_token?";//?client_id=2432222636&client_secret=aa1a707e1bb80621a010edd476ea63bb&grant_type=authorization_code&redirect_uri=http://127.0.01:8085/vlogin&code=CODE";
        Map<String,String> map=new HashMap<>();
        map.put("client_id","2432222636");
        map.put("client_secret","aa1a707e1bb80621a010edd476ea63bb");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://127.0.01:8085/vlogin");
        map.put("code","8b0365c30447bcbcdecf4c9e4f98c410");
        String access_token=HttpclientUtil.doPost(s3,map);
        Map<String,String> map1 = JSON.parseObject(access_token, Map.class);
        System.out.println(map1.get("access_token"));
        return map1.get("access_token");
    }
    public  static Map<String,String> getUserInfo(){
        //2.009cDkGG3N3beCa8a3cb1a5eXFR8FD
        String s4="https://api.weibo.com/2/users/show.json?access_token=2.009cDkGG3N3beCa8a3cb1a5eXFR8FD&uid=1";
        String user_json=HttpclientUtil.doGet(s4);
        Map<String,String> map2 = JSON.parseObject(user_json, Map.class);
        return map2;
    }
    public static void main(String[] args) {
       getCode();
//       getAccess_token();
//       getUserInfo();


    }
}
