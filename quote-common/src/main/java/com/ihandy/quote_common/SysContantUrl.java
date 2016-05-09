package com.ihandy.quote_common;

/**
 * Created by fengwen on 2016/4/29.
 */
public class SysContantUrl {

    /** 首页面 参数：ticket=ST-43945-DXIbKb2cu0FME6dmle7Q-cas*/
    public static String index = "/portal/index.jsp";

    /** 承保页面  参数：calogin&ticket=ST-44088-NMmm4dWQe0bb1mwT2Vha-cas*/
    public static String calogin = "/prpall/index.jsp";

    /**车商渠道专用出单页面 参数：bizType=PROPOSAL&editType=NEW&is4S=Y*/
    public static String quickProposal = "/prpall/business/quickProposal";

    /**续保对话框*/
    public static String editRenewalSearch = "prpall/business/editRenewalSearch";

    /**查看保单数据源  参数："pageSize="+pageSize+"&pageNo="+pageNo*/
    public static String selectRenewal = "/prpall/business/selectRenewal" ;

    /**浏览保单 参数： bizNo=PDAT20151102T000182528*/
    public static String browsePolicyNo = "/prpall/business/browsePolicyNo";

    /**车辆信息 RbQuestBrowsePolicyNo*/
    public static String carTab = "/business/showCitemCar";

    /**关系人信息 RbQuestBrowsePolicyNo*/
    public static String insuredTab = "/business/showCinsured";

    /** 保险责任 RbQuestBrowsePolicyNo*/
    public static String kindTab = "'/business/showCitemKind";

    /**查询理赔信息 参数：bizNo=PDAT20151102T000182528&bizType=POLICY*/
    public String queryClaimsMsg ="/prpall/business/queryClaimsMsg";

}
