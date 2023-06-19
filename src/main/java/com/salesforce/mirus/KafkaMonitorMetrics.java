package com.salesforce.mirus;

import java.util.HashMap;
import java.util.Map;

public class KafkaMonitorMetrics {
  private static final Map<String, Integer> connectorPartitionMap = new HashMap<>();

  public static synchronized void updateConnectorPartitions(
      String connectorName, int numPartitions) {
    connectorPartitionMap.put(connectorName, numPartitions);
  }

  public static synchronized void removeConnector(String connectorName) {
    connectorPartitionMap.remove(connectorName);
  }

  public static synchronized Map<String, Integer> getConnectorPartitionMap() {
    return new HashMap<>(connectorPartitionMap);
  }
}
