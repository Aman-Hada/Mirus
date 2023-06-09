package com.salesforce.mirus.config;

import java.lang.reflect.Field;
import org.apache.kafka.connect.runtime.distributed.DistributedHerder;

public class DistributedHerderWrapper {
  private final DistributedHerder herder;

  public DistributedHerderWrapper(DistributedHerder herder) {
    this.herder = herder;
  }

  public boolean isNeedsReconfigRebalance() {
    // Use reflection to access the private field 'needsReconfigRebalance'
    try {
      Field field = herder.getClass().getDeclaredField("needsReconfigRebalance");
      field.setAccessible(true);
      return field.getBoolean(herder);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      System.out.println("error");
      return false;
    }
  }

}
