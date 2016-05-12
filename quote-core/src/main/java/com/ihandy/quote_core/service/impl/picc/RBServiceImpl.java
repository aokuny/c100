package com.ihandy.quote_core.service.impl.picc;


import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;
import com.ihandy.quote_core.serverpage.picc.XubaoAPage;
import com.ihandy.quote_core.service.IService;


import com.ihandy.quote_core.utils.SysConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by fengwen on 2016/4/29.
 */
@Service
public class RBServiceImpl implements IService {
    private static Logger logger = LoggerFactory.getLogger(RBServiceImpl.class);
    @Override
    public CarInfoResponse getCarInfoByLicenseNo(String licenseNo) {
        XubaoAPage xubaoAPage = new XubaoAPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_EDITRENEWALSEARCH);
        Response response = xubaoAPage.run(request);

        return null;
    }
    @Override
    public QuoteResponse getQuoteInfoByCarInfo(CarInfoResponse carInfo) {
        return null;
    }
    @Override
    public ClaimResponse getClaimInfoByCarInfo(CarInfoResponse carInfo) {
        return null;
    }
    @Override
    public List<ClaimResponse> getClaimInfoList(CarInfoResponse carInfo) {
        return null;
    }


}
