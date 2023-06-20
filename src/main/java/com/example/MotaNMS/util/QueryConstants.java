package com.example.MotaNMS.util;

public class QueryConstants
{
  public static final String DISCOVERY_INSERT_QUERY= "INSERT INTO discovery(ipaddress,type,credential) VALUES(?,?,?)";

  public static final String DISCOVERY_DELETE_QUERY ="DELETE FROM DISCOVERY WHERE id =?";

  public static final String DISCOVERY_SELECT_QUERY ="SELECT id,ipaddress,type,provision FROM DISCOVERY";

  public static final String DISCOVERY_RUN_SELECT_QUERY ="SELECT ipaddress,type,credential FROM DISCOVERY WHERE id = ?";

  public static final String PROVISION_QUERY = "INSERT INTO MONITOR (IPAddress, Type, Credential) SELECT IPAddress, Type, Credential FROM DISCOVERY WHERE id = ?";

  public static final String SET_PROVISION_TRUE_QUERY ="UPDATE DISCOVERY SET Provision = true WHERE id = ?";

  public static final String SET_PROVISION_FALSE_QUERY ="UPDATE DISCOVERY SET Provision = false WHERE id = ?";

  public static final String MONITOR_SELECT_QUERY = "SELECT id,ipaddress,type,status FROM MONITOR";

  public static final String MONITOR_DELETE_QUERY ="DELETE FROM MONITOR WHERE id =?";

  public static final String SELECT_SSH_DEVICES_QUERY ="SELECT * FROM MONITOR WHERE TYPE = 'ssh'";

  public static final String SELECT_PING_DEVICES_QUERY = "SELECT DISTINCT IPAddress FROM MONITOR;";

  public static final String POLLING_INSERT_QUERY ="INSERT INTO polling (ipaddress, type, metricType, metricvalue, timestamp) VALUES (?, ?, ?, ?, ?)";

  public static final String SSH_LATEST_DATA_QUERY ="SELECT DISTINCT metrictype, Metricvalue, TIMESTAMP FROM polling WHERE IPADDRESS = ? AND METRICTYPE IN ('cpu.percent.total', 'ping.packet.rtt', 'disk.percent.used', 'memory.percent.used') ORDER BY TIMESTAMP DESC LIMIT 4;";

  public static final String PING_LATEST_DATA_QUERY ="SELECT MAX(CASE WHEN METRICTYPE = 'ping.packet.sent' THEN Metricvalue END) AS ping_packet_sent, MAX(CASE WHEN METRICTYPE = 'ping.packet.rtt' THEN Metricvalue END) AS ping_packet_rtt, MAX(CASE WHEN METRICTYPE = 'ping.packet.rcv' THEN Metricvalue END) AS ping_packet_rcv, MAX(CASE WHEN METRICTYPE = 'ping.packet.loss' THEN Metricvalue END) AS ping_packet_loss FROM polling WHERE IPADDRESS = ? AND METRICTYPE IN ( 'ping.packet.sent', 'ping.packet.rtt', 'ping.packet.rcv', 'ping.packet.loss' ) AND TIMESTAMP >= NOW() - INTERVAL '2' MINUTE GROUP BY TIMESTAMP ORDER BY TIMESTAMP DESC";

  public static final String TOP_5_RTT_QUERY ="SELECT MAX(p.METRICVALUE) AS max_rtt, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ping' AND p.metricType = 'ping.packet.rtt' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_rtt DESC LIMIT 5;";

  public static final String TOP_5_CPU_QUERY ="SELECT MAX(p.METRICVALUE) AS max_cpu, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'cpu.percent.total' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_cpu DESC LIMIT 5;";

  public static final String TOP_5_MEMORY_QUERY ="SELECT MAX(p.METRICVALUE) AS max_memory, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'memory.percent.used' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_memory DESC LIMIT 5;";

  public static final String TOP_5_DISK_QUERY = "SELECT MAX(p.METRICVALUE) AS max_disk, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'disk.percent.used' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_disk DESC LIMIT 5;";

  public static final String DEVICE_COUNT_QUERY ="SELECT COUNT(*) FILTER (WHERE status = 'up') AS up_count, COUNT(*) FILTER (WHERE status = 'down') AS down_count, COUNT(*) FILTER (WHERE status = 'unknown') AS unknown_count, COUNT(*) AS total_count FROM monitor;";

  public static final String UPDATE_AVAILABILITY_STATUS_QUERY ="UPDATE monitor m SET m.STATUS=CASE WHEN m.IPADDRESS IN (SELECT DISTINCT p.IPADDRESS FROM polling p WHERE p.METRICTYPE='ping.packet.loss' AND p.METRICVALUE < 100 AND p.TIMESTAMP>=NOW() - INTERVAL '2' MINUTE) THEN 'up' ELSE 'down' END;";

  public static final String LAST_24_HOUR_AVAILABILITY_QUERY ="SELECT (COUNT(*) * 100.0) / (SELECT 24 * 60 / 2) AS percentage FROM polling WHERE metrictype = 'ping.packet.loss' AND ipaddress = ? AND metricvalue < '100' AND timestamp >= NOW() - INTERVAL '24' HOUR;";

  public static final String LAST_1_HOUR_CPU_USED_QUERY="SELECT METRICVALUE AS cpu ,TIMESTAMP FROM polling WHERE METRICTYPE='cpu.percent.total' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";

  public static final String LAST_1_HOUR_MEMORY_USED_QUERY="SELECT METRICVALUE AS memory ,TIMESTAMP FROM polling WHERE METRICTYPE='memory.percent.used' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";

  public static final String LAST_1_HOUR_DISK_USED_QUERY="SELECT METRICVALUE AS disk ,TIMESTAMP FROM polling WHERE METRICTYPE='disk.percent.used' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";

  public static final String LAST_1_HOUR_ALL_RTT ="SELECT METRICTYPE, METRICVALUE , TIMESTAMP FROM polling WHERE METRICTYPE IN ('ping.packet.minRtt', 'ping.packet.rtt', 'ping.packet.maxRtt') AND IPADDRESS = ? AND TIMESTAMP >= NOW() - INTERVAL '1' HOUR;";

}
