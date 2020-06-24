package com.dili;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.dili.ss.dto.DTOScan;
import com.dili.ss.retrofitful.annotation.RestfulScan;

/**
 * 由MyBatis Generator工具自动生成
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.dili.ss", "com.dili.trading"})
@RestfulScan({"com.dili.trading.rpc","com.dili.uap.sdk.rpc"})
//@DTOScan(value = {"com.dili.ss"})
@EnableDiscoveryClient
@EnableFeignClients
public class Application extends SpringBootServletInitializer {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
