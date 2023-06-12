// $(document).ready(function() {
//
//   var ajaxData = {
//     url: "http://localhost:8080/api/discovery/load",
//     type: "GET",
//     successCallback : updateTable
//   }
//
//   api.ajaxCall(ajaxData)
//
//   var dataTable = $('#filtertable').DataTable({
//     "pageLength":10,
//     "dom":'<"top">ct<"top"p><"clear">'
//
//   });
//   $("#filterbox").keyup(function(){
//     dataTable.search(this.value).draw();
//   });
//
//   function updateTable(result){
//
//     let data = $.parseJSON(result);
//
//     console.log(data)
//
//     $("#filtertable").dataTable().fnDestroy()
//
//      dataTable = $('#filtertable').DataTable({
//       "pageLength":10,
//       "dom":'<"top">ct<"top"p><"clear">',
//
//       data: data,
//       columns: [
//         { data: 'ID' },
//         { data: 'IPADDRESS' },
//         { data: 'TYPE' },
//         { targets:3,
//           data: 'PROVISION',
//           render:function (data){
//
//             if (data === "TRUE"){
//
//               return '<span class="mode mode_on">TRUE</span>'
//
//             }else {
//
//               return '<span class="mode mode_off">FALSE</span>'
//             }}},
//         {targets: 4,data: 'PROVISION',
//           render: function (data) {
//
//             if (data === "TRUE"){
//
//               return discoveryBtn
//             }else {
//               return discoveryBtn2}}}
//       ],
//       order: [[0, 'asc']],
//
//     });
//
//     $("#filterbox").keyup(function(){
//       dataTable.search(this.value).draw();
//     });
//
//   }
//
//   $("#addForm").submit(function (event){
//
//     event.preventDefault();
//
//     var formData = {
//
//       ip: $("#ipAddress").val(),
//       username: $("#username").val(),
//       password: $("#password").val(),
//       type: $("input[name='type']:checked").val()
//     };
//
//
//     console.log(formData)
//
//     let addajaxData = {
//
//       url: "http://localhost:8080/api/discovery/add",
//       type: "POST",
//       data: JSON.stringify(formData),
//       dataType: 'json',
//     }
//
//     api.ajaxCall(addajaxData);
//
//     discovery.closeForm();
//
//
//   });
//
//   $('#filtertable').on('click', '.deleteDiscovery',function deleteRow(event) {
//     var a = $(event.target);
//
//     var row = a.closest("tr")
//
//     var id = {id: row.find("td:nth-child(1)").text()};
//
//     let deleteAjaxData = {
//
//       url: "http://localhost:8080/api/discovery/delete",
//       type: "POST",
//       data: JSON.stringify(id),
//       dataType: 'json',
//     }
//
//     api.ajaxCall(deleteAjaxData)
//
//   });
//
//   $('#filtertable').on('click', '.runBtn',function run(event) {
//     var a = $(event.target);
//
//     var row = a.closest("tr")
//
//     var id = {id: row.find("td:nth-child(1)").text()};
//
//     let runAjaxData = {
//
//       url: "http://localhost:8080/api/discovery/run",
//       type: "POST",
//       data: JSON.stringify(id),
//       dataType: 'json',
//     }
//
//     api.ajaxCall(runAjaxData)
//
//   });
//   $('#filtertable').on('click', '.provisionBtn',function provision(event) {
//     var a = $(event.target);
//
//     var row = a.closest("tr")
//
//     var id = {id: row.find("td:nth-child(1)").text()};
//
//     let provisionAjaxData = {
//
//       url: "http://localhost:8080/api/discovery/provision",
//       type: "POST",
//       data: JSON.stringify(id),
//       dataType: 'json',
//     }
//
//     api.ajaxCall(provisionAjaxData)
//
//   });
//
//   $('input[name="type"]').change(function() {
//     var selectedOption = $(this).val();
//
//     if (selectedOption === "PING") {
//
//       $('#sshRow').hide();
//       $("#userName").prop('required' ,false);
//       $("#password").prop('required', false);
//
//     } else if (selectedOption === "SSH") {
//       $('#sshRow').show();
//
//     }
//   });
//
// } );
//
//
// function addSuccess(){
//
//
// }
//
//
