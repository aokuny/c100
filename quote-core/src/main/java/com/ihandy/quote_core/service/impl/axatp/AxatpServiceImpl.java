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
        String touBaoAddress ="门头沟toubao";
        String beiBaoAddress ="门头沟beibao";

        String receiveName="张文海";
        String receivePhone="18810253437";
        String receiveVoice="发票抬头";
        String receiveDate="2016-07-01";
        String receiveAddress="门头沟2403";



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
                        paramMap5.put("mobileTelePhone", touBaoMobileTelePhone);
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


                                CalculaterBusinessPremiumPage calculaterBusinessPremiumPage = new CalculaterBusinessPremiumPage(2);
                                Request request13 = new Request();
                                request13.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_BUSINESSPREMIUMCALCULATER);
                                request13.setRequestParam(response9.getResponseMap());
                                Response response13 = calculaterBusinessPremiumPage.run(request13);

                                CalculaterForcePremiumPage calculaterForcePremiumPage = new CalculaterForcePremiumPage(2);
                                Request request14 = new Request();
                                request14.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_FORCEPREMIUMCALCULATER);
                                request14.setRequestParam(response13.getResponseMap());
                                Response response14 = calculaterForcePremiumPage.run(request14);

                                ShowInsuredInfoPage showInsuredInfoPage =new ShowInsuredInfoPage(2);
                                Request request15 = new Request();
                                request15.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWINSUREDINFO);
                                request15.setRequestParam(response14.getResponseMap());
                                Response response15 = showInsuredInfoPage.run(request15);

                                ShowTemporaryInfoPage showTemporaryInfoPage =new ShowTemporaryInfoPage(2);
                                Request request16 = new Request();
                                request16.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SHOWTEMPORARYINFO);
                                Map paramMap16 = response15.getResponseMap();
                                paramMap16.put("engineNo", engineNo);
                                paramMap16.put("vehicleFrameNo", vehicleFrameNo);
                                paramMap16.put("mobileTelePhone", touBaoMobileTelePhone);
                                paramMap16.put("certificateNo", toubaoCertificateNo);
                                request16.setRequestParam(paramMap16);
                                Response response16 = showTemporaryInfoPage.run(request16);


                                ApplyUnderwritePage applyUnderwritePage =new ApplyUnderwritePage(2);
                                Request request17 = new Request();
                                request17.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYUNDERWRITE);
                                request17.setRequestParam(response16.getResponseMap());
                                Response response17 = applyUnderwritePage.run(request17);

                                PayRequestInitPage payRequestInitPage =new PayRequestInitPage(2);
                                Request request18 = new Request();
                                request18.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_PAYREQUESTINIT);
                                request18.setRequestParam(response17.getResponseMap());
                                Response response18 = payRequestInitPage.run(request18);
                                

                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
