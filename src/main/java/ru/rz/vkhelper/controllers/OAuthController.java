package ru.rz.vkhelper.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/login")
@Slf4j
public class OAuthController {

    @Value("${vk.app_id}")
    Integer appId;

    @Value("${vk.redirect_url}")
    String redirectUrl;

    @GetMapping("/auth")
    public void auth(HttpServletResponse httpServletResponse) {
        log.info("Authorization invoked");


        String vkoauthurl = String.format(
                "https://oauth.vk.com/authorize?client_id=%d&redirect_uri=%s&display=page&response_type=code&v=5.130&scope=%d",
                appId,
                redirectUrl,
                262144 + 128 + 65536
        );

        httpServletResponse.setHeader("Location", vkoauthurl);
        httpServletResponse.setStatus(302);
    }
}
