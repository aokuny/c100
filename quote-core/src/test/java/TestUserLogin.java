import com.alibaba.fastjson.JSONObject;
import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import net.sf.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhujiajia on 16/5/25.
 */
public class TestUserLogin {


    public String getCookies() {

        //获取session信息
        String url_login = "http://dm.axatp.com/login.do";
        String cookieValue = HttpsUtil.sendGetForAxatp(url_login, null, "GBK").get("cookieValue");
//        System.out.println(cookieValue);

        //获取要邀请码标识
        StringBuffer param_login = new StringBuffer();
        param_login.append("memberName=jtl_bj&");
        param_login.append("flag=ajaxRecommendCode");
        String login_two = HttpsUtil.sendPost(url_login, param_login.toString(), cookieValue, "GBK").get("html");
//        System.out.println(login_two);

        //获取验证码图片
        String url_randCode = "http://dm.axatp.com/getAdditionNo.do?type=login";
        Map imgMap = HttpsUtil.getURLImgOutByte(url_randCode, cookieValue, "GBK");
//        System.out.println(html_randCode);
        String html_getCode = HttpsUtil.uploadFile((byte[]) imgMap.get("byte"),
                "http://192.168.4.117:8011/GetSeccode.aspx", "code."+imgMap.get("type"));
//        System.out.println(html_getCode);

        //用户登录
        StringBuffer paramSb = new StringBuffer();
        paramSb.append("memberName=jtl_bj&");
        paramSb.append("voucherNoArray=&");
        paramSb.append("voucherNoArrayLogin=&");
        paramSb.append("defaultAgentCode=1&");
        paramSb.append("isVIP=false&");
        paramSb.append("linkResource=&");
        paramSb.append("flag=login&");
        paramSb.append("memberName=jtl_bj&");
        paramSb.append("password=123456&");
        paramSb.append("showRecommendCode=1&");
        paramSb.append("isAgent=3212&");
        paramSb.append("checkRecommendCode=0&");
        paramSb.append("recommendCode=123&");
        paramSb.append("randomCode=" + html_getCode + "&");
        String param = paramSb.toString();
        param = param.substring(0, param.length() - 1);
        String html_index = HttpsUtil.sendPost(url_login, param.toString(), cookieValue, "GBK").get("html");
//        System.out.println(html_index);
        if(!html_index.contains("你已经登录成功，你可以选择以下操作")){
            cookieValue="";
        }
        return cookieValue;

    }


