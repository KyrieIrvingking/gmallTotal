package com.gmall.service;

import com.gmall.bean.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);

    String generate(String memberId);


    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
