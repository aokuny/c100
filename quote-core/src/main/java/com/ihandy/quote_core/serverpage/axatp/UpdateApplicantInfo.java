package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by fengwen on 2016/6/15.
 */
public class UpdateApplicantInfo extends BasePage{
    private static Logger logger = Logger.getLogger(UpdateApplicantInfo.class);

    public UpdateApplicantInfo(int type) {
        super(type);
    }
    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paramMap = request.getRequestParam();
        String name = paramMap.get("personnelName").toString();
        try {
            name = java.net.URLEncoder.encode(name , "gbk");
        }catch (Exception e){}
        String birthday=paramMap.get("birthdayYY").toString()+"-"+paramMap.get("birthdayMM").toString()+"-"+paramMap.get("birthdayDD").toString();

        String postParam="ecInsureId="+paramMap.get("ecInsureId")+"&planDefineId="+paramMap.get("planDefineId")+"&planDefineId2="+paramMap.get("planDefineId2")+"&applyPolicyNo=&policyNo=&lastPolicyNo=&action=updateApplicantInfo&isRenewal="+paramMap.get("isRenewal")+"&selectPayChannel="+paramMap.get("selectPayChannel")+"&linkResource="+paramMap.get("linkResource")+"&isAgent="+paramMap.get("isAgent")+"&personnelName="+name+"&tempIds="+paramMap.get("tempIds")+"&certificateType=1&certificateNo="+paramMap.get("certificateNo")+"&mobileTelephone="+paramMap.get("mobileTelephone_")+"&email="+paramMap.get("email_")+"&sexCode="+paramMap.get("sexCode")+"&birthday="+birthday+"&insuredAddress="+paramMap.get("insuredAddress")+"&postCode=100000";

        Map map = HttpsUtil.sendPost(url,postParam,super.cookieValue,"gb2312");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            Map nextParamsMap = request.getRequestParam();
            response.setResponseMap(nextParamsMap);
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
        return response;
    }
}