    public Map oneStep(String sessionId){
        Map oneStepInfo=new HashMap();
        String cookies=sessionId;
        String url_iframe="http://dm.axatp.com/toPreparation.do?cityCode=110100&cityCode_ajax=&localProvinceCode=110000&localCityCode=&departmentCode=33&linkResource=&selectPayChannel=&isAgent=3212&isRenewal=0&ecInsureId=&planDefineId=3&rt=0&ms=&renewalFlag=&selectCityPage=selectCityPage&insuranceBeginTimeEnd=&pageInfo=selectCity&carcity=&pagereferrer=&cityName=&infoValue=&prohibitValue=&isJZ=1&select_city=%B1%B1%BE%A9%CA%D0&licenceNo=%BE%A9P6WA76&personnelName=%B6%C5%D5%D7%C8%AA";
        String html1 = HttpsUtil.sendGetForAxatp(url_iframe, cookies, "GBK").get("html");
        String url_rediect="http://dm.axatp.com/savePrecisionIndexInfoForPc.do?ecInsureId=&linkResource=&selectPayChannel=&isAgent=3212&cityCode=110100&localProvinceCode=110000&isRenewal=0&planDefineId=3&rt=0&ms=&mark=&pageInfo=selectCity&licenceNo=¾©P6WA76&personnelName=¶ÅÕ×Èª";
        String html2 = HttpsUtil.sendGetForAxatp(url_rediect, cookies, "GBK").get("html");
        String ecInsureId="";
//        System.out.println(html2);
//    <input type="hidden" id="ecInsureId" name="ecInsureId" value="E1E65AD17E74DCCB1BE11805718ABEA7A651776D7A5C6889"/>
//        Map<String, Object> returnMap = new HashMap<>();
//        JSONObject obj = JSONObject.parseObject(html2);
//        JSONObject sid = obj.getJSONArray("data").getJSONObject(0);
//        System.out.println(sid);
        Document doc = Jsoup.parse(html2);
        ecInsureId = doc.getElementById("ecInsureId").attributes().get("value");
        oneStepInfo.put("ecInsureId",ecInsureId);
        String url_location="http://dm.axatp.com/initPrecisionBasicInfo.do?cityName=北京市&ms=&cityCode=110100&licenceNo=京P6WA76&ecInsureId="+ecInsureId+"&transferFlag=0&pagereferrer=&isAgent=3212&pageInfo=selectCity&personnelName=张金生&localProvinceCode=110000";
        String html3 = HttpsUtil.sendGetForAxatp(url_location, cookies, "GBK").get("html");
//        System.out.println(html3);

        //选择车辆信息
        String url_SelectCarInfo="http://dm.axatp.com/carBasicVehiclePriceQuery.do?" +
                "isAgent=3212&ecInsureId="+ecInsureId+"&isRenewal=0&cityCode=110100&localProvinceCode=110000&planDefineId=3&rt=&licenceNo=%BE%A9QS78J1&personnelName=%D5%C5%BD%F0%C9%FA";

        String postParm="defaultAgentCode=1&isVIP=false&birthdayYY=&birthdayMM=&birthdayDD=&newVehicleFlagHidden=0&lastForcePolicyNo=&ecInsureId=E1E65AD17E74DCCB454BB2BB7B0C207DD60E89E0C63AA315&isRenewal=0&linkResource=&isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&selectPayChannel=&pageInfo=carPrecisionInfo&tbsn=&buyerNick=&orderId=&auctionId=&cityPlat=&rt=&ms=&timedefault=&mark=&licenceLimits=&licenceLimitsMsg=%3Cdiv+style%3D%27border%3A1px+solid+%23F22D0D%3Bmargin-left%3A-5px%3Bwidth%3A304px%3B+margin-right%3A0px%3B+background-color%3A%23FFECEC%27%3E%B8%C3%B5%D8%C7%F8%D6%BB%D6%A7%B3%D6%B3%B5%C5%C6%CE%AA%B5%C4%B3%B5%C1%BE%CD%F8%C9%CF%CD%B6%B1%A3%3C%2Fdiv%3E&city_beijing=&bizInsureBeignTime=2016-06-04&forceInsureBeignTime=2016-06-04&pagereferrer=%25E5%259C%25B0%25E9%259D%25A2%25E8%2590%25A5%25E9%2594%2580-%25E6%258E%25A8%25E8%258D%2590&cityName=%25E5%258C%2597%25E4%25BA%25AC%25E5%25B8%2582&carcity=%BE%A9MM3767&live800_URL_JSP=http%3A%2F%2Fonlinecs.axatp.com%2Flive800%2FchatClient%2Fchatbox.jsp&gztFlag=0&viewMode=edit&hideFamilyName=&connectType=1&isFirstIframe=&vehicleId=&newCarPriceOld=&newCarPrice=&newCarSeats=&isJZ=1&ecEnginNo=&searchType=0&flag=&messages=&infoValue=userId%253D7124003%2526loginname%253Djtl_bj%2526grade%253D1%2526name%253Djtl_bj%2526mobileNo%253D%25E4%25BA%25ACMM3767%2526memo%253Dnull%2526timestamp%253D1464922407907%2526hashCode%253D34b72da498118d05574fc2c05cebc497&prohibitValue=null&blackListFlag=&renewalProtectFlag=&knmdFlag=&isCompleteData=&isBeiJingFlag=1&madeDate=&engineNo=FW151330&vehicleFrameNo=LBECFAHC5FZ226987&personnelName=%D5%C5%CE%C4%BA%A3&certificateNo=372527196603254019&mobileTelephone=15311421136&isMortgage=0&BeneficiaryName=";
        StringBuffer paramCarInfo = new StringBuffer();
//        paramCarInfo.append("defaultAgentCode=1&");
//        paramCarInfo.append("isVIP=false&");
//        paramCarInfo.append("newVehicleFlagHidden=0&");
//        paramCarInfo.append("ecInsureId="+ecInsureId+"&");
//        paramCarInfo.append("isRenewal=0&");
//        paramCarInfo.append("isAgent=3212&");
//        paramCarInfo.append("cityCode=110100&");
//        paramCarInfo.append("localProvinceCode=110000&");
//        paramCarInfo.append("planDefineId=3&");
//        paramCarInfo.append("pageInfo=carPrecisionInfo&");
////        paramCarInfo.append("bizInsureBeignTime=2016-06-02&");
////        paramCarInfo.append("forceInsureBeignTime=2016-06-02&");
//        paramCarInfo.append("cityName=北京市&");
//        paramCarInfo.append("carcity=京QS78J1&");
////        paramCarInfo.append("live800_URL_JSP=http://onlinecs.axatp.com/live800/chatClient/chatbox.jsp&");
//        paramCarInfo.append("gztFlag=0&");
//        paramCarInfo.append("viewMode=edit&");
//        paramCarInfo.append("connectType=1&");
//        paramCarInfo.append("isJZ=1&");
//        paramCarInfo.append("searchType=0&");
//        paramCarInfo.append("prohibitValue=null&");
//        paramCarInfo.append("isBeiJingFlag=1&");
          paramCarInfo.append("engineNo=574802&");
          paramCarInfo.append("vehicleFrameNo=LFV2A11G473116388&");
//        paramCarInfo.append("personnelName=张金生&");
//        paramCarInfo.append("mobileTelephone=13511421136&");
//        paramCarInfo.append("certificateNo=1&");
//        paramCarInfo.append("isMortgage=0&");
//        paramCarInfo.append("transferFlag=0&");
//        paramCarInfo.append("isRenewal=0&");
        String param = paramCarInfo.toString();
        param = paramCarInfo.substring(0, param.length() - 1);
        String html_selectPage = HttpsUtil.sendPost(url_SelectCarInfo, postParm, cookies, "GBK").get("html");
//        System.out.println(html_selectPage);
//

//
        return oneStepInfo;
    }

