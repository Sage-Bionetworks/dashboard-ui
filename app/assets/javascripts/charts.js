var charts = {};

// ==============================================
// Appends a new SVG.
// ==============================================
charts.svg = function(width, height, margin) {
  d3.select("#chart svg").remove();
  var svg = d3.select('#chart')
      .append('svg')
      .attr('width', width)
      .attr('height', height)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');
  return svg;
};

// ==============================================
// Renders a multi-line chart.
// ==============================================
charts.line = function(data, width, height, margin) {

  var svg = charts.svg(width, height, margin);

  // Empty data
  if (data.xSeries.length === 0) {
    svg.append('text')
      .attr("y", margin.top)
      .text("[Empty data set. Please select a different time range.]");
    return svg;
  }

  // Compute chart width and height
  var width = width - margin.left - margin.right
    , height = height - margin.top - margin.bottom
    ;

  // The x-axis
  var xScale = d3.time.scale()
    .range([0, width])
    .domain([data.xMin, data.xMax]);

  var xAxis = d3.svg.axis().scale(xScale).orient("bottom");

  svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);

  // The y-axis
  var yScale = d3.scale.linear()
    .range([height, 0])
    .domain([data.yMin, data.yMax]);

  var yAxis = d3.svg.axis().scale(yScale).orient("left");

  svg.append("g")
    .attr("class", "y axis")
    .call(yAxis);

  svg.append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", 10)
    .attr("dy", 10)
    .style("text-anchor", "end")
    .text(data.yLabel);

  svg.selectAll(".y.axis g text").forEach(function(tickText) {
    console.debug(tickText);
  });

  // The lines
  var line = d3.svg.line()
    .interpolate("linear")
    .x(function(d) { return xScale(d.x); })
    .y(function(d) { return yScale(d.y); });

  var color = d3.scale.category10().domain(data.headers);

  var lines = svg.selectAll(".plot")
    .data(data.ySeries)
    .enter().append("g")
    .attr("class", "plot");

  lines.append("path")
    .attr("class", "line")
    .attr("d", function(d) { return line(d.values); })
    .style("stroke", function(d) { return color(d.header); });

  lines.append("text")
    .datum(function(oneSeries) {
      return {
        header: oneSeries.header,
        lastVal: oneSeries.values[oneSeries.values.length - 1]}; })
    .attr("transform", function(d) {
        return "translate(" + xScale(d.lastVal.x) + ","
          + yScale(d.lastVal.y) + ")"; })
    .attr("x", 5)
    .attr("dy", 10)
    .text(function(d) { return d.header; });

  return svg;
};

// ==============================================
// Renders a grouped bar chart.
// ==============================================
charts.bar = function(data, width, height, margin) {

  var svg = charts.svg(width, height, margin);

  // Empty data set
  if (data.xSeries.length === 0) {
    svg.append('text')
      .attr("y", margin.top)
      .text("[Empty data set. Please select a different time range.]");
    return svg;
  }

  // Compute chart width and height
  var width = width - margin.left - margin.right
    , height = height - margin.top - margin.bottom
    ;

  // The x-axis
  var x0 = d3.scale.ordinal()
    .domain(data.xSeries)
    .rangeRoundBands([0, width], 0.2);

  var xAxis = d3.svg.axis()
    .scale(x0)
    .orient("bottom");

  // At maximum, display 15 ticks
  if (data.xSeries.length > 7) {
    var step = Math.floor(data.xSeries.length / 7);
    var tickValues = data.xSeries.filter(function(value, i) { return (i % step) === 0; });
    xAxis.tickValues(tickValues);
  }

  svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);

  // The y-axis
  var y = d3.scale.linear()
    .domain([0, data.yMax * 1.5])
    .range([height, 0]);

  var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .tickFormat(d3.format(".2s"));

  svg.append("g")
    .attr("class", "y axis")
    .call(yAxis);

  // The plot
  var color = d3.scale.category10()
    .domain(data.headers.concat("__hover__"));

  var xGroups = data.xGroups;

  var xGroup = svg.selectAll(".plot")
    .data(xGroups)
    .enter().append("g")
    .attr("class", "plot")
    .attr("transform", function(d) { return "translate(" + x0(d.x) + ",0)"; });

  var x1 = d3.scale.ordinal()
    .domain(data.headers)
    .rangeRoundBands([0, x0.rangeBand()]);

  xGroup.selectAll("rect")
    .data(function(group) { return group.values; })
    .enter().append("rect")
    .attr("width", x1.rangeBand())
    .attr("x", function(d) { return x1(d.header); })
    .attr("y", function(d) { return y(d.y); })
    .attr("height", function(d) { return height - y(d.y); })
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
                .attr("fill", "black");} )
    .on("mouseout", function(d) {
            d3.select(this).style("fill", color(d.header));
            svg.select("#hovertext").remove();} );
  
  return svg;

