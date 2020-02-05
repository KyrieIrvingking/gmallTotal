package com.example.gware.mapper;

import com.example.gware.bean.WmsWareInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface WareInfoMapper extends Mapper<WmsWareInfo> {


    public List<WmsWareInfo> selectWareInfoBySkuid(String skuid);



}
