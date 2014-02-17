dashboard.charts = (function() {

  var isEmptyData, removeSvg, addSvg, addEmptyChart, addChart,
      addAxisX, addAxisY, addPlot,
      bar, hbar, line;

  ////// Private Functions //////

  isEmptyData = function(data) {
    return data.xSeries.length === 0;
  };

  removeSvg = function() {
    d3.select('#chart svg').remove();
  }

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
      .text('[Empty data set. Please select a different time range.]');
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

  //===============================
  // Renders a grouped bar chart.
  //===============================
  bar = function(data, width, height, margin) {

    var w, h, xScale0, xScale1, xAxis, y, yAxis,
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
      .domain(data.xSeries)
      .rangeRoundBands([0, w], 0.2);

    xAxis = d3.svg.axis()
      .scale(xScale0)
      .orient('bottom');

    // At maximum, display 12 ticks on the x-axis
    if (data.xSeries.length > 6) {
      xAxis.tickValues(data.xSeries.filter(function(value, i) {
        return (i % Math.floor(data.xSeries.length / 6)) === 0;
      }));
    }

    // The y-axis
    y = d3.scale.linear()
      .domain([0, data.yMax * 1.5])
      .range([h, 0]);

    yAxis = d3.svg.axis()
      .scale(y)
      .orient('left')
      .tickFormat(d3.format('.2s'));

    // The plot
    svg = addChart(data.xGroups, width, height, margin, xAxis, yAxis);
    chart = svg.chart;
    plot = svg.plot;
    plot.attr('transform', function(d) { return 'translate(' + xScale0(d.x) + ',0)'; });

    xScale1 = d3.scale.ordinal()
      .domain(data.headers)
      .rangeRoundBands([0, xScale0.rangeBand()]);

    color = d3.scale.category10().domain(data.headers.concat('__hover__'));

    plot.selectAll('rect')
      .data(function(group) { return group.values; })
      .enter().append('rect')
      .attr('width', xScale1.rangeBand())
      .attr('x', function(d) { return xScale1(d.header); })
      .attr('y', function(d) { return y(d.y); })
      .attr('height', function(d) { return h - y(d.y); })
      .style('fill', function(d) { return color(d.header); })
      .on('mouseover', function(d) {
        d3.select(this).style('fill', color('__hover__'));
        chart.append('text')
          .text(d.y)
          .attr('id', 'hovertext')
          .attr('text-anchor', 'middle')
          // TODO: The line below should use "xScale1(d.header)".
          // But currently the header is empty.
          // So "xScale0(d.x)" is used here as a temporary hack.
          .attr('x', xScale0(d.x) + xScale1.rangeBand() / 2)
          .attr('y', y(d.y) - 10)
          .attr('fill', 'black');
      })
      .on('mouseout', function(d) {
        d3.select(this).style('fill', color(d.header));
        chart.select('#hovertext').remove();
      });

    // Legend
    legend = chart.selectAll('.legend')
      .data(data.headers.slice())
      .enter().append('g')
      .attr('class', 'legend')
      .attr('transform', function(d, i) { return 'translate(0,' + i * 20 + ')'; });
    legend.append('rect')
      .attr('x', width - 18)
      .attr('width', 18)
      .attr('height', 18)
      .style('fill', color);
    legend.append('text')
      .attr('x', width - 24)
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
        svg, chart, plot, color;

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
    xScale = d3.scale.log()
      .domain([1, data.yMax])
      .range([0, w]);

    xAxis = d3.svg.axis()
      .scale(xScale)
      .orient('top')
      .ticks(5, 'd'); // Ticks are 5 numbers

    // The y scales. Note there is no y-axis on the hbar chart.
    yScale0 = d3.scale.ordinal()
      .domain(data.xSeries)
      .rangeRoundBands([0, h], 0.2);

    yScale1 = d3.scale.ordinal()
      .domain(data.headers)
      .rangeRoundBands([0, yScale0.rangeBand()]);

    // The plot
    svg = addChart(data.xGroups, width, height, margin, xAxis, null);

    plot = svg.plot;
    plot.attr('transform', function(d) { return 'translate(0,' + yScale0(d.x) + ')'; });

    color = d3.scale.category10().domain(data.headers);

    plot.selectAll('rect')
      .data(function(group) { return group.values; })
      .enter().append('rect')
      .attr('class', 'rect')
      .attr('x', function(d) { return xScale(1); })
      .attr('y', function(d) { return yScale1(d.header); })
      .attr('width', function(d) { return xScale(d.y); })
      .attr('height', yScale1.rangeBand())
      .style('fill', function(d) { return color(d.header); });

    plot.selectAll('.score')
      .data(function(group) { return group.values; })
      .enter().append('text')
      .attr('x', function(d) { return xScale(d.y); })
      .attr('y', function(d) { return yScale1(d.header) + yScale1.rangeBand() / 2; })
      .attr('dx', 10)
      .attr('dy', '.36em')
      .attr('text-anchor', 'start')
      .attr('class', 'score')
      .text(function(d) { return d.y; });

    // Labels on the left side
    chart = svg.chart;
    chart.selectAll('.label')
      .data(data.xGroups)
      .enter().append('text')
      .attr('x', -10)
      .attr('y', function(group) { return yScale0(group.x) + yScale0.rangeBand() / 2; })
      .attr('dy', '.36em')
      .attr('text-anchor', 'end')
      .attr('class', 'barLabel')
      .text(function(group) {
        var txt = group.x;
        // TODO: Replace the magic numbers. Hook them up instead to width or margin
        if (txt.length > 36) {
          txt = txt.substring(0, 33);
          txt = txt + '...';
        }
        return txt;
      })
      .on('mouseover', function(d) {
        if (d.x.length > 36) {
          chart.append('text')
            .text(d.x)
            .attr('id', 'hovertext')
            .attr('text-anchor', 'start')
            .attr('x', 10)
            .attr('y', yScale0(d.x) + yScale0.rangeBand() / 2)
            .attr('dy', '.36em')
            .attr('fill', 'orange');
        }
      })
      .on('mouseout', function(d) { chart.select('#hovertext').remove(); });
  };

  //=============================================
  // Renders a multi-series line chart.
  //=============================================
  line = function(data, width, height, margin) {

    var w, h, xScale, xAxis, yScale, yAxis,
        svg, plot, oneLine, color, chart;

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

    oneLine = d3.svg.line()
      .interpolate('linear')
      .x(function(d) { return xScale(d.x); })
      .y(function(d) { return yScale(d.y); });

    color = d3.scale.category10().domain(data.headers);

    plot.append('path')
      .attr('class', 'line')
      .attr('d', function(d) { return oneLine(d.values); })
      .style('stroke', function(d) { return color(d.header); });

    plot.append('text')
      .datum(function(oneSeries) {
        return {
          header: oneSeries.header,
          lastVal: oneSeries.values[oneSeries.values.length - 1]}; })
      .attr('transform', function(d) {
          return 'translate(' + xScale(d.lastVal.x) + ','
            + yScale(d.lastVal.y) + ')'; })
      .attr('x', 5)
      .attr('dy', 10)
      .text(function(d) { return d.header; });

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

  return {
    bar: bar,
    hbar: hbar,
    line: line
  };
})();

