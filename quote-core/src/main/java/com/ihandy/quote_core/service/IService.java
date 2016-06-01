package com.ihandy.quote_core.service;


import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.bean.other.*;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.PostPrecisePricerResponse;
import com.ihandy.quote_core.bean.other.RelaPeopleResponse;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;


import java.util.List;
import java.util.Map;

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
    CarInfoResponse getAllCarInfoByLicenseNo(String licenseNo, int CityCode);
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

    String commitHeBaoInfo(Response response);

    List<HebaoResponse> getHebaoResponse(String licenseNo);
}
