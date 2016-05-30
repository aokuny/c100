package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSaveQueryPayForPage  extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSaveQueryPayForPage.class);

    public HebaoSaveQueryPayForPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        //agreementNo=&riskCode=DAA&comCode=11010286&chgCostRate=1
        String param ="agreementNo="+paraMap.get("prpCmain.businessNature").toString() +
                "&chgCostRate=1" +
                "&comCode="+paraMap.get("comCode").toString()+
                "&riskCode="+paraMap.get("prpCmain.riskCode").toString();
        url = url+"?"+param;
        String param1 = StringBaseUtils.Map2GetParam(paraMap);
        Map map = HttpsUtil.sendPost(url,param1,super.piccSessionId,"UTF-8");
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
                Map dataMap = (Map) data.get(0);
                //1）组装prpDdismantleDetails
                JSONArray jsonArrayPrpDdismantleDetails =JSONArray.fromObject(dataMap.get("prpDdismantleDetails"));
                for(int i=0;i<jsonArrayPrpDdismantleDetails.size();i++){
                    Map mapPrpDdismantleDetails = (Map)jsonArrayPrpDdismantleDetails.get(i);

                    JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpDdismantleDetails.get("id"));
                    Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.agreementNo", mapId.get("agreementNo"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.roleCode", mapId.get("roleCode"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.configCode",mapId.get("configCode"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].id.assignType",mapId.get("assignType"));

                    nextParamsMap.put("prpDdismantleDetails["+i+"].flag",mapPrpDdismantleDetails.get("flag"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].costRate",mapPrpDdismantleDetails.get("costRate"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].businessNature",mapPrpDdismantleDetails.get("businessNature"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleFlag",mapPrpDdismantleDetails.get("roleFlag"));
                    nextParamsMap.put("prpDdismantleDetails["+i+"].roleName",mapPrpDdismantleDetails.get("roleName"));
                    if(mapPrpDdismantleDetails.get("flag").toString().equals("DZA")){
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].flag",mapPrpDdismantleDetails.get("flag"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].costRate",mapPrpDdismantleDetails.get("costRate"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].businessNature",mapPrpDdismantleDetails.get("businessNature"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].roleCode_uni",mapPrpDdismantleDetails.get("roleCode_uni"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].roleFlag",mapPrpDdismantleDetails.get("roleFlag"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].roleName",mapPrpDdismantleDetails.get("roleName"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].id.agreementNo", mapId.get("agreementNo"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].id.roleCode", mapId.get("roleCode"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].id.configCode",mapId.get("configCode"));
                        nextParamsMap.put("prpDdismantleDetails_["+i+"].id.assignType",mapId.get("assignType"));
                    }
                }
                //2）组装prpCsaless(6)
                JSONArray jsonArrayPrpCsaless =JSONArray.fromObject(dataMap.get("prpCsaless"));
                for(int i=0;i<jsonArrayPrpCsaless.size();i++){
                    Map mapPrpCsaless = (Map)jsonArrayPrpCsaless.get(i);
                    JSONArray jsonArrayPrpDdismantleDetailsId =JSONArray.fromObject(mapPrpCsaless.get("id"));
                    Map mapId = (Map)jsonArrayPrpDdismantleDetailsId.get(0);
                    nextParamsMap.put("prpCsaless["+i+"].id.proposalNo", mapId.get("proposalNo"));
                    nextParamsMap.put("prpCsaless["+i+"].id.salesCode", mapId.get("salesCode"));
                    nextParamsMap.put("prpCsaless["+i+"].id.salesDetailCode",mapId.get("salesDetailCode"));

                    nextParamsMap.put("prpCsaless["+i+"].agreementNo",mapPrpCsaless.get("agreementNo"));
                    nextParamsMap.put("prpCsaless["+i+"].flag",mapPrpCsaless.get("flag"));
                    nextParamsMap.put("prpCsaless["+i+"].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber"));
                    nextParamsMap.put("prpCsaless["+i+"].remark",mapPrpCsaless.get("remark"));
                    nextParamsMap.put("prpCsaless["+i+"].riskCode",mapPrpCsaless.get("riskCode"));
                    nextParamsMap.put("prpCsaless["+i+"].salesDetailName",mapPrpCsaless.get("salesDetailName"));
                    nextParamsMap.put("prpCsaless["+i+"].salesName",mapPrpCsaless.get("salesName"));
                    nextParamsMap.put("prpCsaless["+i+"].splitFee",mapPrpCsaless.get("splitFee"));
                    nextParamsMap.put("prpCsaless["+i+"].splitRate",mapPrpCsaless.get("splitRate"));
                    nextParamsMap.put("prpCsaless["+i+"].splitWay",mapPrpCsaless.get("splitWay"));
                    nextParamsMap.put("prpCsaless["+i+"].totalRate",mapPrpCsaless.get("totalRate"));
                    nextParamsMap.put("prpCsaless["+i+"].totalRateMax",mapPrpCsaless.get("totalRateMax"));
                    if(i==0){
                        nextParamsMap.put("prpCsaless_["+i+"].agreementNo",mapPrpCsaless.get("agreementNo"));
                        nextParamsMap.put("prpCsaless_["+i+"].flag",mapPrpCsaless.get("flag"));
                        nextParamsMap.put("prpCsaless_["+i+"].oriSplitNumber",mapPrpCsaless.get("oriSplitNumber"));
                        nextParamsMap.put("prpCsaless_["+i+"].remark",mapPrpCsaless.get("remark"));
                        nextParamsMap.put("prpCsaless_["+i+"].riskCode",mapPrpCsaless.get("riskCode"));
                        nextParamsMap.put("prpCsaless_["+i+"].salesDetailName",mapPrpCsaless.get("salesDetailName"));
                        nextParamsMap.put("prpCsaless_["+i+"].salesName",mapPrpCsaless.get("salesName"));
                        nextParamsMap.put("prpCsaless_["+i+"].splitFee",mapPrpCsaless.get("splitFee"));
                        nextParamsMap.put("prpCsaless_["+i+"].splitRate",mapPrpCsaless.get("splitRate"));
                        nextParamsMap.put("prpCsaless_["+i+"].splitWay",mapPrpCsaless.get("splitWay"));
                        nextParamsMap.put("prpCsaless_["+i+"].totalRate",mapPrpCsaless.get("totalRate"));
                        nextParamsMap.put("prpCsaless_["+i+"].totalRateMax",mapPrpCsaless.get("totalRateMax"));
                        nextParamsMap.put("prpCsaless_["+i+"].id.proposalNo", mapId.get("proposalNo"));
                        nextParamsMap.put("prpCsaless_["+i+"].id.salesCode", mapId.get("salesCode"));
                        nextParamsMap.put("prpCsaless_["+i+"].id.salesDetailCode",mapId.get("salesDetailCode"));
                    }
                }
                //3)
                nextParamsMap.put("maxRateScmCi",dataMap.get("maxRateScmCi"));
                nextParamsMap.put("maxRateScm",dataMap.get("maxRateScm"));
                nextParamsMap.put("levelMaxRateCi",dataMap.get("levelMaxRateCi"));
                nextParamsMap.put("levelMaxRate",dataMap.get("levelMaxRate"));
                //4)组装prpDpayForPolicies // TODO: 2016/5/25

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
