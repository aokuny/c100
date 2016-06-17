import com.ihandy.quote_core.service.IAxatpService;
import com.ihandy.quote_core.service.IService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by fengwen on 2016/6/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-init.xml")
public class TestAxatp {
    private static Logger logger = LoggerFactory.getLogger(TestAxatp.class);
    //@Resource(name="AxatpServiceImpl")
    @Autowired
    private IAxatpService iAxatpService;
    @Test
    public void testLogin(){

    /*  iAxatpService.getQuoteInfoByCarInfo("京MM3767","FW151330","LBECFAHC5FZ226987","张文海","18812345678","11010819830123722X","OD","300000","20000","20000","N","N","N","NDNE","FEDPC");*/

        iAxatpService.createOrderByCarInfo("京MM3767","FW151330","LBECFAHC5FZ226987","张文海","18810253437","11010819830123722X","OD","300000","20000","20000","N","N","N","NDNE","FEDPC");

    }
}
