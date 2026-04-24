package com.babysteps.dto;

public record AuthStatusResponse(boolean authenticated, String username) {
}
