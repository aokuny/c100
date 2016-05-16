package com.ihandy.quote_core.bean.other;

import java.security.Timestamp;

/**
 * Created by fengwen on 2016/4/29.
 */
public class CarInfoResponse extends  BaseResponse{

    private String CarUsedType; //使用性质
   
    private Timestamp ForceEndDate;//交强险到期日期

    private Timestamp BizEndDate;//商业险到期日期
   
    private String CarVin;//车架号
   
    private String MoldName;//品牌型号
  
    private String EngineNo;//发动机号
    
    private String LicenseOwner;// 车主姓名

    private String LicenseOwnerIdCardNo;//证件号码
   
    private int LicenseOwnerIdCardType;//证件类型
    
    private String InsuredName; //被保险人

    private String PostedName;//投保人
   
    private String LicenseNo;//车牌号

    private double PurchasePrice; //购买价格

    private Timestamp CarRegisterDate;//车辆注册日期
  
    private int CarSeated;//座位数量

    private int CityCode ; //城市编码
    
    private int CompanyId; //sourceId公司

    private RelaPeopleResponse relaPeopleResponse;//相关人员

    private QuoteResponse quoteResponse;//报价bean

    private ClaimResponse claimResponse;//出险bean


}
