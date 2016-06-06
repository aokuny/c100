package com.ihandy.quote_core.serverpage.axatp;

import com.ihandy.quote_core.bean.Request;
import com.ihandy.quote_core.bean.Response;
import com.ihandy.quote_core.utils.BasePage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by zhujiajia on 16/5/30.
 */

@Service
public class IndexPage extends BasePage {

    //报警间隔时间
    @Value("${insurance.flag.axatp}")
    private int INSURANCE_FLAG;

    public IndexPage(int INSURANCE_FLAG) {
        super(INSURANCE_FLAG);
    }

    @Override
    public String doRequest(Request request) {
        return null;
    }

    @Override
    public Response getResponse(String html) {
        return null;
    }

    @Override
    public Response run(Request request) {
        return null;
    }
}
