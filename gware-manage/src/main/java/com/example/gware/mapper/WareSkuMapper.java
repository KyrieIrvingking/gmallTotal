package com.example.gware.mapper;

import com.example.gware.bean.WmsWareSku;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface WareSkuMapper extends Mapper<WmsWareSku> {


    public Integer selectStockBySkuid(String skuid);

    public int incrStockLocked(WmsWareSku wmsWareSku);

    public int selectStockBySkuidForUpdate(WmsWareSku wmsWareSku);

    public int  deliveryStock(WmsWareSku wmsWareSku);

    public List<WmsWareSku> selectWareSkuAll();
}
