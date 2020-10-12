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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dili.ss.retrofitful.annotation.RestfulScan;
import com.dili.trading.config.PutMchIdHeaderInterceptor;

/**
 * 由MyBatis Generator工具自动生成
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.dili.ss", "com.dili.trading", "com.dili.orders.config", "com.dili.uap.sdk", "com.dili.logger.sdk" })
@RestfulScan({ "com.dili.trading.rpc", "com.dili.uap.sdk.rpc", "com.dili.bpmc.sdk.rpc" })
//@DTOScan(value = {"com.dili.ss"})
@EnableDiscoveryClient
@EnableFeignClients
public class Application extends SpringBootServletInitializer implements WebMvcConfigurer {

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new PutMchIdHeaderInterceptor());
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
