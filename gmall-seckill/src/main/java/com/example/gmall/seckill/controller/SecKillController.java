package com.example.gmall.seckill.controller;

import com.example.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @RequestMapping("seckill")
    @ResponseBody
    public String redisson(){
        Jedis jedis = redisUtil.getJedis();
        RSemaphore semaphore = redissonClient.getSemaphore("106");
        boolean b = semaphore.tryAcquire();
        int i = Integer.parseInt(jedis.get("106"));
        if(b){
            System.out.println("当前"+(1000-i));
            //用消息队列发出订单消息
        }else {
            System.out.println("失败");
        }
        jedis.close();
        return "2";
    }
    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        Jedis jedis = redisUtil.getJedis();
        jedis.watch("106");
        int i = Integer.parseInt(jedis.get("106"));
        if(i>0) {
            Transaction multi = jedis.multi();
            multi.incrBy("106", -1);
            List<Object> exec = multi.exec();
            if(exec!=null && exec.size()>0){
                System.out.println("当前"+(1000-i));
                //用消息队列发出订单消息
            }else {
                System.out.println("失败");
            }
        }
        jedis.close();
        return "1";
    }
}
