package com.salesforce.mirus;

import java.util.Map;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CustomConnectRestExtension is an implementation of the ConnectRestExtension interface that
 * registers a custom endpoint with the Connect REST server.
 */
public class CustomConnectRestExtension implements ConnectRestExtension {

  private Map<String, ?> configs;
  private static final Logger log = LoggerFactory.getLogger(CustomConnectRestExtension.class);

  /**
   * Registers the custom endpoint with the Connect REST server.
   *
   * @param context The ConnectRestExtensionContext to register the custom endpoint.
   */
  @Override
  public void register(ConnectRestExtensionContext context) {
    try {
      context.configurable().register(new CustomEndpoint(this.configs));
    } catch (NullPointerException e) {
      log.error("An error occurred while registering the custom endpoint: " + e.getMessage());
    }
  }

  @Override
  public void close() {}

  /**
   * Retrieves the version of the Connect REST extension.
   *
   * @return The version of the Connect REST extension.
   */
  @Override
  public String version() {
    return AppInfoParser.getVersion();
  }

  /**
   * Configures the Connect REST extension with the provided configuration.
   *
   * @param configs The configuration map for the Connect REST extension.
   */
  @Override
  public void configure(Map<String, ?> configs) {
    this.configs = configs;
  }
}
