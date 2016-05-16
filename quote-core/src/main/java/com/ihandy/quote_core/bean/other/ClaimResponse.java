package com.ihandy.quote_core.bean.other;

import java.security.Timestamp;

/**
 * Created by fengwen on 2016/4/30.
 */
public class ClaimResponse extends BaseResponse {
    private String PayCompanyName;//保险公司
    private Double PayAmount;//出险金额
    private Timestamp EndCaseTime;//结案时间
    private Timestamp LossTime;//出险时间

    public String getPayCompanyName() {
        return PayCompanyName;
    }

    public void setPayCompanyName(String payCompanyName) {
        PayCompanyName = payCompanyName;
    }

    public Double getPayAmount() {
        return PayAmount;
    }

    public void setPayAmount(Double payAmount) {
        PayAmount = payAmount;
    }

    public Timestamp getEndCaseTime() {
        return EndCaseTime;
    }

    public void setEndCaseTime(Timestamp endCaseTime) {
        EndCaseTime = endCaseTime;
    }

    public Timestamp getLossTime() {
        return LossTime;
    }

    public void setLossTime(Timestamp lossTime) {
        LossTime = lossTime;
    }
}
