package com.equalities.cloud.osb.persistence.jpa.entities;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.equalities.cloud.osb.persistence.ServiceOperationStatus.State;

@Converter(autoApply = true)
public class StateConverter implements AttributeConverter<State, String> {
 
    @Override
    public String convertToDatabaseColumn(State state) {
      if(state == null) {
        throw new IllegalArgumentException("Error! State enumeration value was null. Will not serialize as this is a programing error.");
      }
      
      return state.name();
    }
 
    @Override
    public State convertToEntityAttribute(String code) {
        if (code == null) {
          throw new IllegalArgumentException("Error! State code string value was null. Cannot deserialize.");
        }

        return Stream.of(State.values())
          .filter(c -> c.name().equals(code))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Error! Could not map State enumeration from database value."));
    }
}