let device = {

  onload: function (deviceId) {

    $('#body').html(devicePage)

    let deviceInfoAjaxData = {

      url: "monitor/device",
      type: "POST",
      data: JSON.stringify(deviceId),
      dataType: 'json',
      successCallback: device.updatePage
    }

    api.ajaxCall(deviceInfoAjaxData)

    $("#header").html("Device Info");

    $("#deviceIp").html(deviceId["ip"]);

    $("#deviceType").html(deviceId["type"]);

    $("#deviceStatus").html(deviceId["status"]);

    if (deviceId["type"] === "ssh") {

      $("#pingRow").hide();

      $("#lineChartPingRtt").hide();

    } else {

      $("#sshRow").hide();

      $("#lineChartCpu").hide();

      $("#lineChart").hide();

      $("#networkInterface").hide()
    }
  },

  updatePage: function (devceInfoArray) {

    console.log(devceInfoArray)

    devceInfoArray.forEach(function (jsonArray) {

      if (jsonArray && jsonArray.length > 0) {

        switch (true) {
          case jsonArray[0].hasOwnProperty("PERCENTAGE"):

            const availabilityData = [devceInfoArray[1][0].PERCENTAGE, 100 - devceInfoArray[1][0].PERCENTAGE];

            device.availabilityChart(availabilityData);

            break;

          case ((jsonArray[0].hasOwnProperty("TIMESTAMP")) && (jsonArray[0].hasOwnProperty("METRICTYPE")) && (jsonArray[1].METRICTYPE).toString().includes("ping")):

            const uniqueTimestamps = [];

            const pingRttValues = [];

            const pingMaxRttValues = [];

            const pingMinRttValues = [];

            devceInfoArray[2].forEach(item => {

              const {METRICVALUE, METRICTYPE, TIMESTAMP} = item;

              if (!uniqueTimestamps.includes(TIMESTAMP)) {

                uniqueTimestamps.push(TIMESTAMP);
              }

              if (METRICTYPE === 'ping.packet.rtt') {

                pingRttValues.push(parseFloat(METRICVALUE));

              } else if (METRICTYPE === 'ping.packet.maxRtt') {

                pingMaxRttValues.push(parseFloat(METRICVALUE));

              } else if (METRICTYPE === 'ping.packet.minRtt') {
                pingMinRttValues.push(parseFloat(METRICVALUE));
              }
            });

            device.threeLineChart(uniqueTimestamps, pingRttValues, pingMaxRttValues, pingMinRttValues, "Ping Rtt Chart");

            break;

          case (jsonArray[0].hasOwnProperty('PING_PACKET_SENT')):

            if (jsonArray[0].PING_PACKET_RTT != null) {

              $("#rtt1").html(jsonArray[0].PING_PACKET_RTT);

              $("#sent").html((jsonArray[0].PING_PACKET_SENT).toString().split(".")[0]);

              $("#received").html((jsonArray[0].PING_PACKET_RCV).toString().split(".")[0]);

              $("#loss").html(jsonArray[0].PING_PACKET_LOSS);

            }

            break;

          case ((jsonArray[0].hasOwnProperty("METRICTYPE")) && (((jsonArray[2].METRICTYPE).toString().includes("memory.percent.used")) || ((jsonArray[2].METRICTYPE).toString().includes("cpu.percent.total")) || ((jsonArray[2].METRICTYPE).toString().includes("disk.percent.used")))):

            jsonArray.forEach(item => {

              switch (true) {

                case item.METRICTYPE === "ping.packet.rtt":

                  $("#rtt").html(item.METRICVALUE);

                  break;

                case item.METRICTYPE === "cpu.percent.total":

                  $("#cpu").html(item.METRICVALUE);

                  break;
                case item.METRICTYPE === "disk.percent.used":

                  $("#disk").html(item.METRICVALUE);

                  break;
                case item.METRICTYPE === "memory.percent.used":

                  $("#memory").html(item.METRICVALUE);

                  break;

              }
            });

            break;

          case jsonArray[0].hasOwnProperty("TOTAL_COUNT"):

            $("#up").html(jsonArray[0].UP_COUNT);

            $("#down").html(jsonArray[0].DOWN_COUNT);

            $("#unknown").html(jsonArray[0].UNKNOWN_COUNT);

            $("#total").html(jsonArray[0].TOTAL_COUNT);

            break;
          case jsonArray[0].hasOwnProperty("loopback.interface.name"):

            device.ceateTableBody(jsonArray[0])

            break;
        }
      }
    });
    if (devceInfoArray[0][0] != null && !devceInfoArray[0][0].hasOwnProperty('PING_PACKET_SENT')) {

      var cpu = devceInfoArray[2].map(obj => obj.METRICVALUE);

      var cpuTimestamps = devceInfoArray[2].map(obj => obj.TIMESTAMP);

      var memory = devceInfoArray[3].map(obj => obj.METRICVALUE);

      var memoryTimestamps = devceInfoArray[3].map(obj => obj.TIMESTAMP);

      var disk = devceInfoArray[4].map(obj => obj.METRICVALUE);

      var diskTimestamps = devceInfoArray[4].map(obj => obj.TIMESTAMP);

      device.lineChart(cpuTimestamps, cpu, "CPU chart");

      device.lineChart(memoryTimestamps, memory, "Memory Chart");

      device.lineChart(diskTimestamps, disk, "Disk Chart");

    }
  },

  availabilityChart: function (availabilityData) {

    const pingChartCanvas = $("#pingChart")[0].getContext("2d");

    const pingChart = new Chart(pingChartCanvas, {
      type: "doughnut",
      data: {
        labels: ["Available", "Unavailable"],
        datasets: [
          {
            data: availabilityData,
            backgroundColor: ["#4CBB17", "#ff0000"],
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutoutPercentage: 70,
        plugins: {
          datalabels: {
            formatter: (value, ctx) => {
              return value + "%";
            },
            color: "#fff",
            font: {
              weight: "bold"
            }
          }
        }
      },
    });

  },

  lineChart: function (lables, data, chartName) {

    var chartData = {
      labels: lables,
      datasets: [{
        label: "Last One Hour " + chartName,
        data: data,
        borderColor: 'blue',
        fill: true
      }]
    };

    let chartOptions = {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };

    let ctx = document.getElementById(chartName).getContext('2d');
    let lineChart = new Chart(ctx, {
      type: 'line',
      data: chartData,
      options: chartOptions
    });
  },

  threeLineChart: function (lables, data1, data2, data3, chartName) {

    var chartData = {
      labels: lables,
      datasets: [{
        label: "AVG RTT",
        data: data1,
        borderColor: 'blue',
        fill: true
      },
        {
          label: "MAX RTT",
          data: data2,
          borderColor: 'red',
          fill: true
        },
        {
          label: "MIN RTT",
          data: data3,
          borderColor: 'green',
          fill: true
        }]
    };

    let chartOptions = {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };

    let ctx = document.getElementById(chartName).getContext('2d');
    let lineChart = new Chart(ctx, {
      type: 'line',
      data: chartData,
      options: chartOptions
    });
  },
  createTableRow: function (interfaceName, receivedPackets, receivedBytes, totalBytes, totalPackets, transmittedBytes, transmittedPackets, receivedBPS, totalBPS, transmittedBPS) {
    var row = "<tr>" +
      "<td>" + interfaceName + "</td>" +
      "<td>" + transmittedPackets + "</td>" +
      "<td>" + receivedPackets + "</td>" +
      "<td>" + totalPackets + "</td>" +
      "<td>" + transmittedBytes + "</td>" +
      "<td>" + receivedBytes + "</td>" +
      "<td>" + totalBytes + "</td>" +
      "<td>" + transmittedBPS + "</td>" +
      "<td>" + receivedBPS + "</td>" +
      "<td>" + totalBPS + "</td>" +
      "</tr>";
    return row;
  },

  ceateTableBody: function (networkInterfaceData) {

    var table = $(".networkTable");

    var headers = [
      "transmitted.packets",
      "received.Packets",
      "total.packets",
      "transmitted.bytes",
      "received.bytes",
      "total.bytes",
      "TransmittedBPS",
      "ReceivedBPS",
      "TotalBPS",
    ];


    var rowOrder = ["ethernet", "loopback", "wireless"];

    for (var i = 0; i < rowOrder.length; i++) {
      var rowKey = rowOrder[i];

      var rowData = {};

      for (var key in networkInterfaceData) {
        if (key.includes(rowKey)) {
          rowData[key] = networkInterfaceData[key];

        }
      }


      var row = $("<tr>");

      var header = $("<th>", { scope: "row", text: rowData[rowKey + ".interface.name"] });
      row.append(header);

      for (var j = 0; j < 9; j++) {
        var cellDataKey = rowKey + "." + headers[j];
        var cellData = rowData[cellDataKey] || "";
        var cell = $("<td>", { text: cellData });
        row.append(cell);
      }

      // Append the row to the table body
      table.find("tbody").append(row);
    }
  }
}
