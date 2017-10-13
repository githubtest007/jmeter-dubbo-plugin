package com.test.dubbo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.ApplicationConfig;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * Spring配置
 * 
 * @author wangpeihu
 * @date 2017/10/13 10:03
 *
 */
@Configuration
@ComponentScan
@Slf4j
public class SpringConfiguration {
    @Bean
    ApplicationConfig dubboTestFramework() {
        log.info("init SpringConfiguration:{}", Thread.currentThread().getName());
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubboTestFramework");
        applicationConfig.setId("dubboTestFramework");
        return applicationConfig;
    }
}
