package com.example.MotaNMS.Services;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class monitor {
  EventBus eventBus;

  Vertx vertx;

  private final Logger logger = LoggerFactory.getLogger(monitor.class);

  protected void setEventBus(Vertx vertx1) {
    vertx = vertx1;
    eventBus = vertx.eventBus();

  }

  protected void load(Message<Object> message) {

    try {

      String query = sqlQueries.selectMonitor();

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

    String query = sqlQueries.deleteMonitor();

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
}
