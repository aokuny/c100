package com.ihandy.quote_core.serverpage.picc;

import com.ihandy.quote_common.httpUtil.HttpsUtil;
import com.ihandy.quote_common.httpUtil.StringBaseUtils;
import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import com.ihandy.quote_core.utils.SysConfigInfo;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/11.
 */
public class XubaoAPage extends BasePage {
	private static Logger logger = Logger.getLogger(XubaoAPage.class);
	@Override
	public String doRequest(Request request) {
		 String html= null;
		 String url = request.getUrl();
		 Map paraMap = request.getRequestParam();
		 url = url+ StringBaseUtils.Map2GetParam(paraMap);
	     Map map = HttpsUtil.sendGet(url,super.piccSessionId);
         html = map.get("html").toString();
		 return html;
	}

	@Override
	public Response getResponse(String html) {
		Response response = new Response();
	    if(null!=html){
			response.setResponseMap(null);
			response.setErrCode(SysConfigInfo.SUCCESS200);
			response.setErrMsg(SysConfigInfo.SUCCESS200MSG);
		}else{
			response.setResponseMap(null);
			response.setErrCode(SysConfigInfo.ERROR404);
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
