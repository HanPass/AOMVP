package com.ao.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String configuredUrl = firstNonBlank(
                environment.getProperty("DB_URL"),
                environment.getProperty("DATABASE_URL")
        );

        if (configuredUrl == null || configuredUrl.startsWith("jdbc:")) {
            return;
        }

        URI uri = URI.create(configuredUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            return;
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port(uri) + path(uri);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", jdbcUrl);

        Credentials credentials = credentials(uri);
        if (credentials.username() != null && environment.getProperty("DB_USERNAME") == null) {
            properties.put("spring.datasource.username", credentials.username());
        }
        if (credentials.password() != null && environment.getProperty("DB_PASSWORD") == null) {
            properties.put("spring.datasource.password", credentials.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", properties));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private int port(URI uri) {
        return uri.getPort() == -1 ? 5432 : uri.getPort();
    }

    private String path(URI uri) {
        return uri.getPath() == null || uri.getPath().isBlank() ? "/postgres" : uri.getPath();
    }

    private Credentials credentials(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return new Credentials(null, null);
        }

        String[] parts = userInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : null;
        return new Credentials(username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record Credentials(String username, String password) {
    }
}
