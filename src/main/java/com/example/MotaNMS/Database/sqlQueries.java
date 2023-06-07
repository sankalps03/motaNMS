package com.example.MotaNMS.Database;

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

}
