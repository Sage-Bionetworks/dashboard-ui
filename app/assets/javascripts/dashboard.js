// dashboard module
var dashboard = function($, metric) {

  var configMap = {
    width: 900,
    height: 550
  };

  // jQuery-UI set up
  $(function() {

    $('#dtFrom').datepicker({
      defaultDate: '-1w',
      minDate: '-6M',
      maxDate: '+0D'
    });
    var dtStart = new Date(Number(metric.start));
    $('#dtFrom').datepicker('setDate', dtStart);

    $('#dtTo').datepicker({
      defaultDate: '-1D',
      minDate: '-6M',
      maxDate: '+0D'
    });
    var dtEnd = new Date(Number(metric.end));
    $('#dtTo').datepicker('setDate', dtEnd);

    $('#statCtrls').buttonset();
    switch(metric.stat) {
    case 'avg':
      break;
    case 'max':
      break;
    case 'n':
      break;
    }

    $('#intvlCtrls').buttonset();

    $('.button').button();
  });

  // Statistic button events
  $('#statAvg').click(function() {
    metric.stat = 'avg';
    updateChart(metric);
  });
  $('#statMax').click(function() {
    metric.stat = 'max';
    updateChart(metric);
  });
  $('#statN').click(function() {
    metric.stat = 'n';
    updateChart(metric);
  });

  // Interval button events
  $('#intvlDay').click(function() {
    metric.interval = 'day';
    updateChart(metric);
  });
  $('#intvlHr').click(function() {
    metric.interval = 'hour';
    updateChart(metric);
  });
  $('#intvlM3').click(function() {
    metric.interval = 'm3';
    updateChart(metric);
  });

  // Calendar events
  $('#dtFrom').datepicker({
    onClose: function(selectedDate) {
      $('#dtTo').datepicker('option', 'minDate', selectedDate);
      var from = Date.parse(selectedDate);
      metric.start = String(from);
      updateChart(metric);
    }
  });
  $('#dtTo').datepicker({
    onClose: function(selectedDate) {
      $('#dtFrom').datepicker('option', 'maxDate', selectedDate);
      var to = Date.parse(selectedDate);
      metric.end = String(to);
      updateChart(metric);
    }
  });

  // Date range buttons events
  $('#prev').click(function() {
    var start = Number(metric.start);
    var end = Number(metric.end);
    var diff = end - start;
    end = start;
    start = start - diff;
    metric.start = String(start);
    metric.end = String(end);
    var dtStart = new Date(start);
    var dtEnd = new Date(end);
    $('#dtFrom').datepicker('setDate', dtStart);
    $('#dtTo').datepicker('setDate', dtEnd);
    updateChart(metric);
  });
  $('#next').click(function() {
    var start = Number(metric.start);
    var end = Number(metric.end);
    var diff = end - start;
    start = end;
    end = end + diff;
    metric.start = String(start);
    metric.end = String(end);
    $('#dtFrom').datepicker('setDate', new Date(start));
    $('#dtTo').datepicker('setDate', new Date(end));
    updateChart(metric);
  });

  var bindData = function(type, data) {
    switch(type) {
      case 'bar':
        barChart(data, configMap.width, configMap.height);
        break;
      case 'hbar':
        hbarChart(data, configMap.width, configMap.height);
        break;
      case 'line':
        lineChart(data, configMap.width, configMap.height);
        break;
    }
  };

  var query = function(metric) {
    var q = 'data?type=' + metric.type + '&metric=' + metric.id;
    if ('stat' in metric) {
      q = q + '&stat=' + metric.stat;
    }
    if ('interval' in metric) {
      q = q + '&interval=' + metric.interval;
    }
    if ('start' in metric) {
      q = q + '&start=' + metric.start;
    }
    if ('end' in metric) {
      q = q + '&end=' + metric.end;
    }
    return q;
  };

  var makeChart = function(metric) {
    // D3 XHR
    var q = query(metric);
    d3.json(q, function(error, d) {
      bindData(metric.type, d);
    });
  };
  makeChart(metric);

  var updateChart = function(metric) {
    var svg = d3.select("#chart svg").remove();
    makeChart(metric);
  };
}
