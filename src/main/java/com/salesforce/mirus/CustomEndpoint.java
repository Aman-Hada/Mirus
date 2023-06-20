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

@Path("/connectors/{connector}/health")
public class CustomEndpoint {
  private final Map<String, ?> configs;
  private static final Logger logger = LoggerFactory.getLogger(CustomEndpoint.class);

  public CustomEndpoint(Map<String, ?> configs) {
    this.configs = configs;
  }

  private String getConnectorStatus(String url) {
    try {
      URL apiUrl = new URL(url);
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
      } else {
        logger.error("Error response code: " + responseCode);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private List<String> getTaskStates(String connectorStatus) {
    List<String> taskStates = new ArrayList<>();
    if (connectorStatus != null) {
      try {
        JSONObject statusJson = new JSONObject(connectorStatus);
        JSONArray tasksJson = statusJson.getJSONArray("tasks");
        for (int i = 0; i < tasksJson.length(); i++) {
          JSONObject taskJson = tasksJson.getJSONObject(i);
          String state = taskJson.getString("state");
          taskStates.add(state);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return taskStates;
  }

  private String rebalanceDetector(List<String> taskStates, String connnector) {
    Map<String, Integer> connectorpartitions = KafkaMonitorMetrics.getConnectorPartitionMap();
    Integer partitions = connectorpartitions.get(connnector);
    if (connectorpartitions == null || partitions == null) {
      throw new NullPointerException("Null pointer exception occurred");
    }
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
      return "rebalance not happening";
    } else {
      return "rebalance happening";
    }
  }

  @GET
  public Response healthEndpoint(@PathParam("connector") String url) {
    try {
      String connectorStatus = getConnectorStatus(url);
      List<String> taskStates = getTaskStates(connectorStatus);
      String response = rebalanceDetector(taskStates, url);

      return Response.ok("Custom endpoint response: " + response).build();
    } catch (NullPointerException e) {
      e.printStackTrace();
      String errorMessage = "An error occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }
  }
}
