package com.ihandy.rbquote;

import com.ihandy.quote_core.bean.other.BaseCarInfoResponse;
import com.ihandy.quote_core.service.IService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by fengwen on 2016/5/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-config.xml")
public class TestCase {

    @Resource(name="RBServiceImpl")
    public IService iService;
    @Test
    public  void  testXuBaoIndex(){

    }
    @Test
    public  void  testGetXuBaoCarBaseInfo(){
        //测试index/search/browse/Citemcar/Cinsure
       BaseCarInfoResponse baseCarInfoResponse = iService.getBaseCarInfoByLicenseNo("京P55M11",01);
    }
}
