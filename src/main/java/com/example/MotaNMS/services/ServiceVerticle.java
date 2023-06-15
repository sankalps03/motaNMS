package com.example.MotaNMS.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import static com.example.MotaNMS.util.QueryConstants.*;

public class ServiceVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(ServiceVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise) {

    try {

      eventBus = getVertx().eventBus();

      Discovery discovery = new Discovery();

      Monitor monitor = new Monitor();

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

      eventBus.localConsumer("dashboardLoad",handler ->getTop5Data());

      vertx.setPeriodic(5*60*1000, Handler -> getTop5Data());

      startPromise.complete();

    } catch (Exception exception) {

      logger.error(exception.getMessage());

    }

  }

  private void getTop5Data() {

    List<JsonArray> dashBoardData = new ArrayList<>();

    CompositeFuture.join(top5DbRequest(TOP_5_MEMORY_QUERY),
      top5DbRequest(TOP_5_CPU_QUERY),
      top5DbRequest(TOP_5_DISK_QUERY),
      top5DbRequest(TOP_5_RTT_QUERY),
      top5DbRequest(DEVICE_COUNT_QUERY)).onComplete(handler -> {

      if (handler.succeeded()) {

        for (int i = 0; i < handler.result().size(); i++) {

          JsonArray future = handler.result().resultAt(i);

          dashBoardData.add(future);

        }
        System.out.println(dashBoardData);

        eventBus.publish("updates.Dashboard",dashBoardData.toString());
      }

    });
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
