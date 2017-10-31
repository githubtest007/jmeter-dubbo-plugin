package com.test.dubbo;

import java.util.List;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * Jmeter dubbo test plugin
 *
 * @author wangpeihu
 * @date 2017/10/12 16:43
 */
@Getter
@Setter
public class DubboTestInvoker extends AbstractJavaSamplerClient {
    private static final Logger                log     = LoggerFactory
        .getLogger(DubboTestInvoker.class);
    private String                             parameter;
    private String                             ip;
    private String                             service;
    private String                             method;
    private String                             port;

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        SpringConfiguration.class);

    @Override
    public SampleResult runTest(JavaSamplerContext param) {
        SampleResult results = new SampleResult();
        log.info("==========================================================");
        log.info("param:{}", ToStringBuilder.reflectionToString(param, ToStringStyle.JSON_STYLE));
        log.info("==========================================================");

        // check parameter
        prepareParameter(param);
        // convert parameter

        try {

            results.sampleStart();

            // invoke target
            DubboClient client = context.getBean("dubboClient", DubboClient.class);
            List<String> params = ParameterUtil.getRequestBodyList(parameter);

            String url = "dubbo://" + ip + ":" + port;
            ApiResponse invoke = client.invoke(url, service, method, params);


            results.setResponseData(JSON.toJSONString(invoke.getOriginalResponse()), "UTF-8");
            results.setSuccessful(true);
            results.setResponseMessage(invoke.getRespText());
        } catch (Exception e) {
            results.setSuccessful(false);
            results.setResponseMessage(e.toString());
        } finally {
            results.sampleEnd();
        }
        log.info("invoke over. ");
        return results;
    }

    private void prepareParameter(JavaSamplerContext param) {
        this.parameter = param.getParameter("parameter");
        this.ip = param.getParameter("ip");
        this.service = param.getParameter("service");
        this.method = param.getParameter("method");
        this.port = param.getParameter("port");

    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments param = new Arguments();
        param.addArgument("parameter", this.parameter);
        param.addArgument("ip", this.ip);
        param.addArgument("service", this.service);
        param.addArgument("method", this.method);
        param.addArgument("port", this.port);
        return param;
    }

}
