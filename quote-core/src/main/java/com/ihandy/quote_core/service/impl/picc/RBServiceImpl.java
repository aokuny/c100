package com.ihandy.quote_core.service.impl.picc;


import com.ihandy.quote_core.bean.*;

import com.ihandy.quote_core.bean.Request;

import com.ihandy.quote_core.bean.other.*;
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
    private static String licenseType="02";//车牌类型小型汽车
    @Override
    public BaseCarInfoResponse getBaseCarInfoByLicenseNo(String licenseNo,int CityCode) {
        BaseCarInfoResponse carBaseInfoResponse = new BaseCarInfoResponse();
        Response responseIndex = goXubaoIndex();
        if(responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200){
            Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo,licenseType);
            if(responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200){
                Response responseBrowse = xubaoBrowsePolicyNo(responseSearch);
                if(responseBrowse.getReturnCode() == SysConfigInfo.SUCCESS200){
                    //获取车辆基本信息
                    Response responseCitemCar = xubaoGetCitemCar(responseBrowse);
                    if(responseCitemCar.getReturnCode() == SysConfigInfo.SUCCESS200) {
                        //获取车辆关系人信息
                        Response responseCinsure = xubaoGetCinsure(responseBrowse);
                        if(responseCinsure.getReturnCode() == SysConfigInfo.SUCCESS200) {
                            // TODO: 2016/5/17 将返回数据填充到carInfoResponse中
                            // 1 ) 从search中返回的保单list中获取
                            Map returnSearchMap = responseSearch.getResponseMap();
                            Map lastResultSearchMap = (Map) returnSearchMap.get("lastResult");
                            carBaseInfoResponse.setCarVin(lastResultSearchMap.get("CarVin").toString());//车架号
                            carBaseInfoResponse.setLicenseNo(lastResultSearchMap.get("LicenseNo").toString());//车牌号
                            carBaseInfoResponse.setEngineNo(lastResultSearchMap.get("EngineNo").toString());//发动机号
                            carBaseInfoResponse.setBusinessExpireDate(lastResultSearchMap.get("BusinessExpireDate").toString());//商业险到期日期
                            carBaseInfoResponse.setForceExpireDate(lastResultSearchMap.get("ForceExpireDate").toString());//交强险到期日期
                            // 2 ) 从浏览保单基本车辆信息中获取
                            Map returnCitemCarMap = responseCitemCar.getResponseMap();
                            Map lastResultCitemCarMap = (Map) returnCitemCarMap.get("lastResult");
                            carBaseInfoResponse.setCarSeated(Integer.parseInt(lastResultCitemCarMap.get("CarSeated").toString()));//核定座位数
                            carBaseInfoResponse.setMoldName(lastResultCitemCarMap.get("MoldName").toString());//车型
                            carBaseInfoResponse.setPurchasePrice(Double.parseDouble(lastResultCitemCarMap.get("PurchasePrice").toString()));//新车购买价格
                            carBaseInfoResponse.setCarRegisterDate(lastResultCitemCarMap.get("CarRegisterDate").toString());//车辆首次登记日期
                            carBaseInfoResponse.setCarUsedType(lastResultCitemCarMap.get("CarUsedType").toString());//车辆使用性质
                            // 3 ) 从浏览保单保险关系人信息中获取
                            Map returnCinsureMap = responseCinsure.getResponseMap();
                            Map lastResultCinsureMap = (Map) returnCinsureMap.get("lastResult");
                            Iterator entries = lastResultCinsureMap.entrySet().iterator();
                            while (entries.hasNext()) {
                                Map.Entry entry = (Map.Entry) entries.next();
                                Map value = (Map) entry.getValue();
                                if (value.get("role").toString().equals("投保人")) {
                                    carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
                                }else if(value.get("role").toString().equals("车主")){
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                }else if(value.get("role").toString().equals("被保险人")){
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
                                }else if (value.get("role").toString().equals("被保险人/车主")) {
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                }
                                else if (value.get("role").toString().equals("投保人/车主")) {
                                    carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
                                    carBaseInfoResponse.setLicenseOwner(value.get("name").toString());//车主姓名
                                    carBaseInfoResponse.setCredentislasNum(value.get("IdCardNo").toString());//证件号码
                                    carBaseInfoResponse.setIdType(value.get("IdCardType").toString());//证件类型
                                }
                                else if(value.get("role").toString().equals("投保人/被保险人")){
                                    carBaseInfoResponse.setPostedName(value.get("name").toString());//投保人
                                    carBaseInfoResponse.setInsuredName(value.get("name").toString());//被保险人/车主
                                }
                            }
                            // 4 ) 从参数中获取
                            carBaseInfoResponse.setCityCode(CityCode);
                        }else{
                            logger.info("抓取机器人，【 PICC 获取保单中车辆相关保险人信息错误】");
                        }
                    }else{
                        logger.info("抓取机器人，【 PICC 获取保单中车辆基本信息错误】");
                    }
                }else{
                    logger.info("抓取机器人，【 PICC 按保单号查看保单错误】");
                }
            }else{
                logger.info("抓取机器人，【 PICC 按车牌号和类型查询保单错误】");
            }
        }else{
            logger.info("抓取机器人，【 PICC 跳转续保页面错误】");
        }
        return carBaseInfoResponse;
    }

    @Override
    public CarInfoResponse getAllCarInfoByLicenseNo(String licenseNo) {
        return null;
    }

    @Override
    public SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo ) {
        return null;
    }

    @Override
    public List<RelaPeopleResponse> getRelaPeopleInfoByCarInfoList(String licenseNo) {
        return null;
    }

    @Override
    public List<ClaimResponse> getClaimInfoList(String licenseNo ) {
        List<ClaimResponse> ClaimResponseList = new ArrayList<>();
        Response responseIndex = goXubaoIndex();
        if(responseIndex.getReturnCode() == SysConfigInfo.SUCCESS200) {
            Response responseSearch = xubaoSearchByLicenseNo(responseIndex,licenseNo, licenseType);
            if (responseSearch.getReturnCode() == SysConfigInfo.SUCCESS200) {
                Response claimResponse1 = xubaoQueryClaimsMsg(responseSearch);
                Map lastResultMap = (Map) claimResponse1.getResponseMap().get("lastResult");
                Iterator it = lastResultMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    ClaimResponse claimResponse = new ClaimResponse();
                    Map value = (Map) entry.getValue();
                    claimResponse.setEndCaseTime( value.get("EndCaseTime").toString());
                    claimResponse.setLossTime( value.get("LossTime").toString());
                    claimResponse.setPayAmount( Double.parseDouble(value.get("PayAmount").toString()) );
                    claimResponse.setPayCompanyName( value.get("PayCompanyName").toString());
                    ClaimResponseList.add(claimResponse);
                }

            }
        }
        return ClaimResponseList;
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
        map.put("bizNo",nextParamsMap.get("bizNo").toString());//上年商业保单号
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
        map.put("bizNo",nextParamsMap.get("bizNo").toString());//上年商业保单号
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
        map.put("bizNo",nextParamsMap.get("bizNo").toString());//上年商业保单号
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
        map.put("bizNo",nextParamsMap.get("bizNo").toString());//上年商业保单号
        map.put("bizType","POLICY");
        request.setRequestParam(map);
        request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_QUERYCLAIMSMSG);//GET
        Response responseClaimMsg = xubaoClaimsMsgPage.run(request);
        return responseClaimMsg;
    }

}
