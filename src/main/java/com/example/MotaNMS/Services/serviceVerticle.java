package com.example.MotaNMS.Services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.HashMap;

public class serviceVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(serviceVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise) {

    try {

      eventBus = getVertx().eventBus();

      discovery discovery = new discovery();

      monitor monitor = new monitor();

      discovery.setEventBus(getVertx());

      monitor.setEventBus(getVertx());

      eventBus.localConsumer("discoveryAdd").handler(discovery::add);

      eventBus.localConsumer("discoveryDelete").handler(discovery::delete);

      eventBus.localConsumer("discoveryRun").handler(discovery::run);

      eventBus.localConsumer("discoveryProvision").handler(discovery::provision);

      eventBus.localConsumer("discoveryLoad").handler(discovery::load);

      eventBus.localConsumer("monitorLoad").handler(monitor::load);

      eventBus.localConsumer("monitorDelete").handler(monitor::delete);

      eventBus.localConsumer("monitorLoadDevice").handler(monitor::loadDeviceData);

      vertx.setTimer(5000, handler -> vertx.setPeriodic(5000, Handler -> getTop5Data()));

      startPromise.complete();

    } catch (Exception exception) {

      logger.error(exception.getMessage());

    }

  }

  private Future<HashMap> getTop5Data() {

    Promise<HashMap> promise = Promise.promise();

    String[] topDataTypes = {"rtt", "cpu", "disk", "memory"};

    HashMap<String, JsonArray> dashboardData = new HashMap<>();

    for (String type : topDataTypes) {

      String query = null;

      switch (type) {
        case "rtt":

          query = sqlQueries.selectTop5rtt();

          break;
        case "cpu":

          query = sqlQueries.selectTop5Cpu();

          break;
        case "disk":

          query = sqlQueries.selectTop5Disk();

          break;
        case "memory":

          query = sqlQueries.selectTop5Memory();

          break;
      }

      top5DbRequest(query);

    }
    return promise.future();
  }

  private Future<JsonArray> top5DbRequest(String query) {

    Promise<JsonArray> promise = Promise.promise();

    eventBus.request("toprtt", new JsonObject().put("query", query), messageAsyncResult -> {

      if (messageAsyncResult.succeeded()) {

        String topData = messageAsyncResult.result().body().toString();

        JsonArray topDataArray = new JsonArray(topData);

        promise.complete(topDataArray);

      } else {

        logger.error("cant get device data");

      }

    });

    return promise.future();
  }
}
