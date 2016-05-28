package com.ihandy.rbquote;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.bean.other.BaseCarInfoResponse;
import com.ihandy.quote_core.serverpage.picc.HebaoCalAnciInfoPage;
import com.ihandy.quote_core.service.IService;
import com.ihandy.quote_core.utils.SysConfigInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import javax.annotation.Resource;

/**
 * Created by fengwen on 2016/5/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-init.xml")
public class TestCase {

    @Resource(name="RBServiceImpl")
    public IService iService;
    @Test
    public  void  testXuBaoIndex(){

    }
    @Test
    public  void  testGetXuBaoCarBaseInfo(){
        //测试index/search/browse/Citemcar/Cinsure
      // BaseCarInfoResponse baseCarInfoResponse = iService.getBaseCarInfoByLicenseNo("京P55M11",01);
    }
    @Test
    public  void  testHebao(){
    	Response response = new Response();
		//HebaoCalAnciInfoPage hebaoCalAnciInfoPage = new HebaoCalAnciInfoPage(1);
		//Request request = new Request();
		//Map preMap =(Map)resp.getResponseMap().get("nextParams");
		//request.setRequestParam(preMap);//
		//request.setUrl(SysConfigInfo.PICC_DOMIAN + SysConfigInfo.PICC_CALANCIINFO);// GET
		//Response responseHebaoCalAnciInfo = hebaoCalAnciInfoPage.run(request);
		
	   String code = 	iService.commitHeBaoInfo(response);
	   System.out.println("code = "+code);
    }
}
