package com.ihandy.quote_core.service.impl.axatp;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.bean.other.SaveQuoteResponse;
import com.ihandy.quote_core.serverpage.axatp.*;
import com.ihandy.quote_core.service.IAxatpService;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by fengwen on 2016/6/6.
 */
@Service
public class AxatpServiceImpl implements IAxatpService {
    /**
     * 查询报价信息
     *
     * @param licenseNo  (车牌号)
     * @param engineNo   (发动机号)
     * @param vehicleFrameNo (车架号)
     * @param licenseOwner (车主)
     * @param certificateNo (身份证号)
     * @param cheSun (车损   投保:OD N:不投保:N)
     * @param sanZhe (三者   投保:50000/100000/150000/200000/300000/500000/1000000  不投保:N)
     * @param siJi (司机     投保:10000/20000/50000/10000  不投保:N)
     * @param chengKe (乘客  投保:10000/20000/50000/10000  不投保:N)
     * @param daoQiang (盗抢 投保:THEFT 不投保:N）
     * @param boLi (玻璃     投保:1  不投保:N）
     * @param huaHen (划痕   投保:2000/5000/10000  不投保:N）
     * @param ziRan  (自燃   投保:N  不投保:NDNE）
     * @param sheShui (涉水  投保:N  不投保:FEDPC)
     * @return
     */
    @Override
    public SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo,String engineNo,String vehicleFrameNo, String licenseOwner,String mobilePhone,String certificateNo,String cheSun,String sanZhe,String siJi,String chengKe,String daoQiang,String boLi,String huaHen,String ziRan,String sheShui) {
        SaveQuoteResponse saveQuoteResponse = new SaveQuoteResponse();
         /*String engineNo="FW151330";
        String vehicleFrameNo="LBECFAHC5FZ226987";
        String touBaoMobileTelePhone="18810253437";
        String beiBaoMobileTelePhone = "18810253437";
        String toubaoCertificateNo ="132425196903135852";
        String beibaoCertificateNo ="132425196903135852";
        String touBaoEmail = "784506957@qq.com";
        String beiBaoEmail = "784506957@qq.com";
        String touBaoAddress ="mentougou2412";
        String beiBaoAddress ="mentougou2413";
        String receiveName="张文海";
        String receiveMobile="18810253437";
        String receiveVoice="张文海";
        String receiveDate="2016-07-01";
        String receiveAddress="mentougou2414";
        String validateCode="WPKD2B";*/
        PrecisionIndexPage precisionIndexPage =new PrecisionIndexPage(2);
        Request request1 =new Request();
        request1.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_PRECISIONINDEX);
        Map map = new HashMap<>();
        map.put("licenceNo",licenseNo);
        map.put("personnelName",licenseOwner);
        //map.put("cityCode",cityCode);
        request1.setRequestParam(map);
        Response response1 = precisionIndexPage.run(request1);
        if(response1.getReturnCode()==SysConfigInfo.SUCCESS200){
            ToPreparationPage toPreparationPage =new ToPreparationPage(2);
            Request request2 = new Request();
            request2.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_TOPREPARATION);
            request2.setRequestParam(response1.getResponseMap());
            Response response2  = toPreparationPage.run(request2);
            if(response2.getReturnCode()==SysConfigInfo.SUCCESS200) {
                SavePrecisionIndexInfoForPcPage savePrecisionIndexInfoForPcPage = new SavePrecisionIndexInfoForPcPage(2);
                Request request3 = new Request();
                request3.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SAVEPRECISIONINDEXINFOFORPC);
                request3.setRequestParam(response2.getResponseMap());
                Response response3 = savePrecisionIndexInfoForPcPage.run(request3);
                if (response3.getReturnCode() == SysConfigInfo.SUCCESS200) {
                    InitPrecisionBasicInfoPage initPrecisionBasicInfoPage = new InitPrecisionBasicInfoPage(2);
                    Request request4 = new Request();
                    request4.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                    request4.setRequestParam(response3.getResponseMap());
                    Response response4 = initPrecisionBasicInfoPage.run(request4);
                    if (response4.getReturnCode() == SysConfigInfo.SUCCESS200) {// 获取汽车购买价和汽车型号
                        CarBasicVehiclePriceQuery carBasicVehiclePriceQuery = new CarBasicVehiclePriceQuery(2);
                        Request request5 = new Request();
                        request5.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_CARBASEVEHICLEPRICEQUERY);
                        Map paramMap5 = response4.getResponseMap();
                        paramMap5.put("engineNo", engineNo);
                        paramMap5.put("vehicleFrameNo", vehicleFrameNo);
                        paramMap5.put("mobileTelephone", mobilePhone);
                        paramMap5.put("certificateNo", certificateNo);
                        request5.setRequestParam(paramMap5);
                        Response response5 = carBasicVehiclePriceQuery.run(request5);
                        if (response5.getReturnCode() == SysConfigInfo.SUCCESS200) {
                            CarQueryWhereRbCode carQueryWhereRbCode = new CarQueryWhereRbCode(2);
                            Request request6 = new Request();
                            request6.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_CARQUERYWHERERBCODE);
                            request6.setRequestParam(response5.getResponseMap());
                            Response response6 = carQueryWhereRbCode.run(request6);
                            if(response6.getReturnCode()==SysConfigInfo.SUCCESS200){
                                ApplyQueryPage applyQueryPage =new ApplyQueryPage(2);
                                Request request7 = new Request();
                                request7.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYQUERY);
                                request7.setRequestParam(response6.getResponseMap());
                                Response response7 = applyQueryPage.run(request7);
                                if(response7.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                    Request request8 = new Request();
                                    request8.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                                    request8.setRequestParam(response7.getResponseMap());
                                    Response response8 = initPrecisionBasicInfoPage.run(request8);
                                    if(response8.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                        ReInsureView reInsureView = new ReInsureView(2);
                                        Request request9 = new Request();
                                        request9.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_REINSUREVIEW);
                                        request9.setRequestParam(response8.getResponseMap());
                                        Response response9 = reInsureView.run(request9);
                                        if(response9.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                            ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                                            Request request10 = new Request();
                                            request10.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                                            request10.setRequestParam(response9.getResponseMap());
                                            Response response10 = showBusinessPlanInfoPage.run(request10);
                                            if(response10.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                ShowForcePlanInfoPage showForcePlanInfoPage = new ShowForcePlanInfoPage(2);
                                                Request request11 = new Request();
                                                request11.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWFORCEPLANINFO);
                                                request11.setRequestParam(response10.getResponseMap());
                                                Response response11 = showForcePlanInfoPage.run(request11);
                                                if(response11.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                    CalculaterBusinessPremiumPage calculaterBusinessPremiumPage = new CalculaterBusinessPremiumPage(2);
                                                    Request request13 = new Request();
                                                    request13.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_BUSINESSPREMIUMCALCULATER);
                                                    Map map13 = response11.getResponseMap();
                                                    map13.put("select_OD", cheSun);
                                                    map13.put("select_TP", sanZhe);
                                                    map13.put("select_DL", siJi);
                                                    map13.put("select_PL", chengKe);
                                                    map13.put("select_THEFT", daoQiang);
                                                    map13.put("select_GLASS", boLi);
                                                    map13.put("select_NICK", huaHen);
                                                    map13.put("select_NDNE", ziRan);
                                                    map13.put("select_FEDPC", sheShui);

                                                    request13.setRequestParam(map13);
                                                    Response response13 = calculaterBusinessPremiumPage.run(request13);
                                                    if(response13.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                        CalculaterForcePremiumPage calculaterForcePremiumPage = new CalculaterForcePremiumPage(2);
                                                        Request request14 = new Request();
                                                        request14.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_FORCEPREMIUMCALCULATER);
                                                        request14.setRequestParam(response13.getResponseMap());
                                                        Response response14 = calculaterForcePremiumPage.run(request14);
                                                        if(response14.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                            Map resultMap = response14.getResponseMap();
                                                            saveQuoteResponse.setSource(3);
                                                            try {
                                                                saveQuoteResponse.setBoli(Double.parseDouble(resultMap.get("premium_GLASS").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setBoli(0);
                                                            }
                                                            saveQuoteResponse.setCheDeng(0);
                                                            try {
                                                                saveQuoteResponse.setChengKe(Double.parseDouble(resultMap.get("premium_PL").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setChengKe(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setCheSun(Double.parseDouble(resultMap.get("premium_OD").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setCheSun(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setDaoQiang(Double.parseDouble(resultMap.get("premium_THEFT").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setDaoQiang(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setHuaHen(Double.parseDouble(resultMap.get("premium_NICK").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setHuaHen(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setSanZhe(Double.parseDouble(resultMap.get("premium_TP").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setSanZhe(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setSheShui(Double.parseDouble(resultMap.get("premium_FEDPC").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setSheShui(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setSiJi(Double.parseDouble(resultMap.get("premium_DL").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setSiJi(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setZiRan(Double.parseDouble(resultMap.get("premium_NDNE").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setZiRan(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setJiaoqiang(Double.parseDouble(resultMap.get("forcePremium").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setJiaoqiang(0);
                                                            }
                                                            try {
                                                                saveQuoteResponse.setChechuan(Double.parseDouble(resultMap.get("vehicleTaxPremium").toString()));
                                                            } catch (Exception e) {
                                                                saveQuoteResponse.setChechuan(0);
                                                            }

                                                               /* ShowInsuredInfoPage showInsuredInfoPage =new ShowInsuredInfoPage(2);
                                                                Request request15 = new Request();
                                                                Map paramMap15 = response14.getResponseMap();
                                                                try {
                                                                    receiveName = java.net.URLEncoder.encode(receiveName , "gbk");
                                                                }catch (Exception e){}
                                                                paramMap15.put("receiveName", receiveName);
                                                                paramMap15.put("receiveMobile", receiveMobile);
                                                                try {
                                                                    receiveAddress = java.net.URLEncoder.encode(receiveAddress , "gbk");
                                                                }catch (Exception e){}
                                                                paramMap15.put("receiveAddress", receiveAddress);
                                                                paramMap15.put("sendDate", receiveDate);
                                                                try {
                                                                    receiveVoice = java.net.URLEncoder.encode(receiveVoice , "gbk");
                                                                }catch (Exception e){}
                                                                paramMap15.put("invoice", receiveVoice);
                                                                paramMap15.put("certificateNo",toubaoCertificateNo);
                                                                paramMap15.put("certificateNo_",beibaoCertificateNo);
                                                                try {
                                                                    touBaoAddress = java.net.URLEncoder.encode(touBaoAddress , "gbk");
                                                                }catch (Exception e){}
                                                                try {
                                                                    beiBaoAddress = java.net.URLEncoder.encode(beiBaoAddress , "gbk");
                                                                }catch (Exception e){}
                                                                paramMap15.put("insuredAddress",touBaoAddress);
                                                                paramMap15.put("insuredAddress_",beiBaoAddress);
                                                                paramMap15.put("email", touBaoEmail);
                                                                paramMap15.put("email_", beiBaoEmail);
                                                                paramMap15.put("mobileTelephone",touBaoMobileTelePhone);
                                                                paramMap15.put("mobileTelephone_",beiBaoMobileTelePhone);


                                                                request15.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWINSUREDINFO);
                                                                request15.setRequestParam(paramMap15);
                                                                Response response15 = showInsuredInfoPage.run(request15);

                                                                ShowTemporaryInfoPage showTemporaryInfoPage =new ShowTemporaryInfoPage(2);
                                                                Request request16 = new Request();
                                                                request16.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWTEMPORARYINFO);
                                                                request16.setRequestParam(response15.getResponseMap());
                                                                Response response16 = showTemporaryInfoPage.run(request16);

                                                                UpdateInsuredInfo updateInsuredInfo = new UpdateInsuredInfo(2);
                                                                Request requestUpdateInsured = new Request();
                                                                requestUpdateInsured.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_UPDATEINSUREDINFO);
                                                                requestUpdateInsured.setRequestParam(response16.getResponseMap());
                                                                Response responseUpdateInsured = updateInsuredInfo.run(requestUpdateInsured);

                                                                UpdateApplicantInfo updateApplicantInfo = new UpdateApplicantInfo(2);
                                                                Request requestUpdateApplicant = new Request();
                                                                requestUpdateApplicant.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_UPDATEINSUREDINFO);
                                                                requestUpdateApplicant.setRequestParam(responseUpdateInsured.getResponseMap());
                                                                Response responseUpdateApplicant = updateApplicantInfo.run(requestUpdateApplicant);

                                                                Response secondResponse16 = showTemporaryInfoPage.run(request16);

                                                                ApplyUnderwritePage applyUnderwritePage =new ApplyUnderwritePage(2);
                                                                Request request17 = new Request();
                                                                request17.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYUNDERWRITE);
                                                                request17.setRequestParam(secondResponse16.getResponseMap());
                                                                Response response17 = applyUnderwritePage.run(request17);

                                                                PayRequestInitPage payRequestInitPage =new PayRequestInitPage(2);
                                                                Request request18 = new Request();
                                                                request18.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_PAYREQUESTINIT);
                                                                request18.setRequestParam(response17.getResponseMap());
                                                                Response response18 = payRequestInitPage.run(request18);

                                                                SendValidateCodePage sendValidateCodePage =new SendValidateCodePage(2);
                                                                Request request19 = new Request();
                                                                request19.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SENDVALIDATECODE);
                                                                request19.setRequestParam(response18.getResponseMap());
                                                                Response response19 = sendValidateCodePage.run(request19);

                                                                SaveValidateCodePage saveValidateCodePage = new SaveValidateCodePage(2);
                                                                Request request20 = new Request();
                                                                Map requestPayMap = response19.getResponseMap();
                                                                requestPayMap.put("validateCode",validateCode);
                                                                request20.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SAVEVALIDATECODE);
                                                                request20.setRequestParam(requestPayMap);
                                                                Response response20 = saveValidateCodePage.run(request20);


                                                                PayRequestPage payRequestPage =new PayRequestPage(2);
                                                                Request requestPay = new Request();
                                                                requestPay.setRequestParam( response20.getResponseMap());
                                                                requestPay.setUrl(SysConfigInfo.AXATP_PAYREQUEST);
                                                                Response payResponse = payRequestPage.run(requestPay);*/
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return saveQuoteResponse;
    }

    @Override
    public void createOrderByCarInfo(String licenseNo, String engineNo, String vehicleFrameNo, String licenseOwner, String mobilePhone, String certificateNo, String cheSun, String sanZhe, String siJi, String chengKe, String daoQiang, String boLi, String huaHen, String ziRan, String sheShui) {


        String touBaoEmail = "784506957@qq.com";
        String beiBaoEmail = "784506957@qq.com";
        String touBaoAddress ="mentougou2412";
        String beiBaoAddress ="mentougou2413";
        String receiveName="张文海";
        String receiveMobile="18810253437";
        String receiveVoice="张文海";
        String receiveDate="2016-07-01";
        String receiveAddress="mentougou2414";
        String validateCode="WPKD2B";

        PrecisionIndexPage precisionIndexPage =new PrecisionIndexPage(2);
        Request request1 =new Request();
        request1.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_PRECISIONINDEX);
        Map map = new HashMap<>();
        map.put("licenceNo",licenseNo);
        map.put("personnelName",licenseOwner);
        //map.put("cityCode",cityCode);
        request1.setRequestParam(map);
        Response response1 = precisionIndexPage.run(request1);
        if(response1.getReturnCode()==SysConfigInfo.SUCCESS200){
            ToPreparationPage toPreparationPage =new ToPreparationPage(2);
            Request request2 = new Request();
            request2.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_TOPREPARATION);
            request2.setRequestParam(response1.getResponseMap());
            Response response2  = toPreparationPage.run(request2);
            if(response2.getReturnCode()==SysConfigInfo.SUCCESS200) {
                SavePrecisionIndexInfoForPcPage savePrecisionIndexInfoForPcPage = new SavePrecisionIndexInfoForPcPage(2);
                Request request3 = new Request();
                request3.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SAVEPRECISIONINDEXINFOFORPC);
                request3.setRequestParam(response2.getResponseMap());
                Response response3 = savePrecisionIndexInfoForPcPage.run(request3);
                if (response3.getReturnCode() == SysConfigInfo.SUCCESS200) {
                    InitPrecisionBasicInfoPage initPrecisionBasicInfoPage = new InitPrecisionBasicInfoPage(2);
                    Request request4 = new Request();
                    request4.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                    request4.setRequestParam(response3.getResponseMap());
                    Response response4 = initPrecisionBasicInfoPage.run(request4);
                    if (response4.getReturnCode() == SysConfigInfo.SUCCESS200) {// 获取汽车购买价和汽车型号
                        CarBasicVehiclePriceQuery carBasicVehiclePriceQuery = new CarBasicVehiclePriceQuery(2);
                        Request request5 = new Request();
                        request5.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_CARBASEVEHICLEPRICEQUERY);
                        Map paramMap5 = response4.getResponseMap();
                        paramMap5.put("engineNo", engineNo);
                        paramMap5.put("vehicleFrameNo", vehicleFrameNo);
                        paramMap5.put("mobileTelephone", mobilePhone);
                        paramMap5.put("certificateNo", certificateNo);
                        request5.setRequestParam(paramMap5);
                        Response response5 = carBasicVehiclePriceQuery.run(request5);
                        if (response5.getReturnCode() == SysConfigInfo.SUCCESS200) {
                            CarQueryWhereRbCode carQueryWhereRbCode = new CarQueryWhereRbCode(2);
                            Request request6 = new Request();
                            request6.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_CARQUERYWHERERBCODE);
                            request6.setRequestParam(response5.getResponseMap());
                            Response response6 = carQueryWhereRbCode.run(request6);
                            if(response6.getReturnCode()==SysConfigInfo.SUCCESS200){
                                ApplyQueryPage applyQueryPage =new ApplyQueryPage(2);
                                Request request7 = new Request();
                                request7.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYQUERY);
                                request7.setRequestParam(response6.getResponseMap());
                                Response response7 = applyQueryPage.run(request7);
                                if(response7.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                    Request request8 = new Request();
                                    request8.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                                    request8.setRequestParam(response7.getResponseMap());
                                    Response response8 = initPrecisionBasicInfoPage.run(request8);
                                    if(response8.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                        ReInsureView reInsureView = new ReInsureView(2);
                                        Request request9 = new Request();
                                        request9.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_REINSUREVIEW);
                                        request9.setRequestParam(response8.getResponseMap());
                                        Response response9 = reInsureView.run(request9);
                                        if(response9.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                            ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                                            Request request10 = new Request();
                                            request10.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                                            request10.setRequestParam(response9.getResponseMap());
                                            Response response10 = showBusinessPlanInfoPage.run(request10);
                                            if(response10.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                ShowForcePlanInfoPage showForcePlanInfoPage = new ShowForcePlanInfoPage(2);
                                                Request request11 = new Request();
                                                request11.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWFORCEPLANINFO);
                                                request11.setRequestParam(response10.getResponseMap());
                                                Response response11 = showForcePlanInfoPage.run(request11);
                                                if(response11.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                    CalculaterBusinessPremiumPage calculaterBusinessPremiumPage = new CalculaterBusinessPremiumPage(2);
                                                    Request request13 = new Request();
                                                    request13.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_BUSINESSPREMIUMCALCULATER);
                                                    Map map13 = response11.getResponseMap();
                                                    map13.put("select_OD", cheSun);
                                                    map13.put("select_TP", sanZhe);
                                                    map13.put("select_DL", siJi);
                                                    map13.put("select_PL", chengKe);
                                                    map13.put("select_THEFT", daoQiang);
                                                    map13.put("select_GLASS", boLi);
                                                    map13.put("select_NICK", huaHen);
                                                    map13.put("select_NDNE", ziRan);
                                                    map13.put("select_FEDPC", sheShui);

                                                    request13.setRequestParam(map13);
                                                    Response response13 = calculaterBusinessPremiumPage.run(request13);
                                                    if(response13.getReturnCode()==SysConfigInfo.SUCCESS200) {
                                                        CalculaterForcePremiumPage calculaterForcePremiumPage = new CalculaterForcePremiumPage(2);
                                                        Request request14 = new Request();
                                                        request14.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_FORCEPREMIUMCALCULATER);
                                                        request14.setRequestParam(response13.getResponseMap());
                                                        Response response14 = calculaterForcePremiumPage.run(request14);


                                                        ShowInsuredInfoPage showInsuredInfoPage =new ShowInsuredInfoPage(2);
                                                        Request request15 = new Request();
                                                        Map paramMap15 = response14.getResponseMap();
                                                        try {
                                                            receiveName = java.net.URLEncoder.encode(receiveName , "gbk");
                                                        }catch (Exception e){}
                                                        paramMap15.put("receiveName", receiveName);
                                                        paramMap15.put("receiveMobile", receiveMobile);
                                                        try {
                                                            receiveAddress = java.net.URLEncoder.encode(receiveAddress , "gbk");
                                                        }catch (Exception e){}
                                                        paramMap15.put("receiveAddress", receiveAddress);
                                                        paramMap15.put("sendDate", receiveDate);
                                                        try {
                                                            receiveVoice = java.net.URLEncoder.encode(receiveVoice , "gbk");
                                                        }catch (Exception e){}
                                                        paramMap15.put("invoice", receiveVoice);
                                                        paramMap15.put("certificateNo",certificateNo);
                                                        paramMap15.put("certificateNo_",certificateNo);
                                                        try {
                                                            touBaoAddress = java.net.URLEncoder.encode(touBaoAddress , "gbk");
                                                        }catch (Exception e){}
                                                        try {
                                                            beiBaoAddress = java.net.URLEncoder.encode(beiBaoAddress , "gbk");
                                                        }catch (Exception e){}
                                                        paramMap15.put("insuredAddress",touBaoAddress);
                                                        paramMap15.put("insuredAddress_",beiBaoAddress);
                                                        paramMap15.put("email", touBaoEmail);
                                                        paramMap15.put("email_", beiBaoEmail);
                                                        paramMap15.put("mobileTelephone",mobilePhone);
                                                        paramMap15.put("mobileTelephone_",mobilePhone);


                                                        request15.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWINSUREDINFO);
                                                        request15.setRequestParam(paramMap15);
                                                        Response response15 = showInsuredInfoPage.run(request15);

                                                        ShowTemporaryInfoPage showTemporaryInfoPage =new ShowTemporaryInfoPage(2);
                                                        Request request16 = new Request();
                                                        request16.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWTEMPORARYINFO);
                                                        request16.setRequestParam(response15.getResponseMap());
                                                        Response response16 = showTemporaryInfoPage.run(request16);

                                                        UpdateInsuredInfo updateInsuredInfo = new UpdateInsuredInfo(2);
                                                        Request requestUpdateInsured = new Request();
                                                        requestUpdateInsured.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_UPDATEINSUREDINFO);
                                                        requestUpdateInsured.setRequestParam(response16.getResponseMap());
                                                        Response responseUpdateInsured = updateInsuredInfo.run(requestUpdateInsured);

                                                        UpdateApplicantInfo updateApplicantInfo = new UpdateApplicantInfo(2);
                                                        Request requestUpdateApplicant = new Request();
                                                        requestUpdateApplicant.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_UPDATEINSUREDINFO);
                                                        requestUpdateApplicant.setRequestParam(responseUpdateInsured.getResponseMap());
                                                        Response responseUpdateApplicant = updateApplicantInfo.run(requestUpdateApplicant);

                                                        Response secondResponse16 = showTemporaryInfoPage.run(request16);

                                                        ApplyUnderwritePage applyUnderwritePage =new ApplyUnderwritePage(2);
                                                        Request request17 = new Request();
                                                        request17.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYUNDERWRITE);
                                                        request17.setRequestParam(secondResponse16.getResponseMap());
                                                        Response response17 = applyUnderwritePage.run(request17);

                                                        PayRequestInitPage payRequestInitPage =new PayRequestInitPage(2);
                                                        Request request18 = new Request();
                                                        request18.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_PAYREQUESTINIT);
                                                        request18.setRequestParam(response17.getResponseMap());
                                                        Response response18 = payRequestInitPage.run(request18);

                                                        SendValidateCodePage sendValidateCodePage =new SendValidateCodePage(2);
                                                        Request request19 = new Request();
                                                        request19.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SENDVALIDATECODE);
                                                        request19.setRequestParam(response18.getResponseMap());
                                                        Response response19 = sendValidateCodePage.run(request19);

                                                        SaveValidateCodePage saveValidateCodePage = new SaveValidateCodePage(2);
                                                        Request request20 = new Request();
                                                        Map requestPayMap = response19.getResponseMap();
                                                        requestPayMap.put("validateCode",validateCode);
                                                        request20.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SAVEVALIDATECODE);
                                                        request20.setRequestParam(requestPayMap);
                                                        Response response20 = saveValidateCodePage.run(request20);


                                                        PayRequestPage payRequestPage =new PayRequestPage(2);
                                                        Request requestPay = new Request();
                                                        requestPay.setRequestParam( response20.getResponseMap());
                                                        requestPay.setUrl(SysConfigInfo.AXATP_PAYREQUEST);
                                                        Response payResponse = payRequestPage.run(requestPay);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
