package com.example.MotaNMS.poller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.MotaNMS.util.GeneralConstants.*;
import static com.example.MotaNMS.util.QueryConstants.*;
import static com.example.MotaNMS.util.Utilities.*;
public class PollingVerticle extends AbstractVerticle {

  EventBus eventBus;

  private static final Logger logger = LoggerFactory.getLogger(PollingVerticle.class);

  public void start(Promise<Void> startPromise){

    eventBus  = vertx.eventBus();

    vertx.setPeriodic(2*60*1000 , pingPoll ->{

      pollData("ping");

    });

    vertx.setPeriodic(5*60*1000 , pingPoll ->{

      pollData("ssh");

    });

    startPromise.complete();
  }

  private void pollData(String category) {


    if (category.equals("ssh")){

      getSshDevices().onSuccess(result->{

        String credentials = result.toString();

        JsonArray deviceDataArray = new JsonArray();

        JsonArray credentialArray = new JsonArray(credentials);

        for (Object object:credentialArray) {

          JsonObject dataObject = (JsonObject) object;

          String credential =  dataObject.getString("CREDENTIAL");

          JsonObject deviceData = new JsonObject(credential);

          deviceData.put("type", dataObject.getValue("TYPE"))
            .put("ip", dataObject.getValue("IPADDRESS")).put("category","polling");

          deviceDataArray.add(deviceData);

        }

        vertx.executeBlocking(pollSsh ->{

          JsonArray pollData = runPluginPolling(deviceDataArray);

          insertPollData(pollData);
        });

      });

    }else {

      getPingDevices().onSuccess(result->{

        String credentials = result.toString();

        JsonArray deviceDataArray = new JsonArray();

        JsonArray credentialArray = new JsonArray(credentials);

        for (Object object:credentialArray) {

          JsonObject dataObject = (JsonObject) object;

          JsonObject deviceData = new JsonObject();

          deviceData.put("type", "ping")
            .put("ip", dataObject.getValue("IPADDRESS")).put("category","polling");
          deviceDataArray.add(deviceData);

        }

        vertx.executeBlocking(pollPing ->{

          JsonArray pollData = runPluginPolling(deviceDataArray);

          insertPollData(pollData);

        });

      });
    }

  }

  private Future<String> getSshDevices() {

    Promise<String> promise = Promise.promise();

    String address = SELECT;

    String query = SELECT_SSH_DEVICES_QUERY;

    getDevices(address,query).onComplete(reslt ->{

      if (reslt.succeeded()){

        String credentials = reslt.result().toString();

        promise.complete(credentials);
      }
      else {

        logger.error("couldnt get devices data");
      }
    });
    return promise.future();
  }

  private Future<String> getPingDevices() {

    Promise<String> promise = Promise.promise();

    String address = SELECT;

    String query = SELECT_PING_DEVICES_QUERY;

    getDevices(address,query).onComplete(reslt ->{

      if (reslt.succeeded()){

        String credentials = reslt.result().toString();

        promise.complete(credentials);
      }
      else {

        logger.error("couldn't get devices data");
      }
    });

    return promise.future();
  }

  private Future<String> getDevices(String address, String query) {

    Promise<String> promise = Promise.promise();

    eventBus.request(address, new JsonObject().put("query", query), messageAsyncResult -> {

      if (messageAsyncResult.succeeded()) {

        String credentials = messageAsyncResult.result().body().toString();

        promise.complete(credentials);


      } else {

        logger.error("cant get device data");

      }

    });
    return promise.future();
  }

  private void insertPollData(JsonArray pollData){

    eventBus.request(INSERT,pollData,reply ->
    {

      if (reply.succeeded()){

        logger.info("Polling Data Inserted Successfully");

        eventBus.send(UPDATE,new JsonObject().put("query", UPDATE_AVAILABILITY_STATUS_QUERY));
      }else {

        logger.error("Polling data insert failed");
      }


    });



  }
}
