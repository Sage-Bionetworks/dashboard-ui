var dashboard = (function($) {

  ////// Variables

  var createQuery, bindData, makeChart, init,
      scrollToLoad, dtFromOnClose, dtToOnClose, prevOnClick, nextOnClick,
      prevIntvlOnClick, nextIntvlOnClick, getInterval,
      payload = { page: 0 },
      configMap = {
        width: 900,
        height: 550
      };

  ////// Private Functions

  // Creates a data query from the specified metric
  createQuery = function() {
    var metric = payload.metric,
        page = payload.page;
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
    if (page > 0) {
      q = q + '&page=' + page;
    }
    return q;
  };

  // Binds data to chart
  bindData = function() {
    var margin, dateFormat,
        data = payload.data;
    switch(payload.metric.type) {
      case 'category': // TODO: Find a proper metric to use the bar chart
        margin = {top: 20, right: 60, bottom: 20, left: 60},
        data = dashboard.models.unpack(data, { rows: true, yMinMax: true });
        dashboard.charts.bar(data, configMap.width, configMap.height, margin);
        break;
      case 'unique':
      case 'active-user':
        // Hack alert: Convert timestamps to Strings
        dateFormat = d3.time.format.utc('%m/%d');
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
        dashboard.charts.hbar(data, configMap.width, configMap.height * (1 + payload.page), margin);
        break;
      case 'trending':
      case 'latency':
        margin = {top: 20, right: 60, bottom: 20, left: 60};
        data = dashboard.models.unpack(data, { ySeries: true, xMinMax: true, yMinMax: true });
        dashboard.charts.line(data, configMap.width, configMap.height, margin);
        break;
    }
  };

  // Makes a new chart
  makeChart = function() {
    d3.json(createQuery())
    .on('beforesend', function() {
      dashboard.charts.spin(true, configMap.width, configMap.height);
    })
    .on('load', function(json) {
      // Initialize payload
      payload.data = json;
      payload.page = 0;
      // Clear the loading message
      dashboard.charts.spin(false);
      // Bind data to chart
      bindData();
    })
    .on('error', function() {
      dashboard.charts.spin(false);
    })
    .get();
  };

  ////// Event Handlers

  scrollToLoad = function() {
    if ($(window).scrollTop() === $(document).height() - $(window).height()) {
      payload.page = payload.page + 1;
      d3.json(createQuery(), function(error, newData) {
        var emptyData = (newData.xValues.length === 1 && newData.xValues[0].length === 0);
        if (!emptyData) {
          payload.data.xValues = payload.data.xValues.map(function(xSeries, i) {
            return xSeries.concat(newData.xValues[i]);
          });
          payload.data.yValues = payload.data.yValues.map(function(ySeries, i) {
            return ySeries.concat(newData.yValues[i]);
          });
          bindData();
        }
      });
    }
  };

  dtFromOnClose = function(date) {
    var newStart = String(Date.parse(date));
    if (payload.metric.start !== newStart) {
      $('#dtTo').datepicker('option', 'minDate', date);
      payload.metric.start = newStart;
      makeChart(payload.metric);
    }
  };

  dtToOnClose = function(date) {
    var newEnd = String(Date.parse(date));
    if (payload.metric.end !== newEnd) {
      $('#dtFrom').datepicker('option', 'maxDate', date);
      payload.metric.end = newEnd;
      makeChart(payload.metric);
    }
  };

  prevOnClick = function() {
    var start, end, diff, dtStart, dtEnd, metric = payload.metric;
    start = Number(metric.start);
    end = Number(metric.end);
    diff = end - start;
    end = start;
    start = start - diff;
    metric.start = String(start);
    metric.end = String(end);
    dtStart = new Date(start);
    dtEnd = new Date(end);
    $('#dtFrom').datepicker('option', 'maxDate', dtEnd);
    $('#dtTo').datepicker('option', 'minDate', dtStart);
    $('#dtFrom').datepicker('setDate', dtStart);
    $('#dtTo').datepicker('setDate', dtEnd);
    makeChart();
  };

  nextOnClick = function() {
    var start, end, diff, dtStart, dtEnd, metric = payload.metric;
    start = Number(metric.start);
    end = Number(metric.end);
    diff = end - start;
    start = end;
    end = end + diff;
    metric.start = String(start);
    metric.end = String(end);
    dtStart = new Date(start);
    dtEnd = new Date(end);
    $('#dtFrom').datepicker('option', 'maxDate', dtEnd);
    $('#dtTo').datepicker('option', 'minDate', dtStart);
    $('#dtFrom').datepicker('setDate', dtStart);
    $('#dtTo').datepicker('setDate', dtEnd);
    makeChart();
  };

  prevIntvlOnClick = function() {
    var interval, start, metric = payload.metric;
    interval = getInterval();
    start = Number(metric.start) - 86400000 * interval;
    metric.start = String(start);
    metric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart();
  };

  nextIntvlOnClick = function() {
    var interval, start, metric = payload.metric;
    interval = getInterval();
    var start = Number(metric.start) + 86400000 * interval;
    metric.start = String(start);
    metric.end = String(start);
    $('#dtFrom').datepicker('setDate', new Date(start));
    makeChart();
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

    payload.metric = metric;

    // Render the chart first
    makeChart();

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
      makeChart();
    });
    $('#stats #max').click(function() {
      metric.stat = 'max';
      makeChart();
    });
    $('#stats #n').click(function() {
      metric.stat = 'n';
      makeChart();
    });

    // Interval button events
    $('#intvls #month').click(function() {
      metric.interval = 'month';
      makeChart();
    });
    $('#intvls #week').click(function() {
      metric.interval = 'week';
      makeChart();
    });
    $('#intvls #day').click(function() {
      metric.interval = 'day';
      makeChart();
    });
    $('#intvls #hour').click(function() {
      metric.interval = 'hour';
      makeChart();
    });
    $('#intvls #m3').click(function() {
      metric.interval = 'm3';
      makeChart();
    });

    // Date range buttons events
    $('#prev').click(prevOnClick);
    $('#next').click(nextOnClick);
    $('#prevDay').click(prevIntvlOnClick);
    $('#nextDay').click(nextIntvlOnClick);

    // Infinite scroll on the top charts
    if (metric.type === 'top' || metric.type === 'top-by-day') {
      $(window).scroll(scrollToLoad);
    }
  };

  return {init: init};

})(jQuery);
