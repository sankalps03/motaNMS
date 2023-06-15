package com.example.MotaNMS;

import com.example.MotaNMS.database.DatabaseVerticle;
import com.example.MotaNMS.poller.PollingVerticle;
import com.example.MotaNMS.publicAPI.PublicApiVerticle;
import com.example.MotaNMS.services.ServiceVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    try {

      Vertx vertx = Vertx.vertx();

      vertx.deployVerticle(DatabaseVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("Database verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

      vertx.deployVerticle(PublicApiVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("publicAPI verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

      vertx.deployVerticle(ServiceVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("Service verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

      vertx.deployVerticle(PollingVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("Polling verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

    } catch (Exception exception) {

      logger.error(exception.getMessage());
    }

  }
}
