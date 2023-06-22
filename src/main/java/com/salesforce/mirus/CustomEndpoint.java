package com.salesforce.mirus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CustomEndpoint is a RESTful endpoint class that provides a rebalance check endpoint to determine
 * if a connector is currently undergoing a rebalance or not.
 *
 * <p>We are only concerned with worker level rebalances in source connector, so we call the status
 * endpoint provided by Kafka Connect API to determine rebalance status.
 */
@Path("/connectors")
public class CustomEndpoint {
  private final Map<String, ?> configs;
  private static final Logger logger = LoggerFactory.getLogger(CustomEndpoint.class);

  /**
   * Constructs a CustomEndpoint object with the provided configuration.
   *
   * @param configs The configuration for the endpoint.
   */
  public CustomEndpoint(Map<String, ?> configs) {
    this.configs = configs;
  }

  /**
   * Rebalance endpoint to check if a connector is rebalancing.
   *
   * @param connectorName The name of the connector.
   * @return A response indicating whether the connector is currently undergoing a rebalance. It
   *     returns {@code true} if a rebalance is happening and {@code false} otherwise.
   */
  @GET
  @Path("/{connector}/rebalanceStatus")
  public Response rebalanceEndpoint(@PathParam("connector") String connectorName) {
    try {
      String connectorStatus = getConnectorStatusFromRestAPI(connectorName);
      if (connectorStatus == null)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      List<String> taskStates = parseJsonResponse(connectorStatus);
      boolean response = isRebalancing(taskStates, connectorName);
      return Response.ok(response).build();
    } catch (NullPointerException e) {
      logger.error("NullPointerException ", e.getMessage());
      String errorMessage = "Null pointer exception occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    } catch (IOException e) {
      logger.error("IOException ", e.getMessage());
      String errorMessage = "IO exception occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    } catch (JSONException e) {
      logger.error("Json exception ", e.getMessage());
      String errorMessage = "Json exception occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }
  }

  /**
   * Retrieves the connector status from a REST API endpoint.
   *
   * @param connectorName The name of the connector.
   * @return The connector status as a string.
   * @throws IOException If an I/O error occurs while connecting to the REST API.
   */
  private String getConnectorStatusFromRestAPI(String connectorName) throws IOException {
    String urlString = "http://localhost:8083/connectors/" + connectorName + "/status";
    URL apiUrl = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      return response.toString();
    }

    return null;
  }

  /**
   * Parses the connector status JSON response and extracts the task states.
   *
   * @param connectorStatus The connector status as a JSON string.
   * @return A list of task states.
   * @throws JSONException If an error occurs while parsing the JSON response.
   */
  private List<String> parseJsonResponse(String connectorStatus) throws JSONException {
    List<String> taskStates = new ArrayList<>();
    if (connectorStatus != null) {
      JSONObject statusJson = new JSONObject(connectorStatus);
      JSONArray tasksJson = statusJson.getJSONArray("tasks");
      for (int i = 0; i < tasksJson.length(); i++) {
        JSONObject taskJson = tasksJson.getJSONObject(i);
        String state = taskJson.getString("state");
        taskStates.add(state);
      }
    }
    return taskStates;
  }

  /**
   * Checks if the connector is rebalancing its tasks.
   *
   * @param taskStates The list of task states.
   * @param connectorName The name of the connector.
   * @return {@code true} if the connector is rebalancing, {@code false} otherwise.
   * @throws NullPointerException If a null value is encountered while processing the tasks.
   */
  private boolean isRebalancing(List<String> taskStates, String connectorName)
      throws NullPointerException {
    Map<String, Integer> connectorPartitions =
        ConnectorPartitionsResource.getConnectorPartitionMap();
    Integer partitions = connectorPartitions.get(connectorName);
    boolean allRunning = true;
    int runningTasksCount = 0;
    for (String state : taskStates) {
      if (!state.equals("RUNNING")) {
        allRunning = false;
      } else runningTasksCount++;
    }
    // if tasks are greater than assigned partitions to a connector.
    // rebalance doesn't happen when active tasks are equal to partitions assigned.
    if (allRunning || ((taskStates.size() > partitions) && (runningTasksCount == partitions))) {
      return false;
    } else {
      return true;
    }
  }
}
