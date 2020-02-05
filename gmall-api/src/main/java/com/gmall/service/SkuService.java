package com.gmall.service;

import com.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);
    List<PmsSkuInfo>  getAllSku(String catalog3Id);
    PmsSkuInfo getSkuById(String skuId);
    PmsSkuInfo getSkuByIdFromDb(String skuId);
    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
    boolean checkPrice(String productSkuId, BigDecimal price);
}
