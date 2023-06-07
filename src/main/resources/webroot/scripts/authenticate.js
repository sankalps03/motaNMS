$(function () {
  $("#loginForm").submit(function (event) {

  // var formData = new URLSearchParams();
  //
  // formData.append("username", $("userName").val());
  // formData.append("password",$("#pwd").val())

    var formData = $(this).serialize();

    let ajaxData = {

      url: "http://localhost:8080/loginHandler",
      type: "POST",
      data: formData,
      dataType: 'json',
      failCallback : failed,
      successCallback : succeed
    }

    api.ajaxCall(ajaxData);

  });
});

function failed(data){


  $("#Error").text("Wrong Credentials");

}

function succeed(data){

  window.location.replace("/api")
}
