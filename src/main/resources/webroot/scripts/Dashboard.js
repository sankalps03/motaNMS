$(function () {

  var eventBus = new EventBus('/api/eventbus');

  eventBus.onopen = function () {

    eventBus.registerHandler('updates.Dashboard', function (err, msg) {

      console.log(msg)

      let messageArray = JSON.parse(msg.body)

      messageArray.forEach(function (jsonArray) {

        if (jsonArray && jsonArray.length > 0) {

          switch (true) {
            case jsonArray[0].hasOwnProperty("MAX_CPU"):

              dashboard.updateDashboardTables(jsonArray, "MAX_CPU", "topCpuUsage");

              break;

            case jsonArray[0].hasOwnProperty("MAX_DISK"):

              dashboard.updateDashboardTables(jsonArray, "MAX_DISK", "topDiskUsage");

              break;

            case jsonArray[0].hasOwnProperty("MAX_MEMORY"):

              dashboard.updateDashboardTables(jsonArray, "MAX_MEMORY", "topMemoryUsage");

              break;

            case jsonArray[0].hasOwnProperty("MAX_RTT"):

              dashboard.updateDashboardTables(jsonArray, "MAX_RTT", "topRtt");

              break;

            case jsonArray[0].hasOwnProperty("TOTAL_COUNT"):

              $("#up").html(jsonArray[0].UP_COUNT);

              $("#down").html(jsonArray[0].DOWN_COUNT);

              $("#unknown").html(jsonArray[0].UNKNOWN_COUNT);

              $("#total").html(jsonArray[0].TOTAL_COUNT);

              break;

          }
        }
      })
    });
  }
  dashboard.requestDashboardData();
});

var dashboard = {

  updateDashboardTables: function (result, metricType, tableName) {

    $("#" + tableName).dataTable().fnDestroy()

    console.log(metricType)

    let dataTable = $("#" + tableName).DataTable({
      searching: false, paging: false, info: false,

      data: result,
      columns: [
        {data: 'IPADDRESS'},
        {
          targets: 1, data: metricType,
          render: function (data) {
            return '<div class="progress"><div class="progress-bar progress-bar-success" role="progressbar" style="width:52%; height:5px;" aria-valuenow="52" aria-valuemin="0" aria-valuemax="100"></div></div><span class="progress-parcent">' + data + '</span>'
          }
        },
      ],
      order: [[1, 'Desc']],
    });
  },


  requestDashboardData: function () {

    let ajaxRequest = {
      url: "https://localhost:8080/api/dashboard",
      type: "POST",
    }
    setTimeout(function () {

      api.ajaxCall(ajaxRequest);

    }, 1000);
  }
}
