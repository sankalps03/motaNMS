package com.example.MotaNMS.Services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class serviceVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(serviceVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise){

    try{

    eventBus = getVertx().eventBus();

    discovery discovery = new discovery();

    discovery.setEventBus(getVertx());

    eventBus.localConsumer("discoveryAdd").handler(discovery::add);

    eventBus.localConsumer("discoveryDelete").handler(discovery::delete);

    eventBus.localConsumer("discoveryUpdate").handler(discovery::update);

    eventBus.localConsumer("discoveryRun").handler(discovery::run);

    eventBus.localConsumer("discoveryProvision").handler(discovery::provision);

    eventBus.localConsumer("discoveryLoad").handler(discovery::load);

  }catch (Exception exception){

      logger.error(exception.getMessage());

    }

    }
}
