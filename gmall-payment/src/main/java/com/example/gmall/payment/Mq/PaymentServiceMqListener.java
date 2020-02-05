package com.example.gmall.payment.Mq;

import com.gmall.bean.PaymentInfo;
import com.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {
    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumPaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        Integer count = 0;
        if(mapMessage.getString("count")!=null)
            count= Integer.parseInt("" + mapMessage.getString("count"));
        //调用支付宝的检查接口
        Map<String, Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
        if (resultMap != null && !resultMap.isEmpty()) {
            String trade_status = (String) resultMap.get("trade_status");
            if (StringUtils.isNotBlank(trade_status) &&trade_status.equals("TRADE_SUCCESS")) {
                System.out.println("zaici");
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOrderSn(out_trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setAlipayTradeNo((String) resultMap.get("trade_no"));
                paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));
                paymentService.updatePayment(paymentInfo);
                System.out.println("支付成功");
                return;
            }
        }
        if (count > 0) {
            System.out.println("没有支付成功"+count);
            count--;
            paymentService.sendDelayPaymentResultQueue(out_trade_no, count);
        } else {
            System.out.println("检查次数用尽");
        }
    }
}
