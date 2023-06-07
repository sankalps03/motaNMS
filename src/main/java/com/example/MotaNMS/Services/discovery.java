package com.example.MotaNMS.Services;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;


public class discovery {

  EventBus eventBus;

  public void ebus(Vertx vertx){
     eventBus = vertx.eventBus();
  }



  private final Logger logger = LoggerFactory.getLogger(discovery.class);
  protected void add(Message<Object> message) {

    JsonObject addData = (JsonObject) message.body();

    String credentials = null;

    String ip = addData.getString("ip");

    String type = addData.getString("type");

    if(type.equals("ssh") || type.equals("snmp")){

      credentials= addData.getString("credentials");

    }

    if(!ip.isEmpty() && !type.isEmpty() && ((type.equals("ssh") || type.equals("snmp")) && !credentials.isEmpty() )){

      JsonObject checkedData = new JsonObject();

      checkedData.put("ip",ip).put("type",type).put("credentials",credentials).put("table","discovery");


      eventBus.request("addToDiscovery",checkedData,reply -> {

        if (reply.succeeded()){

          message.reply("");

        }else {
          message.fail(2,"insert failed");

          logger.error("insert failed");
        }
      });

    }else {

      message.fail(2,"Bad Data");

      logger.error("Incomplete or Bad data");
    }
  }

  protected void delete(Message<Object> message) {

    JsonObject deleteData = (JsonObject) message.body();

    String id = deleteData.getString("id");

    int idd = Integer.valueOf(id);

    System.out.println("idd" + idd);

    if (!id.isEmpty()){

      eventBus.request("deleteFromDiscovery",new JsonObject().put("id",idd),reply->{

        if (reply.succeeded()){

          message.reply("row deleted");

        }else {

          message.fail(2,"");

          logger.error("reply.result().toString()");

        }
      });
    }
    else {

      message.fail(2,"No row id to delete");
    }

  }

  protected void update(Message<Object> message) {

    JsonObject updateData = (JsonObject) message.body();

    String credentials = null;

    String ip = updateData.getString("ip");

    String type = updateData.getString("type");

    if(type.equals("ssh") || type.equals("snmp")){

      credentials= updateData.getString("credentials");

    }

  }

  protected void run(Message<Object> message) {
  }

  protected void provision(Message<Object> message) {}


  protected void load(Message<Object> message) {
    try {
    eventBus.request("loadDiscovery",new JsonObject().put("table","discovery"),reply ->{

      if(reply.succeeded()){

        JsonArray discoveryData = (JsonArray) message.body();

        message.reply(discoveryData);

        System.out.println(discoveryData);

      }else {

        message.fail(reply.result().hashCode(),reply.result().toString());

        logger.error(reply.result().toString());
      }

    });
  }catch (Exception exception){

    message.fail(2,exception.getMessage());

    logger.error(exception.getMessage());}
}
}
