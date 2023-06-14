package com.salesforce.mirus;

import io.confluent.connect.rest.ConnectRestExtension;
import io.confluent.connect.rest.RestConfig;
import io.confluent.connect.rest.resources.ConnectorsResource;
import org.apache.kafka.connect.runtime.Herder;
import org.apache.kafka.connect.rest.ConnectRestExtension;
import org.apache.kafka.connect.rest.ConnectRestExtensionContext;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class CustomEndpointFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        // Retrieve the necessary context objects
        Configurable<?> configurable = context.getConfiguration();
        UriInfo uriInfo = context.getResource(UriInfo.class);

        // Get the base URI for the Kafka Connect REST extension
        UriBuilder baseUriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri());

        // Register your custom JAX-RS resource with the ConnectorsResource
        ConnectorsResource connectorsResource = new ConnectorsResource();
        connectorsResource.register(new CustomEndpoint());

        // Get the RestConfig from the ConnectRestExtension
        RestConfig restConfig = configurable.getConfiguration(RestConfig.class);

        // Create an instance of ConnectRestExtension with the necessary configuration
        ConnectRestExtension connectRestExtension = new ConnectRestExtension(baseUriBuilder, restConfig);
        connectRestExtension.register(connectorsResource);

        // Get the Herder instance from the ConnectRestExtension
        Herder herder = connectRestExtension.herder();

        // Set the Herder instance for ConnectorsResource to use
        connectorsResource.setHerder(herder);

        return true;
    }
}

