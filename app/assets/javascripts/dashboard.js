// dashboard module
var dashboard = function($, metric) {

  var configMap = {
    width: 900,
    height: 550
  };

  // jQuery-UI set up
  $(function() {

    $("#dtFrom").datepicker({
      defaultDate: "-1w",
      minDate: "-6M",
      maxDate: "+0D",
      onClose: function(selectedDate) {
        $("#dtTo").datepicker("option", "minDate", selectedDate);
      }
    });

    $("#dtTo").datepicker({
      defaultDate: "+0D",
      minDate: "-6M",
      maxDate: "+0D",
      onClose: function(selectedDate) {
        $("#dtFrom").datepicker("option", "maxDate", selectedDate);
      }
    });

    $(".button").button();

    $("#statCtrls").buttonset();
    $("#intvlCtrls").buttonset();
  });

  var bindChart = function(type, data) {
    switch(type) {
      case "bar":
        barChart(data, configMap.width, configMap.height);
        break;
      case "hbar":
        hbarChart(data, configMap.width, configMap.height);
        break;
      case "line":
        lineChart(data, configMap.width, configMap.height);
        break;
    }
  };

  var query = function(metric) {
    var q = 'data?type=' + metric.type + '&metric=' + metric.id;
    return q;
  };

  // D3 XHR
  d3.json(query(metric), function(error, d) {
    bindChart(metric.type, d);
  });
}
