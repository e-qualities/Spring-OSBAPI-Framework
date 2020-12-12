package com.equalities.cloud.osb.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOperationStatus {

  private ServiceOperationStatus.State state;

  private String description;

  public enum State {
    IN_PROGRESS, SUCCEEDED, FAILED
  }
}