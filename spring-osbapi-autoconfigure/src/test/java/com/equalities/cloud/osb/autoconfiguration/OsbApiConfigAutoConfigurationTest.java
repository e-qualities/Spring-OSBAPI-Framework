package com.equalities.cloud.osb.autoconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.equalities.cloud.osb.autoconfiguration.OsbApiConfigAutoConfiguration;
import com.equalities.cloud.osb.config.OsbApiConfig;

@ExtendWith(MockitoExtension.class)
class OsbApiConfigAutoConfigurationTest {

  //create an ApplicationContextRunner that will 
  // create a context with the configuration under test.
  ApplicationContextRunner contextRunner;
  
  @BeforeEach
  void setUp() {
    contextRunner = new ApplicationContextRunner()
                          .withConfiguration(AutoConfigurations.of(OsbApiConfigAutoConfiguration.class));
  }
  
  @Test
  @DisplayName("exposes all necessary beans.")
  void testAllBeansExposed() {
    contextRunner
    .run((context) -> {
      assertThat(context).getBean(OsbApiConfig.class).isNotNull();
   });
  }
}
