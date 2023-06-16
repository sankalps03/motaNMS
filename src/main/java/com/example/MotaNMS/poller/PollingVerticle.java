package com.example.MotaNMS.poller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Objects;
import static com.example.MotaNMS.util.GeneralConstants.*;
import static com.example.MotaNMS.util.QueryConstants.*;
import static com.example.MotaNMS.util.Utilities.*;

public class PollingVerticle extends AbstractVerticle {

  EventBus eventBus;

  private static final Logger LOGGER = LoggerFactory.getLogger(PollingVerticle.class);

  public void start(Promise<Void> startPromise)
  {
    try
    {
      eventBus = vertx.eventBus();

      long pingTimerId = vertx.setPeriodic(2 * 60 * 1000, pingPoll -> pollData("ping"));

      if (pingTimerId == -1)
      {
        throw new Exception("Set periodic startup for ping failed id : " + pingTimerId);
      }

      long sshTimerId = vertx.setPeriodic(5 * 60 * 1000, pingPoll -> pollData("ssh"));

      if (sshTimerId == -1)
      {
        throw new Exception("Set periodic startup for ssh failed id : " + sshTimerId);
      }

      startPromise.complete();

      LOGGER.info("polling verticle successfully deployed");

    }
    catch (Exception exception)
    {
      startPromise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  private void pollData(String category)
  {
    try
    {
    if (category.equals("ssh"))
    {
      Objects.requireNonNull(getDevices(SELECT_SSH_DEVICES_QUERY)).onSuccess(result ->
      {
        if (result != null) {

          JsonArray deviceDataArray = new JsonArray();

          JsonArray credentialArray = new JsonArray(result);

          for (Object Credentialobject : credentialArray) {
            JsonObject dataObject = (JsonObject) Credentialobject;

            String credential = dataObject.getString("CREDENTIAL");

            JsonObject deviceData = new JsonObject(credential);

            deviceData.put("type", dataObject.getValue("TYPE"))
              .put("ip", dataObject.getValue("IPADDRESS")).put("category", "polling");

            deviceDataArray.add(deviceData);
          }
          vertx.executeBlocking(pollSsh ->
          {
            JsonArray pollData = runPluginPolling(deviceDataArray);

            insertPollData(pollData);
          });
        }else
        {
          LOGGER.error("polling for ssh failed null device list : "+ LocalDateTime.now());
        }
      });
    }
    else
    {
      Objects.requireNonNull(getDevices(SELECT_PING_DEVICES_QUERY)).onSuccess(result ->
      {
        if (result != null) {

          JsonArray deviceDataArray = new JsonArray();

          JsonArray credentialArray = new JsonArray(result);

          for (Object credentialObject : credentialArray) {

            JsonObject dataObject = (JsonObject) credentialObject;

            JsonObject deviceData = new JsonObject();

            deviceData.put("type", "ping")
              .put("ip", dataObject.getValue("IPADDRESS")).put("category", "polling");
            deviceDataArray.add(deviceData);
          }
          vertx.executeBlocking(pollPing ->
          {
            JsonArray pollData = runPluginPolling(deviceDataArray);

            insertPollData(pollData);
          });
        }else
        {
          LOGGER.error("polling for ping failed null device list : " + LocalDateTime.now());
        }
      });
    }
  }
    catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  private Future<String> getDevices(String query)
  {
    Promise<String> promise = null;
    try
    {
      promise = Promise.promise();

      Promise<String> finalPromise = promise;

      eventBus.request(SELECT, new JsonObject().put("query", query), messageAsyncResult ->
      {
        if (messageAsyncResult.succeeded())
        {
          String credentials = messageAsyncResult.result().body().toString();

          finalPromise.complete(credentials);
        }
        else
        {
          finalPromise.fail("Device list fetch failed");

          LOGGER.error("Device list fetch failed " + LocalDateTime.now());
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
    }else
    {
      return null;
    }
  }

  private void insertPollData(JsonArray pollData)
  {
    try
    {
    eventBus.request(INSERT, pollData, reply ->
    {
      if (reply.succeeded())
      {
        LOGGER.debug("Polling Data Inserted Successfully :"+ LocalDateTime.now());

        eventBus.send(UPDATE, new JsonObject().put("query", UPDATE_AVAILABILITY_STATUS_QUERY));
      }
      else
      {
        LOGGER.error("Polling data insert failed : "+ LocalDateTime.now());
      }
    });

  }catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }
}
