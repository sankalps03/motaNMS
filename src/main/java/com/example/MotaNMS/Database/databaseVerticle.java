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


public class databaseVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(databaseVerticle.class);

  EventBus eventBus;

  public static final String DISCOVERY = "discovery";
  public static final String MONITOR = "monitor";
  public static final String POllING = "polling";

  connectionPool pool;

  public void start(Promise<Void> startPromise) {

    eventBus = getVertx().eventBus();

    pool = new connectionPool();

    pool.createConnection();

    eventBus.consumer("addToDiscovery").handler(this::insert);

    eventBus.consumer("deleteFromDiscovery").handler(this::delete);

    eventBus.consumer("loadDiscovery").handler(this::loadTable);

    startPromise.complete();


  }

  private void loadTable(Message message) {

    try {


    JsonObject data = (JsonObject) message.body();

    String query;

    switch (data.getString("table")) {

      case DISCOVERY:

        query = sqlQueries.selectDiscovery();

        break;

      case MONITOR:

        query = "";

        break;

      default:
        logger.error("wrong table to load");

        message.fail(2,"wrong table");

        return;
  }

    Connection connection = pool.getConnection();

    PreparedStatement prepared = connection.prepareStatement(query);

    ResultSet resultSet = prepared.executeQuery();

      JsonArray discoveryData = new JsonArray();

      try {

        while (resultSet.next()){
          discoveryData.add(new JsonObject().put("id",resultSet.getInt("id")).
            put("ip",resultSet.getString("ipaddress")).
            put("type",resultSet.getString("type")).
            put("provision",resultSet.getBoolean("provision"))
          );

        }
        message.reply(discoveryData);
        System.out.println(discoveryData);
      }catch (Exception exception){

        message.fail(2,exception.getMessage());

        logger.error(exception.getMessage());

      }

  }catch (Exception exception){

      message.fail(2,exception.getMessage());

      logger.error(exception.getMessage());
    }
  }

  private void delete(Message<Object> message) {

    JsonObject data = (JsonObject) message.body();

    String query = null;

    int id = Integer.valueOf(data.getInteger("id"));

    try {

      switch (data.getString("table")) {

        case DISCOVERY:

          query = sqlQueries.deleteDiscovery();

          break;

        case MONITOR:

          query = "";

          break;

        case POllING:

          query = "";

          break;

        default:

          logger.error("wrong table to insert");

          return;
      }

      Connection connection = pool.getConnection();

      PreparedStatement prepared = connection.prepareStatement(query);

      prepared.setInt(1, id);

      prepared.execute();

      connectionPool.releaseConnection(connection);

      message.reply("in database");

    } catch (Exception exception) {

      message.fail(2, "delete failed");

      logger.error(exception.getMessage());

    }
  }

  private void insert(Message<Object> message) {

    JsonObject data = (JsonObject) message.body();

    String query = null;

    try {

      Connection connection = pool.getConnection();

      switch (data.getString("table")) {

        case DISCOVERY:

          query = sqlQueries.insertDiscovery();

          break;

        case MONITOR:

          query = "";

          break;

        case POllING:

          query = "";

          break;

        default:

          logger.error("wrong table to delete");

          message.fail(2, "delete failed");

          return;

      }

      PreparedStatement prepared = connection.prepareStatement(query);

      prepared.setString(1, data.getString("ip"));

      prepared.setString(2, data.getString("type"));

      prepared.setString(3, data.getString("credentials"));

      prepared.execute();

      connectionPool.releaseConnection(connection);

      message.reply("inserted");

    } catch (Exception exception) {

      message.fail(2, "delete failed");

      logger.error(exception.getMessage());
    }

  }

}
