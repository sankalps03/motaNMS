var discovery = {

  loadDiscovery: function () {

    $('#body').html(discoveryPage)

    $("#header").html("Discovery");

    common.initializeDatatable("discoveryTable");

    var ajaxData =
      {
        url: "https://localhost:8080/api/discovery/load",
        type: "GET",
        successCallback: this.updateDiscoveryTable
      }

    api.ajaxCall(ajaxData);

    discovery.radioChange();

    discovery.addFormSubmit();

    discovery.provisionBtn();

    discovery.runBtn();

    discovery.deleteBtn();

  },

  radioChange:function (){

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

  runBtn: function (){

    $('#discoveryTable').on('click', '.runBtn', function run(event) {

      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let runAjaxData = {

        url: "https://localhost:8080/api/discovery/run",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery,
        failCallback: discovery.btnsuccessDiscovery,

      }

      api.ajaxCall(runAjaxData)

      common.showNotification("successful","Discovery started ")


    });

  },

  deleteBtn : function (){

    $('#discoveryTable').on('click', '.deleteDiscovery', function deleteRow(event) {

      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      var ip = row.find("td:nth-child(2)").text();

      var result = confirm("Do you want to delete "+ip );

      if (result) {

        let deleteAjaxData = {

          url: "https://localhost:8080/api/discovery/delete",
          type: "POST",
          data: JSON.stringify(id),
          dataType: 'json',
          successCallback: discovery.btnsuccessDiscovery,
          failCallback: discovery.btnsuccessDiscovery,

        }

        api.ajaxCall(deleteAjaxData)
      }

    });

  },
  provisionBtn : function (){

    $('#discoveryTable').on('click', '.provisionBtn', function provision(event) {
      var a = $(event.target);

      var row = a.closest("tr")

      var id = {id: row.find("td:nth-child(1)").text()};

      let provisionAjaxData = {

        url: "https://localhost:8080/api/discovery/provision",
        type: "POST",
        data: JSON.stringify(id),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery,
        failCallback: discovery.btnsuccessDiscovery,
      }

      api.ajaxCall(provisionAjaxData)

    });

  },

  addFormSubmit : function (){

    $("#addForm").submit(function (event) {

      event.preventDefault();

      var formData = {

        ip: $("#ipAddress").val(),
        username: $("#username").val(),
        password: $("#password").val(),
        type: $("input[name='type']:checked").val()
      };

      discovery.closeForm();

      let addajaxData = {

        url: "https://localhost:8080/api/discovery/add",
        type: "POST",
        data: JSON.stringify(formData),
        dataType: 'json',
        successCallback: discovery.btnsuccessDiscovery,
        failCallback: discovery.btnsuccessDiscovery

      }
      api.ajaxCall(addajaxData);
    });
  },


  updateDiscoveryTable: function (result) {

    let data = $.parseJSON(result);

    console.log(data)

    $("#discoveryTable").dataTable().fnDestroy()

    let dataTable = $('#discoveryTable').DataTable({
      "pageLength": 15,
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

    common.searchBar(dataTable);
  },



  openForm: function () {

    document.getElementById("addForm").style.display = "block";

  },

  closeForm: function () {

    document.getElementById("addForm").style.display = "none";

  },

  btnsuccessDiscovery: function (result) {

    if (result.status === 200){

    common.showNotification("successful", result.responseText)

    }else{

      common.showNotification("fail", result.responseText)

    }
    discovery.loadDiscovery();

  },

}
