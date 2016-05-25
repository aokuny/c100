package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
        Map map = HttpsUtil.sendGet(url,super.piccSessionId,"UTF-8");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html) {
        return null;
    }

    @Override
    public Response run(Request request) {
        String html = doRequest(request);
        Response response = getResponse(html);
        return response;
    }
}
