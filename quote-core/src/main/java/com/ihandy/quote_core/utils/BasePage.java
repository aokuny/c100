package com.ihandy.quote_core.utils;

/**
 * Created by fengwen on 2016/5/11.
 */
public abstract  class BasePage  implements  IPage{
	
    public String piccSessionId;//picc登录session
    
    public BasePage(){
    	initPiccLogin();//初始化picc登录session
    }
    
    /**
     * 初始化picc登录session
     */
    public void initPiccLogin(){
    	
    }


}
