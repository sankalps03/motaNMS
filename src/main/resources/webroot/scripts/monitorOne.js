var monitor = {

  loadMonitor: function (){

    $('#body').html(monitorPage)

    let ajaxData = {
      url: "http://localhost:8080/api/monitor/load",
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

        url: "http://localhost:8080/api/monitor/delete",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: monitor.btnsuccessMonitor
      }

      api.ajaxCall(deleteAjaxData)

    });
    },

  updateMonitorTable: function (result) {

    let data = $.parseJSON(result);

    console.log(data)

    $("#monitorTable").dataTable().fnDestroy()

    let dataTable = $('#monitorTable').DataTable({
      "pageLength": 10,
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

            if (data === "TRUE") {

              return '<span class="mode mode_on">ACTIVE</span>'

            } else {

              return '<span class="mode mode_off">INACTIVE</span>'
            }
          }
        },
        {
          targets: 4,
          render: function (data) {

            return deleteBtn;
          }
        }
      ],
      order: [[0, 'asc']],

    });

    $("#filterbox").keyup(function () {
      dataTable.search(this.value).draw();
    });
    },

  btnsuccessMonitor:function  (){

  monitor.loadMonitor;
}

}


