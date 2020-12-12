package com.equalities.cloud.osb.persistence.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;
import com.equalities.cloud.osb.persistence.jpa.entities.StateConverter;

class StateConverterTest {

  StateConverter instance;
  
  @BeforeEach
  void setUp() {
    instance = new StateConverter();
  }

  @ParameterizedTest
  @EnumSource
  void testConvertToDatabaseColumn(State state) {
    assertThat(instance.convertToDatabaseColumn(state)).isEqualTo(state.name());
  }
  
  @Test
  void testConvertToDatabaseColumnThrowsOnNullInput() {
    assertThatThrownBy(() -> {
      instance.convertToDatabaseColumn(null);
    }).isInstanceOf(IllegalArgumentException.class).hasMessage("Error! State enumeration value was null. Will not serialize as this is a programing error.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"SUCCEEDED", "IN_PROGRESS", "FAILED"})
  void testConvertToEntityAttribute(String code) {
    State state = instance.convertToEntityAttribute(code); 
    assertThat(state.name()).isEqualTo(code);
  }
  
  @Test
  void testConvertToEntityAttributeThrowsOnNullInput() {
    assertThatThrownBy(() -> {
      instance.convertToEntityAttribute(null);
    }).isInstanceOf(IllegalArgumentException.class).hasMessage("Error! State code string value was null. Cannot deserialize.");
  }
  
  @Test
  void testConvertToEntityAttributeThrowsOnUnknownCode() {
    assertThatThrownBy(() -> {
      instance.convertToEntityAttribute("nonexistent");
    }).isInstanceOf(RuntimeException.class).hasMessage("Error! Could not map State enumeration from database value.");
  }
}
