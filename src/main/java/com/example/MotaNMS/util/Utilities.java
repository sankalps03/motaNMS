package com.example.MotaNMS.util;

import com.example.MotaNMS.services.Dashboard;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.example.MotaNMS.util.GeneralConstants.PROCESS_TIMEOUT_SECONDS;

public class Utilities {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utilities.class);

  public static JsonArray runPluginPolling(JsonArray data)
  {
    JsonObject credential = data.getJsonObject(0);

    String dataEncoder = (Base64.getEncoder().encodeToString((data).toString().getBytes(StandardCharsets.UTF_8)));

    BufferedReader reader = null;

    Process process = null;

    var dataArray = new JsonArray();

    try
    {
      process = new ProcessBuilder("src/main/java/com/example/MotaNMS/plugins/init.exe",dataEncoder).start();

      if (!process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {

        process.destroy();

        throw new InterruptedException("Process has been interrupted because of timeout (" + PROCESS_TIMEOUT_SECONDS + "seconds). ");
      }

      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;

      while ((line = reader.readLine()) != null)
      {

        if (!(line.equals("null"))){

        var resultArray = new JsonArray(line);

        if (credential.getString("category").equals("discovery")){

          var resultObject=resultArray.getJsonObject(0);

          if(resultObject.getString("status").equals("success"))
          {
            credential.put("status", "successful");
          }
          else
          {
            credential.put("status", "failed");
          }

          dataArray.add(credential);
        }
        else
        {
          dataArray.addAll(resultArray);
        }
      }
      }
    } catch (Exception exception) {

      LOGGER.error(exception.getMessage(),exception.getCause());

    } finally {

      if (reader != null) {

        try{

          reader.close();

        }catch (Exception exception){

          LOGGER.error(exception.getMessage(),exception.getCause());
        }
        process.destroy();
      }
    }
    System.out.println(dataArray);
    return dataArray;
  }
}

