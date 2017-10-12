package com.test.dubbo;

import lombok.Getter;
import lombok.Setter;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.LoggerFactory;


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
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DubboTestInvoker.class);
    private String parameter;
    private String url;
    private String service;
    private String method;
    private String port;


    public SampleResult runTest(JavaSamplerContext paramContext) {

        log.info("paramContext:{}", paramContext);

        // check parameter

        // convert parameter

        // invoke target

        // build response
        SampleResult result = new SampleResult();
        result.setResponseData("", "UTF-8");
        result.setSuccessful(true);
        log.info("invoke over. ");
        return result;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments param = new Arguments();
        param.addArgument("parameter", this.parameter);
        param.addArgument("url", this.url);
        param.addArgument("service", this.service);
        param.addArgument("method", this.method);
        param.addArgument("port", this.port);
        return param;
    }

}
