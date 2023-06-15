package com.example.MotaNMS.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.MotaNMS.util.QueryConstants.*;


public class DatabaseVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);

  EventBus eventBus;
  ConnectionPool pool;

  public void start(Promise<Void> startPromise) {

    eventBus = getVertx().eventBus();

    pool = new ConnectionPool();

    pool.createConnection();

    eventBus.localConsumer("addToDiscovery").handler(this::insert);

    eventBus.localConsumer("insertInPolling").handler(this::insert);

    eventBus.localConsumer("deleteFromDiscovery").handler(this::rowIdOperation);

    eventBus.localConsumer("deleteFromMonitor").handler(this::rowIdOperation);

    eventBus.localConsumer("loadDiscovery").handler(this::select);

    eventBus.localConsumer("loadMonitor").handler(this::select);

    eventBus.localConsumer("getAllSshDevices").handler(this::select);

    eventBus.localConsumer("getAllPingDevices").handler(this::select);

    eventBus.localConsumer("getCredentialsFromDiscovery").handler(this::select);

    eventBus.localConsumer("addToMonitor").handler(this::rowIdOperation);

    eventBus.localConsumer("setProvisionTrue").handler(this::rowIdOperation);

    eventBus.localConsumer("deviceDataFromDB").handler(this::select);

    eventBus.localConsumer("toprtt").handler(this::select);

    eventBus.localConsumer("updateMonitorStatus").handler(this::update);


    startPromise.complete();
  }

  private void update(Message<Object> message) {

    vertx.executeBlocking(selectOperation -> {

      try {

        JsonObject data = (JsonObject) message.body();

        Connection connection = pool.getConnection();

        PreparedStatement prepared = connection.prepareStatement(data.getString("query"));

        prepared.execute();

        prepared.close();


      } catch (Exception exception) {

        logger.error(exception.getMessage());
      }
    });
  }

  private void select(Message message) {

    vertx.executeBlocking(selectOperation -> {

      try {

        JsonObject data = (JsonObject) message.body();

        Connection connection = pool.getConnection();

        String query =data.getString("query");

        PreparedStatement prepared = connection.prepareStatement(query);

        if (data.containsKey("id")){

          prepared.setInt(1,Integer.valueOf(data.getString("id")));

        }else if((query.equals(SSH_LATEST_DATA_QUERY)) ||(query.equals(PING_LATEST_DATA_QUERY))){

          prepared.setString(1,data.getString("ip"));

          prepared.setString(2,data.getString("ip"));

        }else if (query.equals(LAST_24_HOUR_AVAILABILITY_QUERY) || query.equals(LAST_1_HOUR_CPU_USED_QUERY) || query.equals(LAST_1_HOUR_DISK_USED_QUERY) || query.equals(LAST_1_HOUR_MEMORY_USED_QUERY)){

          prepared.setString(1,data.getString("ip"));

        }

        ResultSet resultSet = prepared.executeQuery();

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        List<String> columns = new ArrayList<>(resultSetMetaData.getColumnCount());

        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {

          columns.add(resultSetMetaData.getColumnName(i));

        }

        List<Map<String, String>> resultData = new ArrayList<>();

        while (resultSet.next()) {

          Map<String, String> row = new HashMap<>(columns.size());

          for (String col : columns) {

            row.put(col, resultSet.getString(col));

          }
          resultData.add(row);
        }

        JsonArray result = new JsonArray(resultData);

        message.reply(result);

        prepared.close();

        ConnectionPool.releaseConnection(connection);

        logger.info("select method run Success");


      } catch (Exception exception) {

        message.fail(2, exception.getMessage());

        logger.error(exception.getMessage());
      }
    }, false);

  }

  private void rowIdOperation(Message<Object> message) {

    vertx.executeBlocking(rowOperation -> {

      try {

        JsonObject data = (JsonObject) message.body();

        int id = Integer.valueOf(data.getString("id"));

        Connection connection = pool.getConnection();

        PreparedStatement prepared = connection.prepareStatement(data.getString("query"));

        prepared.setInt(1, id);

        prepared.execute();

        ConnectionPool.releaseConnection(connection);

        message.reply("in database");

        prepared.close();

        logger.info("row operation successful");

      } catch (Exception exception) {

        message.fail(2, "operation failed");

        logger.error(exception.getMessage());

      }
    }, false);
  }

  private void insert(Message<Object> message) {

    vertx.executeBlocking(indertOperaton -> {
      try {

        Object insertData = message.body();

        String query;

        JsonObject data = null;

        JsonArray dataArray = null;

        if (insertData instanceof JsonArray) {

          dataArray = (JsonArray) insertData;

          query = POLLING_INSERT_QUERY;

        } else {
          data = (JsonObject) message.body();

          query = data.getString("query");
        }

        Connection connection = pool.getConnection();

        PreparedStatement prepared = connection.prepareStatement(query);

        if (insertData instanceof JsonArray) {

          for (Object JsonData : dataArray) {

            data = (JsonObject) JsonData;

            prepared.setString(1, data.getString("ip"));

            prepared.setString(2, data.getString("type"));

            prepared.setString(3, data.getString("metricType"));

            prepared.setString(4, data.getString("metricValue"));

            prepared.setString(5, data.getString("timestamp"));

            prepared.addBatch();

          }

          prepared.executeBatch();

        } else {

          prepared.setString(1, data.getString("ip"));

          prepared.setString(2, data.getString("type"));

          prepared.setString(3, data.getJsonObject("credentials").toString());

          prepared.execute();

        }

        ConnectionPool.releaseConnection(connection);

        message.reply("inserted");

        prepared.close();

        logger.info("Insert Successful");

      } catch (Exception exception) {

        message.fail(2, "insert failed");

        logger.error(exception.getMessage());
      }
    }, false);

  }

}
