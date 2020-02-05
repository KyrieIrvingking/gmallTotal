package com.example.gmall.user.controller;

import com.gmall.bean.UmsMember;
import com.gmall.bean.UmsMemberReceiveAddress;
import com.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;
    @RequestMapping("index")
    @ResponseBody
    public String sayHello(){
        return "hello";
    }
    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
      List<UmsMember> umsMembers=  userService.selectAll();
      return  umsMembers;
    }
    @RequestMapping("getReceiveAddr")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddr( String memberId){
        List<UmsMemberReceiveAddress> addr=userService.getAddr(memberId);
        return addr;
    }
}
