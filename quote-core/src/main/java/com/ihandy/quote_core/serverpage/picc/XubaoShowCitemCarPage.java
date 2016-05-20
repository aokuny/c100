package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoShowCitemCarPage extends BasePage{
	
    public XubaoShowCitemCarPage(int type) {
		super(type);
	}

	private static Logger logger = LoggerFactory.getLogger(XubaoShowCitemCarPage.class);
    @Override
    public String doRequest(Request request) {
        String htmlCitemCar = null;
        String html = null;
        String urlCitemCar = request.getUrl();
        Map paraMap = request.getRequestParam();
        urlCitemCar = urlCitemCar +"?"+ StringBaseUtils.Map2GetParam(paraMap);
        //  urlCitemCar =   urlCitemCar+"?editType=SHOW_POLICY&bizType=POLICY&bizNo=PDAT20151102T000182528&riskCode=DAA&minusFlag=&contractNo=%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20&comCode=11029204&originQuery=&proposalNo=TDAT20151102T000210983&rnd554=Fri%20May%2020%2009:14:49%20UTC+0800%202016";
        // urlCitemCar = "http://10.134.136.48:8000/prpall/business/showCitemCar.do?editType=SHOW_POLICY&bizType=POLICY&bizNo=PDAT20151102T000182528&riskCode=DAA&minusFlag=&contractNo=                      &comCode=11029204&originQuery=&proposalNo=TDAT20151102T000210983&rnd554=Fri May 20 09:14:49 UTC+0800 2016";
        Map mapClaimsMsg = HttpsUtil.sendGet(urlCitemCar,super.piccSessionId,"GB2312");
        htmlCitemCar = mapClaimsMsg.get("html").toString();
        return htmlCitemCar;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        String CarUsedType=""; //使用性质
        String MoldName="";//品牌型号
        double PurchasePrice=0; //购买价格
        String CarRegisterDate="";//车辆注册日期
        int CarSeated=0;//座位数量

        if(!html.equals("")||null!=html) {
            Map returnMap = new HashMap<>();
            Map lastResult = new HashMap<>();
            returnMap.put("nextParams", null);
            Document doc = Jsoup.parse(html);
            Elements trs = doc.getElementsByClass("fix_table").select("tr");
            //汽车使用性质
            try {
                Elements tdsCarUsedType = trs.get(4).select("td");
                Element elementCarUsedType =  tdsCarUsedType.get(3).getElementById("prpCitemCar.useNatureCode");
                if (elementCarUsedType.tagName().equals("select")) {
                    Elements CarUsedTypeElements = elementCarUsedType.getAllElements();
                    for (int i = 0; i < CarUsedTypeElements.size(); i++) {
                        if (CarUsedTypeElements.get(i).attributes().hasKey("selected")) {
                            CarUsedType = CarUsedTypeElements.get(9).text();
                            lastResult.put("CarUsedType",CarUsedType);
                        }
                    }
                }
            }catch(Exception e){
                lastResult.put("CarUsedType",CarUsedType);
                logger.info("抓取机器人，【 PICC 解析车辆信息中车辆使用用途失败】");
            }
            //首次登记日期
            try {
                Elements tdsCarRegisterDate = trs.get(5).select("td");
                Element elementCarRegisterDate =  tdsCarRegisterDate.get(1).getElementById("prpCitemCar.enrollDate");
                if(elementCarRegisterDate.tagName().equals("input")){
                    CarRegisterDate =elementCarRegisterDate.attributes().get("value");
                    lastResult.put("CarRegisterDate",CarRegisterDate);
                }
            }catch(Exception e){
                lastResult.put("CarRegisterDate",CarRegisterDate);
                logger.info("抓取机器人，【 PICC 解析车辆信息中初次登记日期失败】");
            }
            //汽车购买价格    //车型编码
            try {
                Elements tdsPurchasePrice = trs.get(6).select("td");
                Element elementPurchasePrice = tdsPurchasePrice.get(3).getElementById("prpCitemCar.purchasePrice");
                if (elementPurchasePrice.tagName().equals("input")) {
                    PurchasePrice = Double.parseDouble(elementPurchasePrice.attributes().get("value"));
                    lastResult.put("PurchasePrice",PurchasePrice);
                }
                Element elementMoldName =  tdsPurchasePrice.get(1).getElementById("prpCitemCar.brandName");
                if(elementMoldName.tagName().equals("input")){
                    MoldName = elementMoldName.attributes().get("value");
                    lastResult.put("MoldName",MoldName);
                }
            }catch (Exception e){
                lastResult.put("PurchasePrice",PurchasePrice);
                logger.info("抓取机器人，【 PICC 解析车辆信息新车购买价格失败】");
                logger.info("抓取机器人，【 PICC 解析车辆信息品牌型号失败】");
            }
            //汽车核定座位数
            try {
                Elements tdsCarSeated = trs.get(7).select("td");
                Element elementCarSeated = tdsCarSeated.get(3).getElementById("prpCitemCar.seatCount");
                if (elementCarSeated.tagName().equals("input")) {
                    CarSeated = Integer.parseInt(elementCarSeated.attributes().get("value"));
                    lastResult.put("CarSeated",CarSeated);
                }
            }catch (Exception exception){
                lastResult.put("CarSeated",CarSeated);
                logger.info("抓取机器人，【 PICC 解析车辆信息核定座位数失败】");
            }
            returnMap.put("lastResult", lastResult);
            response.setResponseMap(returnMap);
            response.setReturnCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
        }else{
            response.setResponseMap(null);
            response.setReturnCode(SysConfigInfo.ERROR404);
            response.setErrMsg(SysConfigInfo.ERROR404MSG);
        }
        return response;
    }

    @Override
    public Response run(Request request) {
        String html = doRequest(request);
        Response response = getResponse(html);
        return response;
    }
}
