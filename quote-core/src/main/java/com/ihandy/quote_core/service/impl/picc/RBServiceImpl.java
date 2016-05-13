package com.ihandy.quote_core.service.impl.picc;


import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;
import com.ihandy.quote_core.serverpage.picc.XubaoBrowsePolicyPage;
import com.ihandy.quote_core.serverpage.picc.XubaoIndexPage;
import com.ihandy.quote_core.serverpage.picc.XubaoSearchPage;
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
    public CarInfoResponse getCarInfoByLicenseNo(String licenseNo ,String licenseType) {

        Response responseIndex = goXubaoIndex();
        Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);
        Response responseBrowse = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);

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

    public  Response goXubaoIndex(){
        XubaoIndexPage xubaoIndexPage = new XubaoIndexPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_EDITRENEWALSEARCH);//GET
        Response response = xubaoIndexPage.run(request);
        return response;
    }

    public  Response xubaoSearchByLicenseNo(Response response,String licenseNo,String licenseType){
        XubaoSearchPage xubaoSearchPage = new XubaoSearchPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        map.put("prpCrenewalVo.engineNo",null);
        map.put("prpCrenewalVo.frameNo",null);
        map.put("prpCrenewalVo.licenseColorCode",null);
        map.put("prpCrenewalVo.licenseNo",licenseNo);
        map.put("prpCrenewalVo.licenseType",licenseType);
        map.put("prpCrenewalVo.othFlag",null);
        map.put("prpCrenewalVo.policyNo",null);
        map.put("prpCrenewalVo.vinNo",null);
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_SELECTRENEWAL);//POST
        Response responseSearch = xubaoSearchPage.run(request);
        return responseSearch;
    }

    public  Response xubaoBrowsePolicyNo(Response response){
        XubaoBrowsePolicyPage xubaoBrowsePolicyPage = new XubaoBrowsePolicyPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("DAT").toString());//上年商业保单号

        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_BROWSEPOLICYNO);//GET
        Response responseBrowse = xubaoBrowsePolicyPage.run(request);
        return responseBrowse;
    }

}
