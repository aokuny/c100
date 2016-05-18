package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.other.*;

import java.util.List;

/**
 * Created by fengwen on 2016/4/29.
 */
public interface IService {
    /**
     * 通过车牌号获取车辆基本信息
     */
    BaseCarInfoResponse getBaseCarInfoByLicenseNo(String licenseNo, int CityCode);
    /**
     * 通过车牌号获取车辆所有关联信息
     */
    CarInfoResponse getAllCarInfoByLicenseNo(String licenseNo);
    /**
     * 通过车辆信息获取险种信息
     */
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo );
    /**
     * 通过车辆信息获取关系人信息
     */
     List<RelaPeopleResponse> getRelaPeopleInfoByCarInfoList(String licenseNo);
    /**
     * 通过车辆信息获取出险信息list
     */
     List<ClaimResponse> getClaimInfoList(String licenseNo);


}
