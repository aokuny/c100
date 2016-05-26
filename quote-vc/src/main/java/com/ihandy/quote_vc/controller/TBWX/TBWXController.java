package com.ihandy.quote_vc.controller.TBWX;

import com.ihandy.qoute_common.springutils.SpringMVCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhujiajia on 16/5/26.
 */
@Controller
@RequestMapping("/TPWX")
public class TBWXController {
    private static Logger logger = LoggerFactory.getLogger(TBWXController.class);

    @RequestMapping("/loginTest")
    @ResponseBody
    public void loginTest(HttpServletRequest request, HttpServletResponse response,String login){

        SpringMVCUtils.renderJson(response, "user:"+login);

    }
}
