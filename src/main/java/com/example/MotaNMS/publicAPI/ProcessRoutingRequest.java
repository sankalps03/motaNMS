package com.example.MotaNMS.publicAPI;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRoutingRequest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRoutingRequest.class);

  EventBus eventBus;

  Vertx vertx;
  protected void setEventBus(Vertx vertx1)
  {

    vertx = vertx1;

    eventBus = vertx.eventBus();

  }

  protected void loadDashboard(RoutingContext context) {

    LOGGER.info("Dashboard load Request");

    eventBus.send("dashboardLoad","dashBoard");

  }

  protected void monitorDevice(RoutingContext context) {

    LOGGER.info("monitor device request");

    JsonObject data = context.body().asJsonObject();

    eventBus.request("monitorLoadDevice",data,reply ->{

      if (reply.succeeded()){

        String discoveryData = reply.result().body().toString();

        context.response().end(discoveryData);

      }
      else {

        sendStatusCode(context,500);
      }
    });
  }

  protected void monitorDelete(RoutingContext context) {

    LOGGER.info("Monitor delete Request");

    String address ="monitorDelete";

    requestEventBusCRUD(context,address);

  }

  protected void monitorLoad(RoutingContext context) {

    LOGGER.info("monitor data request");

    eventBus.request("monitorLoad","monitorData",reply ->{

      if (reply.succeeded()){

        String discoveryData = reply.result().body().toString();

        context.response().end(discoveryData);

      }
      else {

        sendStatusCode(context,500);
      }
    });

  }

  protected void discoveryLoad(RoutingContext routingContext) {

    LOGGER.info("Discovery data request");

    eventBus.request("discoveryLoad","discoveryData",reply ->{

      if (reply.succeeded()){

        String discoveryData = reply.result().body().toString();

        routingContext.response().end(discoveryData);

      }
      else {

        sendStatusCode(routingContext,500);
      }
    });

  }

  protected void discoveryProvision(RoutingContext routingContext) {

    LOGGER.info("provision Request received");

    String address ="discoveryProvision";

    requestEventBusCRUD(routingContext,address);
  }

  protected void discoveryRun(RoutingContext routingContext) {

    LOGGER.info("Run request received");

    JsonObject discoveryAddData = routingContext.body().asJsonObject();

    eventBus.request("discoveryRun",discoveryAddData,reply ->{

      if (reply.succeeded()){

        sendStatusCode(routingContext,200);
      }else {

        sendStatusCode(routingContext,500);
      }

    });
  }

  protected void discoveryDelete(RoutingContext routingContext) {

    LOGGER.info("Discovery delete Request");

    String address ="discoveryDelete";

    requestEventBusCRUD(routingContext,address);

  }

  protected void discoveryAdd(RoutingContext routingContext) {

    LOGGER.info("Discovery add Request");

    String address ="discoveryAdd";

    requestEventBusCRUD(routingContext,address);


  }

  private void requestEventBusCRUD(RoutingContext context, String address){

    try {

      JsonObject data = context.body().asJsonObject();

      eventBus.request(address,data,reply ->{

        if (reply.succeeded()){

          sendStatusCode(context,200);

        }else {

          sendBadRequest(context);
        }

      });

    }catch (Exception exception){

      LOGGER.error(exception.getMessage());
    }
  }

  private void forwardJson(RoutingContext ctx, JsonObject resp) {

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(resp.encode());

  }

  private void sendStatusCode(RoutingContext context, int statusCode ){

    context.response().setStatusCode(statusCode).end();

    LOGGER.info("response status code" + statusCode);

  }

  private void sendBadRequest(RoutingContext context){

    context.fail(400);

    LOGGER.error("Bad request status code 400");
  }

}
