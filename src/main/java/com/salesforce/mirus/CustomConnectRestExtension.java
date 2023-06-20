package com.salesforce.mirus;

import java.util.Map;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomConnectRestExtension implements ConnectRestExtension {

  private Map<String, ?> configs;
  private static final Logger log = LoggerFactory.getLogger(CustomConnectRestExtension.class);

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

  @Override
  public String version() {
    return AppInfoParser.getVersion();
  }

  @Override
  public void configure(Map<String, ?> map) {
    this.configs = configs;
  }
}
