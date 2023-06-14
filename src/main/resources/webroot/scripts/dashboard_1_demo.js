$(function() {

  var eventBus = new EventBus('/api/eventbus');

  eventBus.onopen = function () {

    eventBus.registerHandler('updates.Dashboard', function (err, msg) {

      console.log(msg)

      let messageArray = JSON.parse(msg.body)

      messageArray.forEach(function(jsonArray) {

        if (jsonArray[0].hasOwnProperty("MAX_CPU")){

          updateTop5Cpu(jsonArray,"MAX_CPU","topCpuUsage");


        }else if (jsonArray[0].hasOwnProperty("MAX_DISK")){

          updateTop5Cpu(jsonArray,"MAX_DISK","topDiskUsage");


        }else if (jsonArray[0].hasOwnProperty("MAX_MEMORY")){

          updateTop5Cpu(jsonArray,"MAX_MEMORY","topMemoryUsage");


        }else if (jsonArray[0].hasOwnProperty("MAX_RTT")){

          updateTop5Cpu(jsonArray,"MAX_RTT","topRtt");

        }
      })
    });
  }

  let ajaxRequest={
    url: "https://localhost:8080/api/dashboard",
      type: "POST",
  }

  $(document).ready(function() {

    setTimeout(function() {

      api.ajaxCall(ajaxRequest);

    }, 2000);
  });

   function updateTop5Cpu (result,metricType,tableName) {

    $("#"+tableName).dataTable().fnDestroy()

     console.log(metricType)

    let dataTable = $("#"+tableName).DataTable({
     searching: false, paging: false, info: false,

      data: result,
      columns: [
        {data: 'IPADDRESS'},
        {targets:1 , data: metricType,
        render:function (data){
          return'<div class="progress"><div class="progress-bar progress-bar-success" role="progressbar" style="width:52%; height:5px;" aria-valuenow="52" aria-valuemin="0" aria-valuemax="100"></div></div><span class="progress-parcent">'+data+'</span>'
        }},
      ],
      order: [[1, 'Desc']],
    });
  }

});
