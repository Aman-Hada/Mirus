package com.salesforce.mirus.config;

import java.time.LocalDateTime;

public class NeedsReconfigRebalancePrinter implements Runnable {
  private final DistributedHerderWrapper herderWrapper;

  public NeedsReconfigRebalancePrinter(DistributedHerderWrapper herderWrapper) {
    this.herderWrapper = herderWrapper;
  }

  @Override
  public void run() {
    while (true) {
      boolean needsReconfigRebalance = herderWrapper.isNeedsReconfigRebalance();
      String timestamp = LocalDateTime.now().toString(); // Get the current timestamp

      System.out.println(timestamp + "  needsReconfigRebalance: " + needsReconfigRebalance);

      try {
        Thread.sleep(100); // Adjust the sleep duration as needed
      } catch (InterruptedException e) {
        break;
      }
    }
  }
}
