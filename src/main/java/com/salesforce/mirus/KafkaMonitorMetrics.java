package com.salesforce.mirus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaMonitorMetrics {
  private static final Map<String, Integer> connectorPartitionMap = new ConcurrentHashMap<>();

  public static void updateConnectorPartitions(String connectorName, int numPartitions) {
    connectorPartitionMap.put(connectorName, numPartitions);
  }

  public static void removeConnector(String connectorName) {
    connectorPartitionMap.remove(connectorName);
  }

  public static Map<String, Integer> getConnectorPartitionMap() {
    return new ConcurrentHashMap<>(connectorPartitionMap);
  }
}
