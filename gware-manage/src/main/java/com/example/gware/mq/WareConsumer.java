package com.example.gware.mq;

import com.alibaba.fastjson.JSON;

import com.example.gware.bean.OmsOrder;

import com.example.gware.bean.OmsOrderItem;
import com.example.gware.bean.WmsWareOrderTask;
import com.example.gware.bean.WmsWareOrderTaskDetail;
import com.example.gware.util.ActiveMQUtil;
import com.example.gware.enums.TaskStatus;
import com.example.gware.mapper.WareOrderTaskDetailMapper;
import com.example.gware.mapper.WareOrderTaskMapper;
import com.example.gware.mapper.WareSkuMapper;
import com.example.gware.service.GwareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.*;

/**
 * @param
 * @return
 */
@Component
public class WareConsumer {

    @Autowired
    WareOrderTaskMapper wareOrderTaskMapper;

    @Autowired
    WareOrderTaskDetailMapper wareOrderTaskDetailMapper;

    @Autowired
    WareSkuMapper wareSkuMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    GwareService gwareService;

    @JmsListener(destination = "ORDER_PAY_QUEUE", containerFactory = "jmsQueueListener")
    public void receiveOrder(TextMessage textMessage) throws JMSException {
        String orderTaskJson = textMessage.getText();

        /***
         * 转化并保存订单对象
         */
        OmsOrder orderInfo = JSON.parseObject(orderTaskJson, OmsOrder.class);

        // 将order订单对象转为订单任务对象
        WmsWareOrderTask wmsWareOrderTask = new WmsWareOrderTask();
        wmsWareOrderTask.setConsignee(orderInfo.getReceiverName());
        wmsWareOrderTask.setConsigneeTel(orderInfo.getReceiverPhone());
        wmsWareOrderTask.setCreateTime(new Date());
        wmsWareOrderTask.setDeliveryAddress(orderInfo.getReceiverDetailAddress());
        wmsWareOrderTask.setOrderId(orderInfo.getId());
        ArrayList<WmsWareOrderTaskDetail> wmsWareOrderTaskDetails = new ArrayList<>();

        // 打开订单的商品集合
        List<OmsOrderItem> orderDetailList = orderInfo.getOmsOrderItems();
        for (OmsOrderItem orderDetail : orderDetailList) {
            WmsWareOrderTaskDetail wmsWareOrderTaskDetail = new WmsWareOrderTaskDetail();

            wmsWareOrderTaskDetail.setSkuId(orderDetail.getProductSkuId());
            wmsWareOrderTaskDetail.setSkuName(orderDetail.getProductName());
            wmsWareOrderTaskDetail.setSkuNum(orderDetail.getProductQuantity());
            wmsWareOrderTaskDetails.add(wmsWareOrderTaskDetail);

        }
        wmsWareOrderTask.setDetails(wmsWareOrderTaskDetails);
        wmsWareOrderTask.setTaskStatus(TaskStatus.PAID);
        gwareService.saveWareOrderTask(wmsWareOrderTask);

        textMessage.acknowledge();

        // 检查该交易的商品是否有拆单需求
        List<WmsWareOrderTask> wareSubOrderTaskList = gwareService.checkOrderSplit(wmsWareOrderTask);// 检查拆单

        // 库存削减
        if (wareSubOrderTaskList != null && wareSubOrderTaskList.size() >= 2) {
            for (WmsWareOrderTask orderTask : wareSubOrderTaskList) {
                gwareService.lockStock(orderTask);
            }
        } else {
            gwareService.lockStock(wmsWareOrderTask);
        }


    }

}
