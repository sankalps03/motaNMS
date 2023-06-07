package com.example.MotaNMS.Database;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class connectionPool {

  private static final Logger logger = LoggerFactory.getLogger(connectionPool.class);

  private String URL ="jdbc:h2:~/test";
  private String USER ="sa";
  private String PASSWORD ="";

  private static ArrayBlockingQueue<Connection> connectionPool;

  private static ArrayList< Connection > activeConnection = new ArrayList<>();

  private final int MAXPOOLSIZE = 5;

  private static connectionPool pool;

  public connectionPool(){}

  public static connectionPool getInstance(){

    if (pool == null){

      pool = new connectionPool();
    }

    return pool;
  }

  protected void createConnection(){

    Connection connection;

    connectionPool = new ArrayBlockingQueue<>(MAXPOOLSIZE);

    try {

      for (int i =0 ; i< MAXPOOLSIZE; i++){

      connection = DriverManager.getConnection(URL, USER, PASSWORD);

      connectionPool.add(connection);

      }


    }catch (Exception exception){

      logger.error(exception.getMessage());


    }

  }

  protected Connection getConnection ()
  {

    Connection connection = connectionPool.poll();

    activeConnection.add(connection);

    return connection;
  }

  public static void releaseConnection (Connection connection)
  {

    try
    {
      connectionPool.put(connection);

      activeConnection.remove(connection);

    }
    catch ( InterruptedException exception )
    {
      exception.printStackTrace();
    }

  }

  private void closeAllConnections()
  {
    int size = activeConnection.size();

    for(int index=0;index<size;index++)
    {
      try
      {
        activeConnection.get(index).close();
      }
      catch ( Exception exception )
      {
        exception.printStackTrace();
      }
    }
  }
}
