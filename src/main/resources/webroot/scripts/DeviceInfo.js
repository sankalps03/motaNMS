let device = {

  onload: function (deviceId) {

    $('#body').html(devicePage)

    let deviceInfoAjaxData = {

      url: "https://localhost:8080/api/monitor/device",
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

      $("#lineChartPing").hide();

    } else {

      $("#sshRow").hide();

      $("#lineChartCpu").hide();

      $("#lineChart").hide();
    }


  },

  updatePage: function (devceInfoArray) {

    const availabilityData = [devceInfoArray[1][0].PERCENTAGE, 100 - devceInfoArray[1][0].PERCENTAGE];

    device.availabilityChart(availabilityData);

    if (devceInfoArray[0][0].hasOwnProperty('PING_PACKET_SENT')) {

      $("#rtt1").html(devceInfoArray[0][0].PING_PACKET_RTT);

      $("#sent").html((devceInfoArray[0][0].PING_PACKET_SENT).toString().split(".")[0]);

      $("#received").html((devceInfoArray[0][0].PING_PACKET_RCV).toString().split(".")[0]);

      $("#loss").html(devceInfoArray[0][0].PING_PACKET_LOSS);

    }
    else if (devceInfoArray[0][0].hasOwnProperty('DISK_PERCENT_USED')) {

      $("#cpu").html(devceInfoArray[0][0].CPU_PERCENT_TOTAL);

      $("#memory").html(devceInfoArray[0][0].MEMORY_PERCENT_USED);

      $("#disk").html(devceInfoArray[0][0].DISK_PERCENT_USED);

      $("#rtt").html(devceInfoArray[0][0].PING_PACKET_RTT);

      var cpu = devceInfoArray[2].map(obj => obj.METRICVALUE);

      var cpuTimestamps = devceInfoArray[2].map(obj => obj.TIMESTAMP);

      var memory = devceInfoArray[3].map(obj => obj.METRICVALUE);

      var memoryTimestamps = devceInfoArray[3].map(obj => obj.TIMESTAMP);

      var disk = devceInfoArray[4].map(obj => obj.METRICVALUE);

      var diskTimestamps = devceInfoArray[4].map(obj => obj.TIMESTAMP);

      device.lineChart(cpuTimestamps, cpu, "lineChartCpu");

      device.lineChart(memoryTimestamps, memory, "lineChartMemory");

      device.lineChart(diskTimestamps, disk, "lineChartDisk");

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

    console.log(lables, data)
    var chartData = {
      labels: lables,
      datasets: [{
        label: chartName,
        data: data,
        borderColor: 'blue',
        fill: false
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

    console.log(lables, data)
    var chartData = {
      labels: lables,
      datasets: [{
        label: chartName,
        data: data1,
        borderColor: 'blue',
        fill: false
      }],

      datasets: [{
        label: chartName,
        data: data2,
        borderColor: 'red',
        fill: false
      }],

      datasets: [{
        label: chartName,
        data: data3,
        borderColor: 'green',
        fill: false
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
  }
}
