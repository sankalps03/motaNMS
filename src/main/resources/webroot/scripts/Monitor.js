var monitor = {

  loadMonitor: function () {

    $('#body').html(monitorPage)

    $("#header").html("Monitor");

    let ajaxData = {
      url: "https://localhost:8080/api/monitor/load",
      type: "GET",
      successCallback: this.updateMonitorTable
    }

    api.ajaxCall(ajaxData)

    common.initializeDatatable("monitorTable");

    monitor.viewDeviceInfo();

    monitor.deleteBtn();

  },

  deleteBtn : function (){

    $('#monitorTable').on('click', '.deleteDiscovery', function deleteRow(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      var ip = row.find("td:nth-child(2)").text();

      var result = confirm("Do you want to delete "+ip );

        if (result) {

          let deleteAjaxData = {

            url: "https://localhost:8080/api/monitor/delete",
            type: "POST",
            data: JSON.stringify(id),
            dataType: 'json',
            successCallback: monitor.successMonitor,
            failCallback: monitor.successMonitor,
          }

          api.ajaxCall(deleteAjaxData)
        }

    });

  },

  viewDeviceInfo: function (){

    $('#monitorTable').on('click', '.provisionBtn', function provision(event) {

      var a = $(event.target);

      var row = a.closest("tr")

      var deviceId = {
        ip: row.find("td:nth-child(2)").text(),
        type: row.find("td:nth-child(3)").text(),
        status: row.find("td:nth-child(4)").text()
      };

      device.onload(deviceId)

    });

  },

  updateMonitorTable: function (result) {

    let data = $.parseJSON(result);

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

            } else if (data === "down") {

              return '<span class="mode mode_off">Down</span>'
            } else {

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

    common.searchBar(dataTable);
  },

  successMonitor: function (result) {

    if (result.status === 200) {

      common.showNotification("successful", result.responseText)

    } else {

      common.showNotification("fail", result.responseText)

    }

    monitor.loadMonitor();
  },


}


