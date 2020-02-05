package com.example.gmall.user.service.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.example.gmall.user.mapper.UserMapper;
import com.example.gmall.util.RedisUtil;
import com.gmall.bean.UmsMember;
import com.gmall.bean.UmsMemberReceiveAddress;
import com.gmall.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> selectAll() {
        //   return userMapper.getAllUser();
        List<UmsMember> list = userMapper.selectAll();
        return list;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddr(String memberId) {
//        Example e=new Example(UmsMemberReceiveAddress.class) ;
//        e.createCriteria().andEqualTo("memberId",memberId);
//        umsMemberReceiveAddressMapper.selectByExample(e);
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        // List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(umsMemberReceiveAddress);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String s = jedis.get("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info");
                if (StringUtils.isNotBlank(s)) {
                    UmsMember umsMember1 = JSON.parseObject(s, UmsMember.class);
                    return umsMember1;
                }
            }
            //密码错误
            //缓存没数据--开数据库
            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));
            }
            return umsMemberFromDb;
        } finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token,String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",2*60*60,token);
        jedis.close();
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
          userMapper.insert(umsMember);
          return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(String s) {
        UmsMember umsMember=new UmsMember();
        umsMember.setSourceUid(s);
       return userMapper.selectOne(umsMember);
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddrById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
        List<UmsMember> select = userMapper.select(umsMember);
        if(select!=null){
            return select.get(0);
        }
        return null;
    }
}
