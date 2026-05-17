package org.resourceserver.common.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties( prefix = "app.jwt")
public record JwtProperties (
    String accessSecret,
    String refreshSecret,
    long accessExpiration,
    long refreshExpiration
){ }
