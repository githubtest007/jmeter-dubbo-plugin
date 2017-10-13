import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.test.dubbo.ApiResponse;
import com.test.dubbo.DubboClient;
import com.test.dubbo.ParameterUtil;
import com.test.dubbo.SpringConfiguration;

/**
 *
 * @author wangpeihu
 * @date 2017/10/13 10:30
 *
 */

public class MainTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
            SpringConfiguration.class);
        DubboClient client = applicationContext.getBean("dubboClient", DubboClient.class);
        String ip = "10.148.181.147";
        String parameter = "{'biz': '123'}";
        String service = "com.hpay.risk.limit.api.LimitCheckServiceApi";
        String method = "check";
        int port = 50131;

        String url = "dubbo://" + ip + ":" + port;

        ApiResponse response = client.invoke(url, service, method,
            ParameterUtil.getRequestBodyList(parameter));

        System.out.println("client = " + response);

    }
}
