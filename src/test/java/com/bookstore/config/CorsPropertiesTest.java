package com.bookstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CorsPropertiesTest {

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void shouldLoadCorsPropertiesFromApplicationYml() {
        assertThat(corsProperties).isNotNull();
        assertThat(corsProperties.getAllowedOrigins())
                .containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:3000");
        assertThat(corsProperties.getAllowedMethods())
                .contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(corsProperties.getAllowedHeaders()).contains("*");
        assertThat(corsProperties.isAllowCredentials()).isTrue();
        assertThat(corsProperties.getMaxAge()).isEqualTo(3600L);
    }
}
