package com.example.gmall.cart.conreoller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.util.CookieUtil;
import com.gmall.bean.OmsCartItem;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.service.CartService;
import com.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

//    @RequestMapping("toTrade")
//    @LoginRequired(loginSuccess = true)
//    public String checkCart(HttpServletRequest request, HttpServletResponse response){
//        String memberId = (String) request.getAttribute("memberId");
//        String nickname= (String) request.getAttribute("nickname");
//        return  "trade";
//    }

    /**
     * 勾选前台界面是否选中的同时会更新数据库和缓存中的购物车数据
     * @param isChecked
     * @param skuId
     * @param request
     * @param response
     * @param modelMap
     * @return
     */
    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);
        cartService.cheakCart(omsCartItem);
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        BigDecimal totalAmount=getTotalAmount(omsCartItems);
        System.out.println(totalAmount);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal total=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                total=total.add(omsCartItem.getTotalPrice());
            }
        }
        return  total;
    }

    /**
     * 购物车商品列表
     * @param request
     * @param response
     * @param modelMap
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList( HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){
        List<OmsCartItem> omsCartItems=new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");
        //是否登陆，已登录memberId不为空，从数据查询。
        if(StringUtils.isNotBlank(memberId)) {
          omsCartItems=cartService.cartList(memberId);
        }else {
            //未登录，从cookie获取购物车商品列表
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems=JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }
        //计算单个物品总价  单价*数量
        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList",omsCartItems);
        BigDecimal totalAmount=getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * 1.未登录    从cookie中查询，若cookie中不存在，将商品添加到cookie，否则，更改cookie中该商品数量
     * 2.已登录    从数据库中查询，并更新缓存中的数据
     * @param skuId
     * @param quantity
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response) {
        //根据商品id查出商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductBrand("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("1111");
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setProductSkuId(skuId);
        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //判断是否登陆
        if (StringUtils.isBlank(memberId)) {
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //判断Cookie是否为空
            if(StringUtils.isBlank(cartListCookie)){
                //Cookie为空
                omsCartItems.add(omsCartItem);
            }
            else {
                //Cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exist = if_exist_cart(omsCartItems, omsCartItem);
                if (exist) {
                    //Cookie中已存在当前商品
                    for (OmsCartItem cartItem : omsCartItems) {
                        if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                             cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                           //  cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                } else {
                    //Cookie中不存在当前商品
                    omsCartItems.add(omsCartItem);
                }
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 72, true);
        }
        else {
          OmsCartItem  omsCartItemFromDb= cartService.ifCartExistByUser(memberId,skuId);
            if(omsCartItemFromDb==null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                cartService.addCart(omsCartItem);
            }else {
                //该用户添加过当前商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }
            //同步缓存
            cartService.flushCartCache(memberId);
        }
        return "redirect:/success.html";
    }

    /**
     * 判断cookie中是否已存在该商品
     * @param omsCartItems
     * @param omsCartItem
     * @return
     */
    private boolean if_exist_cart(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b=false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId=  cartItem.getProductSkuId();
           if(productSkuId.equals(omsCartItem.getProductSkuId())){
               b=true;
           }
        }
        return b;
    }

}
