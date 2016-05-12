package com.ihandy.quote_core.utils;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/11.
 */
public interface IPage {
	
	/**
	 * 发送请求，返回html页面
	 * @param request
	 * @return
	 */
    public String doRequest(Request request);
    
    /**
     * 解析html页面，返回需要的数据
     * @param html
     * @return
     */
    public Response getResponse(String html);
    
    /**
     * 结合doRequest、getResponse完成整体HTTP发送到解析流程
     * @param request
     * @return
     */
    public Response run(Request request);

}
