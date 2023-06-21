package com.salesforce.mirus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

@Path("/connectors")
public class CustomEndpoint {
  private final Map<String, ?> configs;
  private static final Logger logger = LoggerFactory.getLogger(CustomEndpoint.class);

  public CustomEndpoint(Map<String, ?> configs) {
    this.configs = configs;
  }

  @GET
  @Path("/{connector}/rebalancestatus")
  public Response healthEndpoint(@PathParam("connector") String connectorName) {
    try {
      String connectorStatus = getConnectorStatus(connectorName);
      List<String> taskStates = getTaskStates(connectorStatus);
      boolean response = rebalanceDetector(taskStates, connectorName);
      return Response.ok(response).build();
    } catch (NullPointerException e) {
      logger.error("NullPointerException ", e);
      String errorMessage = "An error occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    } catch (IOException e) {
      logger.error("IOException ", e);
      String errorMessage = "An error occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    } catch (JSONException e) {
      logger.error("Json exception ", e.getMessage());
      String errorMessage = "An error occurred while processing the request.";
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
    }
  }

  private String getConnectorStatus(String connectorName) throws IOException {
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

  private List<String> getTaskStates(String connectorStatus) throws JSONException {
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

  private boolean rebalanceDetector(List<String> taskStates, String connnector)
      throws NullPointerException {
    Map<String, Integer> connectorpartitions = KafkaMonitorMetrics.getConnectorPartitionMap();
    Integer partitions = connectorpartitions.get(connnector);
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
