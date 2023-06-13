package com.example.MotaNMS.Services;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.MotaNMS.Util.Utilities.runPlugin;
import static com.example.MotaNMS.Util.Utilities.runPluginPolling;


public class discovery {

  EventBus eventBus;

  Vertx vertx;

  protected void setEventBus(Vertx vertx1) {
    vertx = vertx1;
    eventBus = vertx.eventBus();

  }

  private final Logger logger = LoggerFactory.getLogger(discovery.class);

  protected void add(Message<Object> message) {

    JsonObject addData = (JsonObject) message.body();

    String query = sqlQueries.insertDiscovery();

    JsonObject credentials = new JsonObject();

    String username = null;

    String password = null;

    String ip = addData.getString("ip");

    String type = addData.getString("type");

    if (type.equals("ssh")) {

      username = addData.getString("username");

      password = addData.getString("password");

    }

    if (!ip.isEmpty() && !type.isEmpty() && (((type.equals("ssh")) && (!username.isEmpty() && !password.isEmpty())) || (type.equals("ping")))) {

      JsonObject checkedData = new JsonObject();

      credentials.put("username", username)
        .put("password", password)
        .put("port", "22");

      checkedData.put("ip", ip).put("type", type)
        .put("credentials", credentials)
        .put("query", query);


      eventBus.request("addToDiscovery", checkedData, reply -> {

        if (reply.succeeded()) {

          message.reply("");

          logger.info("Added to discovery table");

        } else {
          message.fail(2, "insert failed");

          logger.error("insert failed");
        }
      });

    } else {

      message.fail(2, "Bad Data");

      logger.error("Incomplete or Bad data");
    }
  }

  protected void delete(Message<Object> message) {

    JsonObject deleteData = (JsonObject) message.body();

    String query = sqlQueries.deleteDiscovery();

    String id = deleteData.getString("id");

    if (!id.isEmpty()) {

      eventBus.request("deleteFromDiscovery", new JsonObject().put("id", id).put("query", query), reply -> {

        if (reply.succeeded()) {

          message.reply("row deleted");

          logger.info("Discovery row deleted Successfully");

        } else {

          message.fail(2, "");

          logger.error("row delete failed");

        }
      });
    } else {

      message.fail(2, "No row id to delete");
    }

  }

  protected void update(Message<Object> message) {

    JsonObject updateData = (JsonObject) message.body();

    String credentials = null;

    String ip = updateData.getString("ip");

    String type = updateData.getString("type");

    if (type.equals("ssh") || type.equals("snmp")) {

      credentials = updateData.getString("credentials");

    }

  }

  protected void run(Message<Object> message) {

    try {

      JsonObject run = (JsonObject) message.body();

      String id = run.getString("id");

      System.out.println(id);

      eventBus.request("getCredentialsFromDiscovery", new JsonObject().put("query", sqlQueries.selectRunDiscovery()).put("id", id),
        reply -> {

          if (reply.succeeded()) {

            JsonArray runData = (JsonArray) reply.result().body();

            JsonObject runDataObject = runData.getJsonObject(0);

            String credential =  runDataObject.getString("CREDENTIAL");

            JsonObject credentialObject = new JsonObject(credential);

            credentialObject.put("type", runDataObject.getValue("TYPE"))
              .put("ip", runDataObject.getValue("IPADDRESS"))
              .put("category", "discovery");

            JsonArray credentialArray = new JsonArray();

            credentialArray.add(credentialObject);

            vertx.executeBlocking(discover->{

              JsonArray resultArray = runPluginPolling(credentialArray);

              JsonObject result = resultArray.getJsonObject(0);

              if(result.getString("STATUS").equals("SUCCESSFUL")){

                discover.complete();
              }
              else{

                discover.fail("unsuccessful");
              }

            },false,handler->{

              if(handler.succeeded()){

                eventBus.send("setProvisionTrue",new JsonObject().put("id",id).put("query",sqlQueries.setProvision()));

                message.reply("success");

                logger.info("Discovery run successful");
              }else {

                message.fail(2,"discovery failed");

                logger.error("Discovery failed");
              }

            });

          }

        });

    } catch (Exception exception) {

      logger.error(exception.getMessage());
    }
  }

  protected void provision(Message<Object> message) {

    JsonObject run = (JsonObject) message.body();

    String id = run.getString("id");

    System.out.println(id);

    eventBus.request("addToMonitor",new JsonObject().put("query",sqlQueries.provision()).put("id",id),
      reply ->{

      if(reply.succeeded()){

        message.reply("provisoned sucessfully");

        logger.info("provisiond sucessfully");
      }else {

        message.fail(2,"provisioning failed");

        logger.error("provisioning failed");
      }

      });


  }


  protected void load(Message<Object> message) {
    try {

      String query = sqlQueries.selectDiscovery();

      eventBus.request("loadDiscovery", new JsonObject().put("table", "discovery").put("query", query), reply -> {

        if (reply.succeeded()) {
          String data = reply.result().body().toString();

          message.reply(data);

          logger.info("Discovery data loaded success");

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
}
