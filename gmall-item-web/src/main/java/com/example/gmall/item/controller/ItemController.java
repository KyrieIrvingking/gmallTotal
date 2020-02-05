package com.example.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.PmsProductSaleAttr;
import com.gmall.bean.PmsSkuImage;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.bean.PmsSkuSaleAttrValue;
import com.gmall.service.SkuService;
import com.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;
    @RequestMapping("{skuId}.html")
   // @ResponseBody
    public String index(@PathVariable String skuId, ModelMap modelMap){
        PmsSkuInfo pmsSkuInfo=skuService.getSkuById(skuId);
        modelMap.put("skuInfo",pmsSkuInfo);
        List<PmsProductSaleAttr> pmsProductSaleAttrs=spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),skuId);
        modelMap.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        List<PmsSkuInfo> pmsSkuInfos=skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        HashMap<String,String> skuSaleAttrHashMap=new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k="";
            String v=skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValues=skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
                k+=skuSaleAttrValue.getSaleAttrValueId()+"|";
            }
            skuSaleAttrHashMap.put(k,v);
        }
        String skuSaleAttrHashJsonStr=JSON.toJSONString(skuSaleAttrHashMap);
        modelMap.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);
        return "item";
    }
}
