package com.ihandy.quote_core.service.impl.picc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平安报价线程
 * 
 * @author liming
 *
 */
public class QuoteThreadPingan extends Thread {
	
	private Map<String, String> quoteMap = new HashMap<>();//报价参数
	
	private static Logger logger = LoggerFactory.getLogger(QuoteThreadPingan.class);
	
	public QuoteThreadPingan() {
	}

	public QuoteThreadPingan(String name, Map<String, String> quoteMap) {
		super(name);
		this.quoteMap = quoteMap;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10000);
			logger.info("平安  API接口，【报价开始】");
			logger.info("------------------------------------------------：" + quoteMap.get("LicenseNo"));
			logger.info("平安  API接口，【报价结束】");
		} catch (Exception e) {
			
		}
	}
}
