package com.example.gmall.search.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.annotations.LoginRequired;
import com.gmall.bean.*;
import com.gmall.service.AttrService;
import com.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;


import java.io.IOException;
import java.util.*;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;
    @RequestMapping("index")
    @LoginRequired(loginSuccess = false)
    public String index(){
        return "index";
    }
    @RequestMapping("list.html")

    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) throws IOException {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);
        Set<String> valueIdSet=new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }
         List<PmsBaseAttrInfo> pmsBaseAttrInfoList= attrService.getAttrInfoListByValueId(valueIdSet);
         modelMap.put("attrList",pmsBaseAttrInfoList);
         String[] delValueIds=pmsSearchParam.getValueId();

         if(delValueIds!=null) {
             List<PmsSearchCrumb> pmsSearchCrumbs=new ArrayList<>();
             for (String delValueId : delValueIds) {
                 Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
                 PmsSearchCrumb pmsSearchCrumb=new PmsSearchCrumb();
                 pmsSearchCrumb.setValueId(delValueId);
                 pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delValueId));
             while (iterator.hasNext()) {
                 PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                 List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                 for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                     String id = pmsBaseAttrValue.getId();
                         if (delValueId.equals(id)) {
                             pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                             iterator.remove();
                         }
                     }
                 }
                 pmsSearchCrumbs.add(pmsSearchCrumb);
             }
             modelMap.put("attrValueSelectedList",pmsSearchCrumbs);
         }
         String urlParam=getUrlParam(pmsSearchParam);
         modelMap.put("urlParam",urlParam);
         String keyword=pmsSearchParam.getKeyword();
         if(StringUtils.isNotBlank(keyword)){
             modelMap.put("keyword",keyword);
         }


         return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam,String ...delValueId) {
        String keyword=pmsSearchParam.getKeyword();
        String catalog3Id=pmsSearchParam.getCatalog3Id();
        String[] pmsSkuAttrValues = pmsSearchParam.getValueId();
        String urlParam="";
        if(delValueId==null) System.out.println("null");
        else System.out.println("not null");
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"keyword="+keyword;
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"catalog3Id="+catalog3Id;
        }

        if(pmsSkuAttrValues!=null){
            for (String pmsSkuAttrValue : pmsSkuAttrValues) {
                if(delValueId==null) {
                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }
                else {
                    if (!delValueId.equals(pmsSkuAttrValue)) {
                        urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                    }
                }
            }
        }
        return urlParam;
    }
    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String delValueId) {
        String keyword=pmsSearchParam.getKeyword();
        String catalog3Id=pmsSearchParam.getCatalog3Id();
        String[] pmsSkuAttrValues = pmsSearchParam.getValueId();
        String urlParam="";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"keyword="+keyword;
        }
        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam=urlParam+"&";
            }
            urlParam=urlParam+"catalog3Id="+catalog3Id;
        }
        if(pmsSkuAttrValues!=null){
            for (String pmsSkuAttrValue : pmsSkuAttrValues) {
                if(!pmsSkuAttrValue.equals(delValueId)){
                    urlParam=urlParam+"&valueId="+pmsSkuAttrValue;
                }
            }
        }
        return urlParam;
    }
}
