package com.example.MotaNMS;

import com.example.MotaNMS.Database.databaseVerticle;
import com.example.MotaNMS.Poller.pollingVerticle;
import com.example.MotaNMS.PublicAPI.publicApiVerticle;
import com.example.MotaNMS.Services.serviceVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    try {

      Vertx vertx = Vertx.vertx();

      vertx.deployVerticle(databaseVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("Database verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

      vertx.deployVerticle(publicApiVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("publicAPI verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

      vertx.deployVerticle(serviceVerticle.class.getName()).onComplete(handler ->{

        if (handler.succeeded()){

          logger.info("Service verticle deployed successfully");

        }else {

          logger.error(handler.result().toString());

        }
      });

//      vertx.deployVerticle(pollingVerticle.class.getName()).onComplete(handler ->{
//
//        if (handler.succeeded()){
//
//          logger.info("Polling verticle deployed successfully");
//
//        }else {
//
//          logger.error(handler.result().toString());
//
//        }
//      });

    } catch (Exception exception) {

      logger.error(exception.getMessage());
    }

  }
}
