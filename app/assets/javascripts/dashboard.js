var dashboard = (function($) {

  ////// Variables

  var createQuery, bindData, makeChart, init,
      dtFromOnClose, dtToOnClose, prevOnClick, nextOnClick,
      prevIntvlOnClick, nextIntvlOnClick, getInterval, thisMetric,
      configMap = {
        width: 900,
        height: 550
      };

  ////// Private Functions

  // Creates a data query from the specified metric
  createQuery = function(metric) {
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

  // Binds data to chart
  bindData = function(metricType, data) {
    var margin, dateFormat;
    switch(metricType) {
      case 'category': // TODO: Find a proper metric to use the bar chart
        margin = {top: 20, right: 60, bottom: 20, left: 60},
        data = dashboard.models.unpack(data, { rows: true, yMinMax: true });
        dashboard.charts.bar(data, configMap.width, configMap.height, margin);
        break;
      case 'unique':
      case 'active-user':
        // Hack alert: Convert timestamps to Strings
        dateFormat = d3.time.format('%m/%d');
        data.xValues[0].forEach(function(val, i) {
          data.xValues[0][i] = dateFormat(new Date(Number(val)));
        });
        data.xHeaders[0] = 'datetime';
        margin = {top: 20, right: 60, bottom: 20, left: 60},
        data = dashboard.models.unpack(data, { rows: true, yMinMax: true });
        // TODO: Bar chart for now. Should use a more proper chart for time series
        dashboard.charts.bar(data, configMap.width, configMap.height, margin);
        break;
      case 'top':
      case 'top-by-day':
        margin = {top: 20, right: 800, bottom: 20, left: 20};
        data = dashboard.models.unpack(data, { rows: true, yMinMax: true });
        dashboard.charts.hbar(data, configMap.width, configMap.height, margin);
        break;
      case 'latency':
        margin = {top: 20, right: 60, bottom: 20, left: 60};
        data = dashboard.models.unpack(data, { ySeries: true, xMinMax: true, yMinMax: true });
        dashboard.charts.line(data, configMap.width, configMap.height, margin);
        break;
    }
  };

  // Makes a new chart
  makeChart = function(metric) {
    d3.json(createQuery(metric))
    .on('beforesend', function() {
      dashboard.charts.spin(true, configMap.width, configMap.height);
    })
    .on('load', function(json) {
      dashboard.charts.spin(false);
      bindData(metric.type, json);
    })
    .on('error', function() {
      dashboard.charts.spin(false);
    })
    .get();
  };

  ////// Event Handlers

  dtFromOnClose = function(date) {
    var newStart = String(Date.parse(date));
    if (thisMetric.start !== newStart) {
      $('#dtTo').datepicker('option', 'minDate', date);
      thisMetric.start = newStart;
      makeChart(thisMetric);
    }
  };

  dtToOnClose = function(date) {
    var newEnd = String(Date.parse(date));
    if (thisMetric.end !== newEnd) {
      $('#dtFrom').datepicker('option', 'maxDate', date);
      thisMetric.end = newEnd;
      makeChart(thisMetric);
    }
  };

  prevOnClick = function() {
    var start, end, diff, dtStart, dtEnd;
    start = Number(thisMetric.start);
    end = Number(thisMetric.end);
    diff = end - start;
    end = start;
    start = start - diff;
    thisMetric.start = String(start);
    thisMetric.end = String(end);
    dtStart = new Date(start);
    dtEnd = new Date(end);
    $('#dtFrom').datepicker('option', 'maxDate', dtEnd);
    $('#dtTo').datepicker('option', 'minDate', dtStart);
    $('#dtFrom').datepicker('setDate', dtStart);
    $('#dtTo').datepicker('setDate', dtEnd);
    makeChart(thisMetric);
  };

  nextOnClick = function() {
    var start, end, diff, dtStart, dtEnd;
    start = Number(thisMetric.start);
    end = Number(thisMetric.end);
    diff = end - start;
    start = end;
    end = end + diff;
    thisMetric.start = String(start);
    thisMetric.end = String(end);
    dtStart = new Date(start);
    dtEnd = new Date(end);
    $('#dtFrom').datepicker('option', 'maxDate', dtEnd);
    $('#dtTo').datepicker('option', 'minDate', dtStart);
    $('#dtFrom').datepicker('setDate', dtStart);
    $('#dtTo').datepicker('setDate', dtEnd);
    makeChart(thisMetric);
  };

  prevIntvlOnClick = function() {
    var interval, start;
    interval = getInterval();
    start = Number(thisMetric.start) - 86400000 * interval;
    thisMetric.start = String(start);
    thisMetric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart(thisMetric);
  };

  nextIntvlOnClick = function() {
    var interval, start;
    interval = getInterval();
    var start = Number(thisMetric.start) + 86400000 * interval;
    thisMetric.start = String(start);
    thisMetric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart(thisMetric);
  };

  getInterval = function() {
    if ($('#intvls #day').is(':checked')) {
      return 1;
    }
    if ($('#intvls #week').is(':checked')) {
      return 7;
    }
    if ($('#intvls #month').is(':checked')) {
      return 30;
    }
  }

  ////// Public Functions

  // Initializes the dashboard JavaScript controller
  init = function(metric) {

    thisMetric = metric;

    // Render the chart first
    makeChart(metric);

    // Set up jQueryUI
    var dtStart = new Date(Number(metric.start)),
        dtEnd = new Date(Number(metric.end));

    $('#dtFrom').datepicker({
      defaultDate: '-7D',
      onClose: dtFromOnClose
    });
    $('#dtFrom').datepicker('setDate', dtStart);

    $('#dtTo').datepicker({
      defaultDate: '+0D',
      onClose: dtToOnClose
    });
    $('#dtTo').datepicker('setDate', dtEnd);

    $('#stats').buttonset();
    $('#stats #' + metric.stat).attr('checked', 'checked');
    $('#stats').buttonset('refresh');

    $('#intvls').buttonset();
    $('#intvls #' + metric.interval).attr('checked', 'checked');
    $('#intvls').buttonset('refresh');

    $('.button').button();

    // Statistic button events
    $('#stats #avg').click(function() {
      metric.stat = 'avg';
      makeChart(metric);
    });
    $('#stats #max').click(function() {
      metric.stat = 'max';
      makeChart(metric);
    });
    $('#stats #n').click(function() {
      metric.stat = 'n';
      makeChart(metric);
    });

    // Interval button events
    $('#intvls #month').click(function() {
      metric.interval = 'month';
      makeChart(metric);
    });
    $('#intvls #week').click(function() {
      metric.interval = 'week';
      makeChart(metric);
    });
    $('#intvls #day').click(function() {
      metric.interval = 'day';
      makeChart(metric);
    });
    $('#intvls #hour').click(function() {
      metric.interval = 'hour';
      makeChart(metric);
    });
    $('#intvls #m3').click(function() {
      metric.interval = 'm3';
      makeChart(metric);
    });

    // Date range buttons events
    $('#prev').click(prevOnClick);
    $('#next').click(nextOnClick);
    $('#prevDay').click(prevIntvlOnClick);
    $('#nextDay').click(nextIntvlOnClick);
  };

  return {init: init};

})(jQuery);
