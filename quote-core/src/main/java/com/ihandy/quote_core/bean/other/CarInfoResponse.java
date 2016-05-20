package com.ihandy.quote_core.bean.other;


import java.util.List;

/**
 * Created by fengwen on 2016/4/29.
 */
public class CarInfoResponse{

    private BaseCarInfoResponse carInfoBaseResponse;//车辆基本信息

    private List<RelaPeopleResponse> relaPeopleResponseList;//相关人员

    private SaveQuoteResponse saveQuoteResponse;//报价bean

    private List<ClaimResponse> claimResponseList;//出险信息


    public BaseCarInfoResponse getCarInfoBaseResponse() {
        return carInfoBaseResponse;
    }

    public void setCarInfoBaseResponse(BaseCarInfoResponse carInfoBaseResponse) {
        this.carInfoBaseResponse = carInfoBaseResponse;
    }

    public List<RelaPeopleResponse> getRelaPeopleResponseList() {
        return relaPeopleResponseList;
    }

    public void setRelaPeopleResponseList(List<RelaPeopleResponse> relaPeopleResponseList) {
        this.relaPeopleResponseList = relaPeopleResponseList;
    }

    public SaveQuoteResponse getSaveQuoteResponse() {
        return saveQuoteResponse;
    }

    public void setSaveQuoteResponse(SaveQuoteResponse saveQuoteResponse) {
        this.saveQuoteResponse = saveQuoteResponse;
    }

    public List<ClaimResponse> getClaimResponseList() {
        return claimResponseList;
    }

    public void setClaimResponseList(List<ClaimResponse> claimResponseList) {
        this.claimResponseList = claimResponseList;
    }
}
