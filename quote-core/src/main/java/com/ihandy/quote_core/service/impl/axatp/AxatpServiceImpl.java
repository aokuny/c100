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
    @Override
    public SaveQuoteResponse getQuoteInfoByCarInfo(String licenseNo, String licenseOwner,String cityCode) {
        String engineNo="FW151330";
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
                        paramMap5.put("mobileTelephone", touBaoMobileTelePhone);
                        paramMap5.put("certificateNo", toubaoCertificateNo);
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

                                Request request8 = new Request();
                                request8.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                                request8.setRequestParam(response7.getResponseMap());
                                Response response8 = initPrecisionBasicInfoPage.run(request8);


                                ReInsureView reInsureView =new ReInsureView(2);
                                Request request9 = new Request();
                                request9.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_REINSUREVIEW);
                                request9.setRequestParam(response8.getResponseMap());
                                Response response9 = reInsureView.run(request9);

                                ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                                Request request10 = new Request();
                                request10.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                                request10.setRequestParam(response9.getResponseMap());
                                Response response10  = showBusinessPlanInfoPage.run(request10);

                                ShowForcePlanInfoPage showForcePlanInfoPage = new ShowForcePlanInfoPage(2);
                                Request request11 = new Request();
                                request11.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_SHOWFORCEPLANINFO);
                                request11.setRequestParam(response10.getResponseMap());
                                Response response11  = showForcePlanInfoPage.run(request11);


                                CalculaterBusinessPremiumPage calculaterBusinessPremiumPage = new CalculaterBusinessPremiumPage(2);
                                Request request13 = new Request();
                                request13.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_BUSINESSPREMIUMCALCULATER);
                                request13.setRequestParam(response11.getResponseMap());
                                Response response13 = calculaterBusinessPremiumPage.run(request13);

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
                                Response payResponse = payRequestPage.run(requestPay);

                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
