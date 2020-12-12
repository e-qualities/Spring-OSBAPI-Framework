package com.equalities.cloud.osb.persistence.jpa.entities;

import javax.persistence.Embeddable;

import com.equalities.cloud.osb.persistence.ServiceOperationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JPAServiceOperationStatus {

  private ServiceOperationStatus.State state;

  private String description;
}