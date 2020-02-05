package com.example.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.cart.mapper.OmsCartItemMapper;
import com.example.gmall.util.RedisUtil;
import com.gmall.bean.OmsCartItem;
import com.gmall.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 添加商品到购物车
     * @param omsCartItem
     */
    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (omsCartItem.getMemberId() != null) {
            omsCartItemMapper.insert(omsCartItem);
        }
    }

    /**
     * 更新购物车数据
     * @param omsCartItemFromDb
     */
    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, e);
    }

    /**
     * 判断商品是否已经添加到购物车里了
     * @param memberId
     * @param skuId
     * @return
     */
    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    //同步缓存
    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> select = omsCartItemMapper.select(omsCartItem);
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : select) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        Jedis jedis = redisUtil.getJedis();
        try {
            jedis.del("user:" + memberId + ":cart");
            jedis.hmset("user:" + memberId + ":cart", map);
        } finally {
            jedis.close();
        }
    }

    /**
     * 从缓存查询，购物车商品列表，返回给controller
     * @param userId
     * @return
     */
    @Override
    public List<OmsCartItem> cartList(String userId) {
        List<OmsCartItem> omsCartItemList=new LinkedList<>();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + userId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem1 = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItemList.add(omsCartItem1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //    String message=e.getMessage();
            return null;
        } finally {
            jedis.close();
        }
        return omsCartItemList;
    }
    /**
     * 更新购物车商品状态
     */
    @Override
    public void cheakCart(OmsCartItem omsCartItem) {
        Example e=new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);
        flushCartCache(omsCartItem.getMemberId());
    }
}
