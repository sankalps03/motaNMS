package com.example.MotaNMS.Util;

import io.vertx.core.json.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Utilities {

  public static JsonObject runPlugin(JsonObject credential) {

    var error = new ArrayList<String>();

    String encoder = (Base64.getEncoder().encodeToString((credential).toString().getBytes(StandardCharsets.UTF_8)));

    BufferedReader reader = null;

    Process process = null;

    try {
      process = new ProcessBuilder("src/main/java/com/example/MotaNMS/Plugins/init.exe", encoder).start();

      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;


      while ((line = reader.readLine()) != null) {

        var resultObject = new JsonObject(line);

        credential.put("Result", resultObject);

        if(resultObject.getString("status").equals("success")){

          credential.put("STATUS", "SUCCESSFUL");
        }else{
          credential.put("STATUS", "UNSUCCESSFUL");
        }
      }
    } catch (Exception e) {

      e.printStackTrace();

      error.add(e.getMessage());

    } finally {

      if (reader != null) {

        try{

          reader.close();

        }catch (Exception e){

          error.add(e.getMessage());
        }
        process.destroy();
      }
    }
    if(error.isEmpty()){

    }else{

    }

    System.out.println(credential);
    return credential;
  }
}
