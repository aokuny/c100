package com.ihandy.quote_core.bean.request;

import java.util.Map;

/**
 * Created by fengwen on 2016/4/29.
 */
public class RBRequestSelectRenewal {
    //查看保单数据
    private Map<String, Object> Cookies;
    private String PlateNo;
    private String EngineNo;
    private String CarVin;
    private String ComCode;
    private int type; //只依车牌号取值设为0，车牌+车架号设为1
    private String BizNo;
    private String ForceNo;
    private String BizProposalNo;
    private int PageSize;
    private int PageNum;

    private int jumpToPage;
    private String ipolicyNo;
    private String licenseColorCode;
    private String licenseType;


}
