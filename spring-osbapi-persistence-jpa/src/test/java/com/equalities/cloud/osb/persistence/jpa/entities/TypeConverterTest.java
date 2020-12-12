package com.equalities.cloud.osb.persistence.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;
import com.equalities.cloud.osb.persistence.jpa.entities.TypeConverter;

class TypeConverterTest {
  
  TypeConverter instance;
  
  @BeforeEach
  void setUp() {
    instance = new TypeConverter();
  }

  @ParameterizedTest
  @EnumSource
  void testConvertToDatabaseColumn(Type type) {
    assertThat(instance.convertToDatabaseColumn(type)).isEqualTo(type.name());
  }
  
  @Test
  void testConvertToDatabaseColumnThrowsOnNullInput() {
    assertThatThrownBy(() -> {
      instance.convertToDatabaseColumn(null);
    }).isInstanceOf(IllegalArgumentException.class).hasMessage("Error! Type enumeration value was null. Will not serialize as this is a programing error.");
  }

  @ParameterizedTest
  @ValueSource(strings = {"CREATE", "UPDATE", "DELETE"})
  void testConvertToEntityAttribute(String code) {
    Type type = instance.convertToEntityAttribute(code); 
    assertThat(type.name()).isEqualTo(code);
  }
  
  @Test
  void testConvertToEntityAttributeThrowsOnNullInput() {
    assertThatThrownBy(() -> {
      instance.convertToEntityAttribute(null);
    }).isInstanceOf(IllegalArgumentException.class).hasMessage("Error! Type code string value was null. Cannot deserialize.");
  }
  
  @Test
  void testConvertToEntityAttributeThrowsOnUnknownCode() {
    assertThatThrownBy(() -> {
      instance.convertToEntityAttribute("nonexistent");
    }).isInstanceOf(RuntimeException.class).hasMessage("Error! Could not map Type enumeration from database value.");
  }
}
