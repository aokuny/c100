package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.RelaPeopleResponse;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;

import java.util.List;

/**
 * Created by fengwen on 2016/4/29.
 */
public interface IService {
    /**
     * 通过车牌号获取车辆信息
     */
    CarInfoResponse getCarInfoByLicenseNo(String licenseNo,String licenseType);
    /**
     * 通过车辆信息获取险种信息
     */
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo , String licenseType);
    /**
     * 通过车辆信息获取关系人信息
     */
     RelaPeopleResponse getRelaPeopleInfoByCarInfo(String licenseNo , String licenseType);


    /**
     * 通过车辆信息获取出险信息list
     */
    List<ClaimResponse> getClaimInfoList(String licenseNo ,String licenseType);


}
