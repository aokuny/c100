package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveRefreshPlanByTimesPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveRefreshPlanByTimesPage.class);

    public HebaoSaveRefreshPlanByTimesPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        String param = StringBaseUtils.Map2GetParam(paraMap);
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

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
                JSONArray data = (JSONArray) map1.get("data");
                //1）组装 prpCplanTemps
                for(int i=0;i<data.size();i++){
                    Map mapPrpCplanTemps = (Map)data.get(i);
                    nextParamsMap.put("prpCplanTemps["+i+"].currency",mapPrpCplanTemps.get("currency"));
                    nextParamsMap.put("prpCplanTemps["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
                    nextParamsMap.put("prpCplanTemps["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
                    nextParamsMap.put("prpCplanTemps["+i+"].flag",mapPrpCplanTemps.get("flag"));
                    nextParamsMap.put("prpCplanTemps["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
                    nextParamsMap.put("prpCplanTemps["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                    nextParamsMap.put("prpCplanTemps["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
                    nextParamsMap.put("prpCplanTemps["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
                    nextParamsMap.put("prpCplanTemps["+i+"].planDate",mapPrpCplanTemps.get("planDate").toString());
                    nextParamsMap.put("prpCplanTemps["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
                    nextParamsMap.put("prpCplanTemps["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
                    nextParamsMap.put("prpCplanTemps["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));
                    nextParamsMap.put("prpCplanTemps["+i+"].taxPremium", mapPrpCplanTemps.get("taxPremium"));
                    if(mapPrpCplanTemps.get("isBICI").toString().equals("BI")){
                        nextParamsMap.put("prpCplanTemps_["+i+"].currency",mapPrpCplanTemps.get("currency"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].delinquentFee",mapPrpCplanTemps.get("delinquentFee"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].endorseNo",mapPrpCplanTemps.get("endorseNo"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].flag",mapPrpCplanTemps.get("flag"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].isBICI",mapPrpCplanTemps.get("isBICI"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].netPremium",mapPrpCplanTemps.get("netPremium"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].payNo", mapPrpCplanTemps.get("payNo"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].payReason",mapPrpCplanTemps.get("payReason"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].planDate",mapPrpCplanTemps.get("planDate").toString());
                        nextParamsMap.put("prpCplanTemps_["+i+"].planFee", mapPrpCplanTemps.get("planFee"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].serialNo",mapPrpCplanTemps.get("serialNo"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].subsidyRate",mapPrpCplanTemps.get("subsidyRate"));
                        nextParamsMap.put("prpCplanTemps_["+i+"].taxPremium", mapPrpCplanTemps.get("taxPremium"));
                    }
                }


                returnMap.put("nextParams",nextParamsMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保保存3失败】");
                response.setResponseMap(null);
                response.setReturnCode(SysConfigInfo.ERROR404);
                response.setErrMsg(SysConfigInfo.ERROR404MSG);
            }
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
        Map requestMap = request.getRequestParam();
        Map returnMap =  response.getResponseMap();
        Map nextMap =(Map) requestMap.get("nextParams");
        Set<String> key = nextMap.keySet();//将nextParams遍历写入上个请求的参数Map中
        for (Iterator it = key.iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            String keyValue = nextMap.get(keyName).toString();
            requestMap.put(keyName,keyValue);
        }
        returnMap.put("nextParams",requestMap);
        response.setResponseMap(returnMap);
        return response;
    }
}
