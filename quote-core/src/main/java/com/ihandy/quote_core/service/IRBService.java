package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.Cookie;
import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;

import java.util.List;

/**
 * Created by fengwen on 2016/4/29.
 */
public interface IRBService {
    /**
     * 通过车牌号获取车辆信息
     */
    CarInfoResponse getCarInfoByLicenseNo(String licenseNo, Cookie cookie);
    /**
     * 通过车辆信息获取报价信息
     */
    QuoteResponse getQuoteInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie);

    /**
     * 通过车辆信息获取出险信息
     */
    ClaimResponse getClaimInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie);

    /**
     * 通过车辆信息获取出险信息list
     */
    List<ClaimResponse> getClaimInfoList(CarInfoResponse carInfo, Cookie cookie);


}
