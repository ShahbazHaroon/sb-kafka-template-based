package com.ubaidsample.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
