package com.ihandy.quote_core.service.impl.picc;


import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;
import com.ihandy.quote_core.serverpage.picc.XubaoAPage;
import com.ihandy.quote_core.service.IService;


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

    public CarInfoResponse getCarInfoByLicenseNo(String licenseNo, Cookie cookie) {
        XubaoAPage xubaoAPage = new XubaoAPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();

        request.setRequestParam(map);
        request.setUrl("");
        Map mapA = xubaoAPage.run(request);


        return null;
    }

    public QuoteResponse getQuoteInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }

    public ClaimResponse getClaimInfoByCarInfo(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }

    public List<ClaimResponse> getClaimInfoList(CarInfoResponse carInfo, Cookie cookie) {
        return null;
    }

    public void getAdminTest(String admin, String password) {

    }

}
