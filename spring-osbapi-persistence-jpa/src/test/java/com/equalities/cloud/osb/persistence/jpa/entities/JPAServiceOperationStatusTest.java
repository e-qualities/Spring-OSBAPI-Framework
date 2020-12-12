package com.equalities.cloud.osb.persistence.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.Embeddable;

import org.junit.jupiter.api.Test;

import com.equalities.cloud.osb.persistence.jpa.entities.JPAServiceOperationStatus;

class JPAServiceOperationStatusTest {

  @Test
  void testIsEmbeddable() {
    assertThat(JPAServiceOperationStatus.class.isAnnotationPresent(Embeddable.class)).isTrue();
  }
}
