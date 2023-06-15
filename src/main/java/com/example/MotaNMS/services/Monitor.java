package com.example.MotaNMS.services;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Monitor {
  EventBus eventBus;

  Vertx vertx;

  private final Logger logger = LoggerFactory.getLogger(Monitor.class);

  protected void setEventBus(Vertx vertx1) {
    vertx = vertx1;
    eventBus = vertx.eventBus();

  }

  protected void load(Message<Object> message) {

    try {

      String query = SqlQueries.selectMonitor();

      eventBus.request("loadMonitor", new JsonObject().put("table", "monitor").put("query", query), reply -> {

        if (reply.succeeded()) {
          String data = reply.result().body().toString();

          message.reply(data);

          logger.info("monitor data loaded success");

        } else {

          message.fail(2, "");

          logger.error("could not load data");
        }

      });
    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      logger.error(exception.getMessage());
    }
  }

  protected void delete(Message<Object> message) {

    JsonObject deleteData = (JsonObject) message.body();

    String query = SqlQueries.deleteMonitor();

    String id = deleteData.getString("id");

    if (!id.isEmpty()) {

      eventBus.request("deleteFromMonitor", new JsonObject().put("id", id).put("query", query), reply -> {

        if (reply.succeeded()) {

          message.reply("row deleted");

          logger.info("Monitor row deleted Successfully");

        } else {

          message.fail(2, "");

          logger.error("row delete failed");

        }
      });
    } else {

      message.fail(2, "No row id to delete");
    }
  }

  protected void loadDeviceData(Message<Object> message) {

    List<JsonArray> deviceInfoData = new ArrayList<>();

    JsonObject deviceIp = (JsonObject) message.body();

    String availabilityQuery = SqlQueries.avaiability();

    String query ;

    if(deviceIp.getString("type").equals("ssh")){

      query= SqlQueries.selectAllLatestData();
      CompositeFuture.join(getDeviceInfoData(deviceIp.put("query",query)),
        getDeviceInfoData(deviceIp.put("query",availabilityQuery)),
        getDeviceInfoData(deviceIp.put("query",SqlQueries.cpuLinechart())),
          getDeviceInfoData(deviceIp.put("query",SqlQueries.memoryLinechart())),
            getDeviceInfoData(deviceIp.put("query",SqlQueries.diskLinechart()))).onComplete(handler -> {

        if (handler.succeeded()) {

          for (int i = 0; i < handler.result().size(); i++) {

            JsonArray future = handler.result().resultAt(i);

            deviceInfoData.add(future);

          }
          System.out.println(deviceInfoData);

          message.reply(deviceInfoData.toString());
        }

      });


    }else {

      query = SqlQueries.selectPingLatestData();

      CompositeFuture.join(getDeviceInfoData(deviceIp.put("query",query)),getDeviceInfoData(deviceIp.put("query",availabilityQuery))).onComplete(handler -> {

        if (handler.succeeded()) {

          for (int i = 0; i < handler.result().size(); i++) {

            JsonArray future = handler.result().resultAt(i);

            deviceInfoData.add(future);

          }
          System.out.println(deviceInfoData);

          message.reply(deviceInfoData.toString());
        }

      });


    }

  }

  private Future<JsonArray> getDeviceInfoData(JsonObject device) {

    Promise<JsonArray> promise = Promise.promise();

    eventBus.request("toprtt", device, messageAsyncResult -> {

      if (messageAsyncResult.succeeded()) {

        String deviceInfo = messageAsyncResult.result().body().toString();

        JsonArray deviceInfoArray = new JsonArray(deviceInfo);

        promise.complete(deviceInfoArray);

      } else {

        logger.error("cant get device data");

      }

    });

    return promise.future();
  }
}
