package com.example.MotaNMS.Database;

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


public class databaseVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(databaseVerticle.class);

  EventBus eventBus;
  connectionPool pool;

  public void start(Promise<Void> startPromise) {

    eventBus = getVertx().eventBus();

    pool = new connectionPool();

    pool.createConnection();

    eventBus.localConsumer("addToDiscovery").handler(this::insert);

    eventBus.localConsumer("deleteFromDiscovery").handler(this::rowIdOperation);

    eventBus.localConsumer("loadDiscovery").handler(this::select);

    eventBus.localConsumer("getCredentialsFromDiscovery").handler(this::select);

    eventBus.localConsumer("addToMonitor").handler(this::rowIdOperation);

    eventBus.localConsumer("setProvisionTrue").handler(this::rowIdOperation);

    startPromise.complete();


  }

  private void select(Message message) {

    try {

      JsonObject data = (JsonObject) message.body();

      Connection connection = pool.getConnection();

      PreparedStatement prepared = connection.prepareStatement(data.getString("query"));

      ResultSet resultSet = prepared.executeQuery();

      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

      List<String> columns = new ArrayList<>(resultSetMetaData.getColumnCount());

      for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {

        columns.add(resultSetMetaData.getColumnName(i));

      }

      List<Map<String,String>> resultData = new ArrayList<>();

      while(resultSet.next()){

        Map<String,String> row = new HashMap<>(columns.size());

        for(String col : columns) {

          row.put(col, resultSet.getString(col));

        }
        resultData.add(row);
      }

      JsonArray result = new JsonArray(resultData);

      message.reply(result);

      connectionPool.releaseConnection(connection);

    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      logger.error(exception.getMessage());
    }

  }

  private void rowIdOperation(Message<Object> message) {

    try {

    JsonObject data = (JsonObject) message.body();

    int id = Integer.valueOf(data.getString("id"));

      Connection connection = pool.getConnection();

      PreparedStatement prepared = connection.prepareStatement(data.getString("query"));

      prepared.setInt(1, id);

      prepared.execute();

      connectionPool.releaseConnection(connection);

      message.reply("in database");

    } catch (Exception exception) {

      message.fail(2, "operation failed");

      logger.error(exception.getMessage());

    }
  }

  private void insert(Message<Object> message) {

    JsonObject data = (JsonObject) message.body();

    try {

      Connection connection = pool.getConnection();

      PreparedStatement prepared = connection.prepareStatement(data.getString("query"));

      prepared.setString(1, data.getString("ip"));

      prepared.setString(2, data.getString("type"));

      prepared.setString(3, data.getJsonObject("credentials").toString());

      prepared.execute();

      connectionPool.releaseConnection(connection);

      message.reply("inserted");

    } catch (Exception exception) {

      message.fail(2, "insert failed");

      logger.error(exception.getMessage());
    }

  }

}
