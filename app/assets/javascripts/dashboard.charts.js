dashboard.charts = (function() {

  var isEmptyData, removeSvg, removeTable, countUniqueUsers,
      addSvg, addEmptyChart, addChart, addAxisX, addAxisY, addPlot,
      spin, bar, hbar, line, table;

  ////// Private Functions //////

  isEmptyData = function(data) {
    return data.xSeries.values.length === 0;
  };

  removeSvg = function() {
    d3.select('#chart svg').remove();
  };

  removeTable = function() {
    $('#chart #tableSummary').remove();
    $('#chart table').remove();
  };

  countUniqueUsers = function(data, idIndex) {
    var users = [];
    data.rows.map(function(user) {
      if ($.inArray(user.x[idIndex].value, users) == -1) {
        users.push(user.x[idIndex].value);
      }
    });
    return users.length;
  };

  addSvg = function(width, height) {
    return d3.select('#chart')
      .append('svg')
      .attr('width', width)
      .attr('height', height);
  };

  addEmptyChart = function(width, height) {
    var svg = addSvg(width, height);
    svg.append('text')
      .attr('x', width / 12)
      .attr('y', height / 12)
      .text('[Empty data set. Please select a different time range or enter a different value.]');
    return svg;
  };

  addAxisX = function(chart, x, translate) {
    var axis = chart.append('g').attr('class', 'x axis');
    if (x.orient() && x.orient() === 'bottom') {
      axis.attr('transform', 'translate(0,' + translate + ')');
    }
    axis.call(x);
    return axis;
  };

  addAxisY = function(chart, y) {
    var axis = chart.append('g').attr('class', 'y axis');
    axis.call(y);
    return axis;
  };

  addChart = function(data, width, height, margin, x, y) {
    var chart, xAxis, yAxis, plot;
    chart = addSvg(width, height)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    if (x) {
      xAxis = addAxisX(chart, x, height - margin.top - margin.bottom);
    }
    if (y) {
      yAxis = addAxisY(chart, y);
    }
    plot = chart.selectAll('.plot')
      .data(data)
      .enter().append('g')
      .attr('class', 'plot');
    return {
      chart: chart,
      xAxis: xAxis,
      yAxis: yAxis,
      plot: plot
    }
  };

  ////// Public Methods //////

  spin = function(on, width, height) {
    var svg;
    if (on) {
      removeSvg();
      svg = addSvg();
      svg.append('text')
        .attr('x', width / 12)
        .attr('y', height / 12)
        .text('[Loading... Please wait]');
    }
  };

  //===============================
  // Renders a grouped bar chart.
  //===============================
  bar = function(data, width, height, margin) {

    var w, h, xScale0, xScale1, xAxis, yScale, yAxis,
        svg, chart, plot, color, legend;

    // Remove any existing chart
    removeSvg();

    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }

    // Compute chart width and height
    w = width - margin.left - margin.right;
    h = height - margin.top - margin.bottom;

    // The x-axis
    xScale0 = d3.scale.ordinal()
      .domain(data.xSeries.values)
      .rangeRoundBands([0, w], 0.2);

    xAxis = d3.svg.axis()
      .scale(xScale0)
      .orient('bottom');

    // At maximum, display 12 ticks on the x-axis
    if (data.xSeries.values.length > 6) {
      xAxis.tickValues(data.xSeries.values.filter(function(value, i) {
        return (i % Math.floor(data.xSeries.values.length / 6)) === 0;
      }));
    }

    // The y-axis
    yScale = d3.scale.linear()
      .domain([0, data.yMax * 1.5])
      .range([h, 0]);

    yAxis = d3.svg.axis()
      .scale(yScale)
      .orient('left')
      .tickFormat(d3.format('.2s'));

    // The plot
    svg = addChart(data.rows, width, height, margin, xAxis, yAxis);
    chart = svg.chart;
    plot = svg.plot;
    plot.attr('transform', function(row) { return 'translate(' + xScale0(row.x[0].value) + ',0)'; });

    xScale1 = d3.scale.ordinal()
      .domain(data.yHeaders)
      .rangeRoundBands([0, xScale0.rangeBand()]);

    color = d3.scale.category10().domain(data.yHeaders.concat('__hover__'));

    plot.selectAll('rect')
      .data(function(row) {
        return row.y.map(function (y) {
          // Merge in the x value to be used in the mouse-over function
          return {x: row.x[0].value, header: y.header, value: y.value};
        });
      })
      .enter().append('rect')
      .attr('width', xScale1.rangeBand())
      .attr('x', function(y) { return xScale1(y.header); })
      .attr('y', function(y) { return yScale(y.value); })
      .attr('height', function(y) { return h - yScale(y.value); })
      .style('fill', function(y) { return color(y.header); })
      .on('mouseover', function(y) {
        d3.select(this).style('fill', color('__hover__'))
        chart.append('text')
          .text(y.value)
          .attr('id', 'hovertext')
          .attr('text-anchor', 'middle')
          .attr('x', xScale0(y.x) + xScale1(y.header) + xScale1.rangeBand() / 2)
          .attr('y', yScale(y.value) - 10)
          .attr('fill', 'black');
      })
      .on('mouseout', function(d) {
        d3.select(this).style('fill', color(d.header));
        chart.select('#hovertext').remove();
      });

    // Legend
    legend = chart.selectAll('.legend')
      .data(data.yHeaders.slice())
      .enter().append('g')
      .attr('class', 'legend')
      .attr('transform', function(d, i) { return 'translate(0,' + i * 20 + ')'; });
    legend.append('rect')
      .attr('x', w - 18)
      .attr('width', 18)
      .attr('height', 18)
      .style('fill', color);
    legend.append('text')
      .attr('x', w - 24)
      .attr('y', 9)
      .attr('dy', '.35em')
      .style('text-anchor', 'end')
      .text(function(d) { return d; });
  };

  //=============================================
  // Renders a grouped horizontal bar chart.
  //=============================================
  hbar = function(data, width, height, margin) {

    var w, h, xScale, xAxis, yScale0, yScale1,
        svg, chart, plot, color, labelSelection, iUrl, label,
        scoreOffset, labelOffset;

    // Remove any existing chart
    removeSvg();

    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }

    // Compute chart width and height
    w = width - margin.left - margin.right;
    h = height - margin.top - margin.bottom;

    // The x scale. There is no x-axis.
    xScale = d3.scale.linear()
      .domain([1, data.yMax])
      .range([0, w]);

    // The y scales. Note there is no y-axis on the hbar chart, only the scales.
    yScale0 = d3.scale.ordinal()
      .domain(data.xSeries.values)
      .rangeRoundBands([0, h], 0.2);

    yScale1 = d3.scale.ordinal()
      .domain(data.yHeaders)
      .rangeRoundBands([0, yScale0.rangeBand()]);

    // The plot
    svg = addChart(data.rows, width, height, margin, null, null);

    plot = svg.plot;
    plot.attr('transform', function(row) { return 'translate(0,' + yScale0(row.x[0].value) + ')'; });

    color = d3.scale.category10().domain(data.yHeaders);

    plot.selectAll('rect')
      .data(function(row) { return row.y; })
      .enter().append('rect')
      .attr('class', 'rect')
      .attr('x', function(y) { return xScale(1); })
      .attr('y', function(y) { return yScale1(y.header); })
      .attr('width', function(y) { return xScale(y.value); })
      .attr('height', yScale1.rangeBand())
      .style('fill', function(y) { return color(y.header); });

    // Scores
    scoreOffset = xScale(data.yMax) + 20;
    plot.selectAll('.score')
      .data(function(row) { return row.y; })
      .enter().append('text')
      .attr('x', scoreOffset)
      .attr('y', function(y) { return yScale1(y.header) + yScale1.rangeBand() / 2; })
      .attr('dx', 10)
      .attr('dy', '.36em')
      .attr('text-anchor', 'start')
      .attr('class', 'score')
      .text(function(y) { return y.value; });

    // Labels on the right side
    chart = svg.chart;
    labelSelection = chart.selectAll('.label')
      .data(data.rows)
      .enter();

    // If we have a 'url' x-header, we need to render the label as a link
    iUrl = data.xHeaders.reduce(function(prev, curr, i) {
      if ('url' === curr) {
        return i;
      }
    }, -1);

    if (iUrl >= 0) {
      labels = labelSelection.append('svg:a')
        .attr('xlink:href', function(row){
          return row.x[iUrl].value;
        })
        .append('text');
    } else {
      labels = labelSelection.append('text');
    }

    labelOffset = 200 + data.yMax.toString().length * 2 - margin.left;
    labels.attr('x', labelOffset)
      .attr('y', function(row) { return yScale0(row.x[0].value) + yScale0.rangeBand() / 2; })
      .attr('dy', '.36em')
      .attr('text-anchor', 'start')
      .attr('class', 'label')
      .text(function(row) { return row.x[0].value; });
  };

  //=============================================
  // Renders a multi-series line chart.
  //=============================================
  line = function(data, width, height, margin) {

    var w, h, xScale, xAxis, yScale, yAxis,
        svg, plot, d3Line, color, chart;

    // Remove any existing chart
    removeSvg();

    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }

    // Compute chart width and height
    w = width - margin.left - margin.right;
    h = height - margin.top - margin.bottom;

    // The x-axis
    xScale = d3.time.scale()
      .range([0, w])
      .domain([data.xMin, data.xMax]);

    xAxis = d3.svg.axis().scale(xScale).orient('bottom');

    // The y-axis
    yScale = d3.scale.linear()
      .range([h, 0])
      .domain([data.yMin, data.yMax]);

    yAxis = d3.svg.axis().scale(yScale).orient('left');

    // The plot
    svg = addChart(data.ySeries, width, height, margin, xAxis, yAxis);
    plot = svg.plot;

    d3Line = d3.svg.line()
      .interpolate('linear')
      .x(function(yValue, i) { return xScale(data.xSeries.values[i]); })
      .y(function(yValue) { return yScale(yValue); });

    color = d3.scale.category10().domain(data.yHeaders);

    plot.append('path')
      .attr('class', 'line')
      .attr('d', function(ySeries) { return d3Line(ySeries.values); })
      .style('stroke', function(ySeries) { return color(ySeries.header); });

    plot.append('text')
      .datum(function(ySeries) {
        return {
          header: ySeries.header,
          lastVal: ySeries.values[ySeries.values.length - 1]};
        }
      )
      .attr('transform', function(datum) {
        return 'translate(' + (w + 2) + ','
            + yScale(datum.lastVal) + ')';
        }
      )
      .attr('x', 5)
      .attr('dy', 10)
      .text(function(datum) { return datum.header; });

    // The y-axis label
    chart = svg.chart;
    chart.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', 10)
      .attr('dy', 10)
      .style('text-anchor', 'end')
      .text(data.yLabel);

    // Reset the max on y ticks
    chart.selectAll('.y.axis g text').on('click', function(tickText) {
      if (!data.yMaxOriginal) {
        data.yMaxOriginal = data.yMax;
      }
      data.yMax = Number(tickText);
      line(data, width, height, margin);
    });
    chart.selectAll('.y.axis path').on('click', function(axis) {
      if (data.yMaxOriginal) {
        data.yMax = data.yMaxOriginal;
        delete data['yMaxOriginal'];
        line(data, width, height, margin);
      }
    });
  };

  //=============================================
  // Renders a table.
  //=============================================
  table = function(data, width, height, margin) {

    var tableData, headers, rows, summary, idIndex, urlIndex, noUsers, total;

    // Remove any existing chart or table
    removeSvg();
    removeTable();

    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }

    // get the indexes and the total # of results
    idIndex = jQuery.inArray("id", data.xHeaders);
    urlIndex = jQuery.inArray("url", data.xHeaders);
    total = data.rows.length;
    noUsers = countUniqueUsers(data, idIndex);

    // generate the summary report
    summary = "<div id='tableSummary' class='lead'><p>There are " + total + " results, " 
            + noUsers + " unique users.</p></div>"

    // Get the table data 
    tableData = "<table class='table table-hover'>";

    headers = "<tr>";
    data.xHeaders.map(function(header) {
      if (header != "url" && header != "id") {
        headers += "<th class='lead' >" + header + "</th>";
      }
    });
    headers += "</tr>";
    tableData += headers;

    rows = "";
    data.rows.map(function(row) {
      var rowData = "<tr>";
      row.x.map(function(obj) {
        if (obj.header != "url" && obj.header != "id") {
          // merge the url to username
          if (obj.header == "name") {
            rowData += "<td><a href='" + row.x[urlIndex].value + "' target='_blank'>" + obj.value + "</a></td>";
          } else {
            rowData += "<td>" + obj.value + "</td>";
          }
        }
      });
      rowData += "</tr>";
      rows += rowData;
    });
    tableData += rows;
    tableData += "</table>";

    // Add the summary and table
    $('#chart').append(summary);
    $('#chart').append(tableData);
    return;

  };

  return {
    spin: spin,
    bar: bar,
    hbar: hbar,
    line: line,
    table: table
  };
})();

