package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/13.
 */
public class XubaoBrowsePolicyPage extends BasePage {
	
    public XubaoBrowsePolicyPage(int type) {
		super(type);
	}

	@Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        url = url+"?"+ StringBaseUtils.Map2GetParam(paraMap);
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"GB2312");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map  returnMap  = new HashMap<>();
            Map mapNextParam = new HashMap<>();
            Document doc = Jsoup.parse(html);
            String comCode = doc.getElementById("comCode").attributes().get("value");
            String proposalNo = doc.getElementById("prpCmain.proposalNo").attributes().get("value");
            String riskCode = doc.getElementById("riskCode").attributes().get("value");
            String editType = doc.getElementById("editType").attributes().get("value");
            String bizType = doc.getElementById("bizType").attributes().get("value");
            String contractNo = doc.getElementById("prpCmain.contractNo").attributes().get("value");
           /* comCode = comCode.trim();
            proposalNo = proposalNo.trim();
            riskCode = riskCode.trim();
            editType = editType.trim();
            bizType = bizType.trim();
            contractNo = contractNo.trim();*/

            mapNextParam.put("contractNo", contractNo);
            mapNextParam.put("comCode",comCode);
            mapNextParam.put("proposalNo", proposalNo);
            mapNextParam.put("riskCode", riskCode);
            mapNextParam.put("editType",editType);
            mapNextParam.put("bizType", bizType);


            returnMap.put("nextParams",mapNextParam);
            returnMap.put("lastResult",null);
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
        Map map =(Map) response.getResponseMap().get("nextParams");
        map.put("bizNo",request.getRequestParam().get("bizNo")); //将保单号传到下一个请求页面参数中
        //  Map map1 =(Map) response.getResponseMap().get("nextParams");
        //  SysConfigInfo.SysXubaoParamsMap = map1;
        return response;
    }
}
