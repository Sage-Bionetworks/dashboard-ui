var dashboard;
dashboard.charts = (function() {

  var isEmptyData, removeSvg, addSvg, addEmptyChart, addChart,
      addAxisX, addAxisY, addPlot,
      bar, hbar, line;

  ////// Private Functions //////

  isEmptyData = function(data) {
    return data.xSeries.length === 0;
  };

  removeSvg = function() {
    d3.select("#chart svg").remove();
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
    var axis = chart.append('g')
      .attr('class', 'x axis');
    var pp = x.orient();
    console.debug(typeof pp);
    console.debug(pp);
    if (x.orient() && x.orient() === 'bottom') {
      console.debug("Yay!!!");
      axis.attr('transform', 'translate(0,' + translate + ')');
    }
    axis.call(x);
    return axis;
  };

  addAxisY = function(chart, y) {
    var axis = chart.append('g')
      .attr('class', 'y axis');
    axis.call(y);
    return axis;
  };

  addChart = function(data, width, height, margin, x, y) {
    var chart, xAxis, yAxis, plot;
    removeSvg();
    chart = addSvg(width, height)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
    xAxis = addAxisX(chart, x, height);
    yAxis = addAxisY(chart, y);
    plot = chart.selectAll(".plot")
      .data(data)
      .enter().append("g")
      .attr("class", "plot");
    return {
      chart: chart,
      xAxis: xAxis,
      yAxis: yAxis,
      plot: plot
    }
  };

  ////// Public Methods //////

  //===============================
  //Renders a grouped bar chart.
  //===============================
  bar = function(data, width, height, margin) {

    var w, h, x0, x1, xAxis, y, yAxis,
        chart, plot, color, legend;

    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }

    // Compute chart width and height
    w = width - margin.left - margin.right;
    h = height - margin.top - margin.bottom;

    // The x-axis
    x0 = d3.scale.ordinal()
      .domain(data.xSeries)
      .rangeRoundBands([0, w], 0.2);

    xAxis = d3.svg.axis()
      .scale(x0)
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
      .orient("left")
      .tickFormat(d3.format(".2s"));

    // The plot
    chart = addChart(data.xGroups, width, height, margin, xAxis, yAxis);
    plot = chart.plot;
    plot.attr("transform", function(d) { return "translate(" + x0(d.x) + ",0)"; });

    x1 = d3.scale.ordinal()
      .domain(data.headers)
      .rangeRoundBands([0, x0.rangeBand()]);

    color = d3.scale.category10().domain(data.headers.concat("__hover__"));

    plot.selectAll("rect")
      .data(function(group) { return group.values; })
      .enter().append("rect")
      .attr("width", x1.rangeBand())
      .attr("x", function(d) { return x1(d.header); })
      .attr("y", function(d) { return y(d.y); })
      .attr("height", function(d) { return h - y(d.y); })
      .style("fill", function(d) { return color(d.header); })
      .on("mouseover", function(d) {
        d3.select(this).style("fill", color("__hover__"));
        svg.append("text")
          .text(d.y)
          .attr("id", "hovertext")
          .attr("text-anchor", "middle")
          // TODO: The line below should use "x1(d.header)".
          // But currently the header is empty.
          // So "x0(d.x)" is used here as a temporary hack.
          .attr("x", x0(d.x) + x1.rangeBand() / 2)
          .attr("y", y(d.y) - 10)
          .attr("fill", "black");
      })
      .on("mouseout", function(d) {
        d3.select(this).style("fill", color(d.header));
        svg.select("#hovertext").remove();
      });

    legend = chart.chart.selectAll(".legend")
      .data(data.headers.slice())
      .enter().append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

    legend.append("rect")
      .attr("x", width - 18)
      .attr("width", 18)
      .attr("height", 18)
      .style("fill", color);

    legend.append("text")
      .attr("x", width - 24)
      .attr("y", 9)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text(function(d) { return d; });
  };

  hbar = function(data, width, height, margin) {
    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }
  };

  line = function(data, width, height, margin) {
    // Empty data set
    if (isEmptyData(data)) {
      addEmptyChart(width, height);
      return;
    }
  };

  return {
    bar: bar,
    hbar: hbar,
    line: line
  };
})();

