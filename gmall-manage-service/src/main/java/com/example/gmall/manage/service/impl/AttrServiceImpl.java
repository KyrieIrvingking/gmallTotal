package com.example.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.example.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.example.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.gmall.bean.PmsBaseAttrInfo;
import com.gmall.bean.PmsBaseAttrValue;
import com.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Override
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo=new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList=pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfoList) {
            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValueList=pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValueList);
        }
          return pmsBaseAttrInfoList;
//        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
//        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
//        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
//        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
//            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
//            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
//            List<PmsBaseAttrValue>   pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
//            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
//        }
//
//        return pmsBaseAttrInfos;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
//      if( pmsBaseAttrInfoMapper.existsWithPrimaryKey(pmsBaseAttrInfo)) {
//           pmsBaseAttrInfoMapper.delete(pmsBaseAttrInfo);
//           PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
//           pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
//           pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
//      }
        String id=pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)) {
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            List<PmsBaseAttrValue> list = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : list) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insert(pmsBaseAttrValue);
            }
        }
        else {
            Example example=new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);
            List<PmsBaseAttrValue> pmsBaseAttrValueList=pmsBaseAttrInfo.getAttrValueList();

            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
            for (PmsBaseAttrValue baseAttrValue : pmsBaseAttrValueList) {
                baseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> list=pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return list;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByValueId(Set<String> valueIdSet) {
        String valueIdStr=StringUtils.join(valueIdSet,",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList=pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
        return pmsBaseAttrInfoList;
    }


}
