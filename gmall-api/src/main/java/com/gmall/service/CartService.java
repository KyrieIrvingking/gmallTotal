package com.gmall.service;

import com.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String userId);

    void cheakCart(OmsCartItem omsCartItem);
}
