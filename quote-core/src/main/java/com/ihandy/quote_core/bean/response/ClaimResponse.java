package com.ihandy.quote_core.bean.response;

import java.security.Timestamp;

/**
 * Created by fengwen on 2016/4/30.
 */
public class ClaimResponse extends BaseResponse {
    private String PayCompanyName;//保险公司
    private Double PayAmount;//出险金额
    private Timestamp EndCaseTime;//结案时间
    private Timestamp LossTime;//出险时间

}
