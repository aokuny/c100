package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/24.
 */
public class HebaoCommitEditSubmitUndwrtPage extends BasePage {

    private static Logger logger = Logger.getLogger(HebaoCommitEditSubmitUndwrtPage.class);

    public HebaoCommitEditSubmitUndwrtPage(int type) {
        super(type);
    }

    @Override
    public String doRequest(Request request) {
        String html= "";
        String url = request.getUrl();
        Map paraMap = request.getRequestParam();
        String param ="";
        try{
         	String TDAA = String.valueOf(paraMap.get("TDAA"));
        	String TDZA = String.valueOf(paraMap.get("TDZA"));
        	if(StringUtils.isNotBlank(TDAA) && !"null".equals(TDAA)){
        		param ="bizNo=" +TDAA;
        	}else{
        		param ="bizNo=" +TDZA;
        	}
        }catch(Exception e) {
            logger.info("抓取机器人，【 PICC 核保提交2获取post参数失败】");
        }
        Map map = HttpsUtil.sendPost(url,param,super.piccSessionId,"");
        html = map.get("html").toString();
        return html;
    }

    @Override
    public Response getResponse(String html, Request request) {
        Response response = new Response();
        if(!html.equals("")||null!=html){
            try {
                response.setResponseMap(null);
                response.setReturnCode(SysConfigInfo.SUCCESS200);
                response.setErrMsg(SysConfigInfo.SUCCESS200MSG);

            }
            catch (Exception e){
                logger.info("抓取机器人，【 PICC 核保提交1失败】");
                response.setResponseMap(null);
                response.setReturnCode(SysConfigInfo.ERROR404);
                response.setErrMsg(SysConfigInfo.ERROR404MSG);
            }
        }else{
            response.setResponseMap(null);
            response.setReturnCode(SysConfigInfo.SUCCESS200);
            response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
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
