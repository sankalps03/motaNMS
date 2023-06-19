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
import static com.example.MotaNMS.util.QueryConstants.*;
import static com.example.MotaNMS.util.GeneralConstants.*;
import java.util.ArrayList;
import java.util.List;

public class Monitor {
  EventBus eventBus;

  Vertx vertx;

  private final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

  protected void init(Vertx vertxInstance)
  {

    vertx = vertxInstance;

    eventBus = vertx.eventBus();

  }

  protected void load(Message<Object> message) {

    try {

      eventBus.request(SELECT, new JsonObject().put("table", "monitor").put("query", MONITOR_SELECT_QUERY), reply ->
      {
        if (reply.succeeded()) {
          String tableData = reply.result().body().toString();

          message.reply(tableData);

          LOGGER.debug("Monitor table data load success");

        }
        else {
          message.fail(2, "Monitor table Data load failed");

          LOGGER.debug("Monitor table data load failed");
        }

      });
    }
    catch (Exception exception) {

      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void delete(Message<Object> message) {

    try {

      if (message.body() == null) {

        throw new Exception("Message body is null");
      }

      JsonObject deleteData = (JsonObject) message.body();

      String id = deleteData.getString("id");

      if (!id.isEmpty()) {
        eventBus.request(ROW_ID_OPERATION, new JsonObject().put("id", id).put("query", MONITOR_DELETE_QUERY), reply ->
        {

          if (reply.succeeded()) {
            message.reply("Monitor table row deleted Successfully : row id " + id);

            LOGGER.info("Monitor table row deleted Successfully");

          }
          else
          {
            message.fail(2, "Monitor table row deletion failed : row id " + id);

            LOGGER.debug("row delete failed : row id " + id);
          }
        });
      }
      else
      {
        message.fail(2, "No row id to delete ");

        LOGGER.debug("No row id to delete");
      }
    }
    catch (Exception exception) {
      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void loadDeviceData(Message<Object> message)
  {

    try {
      if (message.body() == null) {

        throw new Exception("Message body is null");
      }

      List<JsonArray> deviceInfoData = new ArrayList<>();

      JsonObject deviceIp = (JsonObject) message.body();

      if (deviceIp.getString("type").equals("ssh"))
      {

        CompositeFuture.join(getDeviceInfoData(deviceIp.put("query", SSH_LATEST_DATA_QUERY)),
          getDeviceInfoData(deviceIp.put("query", LAST_24_HOUR_AVAILABILITY_QUERY)),
          getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_CPU_USED_QUERY)),
          getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_MEMORY_USED_QUERY)),
          getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_DISK_USED_QUERY)))
          .onComplete(handler ->
        {
          if (handler.succeeded())
          {
            for (int futures = 0; futures < handler.result().size(); futures++)
            {
              if (handler.result().resultAt(futures) != null)
              {
                JsonArray future = handler.result().resultAt(futures);

                deviceInfoData.add(future);
              }
            }
            message.reply(deviceInfoData.toString());
          }
        });
      }
      else
      {
        CompositeFuture.join(getDeviceInfoData(deviceIp.put("query", PING_LATEST_DATA_QUERY)),
          getDeviceInfoData(deviceIp.put("query", LAST_24_HOUR_AVAILABILITY_QUERY)),
            getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_ALL_RTT)))
          .onComplete(handler ->
          {
          if (handler.succeeded())
          {
            for (int futures = 0; futures < handler.result().size(); futures++)
            {
              if (handler.result().resultAt(futures) != null)
              {
                JsonArray future = handler.result().resultAt(futures);

                deviceInfoData.add(future);
              }
            }
            message.reply(deviceInfoData.toString());
          }
        });
      }
    }
    catch (Exception exception)
    {
      message.fail(2,exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  private Future<JsonArray> getDeviceInfoData(JsonObject device) {

    Promise<JsonArray> promise = Promise.promise();

    try
    {
      eventBus.request(SELECT, device, messageAsyncResult -> {

        if (messageAsyncResult.succeeded()) {

          String deviceInfo = messageAsyncResult.result().body().toString();

          JsonArray deviceInfoArray = new JsonArray(deviceInfo);

          promise.complete(deviceInfoArray);

          LOGGER.debug("Data fetch successful for :" + device);
        }
        else
        {
          promise.fail("Data fetch failed for: "+device);

          LOGGER.debug("Data fetch failed for: "+device);
        }
      });
    }
    catch (Exception exception)
    {
      promise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(),exception.getCause());
    }
    return promise.future();
  }
}