/*
 * Disable legend for now
 *
    var legend = svg.selectAll(".legend")
        .data(dataSeries.headers.slice())
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
*/
};

// ==============================================
// Renders a grouped horizontal bar chart.
// ==============================================
charts.hbar = function(data, width, height, margin) {

  var svg = charts.svg(width, height, margin);

  // Empty data set
  if (data.xSeries.length === 0) {
    svg.append('text')
      .attr("y", 0)
      .text("[Empty data set. Please select a different time range.]");
    return svg;
  }

  var width = width - margin.left - margin.right
    , height = height - margin.top - margin.bottom;

  // The x-axis
  var xScale = d3.scale.log()
    .domain([1, data.yMax])
    .range([0, width]);

  var xAxis = d3.svg.axis()
    .scale(xScale)
    .orient("top")
    .ticks(5, 'd'); // TODO: Make this a function parameter

  svg.append("g")
      .attr("class", "x axis")
      .call(xAxis);

  // The y scales
  var yScale0 = d3.scale.ordinal()
    .domain(data.xSeries)
    .rangeRoundBands([0, height], 0.2);

  var yScale1 = d3.scale.ordinal()
    .domain(data.headers)
    .rangeRoundBands([0, yScale0.rangeBand()]);

  // The plot
  var color = d3.scale.category10()
    .domain(data.headers);

  var groups = data.xGroups;

  var yGroup = svg.selectAll(".plot")
    .data(groups)
    .enter().append("g")
    .attr("class", "plot")
    .attr("transform", function(d) { return "translate(0," + yScale0(d.x) + ")"; });

  yGroup.selectAll("rect")
    .data(function(group) { return group.values; })
    .enter().append("rect")
    .attr("class", "rect")
    .attr("x", function(d) { return xScale(1); })
    .attr("y", function(d) { return yScale1(d.header); })
    .attr("width", function(d) { return xScale(d.y); })
    .attr("height", yScale1.rangeBand())
    .style("fill", function(d) { return color(d.header); });

  yGroup.selectAll(".score")
    .data(function(group) { return group.values; })
    .enter().append("text")
    .attr("x", function(d) { return xScale(d.y); })
    .attr("y", function(d) { return yScale1(d.header) + yScale1.rangeBand() / 2; })
    .attr("dx", 10)
    .attr("dy", ".36em")
    .attr("text-anchor", "start")
    .attr("class", "score")
    .text(function(d) { return d.y; });

  svg.selectAll(".label")
    .data(groups)
    .enter().append("text")
    .attr("x", -10)
    .attr("y", function(group) {
      return yScale0(group.x) + yScale0.rangeBand() / 2; })
    .attr("dy", ".36em")
    .attr("text-anchor", "end")
    .attr("class", "barLabel")
    .text(function(group) {
      var txt = group.x;
      // TODO: Replace the magic numbers. Hook them up instead to width or margin
      if (txt.length > 39) {
        txt = txt.substring(0, 36);
        txt = txt + "...";
      }
      return txt; })
    .on("mouseover", function(d) {
      if (d.x.length > 39) {
        svg.append("text")
          .text(d.x)
          .attr("id", "hovertext")
          .attr("text-anchor", "start")
          .attr("x", 10)
          .attr("y", yScale0(d.x) + yScale0.rangeBand() / 2)
          .attr("dy", ".36em")
          .attr("fill", "orange");
      }})
    .on("mouseout", function(d) { svg.select("#hovertext").remove(); });
}
