var common ={

  searchBar : function (dataTable){

    $("#filterbox").keyup(function () {

      dataTable.search(this.value).draw();

    });
  },

  initializeDatatable : function (tableName){

    var dataTable = $(tableName).DataTable({
      "pageLength": 20,
      "dom": '<"top">ct<"top"p><"clear">'
    });
  },

  showNotification: function(type, message) {

    let container;

    if (type === "successful")
    {
      container = $('#notificationContainer').html(successNotification);
    }
    else
    {
      container = $('#notificationContainer').html(errorNotification);
    }
    $("#message").html(message);

    container.fadeIn().delay(3000).fadeOut();
  }
}
