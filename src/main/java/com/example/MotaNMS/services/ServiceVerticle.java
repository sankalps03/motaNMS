package com.example.MotaNMS.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

      discovery.setEventBus(getVertx());

      monitor.setEventBus(getVertx());

      dashboard.setEventBus(getVertx());

      eventBus.localConsumer("discoveryAdd").handler(discovery::add);

      eventBus.localConsumer("discoveryDelete").handler(discovery::delete);

      eventBus.localConsumer("discoveryRun").handler(discovery::run);

      eventBus.localConsumer("discoveryProvision").handler(discovery::provision);

      eventBus.localConsumer("discoveryLoad").handler(discovery::load);

      eventBus.localConsumer("monitorLoad").handler(monitor::load);

      eventBus.localConsumer("monitorDelete").handler(monitor::delete);

      eventBus.localConsumer("monitorLoadDevice").handler(monitor::loadDeviceData);

      eventBus.localConsumer("dashboardLoad",handler -> dashboard.getDashboardData());

      long timerId = vertx.setPeriodic(5*60*1000, Handler -> dashboard.getDashboardData());

      if(timerId == -1)
      {
        throw new Exception("Set periodic startup for dashboard failed id : "+ timerId);
      }

      startPromise.complete();

      LOGGER.info("service verticle successfully deployed ");

    } catch (Exception exception)
    {

      startPromise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(),exception.getCause());

    }
  }
}
