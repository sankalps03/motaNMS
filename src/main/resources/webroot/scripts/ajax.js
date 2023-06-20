var api = {
  ajaxCall: function (result) {

    $.ajax({
      type: result.type,

      url: result.url,

      data: result.data,

      dataType: result.dataType,

      contentType : result.contentType,

      success: function (data) {

        if (result.hasOwnProperty('successCallback')) {

          result.successCallback(data);
        }
      },
      error: function (data) {

        if (result.hasOwnProperty('failCallback')) {

          result.failCallback(data);
        }

      }
    });
  }
}
