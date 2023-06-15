package com.example.MotaNMS.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.MotaNMS.util.GeneralConstants.*;
import static com.example.MotaNMS.util.QueryConstants.*;


public class DatabaseVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise)
  {

    try
    {
      eventBus = getVertx().eventBus();

      boolean poolCreated = ConnectionPool.getInstance().createConnection();

      if (!poolCreated)
      {

        throw new Exception("Connection pool creation failed");

      }

      eventBus.localConsumer(INSERT).handler(this::insert);

      eventBus.localConsumer(ROW_ID_OPERATION).handler(this::rowIdOperation);

      eventBus.localConsumer(SELECT).handler(this::select);

      startPromise.complete();
    }
    catch (Exception exception)
    {

      startPromise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(),exception.getCause());

    }
  }

  private void update(Message<Object> message)
  {

    vertx.executeBlocking(selectOperation ->
    {

      PreparedStatement preparedUpdateStatement = null;

      Connection connection = null;

      try
      {

        if (message.body() == null)
        {

          throw new Exception("Null message received in update method");

        }

        JsonObject executionData = (JsonObject) message.body();

        String query = executionData.getString("query");

        connection = ConnectionPool.getInstance().getConnection();

        if (connection.isClosed())
        {

          throw new Exception("Database connection is closed: " + query );

        }

        preparedUpdateStatement = connection.prepareStatement(query);

        if (preparedUpdateStatement == null){

          throw  new Exception("Prepared Statement is null");
        }

        preparedUpdateStatement.execute();

        message.reply("Execution completed:" + query);

        LOGGER.debug("Execution completed:" + query);

      }
      catch (Exception exception)
      {

        message.fail(2,exception.getMessage());

        LOGGER.error(exception.getMessage(),exception.getCause());
      }
      finally
      {

        try
        {
          if (preparedUpdateStatement != null) {

            preparedUpdateStatement.close();

          }
        }
        catch (Exception exception) {

          LOGGER.error(exception.getMessage(),exception.getCause());

        }

        ConnectionPool.getInstance().releaseConnection(connection);

      }
    });
  }

  private void select(Message<Object> message)
  {

    vertx.executeBlocking(selectOperation ->
    {

      PreparedStatement preparedSelectStatement = null;

      Connection connection = null;

      try
      {
        if (message.body() == null)
        {

          throw new Exception("Null message received in update method");

        }


        List<Map<String, String>> resultData = new ArrayList<>();

        JsonObject executionData = (JsonObject) message.body();

        String query = executionData.getString("query");

        connection = ConnectionPool.getInstance().getConnection();

        if (connection.isClosed())
        {

          throw new Exception("Database connection is closed: " + query );

        }

        preparedSelectStatement = connection.prepareStatement(query);

        if (preparedSelectStatement == null){

          throw  new Exception("Prepared Statement is null");
        }

        if (executionData.containsKey("id"))
        {

          preparedSelectStatement.setInt(1,Integer.parseInt(executionData.getString("id")));

        }
        else if((query.equals(SSH_LATEST_DATA_QUERY)) ||(query.equals(PING_LATEST_DATA_QUERY)))
        {

          preparedSelectStatement.setString(1, executionData.getString("ip"));

          preparedSelectStatement.setString(2, executionData.getString("ip"));

        }
        else if (query.equals(LAST_24_HOUR_AVAILABILITY_QUERY) || query.equals(LAST_1_HOUR_CPU_USED_QUERY) || query.equals(LAST_1_HOUR_DISK_USED_QUERY) || query.equals(LAST_1_HOUR_MEMORY_USED_QUERY))
        {

          preparedSelectStatement.setString(1, executionData.getString("ip"));

        }

        ResultSet resultSet = preparedSelectStatement.executeQuery();

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        while (resultSet.next())
        {
          Map<String, String> tableData = new HashMap<>(resultSetMetaData.getColumnCount());

          for (int column = 1; column <= resultSetMetaData.getColumnCount(); column++)
          {
            String columnName = resultSetMetaData.getColumnName(column);

            tableData.put(columnName, resultSet.getString(columnName));
          }
          resultData.add(tableData);
        }

        JsonArray selectResult = new JsonArray(resultData);

        message.reply(selectResult);

        LOGGER.debug("Execution completed:" + query);

      }
      catch (Exception exception)
      {

        message.fail(2, exception.getMessage());

        LOGGER.error(exception.getMessage(),exception.getCause());
      }
      finally
      {

        try
        {
          if (preparedSelectStatement != null)
          {
            preparedSelectStatement.close();
          }
        }
        catch (Exception exception) {

          LOGGER.error(exception.getMessage(),exception.getCause());

        }
        ConnectionPool.getInstance().releaseConnection(connection);
      }
    }, false);

  }

  private void rowIdOperation(Message<Object> message)
  {

    vertx.executeBlocking(rowOperation ->
    {
      PreparedStatement preparedRowOperationStatement = null;

      Connection connection = null;

      try
      {

        if (message.body() == null)
        {

          throw new Exception("Null message received in update method");

        }

        JsonObject executionData = (JsonObject) message.body();

        String query = executionData.getString("query");

        int id = Integer.parseInt(executionData.getString("id"));

        connection = ConnectionPool.getInstance().getConnection();

        if (connection.isClosed())
        {

          throw new Exception("Database connection is closed: " + query );

        }

        preparedRowOperationStatement = connection.prepareStatement(query);

        if (preparedRowOperationStatement == null){

          throw  new Exception("Prepared Statement is null");
        }

        preparedRowOperationStatement.setInt(1, id);

        preparedRowOperationStatement.execute();

        message.reply("Execution completed: "+query);

        LOGGER.debug("Execution Completed:" + query);

      }
      catch (Exception exception)
      {

        message.fail(2, exception.getMessage());

        LOGGER.error(exception.getMessage(),exception.getCause());

      }
      finally
      {

        try
        {
          if (preparedRowOperationStatement != null)
          {
            preparedRowOperationStatement.close();
          }
        }
        catch (Exception exception) {

          LOGGER.error(exception.getMessage(),exception.getCause());

        }
        ConnectionPool.getInstance().releaseConnection(connection);
      }
    }, false);
  }

  private void insert(Message<Object> message)
  {

    vertx.executeBlocking(indertOperaton ->
    {
      PreparedStatement preparedInsertStatement = null;

      Connection connection = null;
      try
      {
        if (message.body() == null)
        {

          throw new Exception("Null message received in update method");

        }

        Object insertData = message.body();

        String query;

        JsonObject jsonInsertData = null;

        JsonArray insertDataArray = null;

        if (insertData instanceof JsonArray)
        {

          insertDataArray = (JsonArray) insertData;

          query = POLLING_INSERT_QUERY;

        }
        else
        {
          jsonInsertData = (JsonObject) message.body();

          query = jsonInsertData.getString("query");
        }

        connection = ConnectionPool.getInstance().getConnection();

        if (connection.isClosed())
        {

          throw new Exception("Database connection is closed: " + query );

        }

        preparedInsertStatement = connection.prepareStatement(query);

        if (preparedInsertStatement == null){

          throw  new Exception("Prepared Statement is null");
        }

        if (insertData instanceof JsonArray) {

          for (Object jsonObjectData : insertDataArray) {

            jsonInsertData = (JsonObject) jsonObjectData;

            preparedInsertStatement.setString(1, jsonInsertData.getString("ip"));

            preparedInsertStatement.setString(2, jsonInsertData.getString("type"));

            preparedInsertStatement.setString(3, jsonInsertData.getString("metricType"));

            preparedInsertStatement.setString(4, jsonInsertData.getString("metricValue"));

            preparedInsertStatement.setString(5, jsonInsertData.getString("timestamp"));

            preparedInsertStatement.addBatch();

          }

          preparedInsertStatement.executeBatch();

        } else {

          preparedInsertStatement.setString(1, jsonInsertData.getString("ip"));

          preparedInsertStatement.setString(2, jsonInsertData.getString("type"));

          preparedInsertStatement.setString(3, jsonInsertData.getJsonObject("credentials").toString());

          preparedInsertStatement.execute();

        }

        message.reply("Execution Completed: "+ query);

        LOGGER.debug("Execution Completed: "+ query);

      } catch (Exception exception) {

        message.fail(2, exception.getMessage());

        LOGGER.error(exception.getMessage(),exception.getCause());
      }
      finally
      {

        try
        {
          if (preparedInsertStatement != null)
          {
            preparedInsertStatement.close();
          }
        }
        catch (Exception exception) {

          LOGGER.error(exception.getMessage(),exception.getCause());

        }
        ConnectionPool.getInstance().releaseConnection(connection);
      }
    }, false);

  }
}
