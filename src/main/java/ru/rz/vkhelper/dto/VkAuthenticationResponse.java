package ru.rz.vkhelper.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@JsonSerialize
@Getter
@Setter
public class VkAuthenticationResponse {
    private String access_token;

    private Integer expires_in;

    private Integer user_id;
}
