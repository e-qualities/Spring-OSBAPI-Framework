package com.equalities.cloud.osb.autoconfiguration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.equalities.cloud.osb.config.OsbApiConfig;

/**
 * Configuration used to expose {@link OsbApiConfig}
 * properties bean.
 */
@Configuration
@EnableConfigurationProperties(OsbApiConfig.class)
public class OsbApiConfigAutoConfiguration {
}
