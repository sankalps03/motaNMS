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

    discovery.ebus(getVertx());

    eventBus.consumer("discoveryAdd").handler(discovery::add);

    eventBus.consumer("discoveryDelete").handler(discovery::delete);

    eventBus.consumer("discoveryUpdate").handler(discovery::update);

    eventBus.consumer("discoveryRun").handler(discovery::run);

    eventBus.consumer("discoveryProvision").handler(discovery::provision);

    eventBus.consumer("discoveryLoad").handler(discovery::load);

  }catch (Exception exception){

      logger.error(exception.getMessage());

    }

    }
}
