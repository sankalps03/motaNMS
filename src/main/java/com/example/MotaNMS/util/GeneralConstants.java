package com.example.MotaNMS.util;

public class GeneralConstants {

  public static final int HTTP_PORT = 8080;

  public static final int PROCESS_TIMEOUT_SECONDS = 60;

  public static final int BATCH_SIZE = 100;

  public static final int SSH_PERIODIC_TIME = 5 * 60 * 1000;

  public static final int PING_PERIODIC_TIME = 2 * 60 * 1000;

  public static final String URL ="jdbc:h2:tcp://localhost/~/test";

  public static final String USER ="sa";

  public static final String PASSWORD ="";

  public static final int MAX_POOL_SIZE = 15;

  public static final int DATABASE_WORKER_POOL_SIZE = 4;

  public static final int SERVICE_WORKER_POOL_SIZE = 1;

  public static final int POLLING_WORKER_POOL_SIZE = 2;

  public static final String SELECT ="Select from database";

  public static final String INSERT ="Insert into database";

  public static final String ROW_ID_OPERATION ="Row id operation";

  public static final String UPDATE ="Update in database";

  public static final String LOAD_DASHBOARD_DATA = "dashboardLoad";

  public static final String LOAD_MONITOR_DEVICE_DATA ="monitorLoadDevice";

  public static final String DELETE_FROM_MONITOR = "monitorDelete";

  public static final String LOAD_MONITOR_TABLE ="monitorLoad";

  public static final String PROVISION_DEVICE = "discoveryProvision";

  public static final String ADD_TO_DISCOVERY ="discoveryAdd";

  public static final String DELETE_FROM_DISCOVERY ="discoveryDelete";

  public static final String RUN_DISCOVERY = "discoveryRun";

  public static final String LOAD_DISCOVERY_TABLE = "discoveryLoad";
}
