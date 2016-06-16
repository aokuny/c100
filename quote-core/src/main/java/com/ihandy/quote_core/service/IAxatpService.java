package com.ihandy.quote_core.service;

import com.ihandy.quote_core.bean.other.SaveQuoteResponse;

/**
 * Created by fengwen on 2016/6/6.
 */
public interface IAxatpService {
    SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo,String engineNo,String vehicleFrameNo,String licenseOwner,String mobilePhone,String certificateNo,String cheSun,String sanZhe,String siJi,String chengKe,String daoQiang,String boLi,String huaHen,String ziRan,String sheShui );
}
