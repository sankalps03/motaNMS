package com.example.MotaNMS.services;

public interface SqlQueries {


  static String insertDiscovery(){
    return "INSERT INTO discovery(ipaddress,type,credential) VALUES(?,?,?)";
  }
  static String deleteDiscovery(){
    return "DELETE FROM DISCOVERY WHERE id =?";
  }

  static String selectDiscovery(){
    return "SELECT id,ipaddress,type,provision FROM DISCOVERY";
  }

  static String selectRunDiscovery(){
    return "SELECT ipaddress,type,credential FROM DISCOVERY WHERE id = ?";
  }

  static String provision(){
    return "INSERT INTO MONITOR (IPAddress, Type, Credential) SELECT IPAddress, Type, Credential FROM DISCOVERY WHERE id = ?";
  }

  static String setProvision(){
    return "UPDATE DISCOVERY SET Provision = true WHERE id = ?";
  }

  static String selectMonitor(){
    return "SELECT id,ipaddress,type,status FROM MONITOR";
  }

  static String deleteMonitor(){
    return "DELETE FROM MONITOR WHERE id =?";
  }

  static String selectSshDevices(){
    return "SELECT * FROM MONITOR WHERE TYPE = 'ssh'";
  }

  static String selectPingDevices(){
    return "SELECT * FROM MONITOR ";
  }

  static String insertPolling(){
    return "INSERT INTO polling (ipaddress, type, metricType, metricvalue, timestamp) VALUES (?, ?, ?, ?, ?)";
  }

  static String selectAllLatestData(){

    return "SELECT MAX(CASE WHEN METRICTYPE='ping.packet.sent' THEN METRICVALUE END) AS ping_packet_sent,MAX(CASE WHEN METRICTYPE='ping.packet.rcv' THEN METRICVALUE END) AS ping_packet_rcv,MAX(CASE WHEN METRICTYPE='ping.packet.rtt' THEN METRICVALUE END) AS ping_packet_rtt,MAX(CASE WHEN METRICTYPE='cpu.percent.total' THEN METRICVALUE END) AS cpu_percent_total,MAX(CASE WHEN METRICTYPE='disk.percent.used' THEN METRICVALUE END) AS disk_percent_used,MAX(CASE WHEN METRICTYPE='memory.percent.used' THEN METRICVALUE END) AS memory_percent_used,MAX(CASE WHEN METRICTYPE='ping.packet.loss' THEN METRICVALUE END) AS ping_packet_loss FROM polling p WHERE IPADDRESS= ? AND METRICTYPE IN ('ping.packet.sent','ping.packet.rcv','ping.packet.rtt','cpu.percent.total','disk.percent.used','memory.percent.used','ping.packet.loss') AND TIMESTAMP=(SELECT MAX(TIMESTAMP) FROM polling WHERE IPADDRESS= ? AND METRICTYPE=p.METRICTYPE);";
  }

  static String selectPingLatestData(){

    return "SELECT MAX(CASE WHEN METRICTYPE='ping.packet.sent' THEN METRICVALUE END) AS ping_packet_sent,MAX(CASE WHEN METRICTYPE='ping.packet.rcv' THEN METRICVALUE END) AS ping_packet_rcv,MAX(CASE WHEN METRICTYPE='ping.packet.rtt' THEN METRICVALUE END) AS ping_packet_rtt,MAX(CASE WHEN METRICTYPE='ping.packet.loss' THEN METRICVALUE END) AS ping_packet_loss FROM polling p WHERE IPADDRESS= ? AND METRICTYPE IN ('ping.packet.sent','ping.packet.rcv','ping.packet.rtt','ping.packet.loss') AND TIMESTAMP=(SELECT MAX(TIMESTAMP) FROM polling WHERE IPADDRESS= ? AND METRICTYPE=p.METRICTYPE);";
  }

  static String selectTop5rtt(){

    return "SELECT MAX(p.METRICVALUE) AS max_rtt, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ping' AND p.metricType = 'ping.packet.rtt' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_rtt DESC LIMIT 5;";
  }
  static String selectTop5Cpu(){

    return "SELECT MAX(p.METRICVALUE) AS max_cpu, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'cpu.percent.total' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_cpu DESC LIMIT 5;";
  }
  static String selectTop5Memory(){

    return "SELECT MAX(p.METRICVALUE) AS max_memory, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'memory.percent.used' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_memory DESC LIMIT 5;";
  }
  static String selectTop5Disk(){

    return "SELECT MAX(p.METRICVALUE) AS max_disk, m.ipaddress FROM polling p, monitor m WHERE p.type = 'ssh' AND p.metricType = 'disk.percent.used' AND  p.timestamp >= NOW() - INTERVAL '5' MINUTE AND p.IPADDRESS  = m.IPADDRESS  GROUP BY p.ipaddress ORDER BY max_disk DESC LIMIT 5;";
  }

  static String deviceCount(){
    return "SELECT COUNT(*) FILTER (WHERE status = 'up') AS up_count, COUNT(*) FILTER (WHERE status = 'down') AS down_count, COUNT(*) FILTER (WHERE status = 'unknown') AS unknown_count, COUNT(*) AS total_count FROM monitor;";
  }

  static String updateMonitorStatus(){

    return "UPDATE monitor m SET m.STATUS=CASE WHEN m.IPADDRESS IN (SELECT DISTINCT p.IPADDRESS FROM polling p WHERE p.METRICTYPE='ping.packet.loss' AND p.METRICVALUE < 50 AND p.TIMESTAMP>=NOW() - INTERVAL '5' MINUTE) THEN 'up' ELSE 'down' END;";
  }

  static String avaiability(){

    return "SELECT (COUNT(*) * 100.0) / (SELECT 24 * 60 / 2) AS percentage FROM polling WHERE metrictype = 'ping.packet.loss' AND ipaddress = ? AND metricvalue < '50' AND timestamp >= NOW() - INTERVAL '24' HOUR;" ;
  }

  static String cpuLinechart(){

    return "SELECT METRICVALUE AS cpu ,TIMESTAMP FROM polling WHERE METRICTYPE='cpu.percent.total' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";
  }

  static String memoryLinechart(){

    return "SELECT METRICVALUE AS memory ,TIMESTAMP FROM polling WHERE METRICTYPE='memory.percent.used' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";
  }

  static String diskLinechart(){

    return "SELECT METRICVALUE AS disk ,TIMESTAMP FROM polling WHERE METRICTYPE='disk.percent.used' AND IPADDRESS= ? AND TIMESTAMP>=NOW() - INTERVAL '1' HOUR;";
  }


}

