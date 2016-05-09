package com.ihandy.quote_core.bean.other;

/**
 * Created by fengwen on 2016/4/29.
 */
public class RBRequestBrowsePolicyNo {
   //浏览保单号请求

 /*   var editType = document.getElementById("editType");
    var bizType = document.getElementById("bizType");
    var bizNo = document.getElementById("bizNo");
    var riskCode = document.getElementById("prpCmain.riskCode");
    var minusFlag = document.getElementById("minusFlag");
    var contractNo = document.getElementById("prpCmain.contractNo");
    var comCode = document.getElementById("prpCmain.comCode");
    var originQuery = document.getElementById("originQuery");
    var proposalNo = document.getElementById("prpCmain.proposalNo");
    if(proposalNo.value == null || proposalNo.value==''){
        proposalNo = document.getElementById("prpCmainCI.proposalNo");
    }
    var prarmeterURL = "?";
    prarmeterURL += 'editType=' + editType.value + '&bizType=' + bizType.value
    + '&bizNo=' + bizNo.value + '&riskCode=' + document.getElementById("riskCode").value
    + '&minusFlag=' + minusFlag.value + "&contractNo=" + contractNo.value
    +"&comCode="+comCode.value+"&originQuery="+originQuery.value+"&proposalNo="+proposalNo.value;*/
    private int hideEditType;//编辑类型
    private int hideBizType;//业务类型
    private String bizNo;//
    private String riskCode;//产品代码
    private String minusFlag;
    private String contractNo;
    private String comCode;//归属机构部门
    private String originQuery;
    private String proposalNo;

}
