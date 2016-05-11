package com.ihandy.quote_core.bean.response;

import java.util.Map;

/**
 * Created by fengwen on 2016/5/9.
 */
public interface IRbResponseService {
    Map parseCarInfoByNodeId(int nodeId,Map map);
}
