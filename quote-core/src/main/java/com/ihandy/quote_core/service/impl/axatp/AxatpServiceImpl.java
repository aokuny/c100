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
        String mobileTelePhone="18810253437";
        String certificateNo ="132425196903135852";
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
                        paramMap5.put("mobileTelePhone", mobileTelePhone);
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
                                SavePrecisionCarInfo savePrecisionCarInfo = new SavePrecisionCarInfo(2);
                                Request request7 = new Request();
                                request7.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_SAVEPRECISIONCARINFO);
                                request7.setRequestParam(response6.getResponseMap());
                                Response response7 = savePrecisionCarInfo.run(request7);

                                ApplyQueryPage applyQueryPage =new ApplyQueryPage(2);
                                Request request10 = new Request();
                                request10.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_APPLYQUERY);
                                request10.setRequestParam(response7.getResponseMap());
                                Response response10 = applyQueryPage.run(request10);

                                Request request11 = new Request();
                                request11.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_INITPRECISIONBASICINFO);
                                request11.setRequestParam(response10.getResponseMap());
                                Response response11 = initPrecisionBasicInfoPage.run(request11);

                                HandleApplyQueryReturnPage handleApplyQueryReturnPage =new HandleApplyQueryReturnPage(2);
                                Request request12 = new Request();
                                request12.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_HANDLEAPPLYQUERYRETURN);
                                request12.setRequestParam(response11.getResponseMap());
                                Response response12 = handleApplyQueryReturnPage.run(request12);


                                ReInsureView reInsureView =new ReInsureView(2);
                                Request request8 = new Request();
                                request8.setUrl(SysConfigInfo.AXATP_DOMIAN + SysConfigInfo.AXATP_REINSUREVIEW);
                                request8.setRequestParam(response12.getResponseMap());
                                Response response8 = reInsureView.run(request8);

                                ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                                Request request9 = new Request();
                                request9.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                                request9.setRequestParam(response8.getResponseMap());
                                Response response9  = showBusinessPlanInfoPage.run(request9);
                            }

                          /*  ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                            Request request7 = new Request();
                            request7.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                            request7.setRequestParam(response6.getResponseMap());
                            Response response7  = showBusinessPlanInfoPage.run(request7);*/
                        }
                    }
               /*
                ShowBusinessPlanInfoPage showBusinessPlanInfoPage = new ShowBusinessPlanInfoPage(2);
                Request request2 = new Request();
                request2.setUrl(SysConfigInfo.AXATP_DOMIAN+SysConfigInfo.AXATP_SHOWBUSINESSPLANINFO);
                request2.setRequestParam(response.getResponseMap());
                Response response2  = showBusinessPlanInfoPage.run(request2);*/

                }
            }
        }
        return null;
    }
}
