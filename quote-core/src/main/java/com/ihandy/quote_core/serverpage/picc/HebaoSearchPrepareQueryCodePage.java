package com.ihandy.quote_core.serverpage.picc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;

import net.sf.json.JSONArray;

public class HebaoSearchPrepareQueryCodePage  extends BasePage{
    private static Logger logger = Logger.getLogger(HebaoSearchPrepareQueryCodePage.class);

    public HebaoSearchPrepareQueryCodePage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            html = html.replaceAll("\r|\n|\t", "");

            Document doc = Jsoup.parse(html);
	          /*



		       */
            //  String param ="prpCproposalVo.underWriteEndDate=&prpCproposalVo.underWriteEndDate2=";

            Map  returnMap  = new HashMap<>();
            Map nextParamMap = new LinkedHashMap<>();
            try{
                nextParamMap.put("comCode", doc.getElementById("comCode").attributes().get("value"));
                nextParamMap.put("riskCode", doc.getElementById("riskCode").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.checkFlag", doc.getElementById("prpCproposalVo.checkFlag").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.underWriteFlag", doc.getElementById("prpCproposalVo.underWriteFlag").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.strStartDate", doc.getElementById("prpCproposalVo.strStartDate").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.othFlag", doc.getElementById("prpCproposalVo.othFlag").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.checkUpCode", doc.getElementById("prpCproposalVo.checkUpCode").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.operatorCode1", doc.getElementById("prpCproposalVo.operatorCode1").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.businessNature", doc.getElementById("prpCproposalVo.businessNature").attributes().get("value"));
                nextParamMap.put("noNcheckFlag", doc.getElementById("noNcheckFlag").attributes().get("value"));
                nextParamMap.put("jfcdURL", doc.getElementById("jfcdURL").attributes().get("value"));
                nextParamMap.put("prpallURL", doc.getElementById("prpallURL").attributes().get("value"));
                nextParamMap.put("bizNoZ", doc.getElementById("bizNoZ").attributes().get("value"));
                nextParamMap.put("pageNo_", doc.getElementById("pageNo_").attributes().get("value"));
                nextParamMap.put("pageSize_", doc.getElementById("pageSize_").attributes().get("value"));
                nextParamMap.put("scmIsOpen", doc.getElementById("scmIsOpen").attributes().get("value"));
                nextParamMap.put("searchConditionSwitch", doc.getElementById("searchConditionSwitch").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.proposalNo", doc.getElementById("prpCproposalVo.proposalNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.policyNo", doc.getElementById("prpCproposalVo.policyNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.licenseNo", doc.getElementById("prpCproposalVo.licenseNo").attributes().get("value"));
                //key.equals("prpCproposalVo.licenseNo")
                nextParamMap.put("prpCproposalVo.vinNo", doc.getElementById("prpCproposalVo.vinNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.insuredCode", doc.getElementById("prpCproposalVo.insuredCode").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.insuredName", doc.getElementById("prpCproposalVo.insuredName").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.contractNo", doc.getElementById("prpCproposalVo.contractNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.operateDate", "");
                nextParamMap.put("prpCproposalVo.operateDate2", "");
                nextParamMap.put("prpCproposalVo.startDate", "");
                nextParamMap.put("prpCproposalVo.startDate2", "");
                nextParamMap.put("prpCproposalVo.dmFlag", "all");//全部
                nextParamMap.put("prpCproposalVo.underWriteFlagC", doc.getElementById("prpCproposalVo.underWriteFlagC").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.brandName", doc.getElementById("prpCproposalVo.brandName").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.engineNo", doc.getElementById("prpCproposalVo.engineNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.frameNo", doc.getElementById("prpCproposalVo.frameNo").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.riskCode", doc.getElementById("prpCproposalVo.riskCode").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.appliCode", doc.getElementById("prpCproposalVo.appliCode").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.apliName", doc.getElementById("prpCproposalVo.apliName").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.makeCom", doc.getElementById("prpCproposalVo.makeCom").attributes().get("value"));
                nextParamMap.put("makeComDes", doc.getElementById("makeComDes").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.operatorCode", doc.getElementById("prpCproposalVo.operatorCode").attributes().get("value"));
                nextParamMap.put("operatorCodeDes", doc.getElementById("operatorCodeDes").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.comCode", doc.getElementById("prpCproposalVo.comCode").attributes().get("value"));
                nextParamMap.put("comCodeDes", doc.getElementById("comCodeDes").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.handlerCode", doc.getElementById("prpCproposalVo.handlerCode").attributes().get("value"));
                nextParamMap.put("handlerCodeDes", doc.getElementById("handlerCodeDes").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.handler1Code", doc.getElementById("prpCproposalVo.handler1Code").attributes().get("value"));
                nextParamMap.put("handler1CodeDes", doc.getElementById("handler1CodeDes").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.endDate", doc.getElementById("prpCproposalVo.endDate").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.endDate2", doc.getElementById("prpCproposalVo.endDate2").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.underWriteEndDate", doc.getElementById("prpCproposalVo.underWriteEndDate").attributes().get("value"));
                nextParamMap.put("prpCproposalVo.underWriteEndDate2", doc.getElementById("prpCproposalVo.underWriteEndDate2").attributes().get("value"));




                returnMap.put("nextParams",nextParamMap);
                response.setResponseMap(returnMap);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
            }
            catch (Exception e){
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
        return response;
    }
}
