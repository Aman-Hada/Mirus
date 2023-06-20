package com.salesforce.mirus;

import java.util.Map;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

public class CustomConnectRestExtension implements ConnectRestExtension {

  private Map<String, ?> configs;

  @Override
  public void register(ConnectRestExtensionContext context) {
    context.configurable().register(new CustomEndpoint(this.configs));
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
