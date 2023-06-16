package com.example.MotaNMS.services;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.MotaNMS.util.GeneralConstants.SELECT;
import static com.example.MotaNMS.util.QueryConstants.*;

public class Dashboard
{

  private static final Logger LOGGER = LoggerFactory.getLogger(Dashboard.class);

  EventBus eventBus;

  Vertx vertx;

  protected void setEventBus(Vertx vertx1)
  {
    vertx = vertx1;

    eventBus = vertx.eventBus();
  }
  protected void getDashboardData()
  {
    try
    {

      List<JsonArray> dashBoardData = new ArrayList<>();

      CompositeFuture.join(dashboardDbRequest(TOP_5_MEMORY_QUERY),
          dashboardDbRequest(TOP_5_CPU_QUERY),
          dashboardDbRequest(TOP_5_DISK_QUERY),
          dashboardDbRequest(TOP_5_RTT_QUERY),
          dashboardDbRequest(DEVICE_COUNT_QUERY))
        .onComplete(handler ->
        {

          if (handler.succeeded()) {

            for (int futureResult = 0; futureResult < handler.result().size(); futureResult++)
            {

              if(handler.result().resultAt(futureResult) != null)
              {

                JsonArray future = handler.result().resultAt(futureResult);

                dashBoardData.add(future);

              }
            }
            eventBus.publish("updates.Dashboard",dashBoardData.toString());

            LOGGER.debug("Dashboard data published Successfully for :" + LocalDateTime.now());
          }

        });
    }
    catch (Exception exception)
    {

      LOGGER.error(exception.getMessage(),exception.getCause());

    }
  }


  private Future<JsonArray> dashboardDbRequest(String query) {

    Promise<JsonArray> promise = null;

    try {

      promise = Promise.promise();

      Promise<JsonArray> finalPromise = promise;

      eventBus.request(SELECT, new JsonObject().put("query", query), messageAsyncResult ->
      {

        if (messageAsyncResult.succeeded()) {

          String dashboardData = messageAsyncResult.result().body().toString();

          JsonArray dashboardDataArray = new JsonArray(dashboardData);

          finalPromise.complete(dashboardDataArray);

        } else
        {
          finalPromise.fail(messageAsyncResult.cause());

          LOGGER.error(messageAsyncResult.cause().getMessage() + " : " + query);

        }

      });

    }
    catch (Exception exception)
    {
      if (promise != null)
      {
        promise.fail(exception.getCause());

      }

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
    if (promise != null)
    {
      return promise.future();
    }
    return null;
  }
}
