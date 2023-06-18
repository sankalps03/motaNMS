package com.example.MotaNMS.services;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.MotaNMS.util.GeneralConstants.*;
import static com.example.MotaNMS.util.Utilities.*;
import static com.example.MotaNMS.util.QueryConstants.*;


public class Discovery {

  EventBus eventBus;

  Vertx vertx;

  protected void setEventBus(Vertx vertxx) {

    vertx = vertxx;

    eventBus = vertx.eventBus();
  }

  private final Logger LOGGER = LoggerFactory.getLogger(Discovery.class);

  protected void add(Message<Object> message) {

    try {

      if (message.body() == null) {

        throw new Exception("Message body is null");
      }

      JsonObject addData = (JsonObject) message.body();

      JsonObject credentials = new JsonObject();

      String username = null;

      String password = null;

      String ip = addData.getString("ip");

      String type = addData.getString("type");

      if (type.equals("ssh"))
      {
        username = addData.getString("username");

        password = addData.getString("password");
      }

      if (!ip.isEmpty() && !type.isEmpty() && (((type.equals("ssh")) && (!username.isEmpty() && !password.isEmpty())) || (type.equals("ping"))))
      {
        JsonObject checkedData = new JsonObject();

        credentials.put("username", username)
          .put("password", password)
          .put("port", "22");

        checkedData.put("ip", ip).put("type", type)
          .put("credentials", credentials)
          .put("query", DISCOVERY_INSERT_QUERY);


        eventBus.request(INSERT, checkedData, reply ->
        {

          if (reply.succeeded()) {

            message.reply("Added to discovery table : " + ip);

            LOGGER.debug("Added to discovery table : " + ip);

          } else {
            message.fail(2, reply.cause().getMessage() + ip);

            LOGGER.debug("insert failed for : " + ip);
          }
        });

      }
      else
      {
        message.fail(2, "Bad data received to add discovery  :" + ip);

        LOGGER.debug("Incomplete or Bad data to add to discovery for : " + ip);
      }
    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());

    }
  }

  protected void delete(Message<Object> message)
  {
    try {

      if (message.body() == null) {

        throw new Exception("Message body is null");
      }

      JsonObject deleteData = (JsonObject) message.body();

      String id = deleteData.getString("id");

      if (!id.isEmpty()) {

        eventBus.request(ROW_ID_OPERATION, new JsonObject().put("id", id).put("query", DISCOVERY_DELETE_QUERY), reply ->
        {

          if (reply.succeeded()) {

            message.reply("row deleted succesfuly: row id " + id);

            LOGGER.debug("Discovery row deleted Successfully : row id " + id);

          } else {

            message.fail(2, reply.cause().getMessage());

            LOGGER.debug("row delete failed");

          }
        });
      }
      else
      {
        message.fail(2, "No row id to delete");
      }

    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }


  protected void run(Message<Object> message) {

    try
    {
      if (message.body() == null)
      {

        throw new Exception("Message body is null");
      }

      JsonObject run = (JsonObject) message.body();

      String id = run.getString("id");

      eventBus.request(SELECT, new JsonObject().put("query", DISCOVERY_RUN_SELECT_QUERY).put("id", id), reply ->
      {

        if (reply.succeeded())
        {
          JsonArray credentialArray = new JsonArray();

          JsonArray runData = (JsonArray) reply.result().body();

          JsonObject runDataObject = runData.getJsonObject(0);

          String credential = runDataObject.getString("CREDENTIAL");

          JsonObject credentialObject = new JsonObject(credential);

          credentialObject.put("type", runDataObject.getValue("TYPE"))
            .put("ip", runDataObject.getValue("IPADDRESS"))
            .put("category", "discovery");

          credentialArray.add(credentialObject);

          vertx.executeBlocking(discover ->
          {

            JsonArray resultArray = runPluginPolling(credentialArray);

            JsonObject result = resultArray.getJsonObject(0);

            if (result.getString("status").equals("successful"))
            {
              discover.complete();
            } else {
              discover.fail("unsuccessful");
            }

          }, false, handler ->
          {

            if (handler.succeeded()) {

              eventBus.send(ROW_ID_OPERATION, new JsonObject().put("id", id).put("query", SET_PROVISION_TRUE_QUERY));

              message.reply("Discovery success for : " + credentialObject.getString("ip"));

              LOGGER.debug("Discovery run successful for : " + credentialObject.getString("ip"));
            } else {

              message.fail(2, "Discovery failed for : " + credentialObject.getString("ip"));

              LOGGER.debug("Discovery failed for : " + credentialObject.getString("ip"));
            }
          });
        }
      });

    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void provision(Message<Object> message)
  {

    try
    {
      if (message.body() == null)
      {
        throw new Exception("Message body is null");
      }

      JsonObject run = (JsonObject) message.body();

      String id = run.getString("id");

      eventBus.request(ROW_ID_OPERATION, new JsonObject().put("query", PROVISION_QUERY).put("id", id),
        reply ->
        {
          if (reply.succeeded())
          {
            message.reply("Device provisoned sucessfully : row id "+ id);

            LOGGER.debug("Device provisiond sucessfully :row id " + id);
          }
          else
          {
            message.fail(2, reply.cause().getMessage()+ id);

            LOGGER.debug("provisioning failed for :row id " +id);
          }
        });
    }
    catch (Exception exception)
    {

      message.fail(2,exception.getMessage());

      LOGGER.error(exception.getMessage(), exception.getCause());

    }
  }


  protected void load(Message<Object> message)
  {
    try
    {
      eventBus.request(SELECT, new JsonObject().put("table", "discovery").put("query", DISCOVERY_SELECT_QUERY), reply ->
      {
        if (reply.succeeded())
        {
          String tableData = reply.result().body().toString();

          message.reply(tableData);

          LOGGER.debug("Discovery table loaded success");

        }
        else
        {
          message.fail(2, "Discovery table load failed ");

          LOGGER.debug("Discovery table load failed");
        }

      });
    } catch (Exception exception) {

      message.fail(2, exception.getMessage());

      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }
}
