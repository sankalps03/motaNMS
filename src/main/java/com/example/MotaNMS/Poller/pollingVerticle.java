package com.example.MotaNMS.Poller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.MotaNMS.Services.sqlQueries;

import static com.example.MotaNMS.Util.Utilities.*;

public class pollingVerticle extends AbstractVerticle {

  EventBus eventBus;

  private static final Logger logger = LoggerFactory.getLogger(pollingVerticle.class);

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

        JsonArray credentialArray = new JsonArray(credentials);

        vertx.executeBlocking(pollSsh ->{

          JsonArray pollData = runPluginPolling(credentialArray,new JsonObject().put("category","polling").put("type","ping"));

          insertPollData(pollData);
        });

      });

    }else {

      getPingDevices().onSuccess(result->{

        String credentials = result.toString();

        JsonArray credentialArray = new JsonArray(credentials);

        vertx.executeBlocking(pollSsh ->{

          JsonArray pollData = runPluginPolling(credentialArray,new JsonObject().put("category","polling").put("type","ssh"));

          insertPollData(pollData);

        });

      });
    }

  }

  private Future<String> getSshDevices() {

    Promise<String> promise = Promise.promise();

    String address = "getAllSshDevices";

    String query = sqlQueries.selectSshDevices();

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

    String address = "getAllPingDevices";

    String query = sqlQueries.selectPingDevices();


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

  private Future<String> getDevices(String address, String query) {

    Promise<String> promise = Promise.promise();

    eventBus.request(address, new JsonObject().put("query", query), messageAsyncResult -> {

      if (messageAsyncResult.succeeded()) {

        String credentials = messageAsyncResult.result().body().toString();

        System.out.println(credentials);

        promise.complete(credentials);


      } else {

        logger.error("cant get device data");

      }

    });
    return promise.future();
  }

  private void insertPollData(JsonArray pollData){



  }
}
