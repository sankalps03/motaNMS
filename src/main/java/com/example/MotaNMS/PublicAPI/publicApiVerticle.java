package com.example.MotaNMS.PublicAPI;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class publicApiVerticle extends AbstractVerticle {

  private static final int HTTP_PORT = 8080;

  private static final Logger logger = LoggerFactory.getLogger(publicApiVerticle.class);

  EventBus eventBus;

  public void start(Promise<Void> startPromise){

    try{

      Router router = Router.router(vertx);

      eventBus = getVertx().eventBus();

      BodyHandler bodyHandler = BodyHandler.create();

      router.post().handler(bodyHandler);

      Route handler = router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

      PropertyFileAuthentication authenticate = PropertyFileAuthentication.create(vertx,"./user.properties");

      router.route("/api/*").handler(RedirectAuthHandler.create(authenticate,"login.html"));

      router.route("/loginHandler").handler(FormLoginHandler.create(authenticate)).failureHandler(context -> context.reroute("/api"));

      router.route("/api/*").handler(StaticHandler.create().setCachingEnabled(false).setIndexPage("motaNMS.html"));

      router.route("/logout").handler(context -> {

        context.clearUser();

        context.reroute("/api");
      });

      SockJSHandler jsHandler = SockJSHandler.create(vertx);

      SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions()
        .addInboundPermitted(new PermittedOptions().setAddressRegex("updates.*"))
        .addOutboundPermitted(new PermittedOptions().setAddressRegex("updates.*"));

      router.mountSubRouter("/eventbus",jsHandler.bridge(bridgeOptions));

      router.route(HttpMethod.POST,"/api/discovery/add").handler(this::discoveryAdd);

      router.route(HttpMethod.POST,"/api/discovery/delete").handler(this::discoveryDelete);

      router.route(HttpMethod.POST,"/api/discovery/update").handler(this::discoveryUpdate);

      router.route(HttpMethod.POST,"/api/discovery/run").handler(this::discoveryRun);

      router.route(HttpMethod.POST,"/api/discovery/provision").handler(this::discoveryProvision);

      router.route(HttpMethod.GET,"/api/discovery/load").handler(this::discoveryLoad);

      router.route(HttpMethod.GET,"/api/monitor/load").handler(this::monitorLoad);

      router.route(HttpMethod.POST,"/api/monitor/delete").handler(this::monitorDelete);

      router.route(HttpMethod.POST,"/api/monitor/device").handler(this::monitorDevice);

      router.route().handler(StaticHandler.create().setCachingEnabled(false));

      vertx.createHttpServer().requestHandler(router).listen(HTTP_PORT).onSuccess(done -> logger.info("Listening on port: "+ HTTP_PORT));

      startPromise.complete();

    }catch(Exception exception){

      logger.error(exception.getMessage());
    }
  }

  private void monitorDevice(RoutingContext context) {

    logger.info("monitor device request");

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

  private void monitorDelete(RoutingContext context) {

    logger.info("Monitor delete Request");

    String address ="monitorDelete";

    requestEventBusCRUD(context,address);

  }

  private void monitorLoad(RoutingContext context) {

    logger.info("monitor data request");

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

  private void discoveryLoad(RoutingContext routingContext) {

    logger.info("Discovery data request");

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

  private void discoveryProvision(RoutingContext routingContext) {

    logger.info("provision Request received");

    String address ="discoveryProvision";

    requestEventBusCRUD(routingContext,address);
  }

  private void discoveryRun(RoutingContext routingContext) {

    logger.info("Run request received");

    JsonObject discoveryAddData = routingContext.body().asJsonObject();

    eventBus.request("discoveryRun",discoveryAddData,reply ->{

      if (reply.succeeded()){

        sendStatusCode(routingContext,200);
      }else {

        sendStatusCode(routingContext,500);
      }

    });
  }

  private void discoveryUpdate(RoutingContext routingContext) {

    String address ="discoveryUpdate";

    requestEventBusCRUD(routingContext,address);
  }

  private void discoveryDelete(RoutingContext routingContext) {

    logger.info("Discovery delete Request");

    String address ="discoveryDelete";

    requestEventBusCRUD(routingContext,address);

  }

  private void discoveryAdd(RoutingContext routingContext) {

    logger.info("Discovery add Request");

    String address ="discoveryAdd";

    requestEventBusCRUD(routingContext,address);


  }

  private void requestEventBusCRUD(RoutingContext context, String address){

    JsonObject data = context.body().asJsonObject();

    eventBus.request(address,data,reply ->{

      if (reply.succeeded()){

        sendStatusCode(context,200);

      }else {

        sendBadRequest(context);
      }

    });

  }

  private void forwardJson(RoutingContext ctx, JsonObject resp) {

    ctx.response()
      .putHeader("Content-Type", "application/json")
      .end(resp.encode());

  }

  private void sendStatusCode(RoutingContext context, int statusCode ){

    context.response().setStatusCode(statusCode).end();

    logger.info("response status code", statusCode);

  }

  private void sendBadRequest(RoutingContext context){

    context.fail(400);

    logger.error("Bad request status code 400");
  }

}
