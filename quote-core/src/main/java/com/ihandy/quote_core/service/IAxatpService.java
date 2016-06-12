package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.other.SaveQuoteResponse;

/**
 * Created by fengwen on 2016/6/6.
 */
public interface IAxatpService {
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo,String licenseOwner,String cityCode );
}
