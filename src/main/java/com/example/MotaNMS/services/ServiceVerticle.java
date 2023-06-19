package com.example.MotaNMS.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.MotaNMS.util.GeneralConstants.*;

public class ServiceVerticle extends AbstractVerticle
{

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise)
  {

    try
    {

      eventBus = getVertx().eventBus();

      Discovery discovery = new Discovery();

      Monitor monitor = new Monitor();

      Dashboard dashboard = new Dashboard();

      discovery.init(getVertx());

      monitor.init(getVertx());

      dashboard.init(getVertx());

      eventBus.localConsumer(ADD_TO_DISCOVERY).handler(discovery::add);

      eventBus.localConsumer(DELETE_FROM_DISCOVERY).handler(discovery::delete);

      eventBus.localConsumer(RUN_DISCOVERY).handler(discovery::run);

      eventBus.localConsumer(PROVISION_DEVICE).handler(discovery::provision);

      eventBus.localConsumer(LOAD_DISCOVERY_TABLE).handler(discovery::load);

      eventBus.localConsumer(LOAD_MONITOR_TABLE).handler(monitor::load);

      eventBus.localConsumer(DELETE_FROM_MONITOR).handler(monitor::delete);

      eventBus.localConsumer(LOAD_MONITOR_DEVICE_DATA).handler(monitor::loadDeviceData);

      eventBus.localConsumer(LOAD_DASHBOARD_DATA,handler -> dashboard.getDashboardData());

      startPromise.complete();

      LOGGER.info("service verticle successfully deployed ");

    }
    catch (Exception exception)
    {
      startPromise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }
}
