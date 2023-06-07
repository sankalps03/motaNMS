var api = {
  ajaxCall: function (result) {

    console.log("ajax call")

    $.ajax({
      type: result.type,

      url: result.url,

      data: result.data,

      dataType: result.dataType,

      contentType : result.contentType,

      success: function (data) {

        if (result.hasOwnProperty('successCallback')) {

          console.log(data)

          result.successCallback(data);
        }
      },
      error: function (data) {

        if (result.hasOwnProperty('failCallback')) {

          console.log(data)

          result.failCallback(data);
        }

      }
    });
  }
}
