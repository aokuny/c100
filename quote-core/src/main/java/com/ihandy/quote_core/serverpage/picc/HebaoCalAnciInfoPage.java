package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/23.
 */
public class HebaoCalAnciInfoPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoCalAnciInfoPage.class);

    public HebaoCalAnciInfoPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= null;
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        String param = StringBaseUtils.Map2GetParam(paraMap);
        url = url+"?"+param;
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");

        html = map.get("html").toString();
        return html;
    }
    /**************** 解析json 字符串
     * {
     "totalRecords": 1,
     "data": [
     {
     "discountRateBI": 31.1499,
     "origBusiType": "B",
     "averProfitRate": 5,
     "busiTypeCommBIUp": 0,
     "operSellExpensesAmountBI": 378.6482,
     "sellExpensesRateCIUp": 4,
     "discountRateCIUp": 30,
     "sellExpensesAmount": 131.4184,
     "discountRateBIAmount": 1223.6584,
     "operatePayRate": 0.3829,
     "actProCommRate": 50,
     "operateProfitRate": 67.3486,
     "operCommRateBIUp": null,
     "expProCommRateUp": 56.15,
     "profitRateBIUp": 31.15,
     "operateCommCI": 0,
     "standbyField1": "A险L险属于高赔付险种",
     "discountRateUpAmount": 2439.145,
     "sellExpensesCIUpAmount": 23.2332,
     "discountRateCIAmount": 369.17,
     "operSellExpensesRateBI": 14,
     "discountRateUp": 50,
     "sumPremium": 3285.46,
     "minNetSumPremium": 1865.3557,
     "breakEvenValue": 0.01,
     "operCommRate": 0,
     "operCommRateAmount": 0,
     "discountRateCI": 38.86,
     "busiRiskRate": 81.4679,
     "baseActBusiType": null,
     "anciIndiConfQueryVoList": [],
     "standPayRate": 0.2579,
     "busiTypeCommCIUp": 0,
     "busiBalanRate": 76.9879,
     "operSellExpensesRateCI": 4,
     "discountRateAmount": 1592.83,
     "operSellExpensesAmount": 401.8814,
     "sumPremiumCI": 580.83,
     "discountRate": 32.6514,
     "operSellExpensesRate": 12.2321,
     "sellExpensesBIUpAmount": 378.6482,
     "operateCommRateCI": 0,
     "strKindBusiTypeC": "",
     "businessCode": null,
     "strKindBusiTypeB": "",
     "strKindBusiTypeA": "050100 050500 050600 050701 050702 050231 050912 050921 050928 050929 ",
     "busiStandardBalanRate": 62.02,
     "discountRateBIUpAmount": 1223.6623,
     "strKindBusiTypeE": "050200 050911 ",
     "operCommRateCIUp": 4,
     "strKindBusiTypeD": "",
     "minNetSumPremiumBI": 1504.5429,
     "baseExpBusiType": null,
     "actBusiType": "A",
     "sellExpensesRate": 4,
     "sumPremiumBI": 2704.63,
     "operSellExpensesAmountCI": 23.2332,
     "operateCommRateBI": 0,
     "discountRateCIUpAmount": 285,
     "operateCommBI": 0,
     "actProCommRateUp": null,
     "averageRate": 10.78,
     "sellExpensesRateBIUp": 14,
     "minNetSumPremiumCI": 741.4997,
     "proCommRateBIUp": 46.15,
     "expBusiType": "A"
     }
     ]
     }* **************
     */
    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map nextParamsMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = new JSONArray();
                jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                JSONArray jsonArray2 = (JSONArray) map1.get("data");
                Map dataMap = (Map) jsonArray2.get(0);
                Set<String> key2 = dataMap.keySet();
                for (Iterator it2 = key2.iterator(); it2.hasNext();) {
                    String keyName2 = (String) it2.next();
                    String keyValue2 = dataMap.get(keyName2).toString();
                    String keyName3 = "prpAnciInfo."+keyName2;
                    nextParamsMap.put(keyName3,keyValue2);
                }
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保计算辅助核保失败】");
            }
            returnMap.put("nextParams",nextParamsMap);
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
        Response response = getResponse(html, request);
        //上个请求返回的参数继续传递下去
        Map returnMap =  response.getResponseMap();
        Map requestMap = request.getRequestParam();
        Map nextMap =(Map) requestMap.get("nextParams");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it2 = key.iterator(); it2.hasNext();) {
            String keyName = (String) it2.next();
            String keyValue = nextMap.get(keyName).toString();
            requestMap.put(keyName,keyValue);
        }
        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }
}