    @Test
    public void Price(){
//        String cookies=getCookies();
//        Map oneStepInfo=oneStep(cookies);
//
//
//        String url_ajaxCarQueryWhereRbCode="http://dm.axatp.com/ajaxCarQueryWhereRbCode.do?" +
//                "rbCode=XDABJD0010&ecInsureId="+oneStepInfo.get("ecInsureId")+"&isRenewal=0&isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&rt=&ms=";
//        String html_ajaxCarQueryWhereRbCode =
//                HttpsUtil.sendGetForAxatp(url_ajaxCarQueryWhereRbCode, cookies, "GBK").get("html");
//
//
//        String url_ajaxIsFirstCalcSuccess="http://dm.axatp.com/ajaxIsFirstCalcSuccess.do?_=1464920032919&ecInsureId=" +
//                oneStepInfo.get("ecInsureId")+"&planCode=108&status=1";
//        String html_ajaxIsFirstCalcSuccess =
//                HttpsUtil.sendGetForAxatp(url_ajaxIsFirstCalcSuccess, cookies, "GBK").get("html");
//
//        String url_ajaxPrecisionCommonMessage="http://dm.axatp.com/ajaxPrecisionCommonMessage.do?" +
//                "ajaxCommonMsgType=saveTempInfo&ecInsureId="+oneStepInfo.get("ecInsureId")+"&_=1464920025003";
//        String html_ajaxPrecisionCommonMessage =
//                HttpsUtil.sendGetForAxatp(url_ajaxPrecisionCommonMessage, cookies, "GBK").get("html");
//
//        //ajaxSaveCalcResult.do???double
//
//        http://dm.axatp.com/savePrecisionCarInfo.do?isAgent=3212&cityCode=110100&ecInsureId=E1E65AD17E74DCCB454BB2BB7B0C207DD60E89E0C63AA315&isRenewal=0&localProvinceCode=110000&planDefineId=3&rt=&licenceNo=%BE%A9MM3767&autoModelCode=XDABJD0010&vehicleCode=XDABJD0010



//        String url_savePrecisionCarInfo="http://dm.axatp.com/savePrecisionCarInfo.do";
//        String urlP_contactHistory="action=timeout&ecInsureId="+oneStepInfo.get("ecInsureId")+"&isAgent=3212&pageInfo=businessPlanInfo&ms=";
//
//        String html_contactHistory =
//                HttpsUtil.sendPost(url_contactHistory, urlP_contactHistory, cookies, "GBK").get("html");
//        String url_applyQuery="http://dm.axatp.com/applyQuery.do?" +
//                "isJZ=1&ecInsureId="+oneStepInfo.get("ecInsureId")+"&cityCode=110100&autoModeCode=XDABJD0010&tbsn=";
//        String html_applyQuery =
//                HttpsUtil.sendGetForAxatp(url_applyQuery, cookies, "GBK").get("html");

//
//        String url_contactHistory="http://dm.axatp.com/contactHistory.do";
//        String urlP_contactHistory="action=timeout&ecInsureId="+oneStepInfo.get("ecInsureId")+"&isAgent=3212&pageInfo=businessPlanInfo&ms=";
//
////        String html_contactHistory =
//                HttpsUtil.sendPost(url_contactHistory, urlP_contactHistory, cookies, "GBK").get("html");
//
//
//        String url_pkgPremiumCalc="http://dm.axatp.com/pkgPremiumCalc.do?" +
//                "&ecInsureId=" +
//                oneStepInfo.get("ecInsureId")+"&pkgName=class&calcFlag=true&isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&bizInsureBeignTime=2016-07-10";
//        String html_pkgPremiumCalc =
//                HttpsUtil.sendGetForAxatp(url_pkgPremiumCalc, cookies, "UTF-8").get("html");



//http://dm.axatp.com/pkgPremiumCalc.do?_=1464920030314&ecInsureId=E1E65AD17E74DCCB454BB2BB7B0C207D762ACDBFBC367A37&pkgName=economic&calcFlag=true&isAgent=3212&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&bizInsureBeignTime=2016-07-10

//        http://dm.axatp.com/contactHistory.do



//        //商业险报价
//        String url_PriceBus="http://dm.axatp.com/businessPremiumCalculater.do?" +
//                "isAgent=3212&ecInsureId=" +
//                oneStepInfo.get("ecInsureId")+"&cityCode=110100&localProvinceCode=110000&planDefineId=3&isRenewal=0&rt=&id=_TP&bizInsureBeignTime=2016-07-10";
//        StringBuffer paramBusInfo = new StringBuffer();
//        String url_post="ecInsureId="+oneStepInfo.get("ecInsureId")+"&isDesignatdDriverArea=0&isDesignatdDriver=0&planDefineId=3&planDefineId2=&selectPayChannel=undefined&linkResource=&pageInfo=businessPlanInfo&lastForcePolicyNo=&tbsn=&errorMessage=&isAgent=3212&cityCode=110100&localProvinceCode=110000&isRenewal=0&amount_OD=&insuredAmountminFloat=&insuredAmountmaxFloat=&flag=1&rt=&ms=&isJingHu=1&runCardCertificateDate=undefined&newPkgName=class&personnelName=%D5%C5%CE%C4%BA%A3&vehicleLicenceCode=%BE%A9MM3767&cityName=%25E5%258C%2597%25E4%25BA%25AC%25E5%25B8%2582&pagereferrer=%25E5%259C%25B0%25E9%259D%25A2%25E8%2590%25A5%25E9%2594%2580-%25E6%258E%25A8%25E8%258D%2590&infoValue=userId%253D7124003%2526loginname%253Djtl_bj%2526grade%253D1%2526name%253Djtl_bj%2526mobileNo%253D%25E4%25BA%25ACMM3767%2526memo%253Dnull%2526timestamp%253D1464918189222%2526hashCode%253D607ee23738a4be894918d1f2d8171886&prohibitValue=&live800_URL_JSP=http%3A%2F%2Fonlinecs.axatp.com%2Flive800%2FchatClient%2Fchatbox.jsp&isPresent=N&isJZ=1&initPremiumFlagBIZ=1&businessPremium_biz=&bizStampTax_biz=&totalDiscountPremium_biz=&originalBusinessPremium_biz=&economic_FlagBIZ=1&class_FlagBIZ=1&disabledDates=&goodCustomerSwitch=N&bizInsureBeignTime=2016-07-10&selectPkgType=class&pkgSelect=free&select_OD=OD&select_TP=500000&select_DL=10000&select_PL=10000&select_THEFT=THEFT&select_GLASS=1&select_NICK=N&select_NDNE=NDNE&select_FEDPC=FEDPC";
//        String html_BusPrice = HttpsUtil.sendPost(url_PriceBus, url_post, cookies, "GBK").get("html");
//        System.out.println(html_pkgPremiumCalc);


    }


}
