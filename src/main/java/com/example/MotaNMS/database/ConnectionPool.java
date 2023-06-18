package com.example.MotaNMS.database;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ArrayBlockingQueue;

import static com.example.MotaNMS.util.GeneralConstants.*;

public class ConnectionPool {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

  private static ArrayBlockingQueue<Connection> connections;

  private static ConnectionPool pool;

  private ConnectionPool() {
  }

  public static ConnectionPool getInstance()
  {

    if (pool == null)
    {

      pool = new ConnectionPool();

    }

    return pool;
  }

  protected boolean createConnection()
  {

    boolean poolCreated = true;

    Connection connection;

    connections = new ArrayBlockingQueue<>(MAX_POOL_SIZE);

    try
    {

      for (int i = 0; i < MAX_POOL_SIZE; i++) {

        connection = DriverManager.getConnection(URL, USER, PASSWORD);

        connections.add(connection);

      }

    } catch (Exception exception)
    {

      poolCreated = false;

      LOGGER.error(exception.getMessage(), exception.getCause());

    }

    return poolCreated;
  }

  protected Connection getConnection()
  {
    Connection connection = null;
    try
    {

      connection = connections.take();

    } catch (Exception exception) {

      LOGGER.error(exception.getMessage(), exception.getCause());

    }

    return connection;
  }

  protected void releaseConnection(Connection connection)
  {

    try
    {
      connections.put(connection);

    } catch (Exception exception) {

      LOGGER.error(exception.getMessage(), exception.getCause());

    }

  }

  protected void closeAllConnections()
  {

    for (int index = 0; index < connections.size(); index++)
    {
      try
      {
        connections.take().close();

      }
      catch (Exception exception)
      {
        LOGGER.error(exception.getMessage(), exception.getCause());

      }
    }
  }
}
