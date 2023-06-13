package com.example.MotaNMS.Services;

public interface sqlQueries{


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
    return "SELECT id,ipaddress,type FROM MONITOR";
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

    return "SELECT p.METRICTYPE, p.METRICVALUE FROM polling p WHERE p.IPADDRESS = ? AND p.METRICTYPE IN ('ping.packet.sent', 'ping.packet.rcv', 'ping.packet.rtt', 'cpu.percent.total', 'disk.percent.used','memory.percent.used','ping.packet.loss') AND p.TIMESTAMP = (SELECT MAX(TIMESTAMP) FROM polling WHERE IPADDRESS = ? AND METRICTYPE = p.METRICTYPE )";
  }

  static String selectPingLatestData(){

    return "SELECT p.METRICTYPE, p.METRICVALUE FROM polling p WHERE p.IPADDRESS = ? AND p.METRICTYPE IN ('ping.packet.sent', 'ping.packet.rcv', 'ping.packet.rtt','ping.packet.loss') AND p.TIMESTAMP = ( SELECT MAX(TIMESTAMP) FROM polling WHERE IPADDRESS = ? AND METRICTYPE = p.METRICTYPE )";
  }


}

