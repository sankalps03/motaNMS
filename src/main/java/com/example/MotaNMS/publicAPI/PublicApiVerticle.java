package com.example.MotaNMS.publicAPI;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.example.MotaNMS.util.GeneralConstants.*;


public class PublicApiVerticle extends AbstractVerticle
{
  private static final Logger LOGGER = LoggerFactory.getLogger(PublicApiVerticle.class);

  public void start(Promise<Void> startPromise) {

    try
    {

      ProcessRoutingRequest process = new ProcessRoutingRequest();

      process.setEventBus(getVertx());

      Router router = Router.router(vertx);

      BodyHandler bodyHandler = BodyHandler.create();

      router.post().handler(bodyHandler);

      router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

      PropertyFileAuthentication authenticate = PropertyFileAuthentication.create(vertx, "./user.properties");

      router.route("/api/*").handler(RedirectAuthHandler.create(authenticate, "login.html"));

      router.route("/loginHandler").handler(FormLoginHandler.create(authenticate)).failureHandler(context -> context.reroute("/api"));

      router.route("/api/*").handler(StaticHandler.create().setCachingEnabled(false).setIndexPage("motaNMS.html"));

      router.route("/logout").handler(context ->
      {

        context.clearUser();


        context.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate")
          .putHeader("Pragma", "no-cache")
          .putHeader("Expires", "-1");

        context.reroute("/api");


      });

      SockJSHandler jsHandler = SockJSHandler.create(vertx);

      SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions()
        .addInboundPermitted(new PermittedOptions().setAddressRegex("updates.*"))
        .addOutboundPermitted(new PermittedOptions().setAddressRegex("updates.*"));

      router.mountSubRouter("/api/eventbus", jsHandler.bridge(bridgeOptions));

      router.route(HttpMethod.POST, "/api/discovery/add").handler(process::addToDiscovery);

      router.route(HttpMethod.POST, "/api/discovery/delete").handler(process::discoveryDelete);

      router.route(HttpMethod.POST, "/api/discovery/run").handler(process::discoveryRun);

      router.route(HttpMethod.POST, "/api/discovery/provision").handler(process::provisionDevice);

      router.route(HttpMethod.GET, "/api/discovery/load").handler(process::loadDiscoveryTable);

      router.route(HttpMethod.GET, "/api/monitor/load").handler(process::loadMonitorTable);

      router.route(HttpMethod.POST, "/api/monitor/delete").handler(process::deleteFromMonitor);

      router.route(HttpMethod.POST, "/api/monitor/device").handler(process::loadMonitorDeviceData);

      router.route(HttpMethod.POST, "/api/dashboard").handler(process::loadDashboardData);

      router.route().handler(StaticHandler.create().setCachingEnabled(false));

      vertx.createHttpServer(new HttpServerOptions().setSsl(true).setKeyStoreOptions(
          new JksOptions().setPath("server-keystore.jks").setPassword("sankalp")))
        .requestHandler(router).listen(HTTP_PORT)
        .onComplete(listening ->
          {
            if (listening.succeeded())
            {
              startPromise.complete();

              LOGGER.info("Listening on port: " + HTTP_PORT);
            }
            else
            {
              startPromise.fail(listening.cause());

              LOGGER.error(listening.cause().getMessage(),listening.cause());
            }
          });

          LOGGER.info("public api successfully deployed");

    } catch (Exception exception)
    {
      startPromise.fail(exception.getCause());

      LOGGER.error(exception.getMessage(),exception.getCause());
    }
  }
}
