package ru.rz.vkhelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = "ru.rz.vkhelper")
// @EnableAutoConfiguration
@EnableRetry
//@EnableTransactionManagement
public class VkHelperApplication {
    public static void main(String[] args){
        SpringApplication.run(VkHelperApplication.class, args);
    }
}
