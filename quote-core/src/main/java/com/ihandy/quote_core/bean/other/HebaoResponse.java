package com.ihandy.quote_core.bean.other;

/**
 * Created by fengwen on 2016/5/25.
 */
public class HebaoResponse {
    private int Source;
    private int SubmitStatus;
    private String SubmitResult;
    private String BizNo;
    private String ForceNo;
    private double BizRate;
    private double ForceRate;

    public int getSource() {
        return Source;
    }

    public void setSource(int source) {
        Source = source;
    }

    public double getForceRate() {
        return ForceRate;
    }

    public void setForceRate(double forceRate) {
        ForceRate = forceRate;
    }

    public double getBizRate() {
        return BizRate;
    }

    public void setBizRate(double bizRate) {
        BizRate = bizRate;
    }

    public String getForceNo() {
        return ForceNo;
    }

    public void setForceNo(String forceNo) {
        ForceNo = forceNo;
    }

    public String getBizNo() {
        return BizNo;
    }

    public void setBizNo(String bizNo) {
        BizNo = bizNo;
    }

    public int getSubmitStatus() {
        return SubmitStatus;
    }

    public void setSubmitStatus(int submitStatus) {
        SubmitStatus = submitStatus;
    }

    public String getSubmitResult() {
        return SubmitResult;
    }

    public void setSubmitResult(String submitResult) {
        SubmitResult = submitResult;
    }
}
