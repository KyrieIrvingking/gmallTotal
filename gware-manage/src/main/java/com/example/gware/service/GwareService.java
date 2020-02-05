package com.example.gware.service;

import com.example.gware.bean.WmsWareInfo;
import com.example.gware.bean.WmsWareOrderTask;
import com.example.gware.bean.WmsWareSku;

import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */
public interface GwareService {
    public Integer  getStockBySkuId(String skuid);

    public boolean  hasStockBySkuId(String skuid,Integer num);

    public List<WmsWareInfo> getWareInfoBySkuid(String skuid);

    public void addWareInfo();

    public Map<String,List<String>> getWareSkuMap(List<String> skuIdlist);

    public void addWareSku(WmsWareSku wmsWareSku);

    public void deliveryStock(WmsWareOrderTask taskExample) ;

    public WmsWareOrderTask saveWareOrderTask(WmsWareOrderTask wmsWareOrderTask);

    public  List<WmsWareOrderTask>   checkOrderSplit(WmsWareOrderTask wmsWareOrderTask);

    public void lockStock(WmsWareOrderTask wmsWareOrderTask);

    public List<WmsWareOrderTask> getWareOrderTaskList(WmsWareOrderTask wmsWareOrderTask);

    public List<WmsWareSku> getWareSkuList();

    public List<WmsWareInfo> getWareInfoList();
}
