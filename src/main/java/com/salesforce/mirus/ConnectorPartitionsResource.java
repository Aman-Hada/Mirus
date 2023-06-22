package com.salesforce.mirus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectorPartitionsResource is a utility class that manages the partition mapping for connectors.
 * It provides methods to update, remove, and retrieve the connector partition map.
 */
public class ConnectorPartitionsResource {
  private static final Map<String, Integer> connectorPartitionMap = new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(ConnectorPartitionsResource.class);

  /**
   * Updates the connector partition map with the specified connector name and number of partitions
   * assigned to it.
   *
   * @param connectorName The name of the connector.
   * @param numPartitions The number of partitions for the connector.
   */
  public static void updateConnectorPartitionMap(String connectorName, int numPartitions) {
    try {
      connectorPartitionMap.put(connectorName, numPartitions);
      logger.info(
          "Updated connector partitions - Connector: {}, Partitions: {} in connectorPartitionMap to keep a track of no. of partitions in a connector",
          connectorName,
          numPartitions);
    } catch (NullPointerException e) {
      logger.error("Null value encountered while updating connector partitions");
      e.printStackTrace();
    }
  }

  /**
   * Removes the connector from the connector partition map.
   *
   * @param connectorName The name of the connector to be removed.
   */
  public static void removeConnector(String connectorName) {
    try {
      connectorPartitionMap.remove(connectorName);
      logger.info("Removed connector - Connector: {} in connectorPartitionMap", connectorName);
    } catch (NullPointerException e) {
      logger.error("Null value encountered while removing connector");
      e.printStackTrace();
    }
  }

  /**
   * Retrieves the connector partition map.
   *
   * @return The connector partition map.
   */
  public static Map<String, Integer> getConnectorPartitionMap() {
    return connectorPartitionMap;
  }
}
