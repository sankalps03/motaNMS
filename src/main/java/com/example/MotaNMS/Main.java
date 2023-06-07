package com.example.MotaNMS;

import com.example.MotaNMS.Database.databaseVerticle;
import com.example.MotaNMS.PublicAPI.publicApiVerticle;
import com.example.MotaNMS.Services.serviceVerticle;
import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(databaseVerticle.class.getName());

    vertx.deployVerticle(publicApiVerticle.class.getName());

    vertx.deployVerticle(serviceVerticle.class.getName());
  }

}
