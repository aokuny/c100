package com.ihandy.quote_core.bean.response.impl;

import com.ihandy.quote_core.bean.response.IResponseService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengwen on 2016/5/9.
 */
@Service
public class ResponseServiceImpl implements IResponseService {

    public Map parseCarInfoByNodeId(int nodeId , Map returnMap) {
        // 1、返回信息中有下次页面请求需要的参数，返回nextParam map
        // 2、返回信息中有最终返回map中需要的结果, 返回response map
        Map map =new HashMap();
        Map requestMap =new HashMap();
        Map responseMap = new HashMap();
        map.put("requestParam",requestMap);
        map.put("responseResult",responseMap);
        return map;
    }
}
