$(document).ready(function() {
  var dataTable = $('#filtertable').DataTable({
    "pageLength":10,
    "dom":'<"top">ct<"top"p><"clear">'

  });
  $("#filterbox").keyup(function(){
    dataTable.search(this.value).draw();
  });
} );
