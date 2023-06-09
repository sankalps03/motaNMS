package com.example.MotaNMS;

import com.example.MotaNMS.database.DatabaseVerticle;
import com.example.MotaNMS.poller.PollingVerticle;
import com.example.MotaNMS.publicAPI.PublicApiVerticle;
import com.example.MotaNMS.services.ServiceVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.MotaNMS.util.GeneralConstants.*;


public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  private static Vertx vertx;

  public static void main(String[] args)
  {
    try
    {
       vertx = Vertx.vertx();

      CompositeFuture.all(
        vertx.deployVerticle(DatabaseVerticle.class.getName(),new DeploymentOptions().setWorkerPoolName("database").setWorkerPoolSize(DATABASE_WORKER_POOL_SIZE).setInstances(2)),
        vertx.deployVerticle(PublicApiVerticle.class.getName()),
        vertx.deployVerticle(ServiceVerticle.class.getName(),new DeploymentOptions().setWorkerPoolName("service").setWorkerPoolSize(SERVICE_WORKER_POOL_SIZE)),
        vertx.deployVerticle(PollingVerticle.class.getName(),new DeploymentOptions().setWorkerPoolName("polling").setWorkerPoolSize(POLLING_WORKER_POOL_SIZE))
      )
        .onComplete(result ->
      {
        if (result.succeeded())
        {
          LOGGER.info("All veritcles deployed Successfully");
        }
        else
        {
          stopVertx();

          LOGGER.error(result.cause().getMessage(),result.cause());
        }
      });

    }
    catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }

  }

  public static void stopVertx() {

    vertx.close(result ->
    {
      if (result.succeeded())
      {
        LOGGER.info("Vert.x instance gracefully stopped.");
      }
      else
      {
        LOGGER.error(result.cause().getMessage(),result.cause());
      }
    });
  }
}
