package com.example.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.payment.config.AlipayConfig;
import com.gmall.bean.OmsOrder;
import com.gmall.bean.PaymentInfo;
import com.gmall.service.OrderService;
import com.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    AlipayClient  alipayClient;
    @Autowired
    PaymentService paymentService;
    @Reference
    OrderService  orderService;
    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliPayCallBackReturrn(String outTrade,BigDecimal totalAmount,HttpServletRequest request,ModelMap modelMap){
       //获取支付包参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String totalAmount1 = request.getParameter("totalAmount");
        String trade_status = request.getParameter("trade_status");
        String subject=request.getParameter("subject");
        String queryString = request.getQueryString();
        //更新用户的支付状态
        if(StringUtils.isNotBlank(sign)) {
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(queryString);
            paymentService.updatePayment(paymentInfo);
        }
        //支付成功后，订单服务更新，从而引起库存，物流服务
        System.out.println("finish");
        return "finish";
    }
    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String memberId=(String) request.getAttribute("memberId");
        String nickname=(String) request.getAttribute("nickname");
        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        return "index";
    }
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){
        String form="";
        AlipayTradePagePayRequest alipayTradePagePayRequest=new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        Map<String,Object> map=new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","zqs");
        String param= JSON.toJSONString(map);
        alipayTradePagePayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayTradePagePayRequest).getBody(); //调用SDK生成表单
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //保存用户支付信息
        OmsOrder omsOrder=orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("zqs");
        paymentService.savePaymentInfo(paymentInfo);
        System.out.println(paymentInfo);
        //向消息中间件发送一个检查支付状态的延迟消息队列
        paymentService.sendDelayPaymentResultQueue(outTradeNo,5);
        return form;
    }
}
