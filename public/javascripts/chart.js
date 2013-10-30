
function multiSeriesLineChart(data, width, height) {

    var timeSeries = getTimeSeries(data);

    // Conventional margins
    var margin = {top: 20, right: 80, bottom: 30, left: 50},
        width = width - margin.left - margin.right,
        height = height - margin.top - margin.bottom;

    // Set up the x-axis
    var xScale = d3.time.scale()
        .range([0, width])
        .domain([timeSeries.xMin, timeSeries.xMax]);

    var xAxis = d3.svg.axis().scale(xScale).orient("bottom");

    // Set up the y-axis
    var yScale = d3.scale.linear()
        .range([height, 0])
        .domain([timeSeries.yMin, timeSeries.yMax]);

    var yAxis = d3.svg.axis().scale(yScale).orient("left");

    // Set up line
    var line = d3.svg.line()
        .interpolate("linear")
        .x(function(d) { return xScale(d.x); })
        .y(function(d) { return yScale(d.y); });

    // Set up color
    var color = d3.scale.category10().domain(timeSeries.headers);

    // Render SVG
    var svg = d3.select("#chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    // Render the x-axis
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    // Render the y-axis
    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".71em")
        .style("text-anchor", "end")
        .text(timeSeries.yLabel);

    // Render the lines
    var dataSeries = svg.selectAll(".timeSeries")
        .data(timeSeries.ySeries)
        .enter().append("g")
        .attr("class", "timeSeries");

    dataSeries.append("path")
        .attr("class", "line")
        .attr("d", function(d) { return line(d.values); })
        .style("stroke", function(d) { return color(d.header); });

    // Render the labels
    dataSeries.append("text")
        .datum(function(oneSeries) {
            return {
                header: oneSeries.header,
                lastVal: oneSeries.values[oneSeries.values.length - 1]
            }; })
        .attr("transform", function(d) {
            return "translate(" + xScale(d.lastVal.x) + "," + yScale(d.lastVal.y) + ")";
            })
        .attr("x", 3)
        .attr("dy", ".35em")
        .text(function(d) { return d.header; });
}

function groupedBarChart(data, width, height) {

    var dataSeries = getSeries(data);

    var margin = {top: 20, right: 60, bottom: 20, left: 60},
        width = width - margin.left - margin.right,
        height = height - margin.top - margin.bottom;

    var x0 = d3.scale.ordinal()
        .domain(dataSeries.xSeries)
        .rangeRoundBands([0, width], 0.2);

    var x1 = d3.scale.ordinal()
        .domain(dataSeries.headers)
        .rangeRoundBands([0, x0.rangeBand()]);

    var xAxis = d3.svg.axis()
        .scale(x0)
        .orient("bottom");

    var y = d3.scale.linear()
        .domain([0, dataSeries.yMax * 1.5])
        .range([height, 0]);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .tickFormat(d3.format(".2s"));

    var color = d3.scale.category10()
        .domain(dataSeries.headers);

    var svg = d3.select("#chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    var xGroups = groupByX(dataSeries);

    var xGroup = svg.selectAll(".group")
        .data(xGroups)
        .enter().append("g")
        .attr("class", "group")
        .attr("transform", function(d) { return "translate(" + x0(d.x) + ",0)"; });

    xGroup.selectAll("rect")
        .data(function(group) { return group.values; })
        .enter().append("rect")
        .attr("width", x1.rangeBand())
        .attr("x", function(d) { return x1(d.header); })
        .attr("y", function(d) { return y(d.y); })
        .attr("height", function(d) { return height - y(d.y); })
        .style("fill", function(d) { return color(d.header); });

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
}

function hGroupedBarChart(data, width, height) {

    var dataSeries = getSeries(data);

    var margin = {top: 60, right: 200, bottom: 20, left: 160},
        width = width - margin.left - margin.right,
        height = height - margin.top - margin.bottom;

    var xScale = d3.scale.log()
        .domain([1, dataSeries.yMax])
        .range([0, width]);

    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("top");

    var yScale0 = d3.scale.ordinal()
        .domain(dataSeries.xSeries)
        .rangeRoundBands([0, height], 0.2);

    var yScale1 = d3.scale.ordinal()
        .domain(dataSeries.headers)
        .rangeRoundBands([0, yScale0.rangeBand()]);

    var color = d3.scale.category10()
        .domain(dataSeries.headers);

    var svg = d3.select("#chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "x axis")
        .call(xAxis);

    var groups = groupByX(dataSeries);

    var yGroup = svg.selectAll(".group")
        .data(groups)
        .enter().append("g")
        .attr("class", "group")
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
        .attr("y", function(group) { return yScale0(group.x) + yScale0.rangeBand() / 2; } )
        .attr("dy", ".36em")
        .attr("text-anchor", "end")
        .attr('class', 'barLabel')
        .text(function(group) { return group.x; });
}
