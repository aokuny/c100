package com.ihandy.rbquote;

import com.ihandy.quote_core.service.IService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by fengwen on 2016/5/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-config.xml")
public class testQuoteVC   {
    @Resource(name="RBServiceImpl")
    private IService irbService;


    @Test
    public void testGetCarInfoByLicenseNo() {

        irbService.getCarInfoByLicenseNo("123",null);

    }

    @Test
    public void testGetAdmin() {

        irbService.getAdminTest("admin","admin");

    }

}
