package com.equalities.cloud.osb.persistence.jpa.entities;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.equalities.cloud.osb.persistence.ServiceOperationEntity.Type;

@Converter(autoApply = true)
public class TypeConverter implements AttributeConverter<Type, String> {
 
    @Override
    public String convertToDatabaseColumn(Type type) {
      if(type == null) {
        throw new IllegalArgumentException("Error! Type enumeration value was null. Will not serialize as this is a programing error.");
      }
      
      return type.name();
    }
 
    @Override
    public Type convertToEntityAttribute(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Error! Type code string value was null. Cannot deserialize.");
        }

        return Stream.of(Type.values())
          .filter(c -> c.name().equals(code))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error! Could not map Type enumeration from database value."));
    }
}