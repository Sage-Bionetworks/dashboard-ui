var dashboard = (function($) {

  ////// Variables

  var createQuery, bindData, makeChart, init,
      dtFromOnClose, dtToOnClose, prevOnClick, nextOnClick,
      prevDayOnClick, nextDayOnClick, thisMetric,
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
  bindData = function(chartType, data) {
    switch(chartType) {
      case 'bar':
        // TODO: Pass in margin as a parameter. Define margin as part of a metric.
        var margin = {top: 20, right: 60, bottom: 20, left: 60},
            dateFormat = d3.time.format('%m/%d');
        // TODO: Formatting hack. Time series should not use bar chart.
        data.values.forEach(function(row) {
            row[0] = dateFormat(new Date(Number(row[0])));
        });
        data = dashboard.models.unpack(data, { groupByX: true });
        dashboard.charts.bar(data, configMap.width, configMap.height, margin);
        break;
      case 'hbar':
        var margin = {top: 60, right: 100, bottom: 20, left: 300};
        data = dashboard.models.unpack(data, { groupByX: true });
        dashboard.charts.hbar(data, configMap.width, configMap.height, margin);
        break;
      case 'line':
        var margin = {top: 20, right: 60, bottom: 20, left: 60};
        data = dashboard.models.unpack(data, { timeSeries: true });
        dashboard.charts.line(data, configMap.width, configMap.height, margin);
        break;
    }
  };

  // Makes a new chart
  makeChart = function(metric) {
    var q = createQuery(metric);
    d3.json(q, function(error, d) {
      bindData(metric.type, d);
    });
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

  prevDayOnClick = function() {
    var start = Number(thisMetric.start) - 86400000;
    thisMetric.start = String(start);
    thisMetric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart(thisMetric);
  };

  nextDayOnClick = function() {
    var start = Number(thisMetric.start) + 86400000;
    thisMetric.start = String(start);
    thisMetric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart(thisMetric);
  };

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
      minDate: '-7M',
      maxDate: '+0D',
      onClose: dtFromOnClose
    });
    $('#dtFrom').datepicker('setDate', dtStart);

    $('#dtTo').datepicker({
      defaultDate: '+0D',
      minDate: '-7M',
      maxDate: '+0D',
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
    $('#prevDay').click(prevDayOnClick);
    $('#nextDay').click(nextDayOnClick);
  };

  return {init: init};

})(jQuery);
