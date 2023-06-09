package com.example.MotaNMS.Services;

interface sqlQueries{


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
    return "SELECT ipaddress,type,credential FROM DISCOVERY";
  }

  static String provision(){
    return "INSERT INTO MONITOR (IPAddress, Type, Credential) SELECT IPAddress, Type, Credential FROM DISCOVERY WHERE id = ?";
  }

  static String setProvision(){
    return "UPDATE DISCOVERY SET Provision = true WHERE id = ?";
  }

}
