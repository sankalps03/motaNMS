package com.example.MotaNMS.publicAPI;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.MotaNMS.util.GeneralConstants.*;

public class ProcessRoutingRequest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRoutingRequest.class);

  EventBus eventBus;

  Vertx vertx;

  protected void setEventBus(Vertx vertx1)
  {

    try
    {
      vertx = vertx1;

      eventBus = vertx.eventBus();

    } catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(), exception.getCause());
    }

  }

  protected void loadDashboardData(RoutingContext context)
  {

    try
    {
    eventBus.send(LOAD_DASHBOARD_DATA, "dashBoard");

    LOGGER.debug("Request received for : "+ context.normalizedPath());

  }catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void loadMonitorDeviceData(RoutingContext context) {

    try
    {
      LOGGER.debug("Request received for : "+ context.normalizedPath());

      if (context.body() == null) {

        throw new Exception("Received null context body");

      }
      JsonObject data = context.body().asJsonObject();

      eventBus.request(LOAD_MONITOR_DEVICE_DATA, data, reply ->
      {
        if (reply.succeeded())
        {
          String monitorDeviceData = reply.result().body().toString();

          context.response().end(monitorDeviceData);

        } else
        {
          sendFailResponse(context,500);
        }
      });
    } catch (Exception exception)
    {

      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void deleteFromMonitor(RoutingContext context)
  {
    try
    {
      LOGGER.debug("Request received for : "+ context.normalizedPath());

      requestEventBusCRUD(context, DELETE_FROM_MONITOR);

    } catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(), exception.getCause());
    }
  }

  protected void loadMonitorTable(RoutingContext context)
  {
    try {

      LOGGER.debug("Request received for : "+ context.normalizedPath());

    eventBus.request(LOAD_MONITOR_TABLE, "monitorData", reply ->
    {
      if (reply.succeeded())
      {
        String discoveryData = reply.result().body().toString();

        context.response().end(discoveryData);

        LOGGER.debug("Response sent for : " + context.normalizedPath());
      }
      else
      {
        sendFailResponse(context,500);
      }
    });

  }catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void loadDiscoveryTable(RoutingContext routingContext)
  {
    try {

      LOGGER.debug("Request received for : "+ routingContext.normalizedPath());

    eventBus.request(LOAD_DISCOVERY_TABLE, "discoveryData", reply -> {

      if (reply.succeeded())
      {
        String discoveryData = reply.result().body().toString();

        routingContext.response().end(discoveryData);

        LOGGER.debug("Response sent for : " + routingContext.normalizedPath());
      }
      else
      {
        sendFailResponse(routingContext,500);
      }
    });

  }catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void provisionDevice(RoutingContext routingContext)
  {

    try
    {

      LOGGER.debug("Request received for : "+ routingContext.normalizedPath());

      requestEventBusCRUD(routingContext, PROVISION_DEVICE);

  }
    catch (Exception exception){

      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void discoveryRun(RoutingContext routingContext)
  {
    try {

    LOGGER.debug("Request received for : "+ routingContext.normalizedPath());

    if (routingContext.body() == null)
    {
      throw new Exception("Received null context body");
    }

    JsonObject discoveryRunData = routingContext.body().asJsonObject();

    eventBus.request(RUN_DISCOVERY, discoveryRunData, reply ->
    {
      if (reply.succeeded())
      {
        sendStatusCode(routingContext, 200);
      } else
      {
        sendFailResponse(routingContext,500);
      }
    });
  }
    catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void discoveryDelete(RoutingContext routingContext) {

    try
    {
    LOGGER.debug("Request received for : "+ routingContext.normalizedPath());

    requestEventBusCRUD(routingContext, DELETE_FROM_DISCOVERY);

  }
    catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  protected void addToDiscovery(RoutingContext routingContext)
  {
    try {

    LOGGER.debug("Request received for : "+ routingContext.normalizedPath());

    requestEventBusCRUD(routingContext, ADD_TO_DISCOVERY);

  }catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  private void requestEventBusCRUD(RoutingContext context, String address)
  {
    try
    {
      if (context.body() == null)
      {
        throw new Exception("Received null context body");
      }

      JsonObject data = context.body().asJsonObject();

      eventBus.request(address, data, reply ->
      {

        if (reply.succeeded())
        {

          sendStatusCode(context, 200);

        } else
        {
          sendFailResponse(context,400);
        }

      });

    }
    catch (Exception exception)
    {
      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }

  private void sendStatusCode(RoutingContext context, int statusCode)
  {
    context.response().setStatusCode(statusCode).end();

    LOGGER.debug("Response sent for : " + context.normalizedPath() + "status code "+ statusCode);
  }

  private void sendFailResponse(RoutingContext context, int statusCode)
  {
    context.fail(statusCode);

    LOGGER.debug("Context fail sent for : " + context.normalizedPath());
  }

}
