package com.gmall.service;


import com.gmall.bean.UmsMember;
import com.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> selectAll();
    List<UmsMemberReceiveAddress> getAddr(String memberId);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token,String memeberId);

    UmsMember addOauthUser(UmsMember umsMember);

    UmsMember checkOauthUser(String s);

    UmsMemberReceiveAddress getReceiveAddrById(String receiveAddressId);
}
