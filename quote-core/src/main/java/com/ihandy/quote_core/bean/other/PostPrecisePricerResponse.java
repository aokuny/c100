package com.ihandy.quote_core.bean.other;
/**
 * 险种信息上传返回结果
 * @author liming
 *
 */
public class PostPrecisePricerResponse {
	/**
	 * 	1:请求成功 
	 * 	-10000：输入的参数是否有空或者长度不符合要求；？
	 * 	-10001：校验参数错误；？
	 * 	-10002:请求报价信息失败；？
	 */
	
	private String BusinessStatus;
	/**
	 * 错误信息描述
	 */
	private String StatusMessage;
	
	public String getBusinessStatus() {
		return BusinessStatus;
	}
	
	public void setBusinessStatus(String businessStatus) {
		BusinessStatus = businessStatus;
	}
	
	public String getStatusMessage() {
		return StatusMessage;
	}
	
	public void setStatusMessage(String statusMessage) {
		StatusMessage = statusMessage;
	}
}
