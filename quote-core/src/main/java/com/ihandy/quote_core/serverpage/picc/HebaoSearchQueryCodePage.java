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
import java.util.Map;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoSearchQueryCodePage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoSearchQueryCodePage.class);

    public HebaoSearchQueryCodePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        url = url+"?"+"pigeNo=1&pageSize=10";
        Map paraMap = request.getRequestParam();
        String param ="comCode=11010286&riskCode=DAA&prpCproposalVo.checkFlag=&prpCproposalVo.underWriteFlag=&prpCproposalVo.strStartDate=&prpCproposalVo.othFlag=&prpCproposalVo.checkUpCode=&prpCproposalVo.operatorCode1=&prpCproposalVo.businessNature=&noNcheckFlag=0&jfcdURL=http://10.134.136.48:8100/cbc&prpallURL=http://10.134.136.48:8000/prpall&bizNoZ=&pageNo_=1&pageSize_=10&scmIsOpen=1111100000&searchConditionSwitch=0&prpCproposalVo.proposalNo=&prpCproposalVo.policyNo=&prpCproposalVo.licenseNo=&prpCproposalVo.vinNo=&prpCproposalVo.insuredCode=&prpCproposalVo.insuredName=&prpCproposalVo.contractNo=&prpCproposalVo.operateDate=&prpCproposalVo.operateDate2=&prpCproposalVo.startDate=&prpCproposalVo.startDate2=&prpCproposalVo.dmFlag=all&prpCproposalVo.underWriteFlagC=&prpCproposalVo.brandName=&prpCproposalVo.engineNo=&prpCproposalVo.frameNo=&prpCproposalVo.riskCode=&prpCproposalVo.appliCode=&prpCproposalVo.apliName=&prpCproposalVo.makeCom=&makeComDes=&prpCproposalVo.operatorCode=&operatorCodeDes=&prpCproposalVo.comCode=&comCodeDes=&prpCproposalVo.handlerCode=&handlerCodeDes=&prpCproposalVo.handler1Code=&handler1CodeDes=&prpCproposalVo.endDate=&prpCproposalVo.endDate2=&prpCproposalVo.underWriteEndDate=&prpCproposalVo.underWriteEndDate2=";
        param.replace("prpCproposalVo.licenseNo","prpCproposalVo.licenseNo="+paraMap.get("licenseNo").toString());
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        //解析{"totalRecords":2,"data":[{"dmFlag":"0","policyNo":"                      ","contractNo":"                      ","specialflag":"初始值","underWriteEndDate":{"date":25,"day":3,"timezoneOffset":-480,"year":116,"month":4,"hours":0,"seconds":0,"minutes":0,"time":1464105600000,"nanos":0},"licenseNo":"京P55M11","checkStatus":"初始状态","operateDate":{"date":25,"day":3,"timezoneOffset":-480,"year":116,"month":4,"hours":0,"seconds":0,"minutes":0,"time":1464105600000,"nanos":0},"comCode":"11010286","checkFlag":"初始值","proposalNo":"TDAA201611010000955201","underWriteFlag":"见费出单待缴费","insuredName":"朱佳佳","operatorCode":"020083    ","startDate":{"date":26,"day":0,"timezoneOffset":-480,"year":116,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1466870400000,"nanos":0}},{"dmFlag":"0","policyNo":"                      ","contractNo":"                      ","specialflag":"初始值","underWriteEndDate":{"date":25,"day":3,"timezoneOffset":-480,"year":116,"month":4,"hours":0,"seconds":0,"minutes":0,"time":1464105600000,"nanos":0},"licenseNo":"京P55M11","checkStatus":"初始状态","operateDate":{"date":25,"day":3,"timezoneOffset":-480,"year":116,"month":4,"hours":0,"seconds":0,"minutes":0,"time":1464105600000,"nanos":0},"comCode":"11010286","checkFlag":"初始值","proposalNo":"TDZA201611010000977639","underWriteFlag":"见费出单待缴费","insuredName":"朱佳佳","operatorCode":"020083    ","startDate":{"date":26,"day":0,"timezoneOffset":-480,"year":116,"month":5,"hours":0,"seconds":0,"minutes":0,"time":1466870400000,"nanos":0}}],"startIndex":1,"recordsReturned":10}
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map lastResultMap = new HashMap<>();
            try{
                Map map = new HashMap<>();
                map = StringBaseUtils.parseJSON2Map(html);
                JSONArray jsonArray = JSONArray.fromObject(map);
                Map map1 = (Map) jsonArray.get(0);
                JSONArray jsonArrayData = (JSONArray) map1.get("data");
                for(int i=0;i<jsonArrayData.size();i++){
                    Map mapHebao = (Map)jsonArrayData.get(i);
                    String underWriteFlag =  mapHebao.get("underWriteFlag").toString();
                    String proposalNo = mapHebao.get("proposalNo").toString();
                    Map mapResult = new HashMap<>();

                    mapResult.put("underWriteFlag", underWriteFlag);
                    mapResult.put("proposalNo", proposalNo);
                    mapResult.put("policyNo", mapHebao.get("policyNo").toString());
                    lastResultMap.put(i, mapResult);

                }
                returnMap.put("lastResult",lastResultMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保查询失败】");
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
        Response response = getResponse(html);
        return response;
    }
}
