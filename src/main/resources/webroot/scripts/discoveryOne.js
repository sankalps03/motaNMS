var discovery = {

  loadDiscovery: function () {

    $('#body').html(discoveryPage)

    var ajaxData = {
      url: "http://localhost:8080/api/discovery/load",
      type: "GET",
      successCallback: this.updateDiscoveryTable
    }

    api.ajaxCall(ajaxData)

    var dataTable = $('#discoveryTable').DataTable({
      "pageLength": 20,
      "dom": '<"top">ct<"top"p><"clear">'

    });
    $("#filterbox").keyup(function () {
      dataTable.search(this.value).draw();
    });

    $("#addForm").submit(function (event) {

      event.preventDefault();

      var formData = {

        ip: $("#ipAddress").val(),
        username: $("#username").val(),
        password: $("#password").val(),
        type: $("input[name='type']:checked").val()
      };


      console.log(formData)

      discovery.closeForm();


      let addajaxData = {

        url: "http://localhost:8080/api/discovery/add",
        type: "POST",
        data: JSON.stringify(formData),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery
      }

      api.ajaxCall(addajaxData);


    });

    $('#discoveryTable').on('click', '.deleteDiscovery', function deleteRow(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let deleteAjaxData = {

        url: "http://localhost:8080/api/discovery/delete",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery

      }

      api.ajaxCall(deleteAjaxData)

    });

    $('#discoveryTable').on('click', '.runBtn', function run(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let runAjaxData = {

        url: "http://localhost:8080/api/discovery/run",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery

      }

      api.ajaxCall(runAjaxData)

    });
    $('#discoveryTable').on('click', '.provisionBtn', function provision(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let provisionAjaxData = {

        url: "http://localhost:8080/api/discovery/provision",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery
      }

      api.ajaxCall(provisionAjaxData)

    });

    $('input[name="type"]').change(function () {
      var selectedOption = $(this).val();

      if (selectedOption === "ping") {

        $('#sshRow').hide();
        $("#username").prop('required', false);
        $("#password").prop('required', false);

      } else if (selectedOption === "ssh") {
        $('#sshRow').show();

      }
    });


  },


  updateDiscoveryTable: function (result) {

    let data = $.parseJSON(result);

    console.log(data)

    $("#discoveryTable").dataTable().fnDestroy()

    let dataTable = $('#discoveryTable').DataTable({
      "pageLength": 10,
      "dom": '<"top">ct<"top"p><"clear">',

      data: data,
      columns: [
        {data: 'ID'},
        {data: 'IPADDRESS'},
        {data: 'TYPE'},
        {
          targets: 3, data: 'PROVISION',
          render: function (data) {

            if (data === "TRUE") {

              return deleteBtn + runBtn + provisionBtn;
            } else {
              return deleteBtn + runBtn;
            }
          }
        }
      ],
      order: [[0, 'asc']],

    });

    $("#filterbox").keyup(function () {
      dataTable.search(this.value).draw();
    });

  },

  openForm: function() {
    document.getElementById("addForm").style.display = "block";},

  closeForm:function() {
    document.getElementById("addForm").style.display = "none";},

  btnsuccessDiscovery :function  (){

  discovery.loadDiscovery();
}


}

