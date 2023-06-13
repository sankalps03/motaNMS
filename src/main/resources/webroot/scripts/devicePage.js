

var device ={

  onload: function (deviceId){

    $('#body').html(devicePage)

    if (deviceId["type"] === "ping"){

      document.getElementById("sshRow").style.display = "none";

    }


    let deviceAjaxData = {

      url: "http://localhost:8080/api/monitor/device",
      type: "POST",
      data: JSON.stringify(deviceId),
      dataType: 'json',
      successCallback: device.updatePage
    }

    api.ajaxCall(deviceAjaxData)

  },

  updatePage : function (deviceData){

    document.getElementById("rtt").innerHTML = deviceData["ping.packet.rtt"];

    document.getElementById("sent").innerHTML = deviceData["ping.packet.sent"];

    document.getElementById("received").innerHTML = deviceData["ping.packet.rcv"];

    document.getElementById("loss").innerHTML = deviceData["ping.packet.loss"];

    if(data.hasOwnProperty('disk.percent.used')) {

      document.getElementById("cpu").innerHTML = deviceData["cpu.percent.total"];

      document.getElementById("memory").innerHTML = deviceData["memory.percent.used"];

      document.getElementById("disk").innerHTML = deviceData["disk.percent.used"];
    }

  }
}
