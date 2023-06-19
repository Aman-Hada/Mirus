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

  private String rebalanceDetector(List<String> taskStates, String connnector) {
    Map<String, Integer> connectorpartitions = KafkaMonitorMetrics.getConnectorPartitionMap();
    Integer partitions = connectorpartitions.get(connnector);
    System.out.println(
        "the connector and no. of partitions assigned to it are : "
            + connnector
            + " "
            + partitions);
    boolean flag1 = false;
    for (String state : taskStates) {
      if (!state.equals("RUNNING")) {
        flag1 = true;
        break;
      }
    }
    if (flag1) {
      // Rebalance happening if the number of task states is greater than the number of partitions
      // assigned to the connector
      if (taskStates.size() > partitions) {
        // Check if the number of running status tasks is equal to the number of partitions
        int runningTasksCount = 0;
        for (String state : taskStates) {
          if (state.equals("RUNNING")) {
            runningTasksCount++;
          }
        }
        if (runningTasksCount == partitions) {
          return "rebalance not happening";
        } else {
          return "rebalance happening";
        }
      } else {
        return "rebalance happening";
      }
    } else {
      return "rebalance not happening";
    }
  }

  @GET
  public Response myEndpoint(@PathParam("connector") String url) {

    String connectorStatus = getConnectorStatus(url);
    List<String> taskStates = getTaskStates(connectorStatus);
    String response = rebalanceDetector(taskStates, url);

    return Response.ok("Custom endpoint response :" + response).build();
  }
}
