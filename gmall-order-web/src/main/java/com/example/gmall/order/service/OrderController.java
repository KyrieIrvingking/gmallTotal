package com.example.gmall.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.annotations.LoginRequired;
import com.gmall.bean.OmsCartItem;
import com.gmall.bean.OmsOrder;
import com.gmall.bean.OmsOrderItem;
import com.gmall.bean.UmsMemberReceiveAddress;
import com.gmall.service.CartService;
import com.gmall.service.OrderService;
import com.gmall.service.SkuService;
import com.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService  skuService;
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap){
        String memberId=(String) request.getAttribute("memberId");
        String nickname=(String) request.getAttribute("nickname");
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<UmsMemberReceiveAddress> userAddressList = userService.getAddr(memberId);

        List<OmsOrderItem> omsOrderItems=new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                OmsOrderItem omsOrderItem=new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setProductQuantity(BigDecimal.valueOf(1));
                omsOrderItems.add(omsOrderItem);
            }
        }
        modelMap.put("userAddressList",userAddressList);
        modelMap.put("omsCartItems",omsCartItems);
        modelMap.put("totalAmount",getTotalAmount(omsCartItems));
        String tradeCode=orderService.generate(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems){
        BigDecimal totalAmount=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice=omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked().equals("1")){
                totalAmount=totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, String tradeCode, BigDecimal totalAmount, HttpServletRequest request, HttpServletResponse response)
    {
        String memberId=(String) request.getAttribute("memberId");
        String nickname=(String) request.getAttribute("nickname");
        //检查交易码
        String success=orderService.checkTradeCode(memberId,tradeCode);
        if(success.equals("success")) {
            //根据用户id获得购买的商品列表，总价
            List<OmsOrderItem> omsOrderItems=new ArrayList<>();
            OmsOrder omsOrder=new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            String outTradeNo="gmall";
            outTradeNo=outTradeNo+System.currentTimeMillis();
            //SimpleDateFormat sdf=new SimpleDateFormat("YYYYMMDDHHmmss");
            //outTradeNo=outTradeNo+sdf;
            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress=userService.getReceiveAddrById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            Calendar c=Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date date=c.getTime();
            omsOrder.setReceiveTime(date);
            omsOrder.setSourceType(0);
            omsOrder.setStatus("0");
            List<OmsCartItem> omsCartItems=cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                 if(omsCartItem.getIsChecked().equals("1")){
                     OmsOrderItem omsOrderItem=new OmsOrderItem();
                     //检验价格
                  boolean b=skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                  if(b==false){
                      ModelAndView modelAndView=new ModelAndView("tradeFail");
                      return modelAndView;
                  }
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setOrderSn(outTradeNo);//外部订单号
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");
                     //检验库存，远程调用库存系统

                    omsOrderItems.add(omsOrderItem);
                 }
            }
             omsOrder.setOmsOrderItems(omsOrderItems);
            //删除购物车对应物品
              orderService.saveOrder(omsOrder);
            //重定向到支付系统
            ModelAndView modelAndView=new ModelAndView("redirect:http://localhost:8087/index");
            modelAndView.addObject("totalAmount",totalAmount);
            modelAndView.addObject("outTradeNo",outTradeNo);
            return modelAndView;
        }else{
            ModelAndView modelAndView=new ModelAndView("tradeFail");
            return modelAndView;
        }

    }
}
