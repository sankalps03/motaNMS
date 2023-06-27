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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Monitor {
  EventBus eventBus;

  Vertx vertx;

  private final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

  protected void init(Vertx vertxInstance) {

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

        } else {
          message.fail(2, "Monitor table Data load failed");

          LOGGER.debug("Monitor table data load failed");
        }

      });
    } catch (Exception exception) {

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

          } else {
            message.fail(2, "Monitor table row deletion failed : row id " + id);

            LOGGER.debug("row delete failed : row id " + id);
          }
        });
      } else {
        message.fail(2, "No row id to delete ");

        LOGGER.debug("No row id to delete");
      }
    } catch (Exception exception) {
      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void loadDeviceData(Message<Object> message) {

    try {
      if (message.body() == null) {

        throw new Exception("Message body is null");
      }

      List<JsonArray> deviceInfoData = new ArrayList<>();

      JsonObject deviceIp = (JsonObject) message.body();

      if (deviceIp.getString("type").equals("ssh")) {

        CompositeFuture.join(getDeviceInfoData(deviceIp.put("query", SSH_LATEST_DATA_QUERY)),
            getDeviceInfoData(deviceIp.put("query", LAST_24_HOUR_AVAILABILITY_QUERY)),
            getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_CPU_USED_QUERY)),
            getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_MEMORY_USED_QUERY)),
            getDeviceInfoData(deviceIp.put("query", LAST_1_HOUR_DISK_USED_QUERY)),
            getNetworkInterfaceData(deviceIp))
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
      } else {
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
    } catch (Exception exception) {
      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  private Future<JsonArray> getDeviceInfoData(JsonObject device) {

    Promise<JsonArray> promise = Promise.promise();

    try {
      eventBus.request(SELECT, device, messageAsyncResult -> {

        if (messageAsyncResult.succeeded()) {

          String deviceInfo = messageAsyncResult.result().body().toString();

          JsonArray deviceInfoArray = new JsonArray(deviceInfo);

          promise.complete(deviceInfoArray);

          LOGGER.debug("Data fetch successful for :" + device);
        } else {
          promise.fail("Data fetch failed for: " + device);

          LOGGER.debug("Data fetch failed for: " + device);
        }
      });
    } catch (Exception exception) {
      promise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
    return promise.future();
  }

  private Future<JsonArray> getNetworkInterfaceData(JsonObject deviceInfo) {

    Promise<JsonArray> promise = Promise.promise();

    try {

      getDeviceInfoData(deviceInfo.put("query", LAST_TWO_ENTRIES_OF_NETWORK_INTERFACES))
        .onComplete(handler -> {

          if (handler.succeeded()) {
            String wirelessTransmittedBPS;

            String wirelessReceivedBPS;

            String wirelessTotalBPS;

            String loopbackTransmittedBPS;

            String loopbackReceivedBPS;

            String loopbackTotalBPS;

            String ethernetTransmittedBPS;

            String ethernetReceivedBPS;

            String ethernetTotalBPS;

            if (handler.result() != null) {

              JsonArray networkInterfaceArray = handler.result();

              JsonArray processedNetworkInterfaceArray = new JsonArray();

              JsonObject lastData = networkInterfaceArray.getJsonObject(0);

              JsonObject secondLastData = new JsonObject();

              if (networkInterfaceArray.size()>=2){

                secondLastData = networkInterfaceArray.getJsonObject(1);

              }

              if (lastData.getString("uptime") != null) {

                if (secondLastData.getString("uptime") == null) {

                  float lastUptime = Float.parseFloat(lastData.getString("uptime"));

                  wirelessTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.transmitted.bytes")), 0, lastUptime, 0);

                  wirelessReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.received.bytes")), 0, lastUptime, 0);

                  wirelessTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.total.bytes")), 0, lastUptime, 0);

                  loopbackTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.transmitted.bytes")), 0, lastUptime, 0);

                  loopbackReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.received.bytes")), 0, lastUptime, 0);

                  loopbackTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.total.bytes")), 0, lastUptime, 0);

                  ethernetTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.transmitted.bytes")), 0, lastUptime, 0);

                  ethernetReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.received.bytes")), 0, lastUptime, 0);

                  ethernetTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.total.bytes")), 0, lastUptime, 0);

                } else {

                  float lastUptime = Float.parseFloat(lastData.getString("uptime"));

                  float secondLastUptime = Float.parseFloat(secondLastData.getString("uptime"));

                  wirelessTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.transmitted.bytes")), Long.parseLong(secondLastData.getString("wireless.transmitted.bytes")), lastUptime, secondLastUptime);

                  wirelessReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.received.bytes")), Long.parseLong(secondLastData.getString("wireless.received.bytes")), lastUptime, secondLastUptime);

                  wirelessTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("wireless.total.bytes")), Long.parseLong(secondLastData.getString("wireless.total.bytes")), lastUptime, secondLastUptime);

                  loopbackTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.transmitted.bytes")), Long.parseLong(secondLastData.getString("loopback.transmitted.bytes")), lastUptime, secondLastUptime);

                  loopbackReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.received.bytes")), Long.parseLong(secondLastData.getString("loopback.received.bytes")), lastUptime, secondLastUptime);

                  loopbackTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("loopback.total.bytes")), Long.parseLong(secondLastData.getString("loopback.total.bytes")), lastUptime, secondLastUptime);

                  ethernetTransmittedBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.transmitted.bytes")), Long.parseLong(secondLastData.getString("ethernet.transmitted.bytes")), lastUptime, secondLastUptime);

                  ethernetReceivedBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.received.bytes")), Long.parseLong(secondLastData.getString("ethernet.received.bytes")), lastUptime, secondLastUptime);

                  ethernetTotalBPS = bytesPerSecond(Long.parseLong(lastData.getString("ethernet.total.bytes")), Long.parseLong(secondLastData.getString("ethernet.total.bytes")), lastUptime, secondLastUptime);
                }

                lastData.put("wireless.TransmittedBPS", wirelessTransmittedBPS)
                  .put("wireless.ReceivedBPS", wirelessReceivedBPS)
                  .put("wireless.TotalBPS", wirelessTotalBPS)
                  .put("loopback.TransmittedBPS", loopbackTransmittedBPS)
                  .put("loopback.ReceivedBPS", loopbackReceivedBPS)
                  .put("loopback.TotalBPS", loopbackTotalBPS)
                  .put("ethernet.TransmittedBPS", ethernetTransmittedBPS)
                  .put("ethernet.ReceivedBPS", ethernetReceivedBPS)
                  .put("ethernet.TotalBPS", ethernetTotalBPS);

              }
              processedNetworkInterfaceArray.add(lastData);

              promise.complete(processedNetworkInterfaceArray);
            }
          } else
          {
            promise.fail(handler.cause());
          }
        });
    } catch (Exception exception)
    {

      promise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
    return promise.future();
  }

  private String bytesPerSecond(long lastBytes, long secondLastBytes, float lastUptime, float secondLastUptime) {

    String bPS;

    if (lastBytes == 0 || lastUptime == 0) {

      return "0";
    }

    if ((lastBytes >= secondLastBytes) && (lastUptime > secondLastUptime)) {

      bPS = new DecimalFormat("#.##").format((lastBytes - secondLastBytes) / (lastUptime - secondLastUptime));

    } else {

      bPS = new DecimalFormat("#.##").format(lastBytes / lastUptime);

    }

    return bPS;
  }
}
