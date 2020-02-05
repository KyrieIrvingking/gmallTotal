package com.example.gmall.order.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.mq.ActiveMQUtil;
import com.example.gmall.order.mapper.OmsOrderItemMapper;
import com.example.gmall.order.mapper.OmsOrderMapper;
import com.example.gmall.util.RedisUtil;
import com.gmall.bean.OmsOrder;
import com.gmall.bean.OmsOrderItem;
import com.gmall.service.OrderService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
             jedis = redisUtil.getJedis();
             String s = jedis.get("user:" + memberId + "tradeCode");
            //使用lua脚本 防止高并发订单攻击
            //对比防重删令牌
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList("user:" + memberId + "tradeCode"), Collections.singletonList(tradeCode));
            if(eval!=null && eval!=0){
               // jedis.del("user:" + memberId + "tradeCode");
                return "success";
            }else {
                return "fail";
            }
        }finally {
            jedis.close();
        }
    }
    @Override
    public String generate(String memberId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodekey="user:"+memberId+"tradeCode";
        String tradeCodeValue= UUID.randomUUID()+"";
        jedis.setex(tradeCodekey,60*60*2,tradeCodeValue);
        return tradeCodeValue;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String orderId=omsOrder.getId();
        List<OmsOrderItem> omsOrderItems=omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //cartService.del()
        }


    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder=new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1= omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example e=new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();

        omsOrderUpdate.setStatus("1");
        //发送一个订单已支付队列
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);
            TextMessage textMessage=new ActiveMQTextMessage();
            // MapMessage mapMessage=new ActiveMQMapMessage();
            // mapMessage.setString();
            //查询订单对象
            OmsOrder omsOrder1=new OmsOrder();
            omsOrder1.setOrderSn(omsOrder.getOrderSn());
            OmsOrder omsOrder2 = omsOrderMapper.selectOne(omsOrder1);
            OmsOrderItem omsOrderItem=new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
            omsOrder2.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrder2));
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate,e);
            producer.send(textMessage);
            session.commit();
        } catch (JMSException ex) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
            ex.printStackTrace();
        }
        finally {
            try {
                connection.close();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }
}
