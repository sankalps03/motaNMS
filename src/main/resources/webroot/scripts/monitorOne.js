var monitor = {

  loadMonitor: function (){

    $('#body').html(monitorPage)

    document.getElementById("header").innerHTML = "Monitor";


    let ajaxData = {
      url: "https://localhost:8080/api/monitor/load",
      type: "GET",
      successCallback: this.updateMonitorTable
    }

    api.ajaxCall(ajaxData)

    let dataTable = $('#monitorTable').DataTable({
      "pageLength": 10,
      "dom": '<"top">ct<"top"p><"clear">'

    });
    $("#filterbox").keyup(function () {
      dataTable.search(this.value).draw();
    });


    $('#monitorTable').on('click', '.deleteDiscovery', function deleteRow(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let deleteAjaxData = {

        url: "https://localhost:8080/api/monitor/delete",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: monitor.btnsuccessMonitor
      }

      api.ajaxCall(deleteAjaxData)

    });

    $('#monitorTable').on('click', '.provisionBtn', function provision(event) {

      var a = $(event.target);

      var row = a.closest("tr")

      var deviceId  = {ip: row.find("td:nth-child(2)").text(),
        type: row.find("td:nth-child(3)").text()};

      device.onload(deviceId)

      console.log(deviceId)

    });

      },

  updateMonitorTable: function (result) {

    let data = $.parseJSON(result);

    console.log(data)

    $("#monitorTable").dataTable().fnDestroy()

    let dataTable = $('#monitorTable').DataTable({
      "pageLength": 20,
      "dom": '<"top">ct<"top"p><"clear">',

      data: data,
      columns: [
        {data: 'ID'},
        {data: 'IPADDRESS'},
        {data: 'TYPE'},
        {
          targets: 3,
          data: 'STATUS',
          render: function (data) {

            if (data === "up") {

              return '<span class="mode mode_on">up</span>'

            } else if(data === "down") {

              return '<span class="mode mode_off">Down</span>'
            }
            else {

              return '<span class="mode mode_process">UNKNOWN</span>'

            }
          }
        },
        {
          targets: 4,
          render: function (data) {

            return deleteBtn + provisionBtn;
          }
        }
      ],
      order: [[0, 'asc']],

    });

    $("#filterbox").keyup(function () {
      dataTable.search(this.value).draw();
    });
    },

  btnsuccessMonitor:function  (result){

    discovery.showNotification("success","success")

  monitor.loadMonitor;
},
  btnFailMonitor:function  (result){

    discovery.showNotification("fail","error")

    monitor.loadMonitor;

  }

}


