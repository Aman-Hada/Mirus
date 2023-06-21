package com.salesforce.mirus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class KafkaMonitorMetrics {
  private static final Map<String, Integer> connectorPartitionMap = new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorMetrics.class);


  public static void updateConnectorPartitions(String connectorName, int numPartitions) {
    connectorPartitionMap.put(connectorName, numPartitions);
    logger.info("Updated connector partitions - Connector: {}, Partitions: {}", connectorName, numPartitions);
  }

  public static void removeConnector(String connectorName) {
    connectorPartitionMap.remove(connectorName);
    logger.info("Removed connector - Connector: {}", connectorName);
  }

  public static Map<String, Integer> getConnectorPartitionMap() {
    return new ConcurrentHashMap<>(connectorPartitionMap);
  }
}
