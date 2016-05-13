package com.ihandy.quote_core.service.impl.picc;


import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;

import com.ihandy.quote_core.bean.other.CarInfoResponse;
import com.ihandy.quote_core.bean.other.ClaimResponse;
import com.ihandy.quote_core.bean.other.QuoteResponse;
import com.ihandy.quote_core.serverpage.picc.*;
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
        if(responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200){
            Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);
            if(responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200){
                Response responseBrowse = xubaoBrowsePolicyNo(responseSearch);
                if(responseBrowse.getReturnCode() == SysConfigInfo.SUCCESS200){
                    //获取车辆基本信息
                    Response responseCitemCar = xubaoGetCitemCar(responseBrowse);
                    //获取车辆关系人信息
                    Response responseCinsure = xubaoGetCinsure(responseBrowse);
                    //获取车辆险种信息
                    Response responseCitemKind = xubaoGetCitemKind(responseBrowse);
                }else{
                    logger.info("抓取机器人，【 PICC 按保单号查看保单错误】");
                }
            }else{
                logger.info("抓取机器人，【 PICC 按车牌号和类型查询保单错误】");
            }
        }else{
            logger.info("抓取机器人，【 PICC 跳转续保页面错误】");
        }


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

    /*******************************************************************************
     * 跳转续保页面
     * @return response
     *          nextParams null
     *          lastResult null
     * *******************************************************************************/
    public  Response goXubaoIndex(){
        XubaoIndexPage xubaoIndexPage = new XubaoIndexPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_EDITRENEWALSEARCH);//GET
        Response response = xubaoIndexPage.run(request);
        return response;
    }

    /*******************************************************************************
     * 按车牌号查询续保单
     * @param response
     * @param licenseNo
     * @param licenseType
     * @return response
     *          nextParams(上年 DAT,上年 DZA)
     *          lastResult(车牌号，发动机号，车架号)
     * *******************************************************************************/
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


    /*******************************************************************************
     * 按保单号浏览保单
     * @param response
     * @return
     * *******************************************************************************/
    public  Response xubaoBrowsePolicyNo(Response response){
        XubaoBrowsePolicyPage xubaoBrowsePolicyPage = new XubaoBrowsePolicyPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("bizNo").toString());//上年商业保单号

        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_BROWSEPOLICYNO);//GET
        Response responseBrowse = xubaoBrowsePolicyPage.run(request);
        return responseBrowse;
    }

    /*******************************************************************************
     * 查询车辆基本信息
     * @param response
     * @return
     * *******************************************************************************/
    public Response xubaoGetCitemCar(Response response){
        XubaoShowCitemCarPage xubaoShowCitemCarPage = new XubaoShowCitemCarPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("DAT").toString());//上年商业保单号
        map.put("bizType","POLICY");
        map.put("comCode","11029204");
        map.put("contractNo",null);
        map.put("editType","SHOW_POLICY");
        map.put("minusFlag","originQuery");
        map.put("proposalNo",null);
        map.put("riskCode","DAA");
        map.put("rnd704",new Date().toString());
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);//GET
        Response responseShowCitemCar = xubaoShowCitemCarPage.run(request);
        return responseShowCitemCar;
    }

    /*******************************************************************************
     * 查询车辆人员关系信息
     * @param response
     * @return
     * *******************************************************************************/
    public Response xubaoGetCinsure(Response response){
        XubaoShowCinsuredPage xubaoShowCinsuredPage = new XubaoShowCinsuredPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("DAT").toString());//上年商业保单号
        map.put("bizType","POLICY");
        map.put("comCode","11029204");
        map.put("contractNo",null);
        map.put("editType","SHOW_POLICY");
        map.put("minusFlag","originQuery");
        map.put("proposalNo",null);
        map.put("riskCode","DAA");
        map.put("rnd704",new Date().toString());
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);//GET
        Response responseShowCinsured = xubaoShowCinsuredPage.run(request);
        return responseShowCinsured;
    }

    /*******************************************************************************
     * 获取车辆保险责任
     * @param response
     * @return
     * *******************************************************************************/
    public Response xubaoGetCitemKind(Response response){

        XubaoShowCitemKindPage xubaoShowCitemKindPage = new XubaoShowCitemKindPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("DAT").toString());//上年商业保单号
        map.put("bizType","POLICY");
        map.put("comCode","11029204");
        map.put("contractNo",null);
        map.put("editType","SHOW_POLICY");
        map.put("minusFlag","originQuery");
        map.put("proposalNo",null);
        map.put("riskCode","DAA");
        map.put("rnd704",new Date().toString());
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CARTAB);//GET
        Response responseShowCitemKind = xubaoShowCitemKindPage.run(request);
        return responseShowCitemKind;
    }

    /*******************************************************************************
     * 查询出险理赔信息
     * @param response
     * @return response
                PayCompanyName;//保险公司
                PayAmount;//出险金额
                EndCaseTime;//结案时间
                LossTime;//出险时间
     * *******************************************************************************/
    public Response xubaoQueryClaimsMsg(Response response){

        XubaoClaimsMsgPage xubaoClaimsMsgPage = new XubaoClaimsMsgPage();
        Request request = new Request();
        Map<String, String> map = new HashMap<String, String>();
        Map responseParam = response.getResponseMap();
        Map nextParamsMap = (Map) responseParam.get("nextParams");
        map.put("bizNo",nextParamsMap.get("DAT").toString());//上年商业保单号
        map.put("bizType","POLICY");
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_QUERYCLAIMSMSG);//GET
        Response responseClaimMsg = xubaoClaimsMsgPage.run(request);
        return responseClaimMsg;
    }

}
