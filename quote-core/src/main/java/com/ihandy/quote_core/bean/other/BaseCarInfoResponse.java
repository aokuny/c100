package com.ihandy.quote_core.bean.other;

/**
 * Created by fengwen on 2016/5/17.
 */
public class BaseCarInfoResponse {
    private String CarUsedType; //使用性质
    private String ForceExpireDate;//交强险到期日期
    private String BusinessExpireDate;//商业险到期日期
    private String CarVin;//车架号
    private String MoldName;//品牌型号
    private String EngineNo;//发动机号
    private String LicenseOwner;// 车主姓名
    private String CredentislasNum;//证件号码(车主)
    private String IdType;//证件类型
    private String InsuredName; //被保险人
    private String PostedName;//投保人
    private String LicenseNo;//车牌号
    private double PurchasePrice; //购买价格
    private String CarRegisterDate;//车辆注册日期
    private int CarSeated;//座位数量
    private int CityCode ; //城市编码
    private String ciPolicyNo;//上一年交强险投保单号
    private String biPolicyNo;//上一年商业险投保单号

    public String getCarUsedType() {
        return CarUsedType;
    }

    public void setCarUsedType(String carUsedType) {
        CarUsedType = carUsedType;
    }

    public String getForceExpireDate() {
        return ForceExpireDate;
    }

    public void setForceExpireDate(String forceExpireDate) {
        ForceExpireDate = forceExpireDate;
    }

    public String getBusinessExpireDate() {
        return BusinessExpireDate;
    }

    public void setBusinessExpireDate(String businessExpireDate) {
        BusinessExpireDate = businessExpireDate;
    }

    public String getCarVin() {
        return CarVin;
    }

    public void setCarVin(String carVin) {
        CarVin = carVin;
    }

    public String getMoldName() {
        return MoldName;
    }

    public void setMoldName(String moldName) {
        MoldName = moldName;
    }

    public String getEngineNo() {
        return EngineNo;
    }

    public void setEngineNo(String engineNo) {
        EngineNo = engineNo;
    }

    public String getLicenseOwner() {
        return LicenseOwner;
    }

    public void setLicenseOwner(String licenseOwner) {
        LicenseOwner = licenseOwner;
    }

    public String getCredentislasNum() {
        return CredentislasNum;
    }

    public void setCredentislasNum(String credentislasNum) {
        CredentislasNum = credentislasNum;
    }

    public String getIdType() {
        return IdType;
    }

    public void setIdType(String idType) {
        IdType = idType;
    }

    public String getInsuredName() {
        return InsuredName;
    }

    public void setInsuredName(String insuredName) {
        InsuredName = insuredName;
    }

    public String getPostedName() {
        return PostedName;
    }

    public void setPostedName(String postedName) {
        PostedName = postedName;
    }

    public String getLicenseNo() {
        return LicenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        LicenseNo = licenseNo;
    }

    public double getPurchasePrice() {
        return PurchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        PurchasePrice = purchasePrice;
    }

    public String getCarRegisterDate() {
        return CarRegisterDate;
    }

    public void setCarRegisterDate(String carRegisterDate) {
        CarRegisterDate = carRegisterDate;
    }

    public int getCarSeated() {
        return CarSeated;
    }

    public void setCarSeated(int carSeated) {
        CarSeated = carSeated;
    }

    public int getCityCode() {
        return CityCode;
    }

    public void setCityCode(int cityCode) {
        CityCode = cityCode;
    }

	public String getCiPolicyNo() {
		return ciPolicyNo;
	}

	public void setCiPolicyNo(String ciPolicyNo) {
		this.ciPolicyNo = ciPolicyNo;
	}

	public String getBiPolicyNo() {
		return biPolicyNo;
	}

	public void setBiPolicyNo(String biPolicyNo) {
		this.biPolicyNo = biPolicyNo;
	}
}
