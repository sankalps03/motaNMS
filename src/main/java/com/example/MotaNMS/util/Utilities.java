package com.example.MotaNMS.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Utilities {

  public static JsonArray runPluginPolling(JsonArray data) {

    System.out.println(data);

    JsonObject credential = data.getJsonObject(0);

    String dataEncoder = (Base64.getEncoder().encodeToString((data).toString().getBytes(StandardCharsets.UTF_8)));

    BufferedReader reader = null;

    Process process = null;

    var dataArray = new JsonArray();

    try {
      process = new ProcessBuilder("src/main/java/com/example/MotaNMS/plugins/poll.exe",dataEncoder).start();

      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;

      while ((line = reader.readLine()) != null) {

        if (!(line.equals("null"))){

        var resultArray = new JsonArray(line);

        if (credential.getString("category").equals("discovery")){

          var resultObject=resultArray.getJsonObject(0);

          System.out.println(resultObject);

          if(resultObject.getString("status").equals("success")){
            credential.put("STATUS", "SUCCESSFUL");
          }else{
            credential.put("STATUS", "UNSUCCESSFUL");
          }

          dataArray.add(credential);
        }else {

          dataArray.addAll(resultArray);

        }
      }
      }
    } catch (Exception e) {

      e.printStackTrace();


    } finally {

      if (reader != null) {

        try{

          reader.close();

        }catch (Exception e){

        }
        process.destroy();
      }
    }
    return dataArray;
  }
}

