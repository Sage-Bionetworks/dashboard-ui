
function multiSeriesLineChart(width, height, data) {

    var fields = data.fields,
        values = data.values;

    // Convert the timestamps to Date
    values.forEach(function(row) {row[0] = new Date(row[0] * 1000);});

    // Convert the rest of the values matrix to annotated data series
    var multiSeries = fields
        .filter(function(field) {return field !== "timestamp";})
        .map(function(field, i) {
            return {
                field: field,
                values: values.map(function(row) {
                    return {timestamp: row[0], value: row[i + 1]};
                })};
        });

    // Conventional margins
    var margin = {top: 20, right: 80, bottom: 30, left: 50},
        width = width - margin.left - margin.right,
        height = height - margin.top - margin.bottom;

    // Set up the x-axis
    var xScale = d3.time.scale()
        .range([0, width])
        .domain(d3.extent(values, function(row) { return row[0]; }));

    var xAxis = d3.svg.axis().scale(xScale).orient("bottom");

    // Set up the y-axis
    var yScale = d3.scale.linear()
        .range([height, 0])
        .domain([
            d3.min(values, function(row) {
                return d3.min(row.slice(1, row.length), function(val) { return val; });
            }),
            d3.max(values, function(row) {
                return d3.max(row.slice(1, row.length), function(val) { return val; });
            })
        ]);

    var yAxis = d3.svg.axis()
        .scale(yScale)
        .orient("left");

    // Set up line
    var line = d3.svg.line()
        .interpolate("linear")
        .x(function(d) { return xScale(d.timestamp); })
        .y(function(d) { return yScale(d.value); });

    // Set up color
    var color = d3.scale.category10()
        .domain(fields.filter(function(field) {
            return field !== "timestamp";}));

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
        .text("Latency (ms)");

    // Render the lines
    var dataSeries = svg.selectAll(".dataSeries")
        .data(multiSeries)
        .enter().append("g")
        .attr("class", "dataSeries");

    dataSeries.append("path")
        .attr("class", "line")
        .attr("d", function(d) { return line(d.values); })
        .style("stroke", function(d) { return color(d.field); });

    // Render the labels
    dataSeries.append("text")
        .datum(function(d) {
            return {field: d.field, value: d.values[d.values.length - 1]}; })
        .attr("transform", function(d) {
            return "translate(" + xScale(d.value.timestamp) + "," + yScale(d.value.value) + ")"; })
        .attr("x", 3)
        .attr("dy", ".35em")
        .text(function(d) { return d.field; });
}

function horizontalBarChart(width, height, data) {

    var values = data.fields.map(function(row, i) {
        return {field: row, value: data.values[i]};
    });

    var margin = {top: 60, right: 200, bottom: 20, left: 160},
        width = width - margin.left - margin.right,
        height = height - margin.top - margin.bottom;

    var xScale = d3.scale.log()
        .domain([1, d3.max(values, function(d) { return d.value; })])
        .range([0, width]);

    var xAxis = d3.svg.axis()
        .scale(xScale)
        .orient("top");

    var yScale = d3.scale.ordinal()
        .domain(values.map(function(d) { return d.field; }))
        .rangeRoundBands([0, height], .2);

    var svg = d3.select("#drilldown").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "x axis")
        .call(xAxis);

    svg.selectAll(".bar")
        .data(values)
        .enter().append("rect")
        .attr("class", "bar")
        .attr("x", function(d) { return xScale(1); })
        .attr("y", function(d) { return yScale(d.field); })
        .attr("width", function(d) { return xScale(d.value); })
        .attr("height", yScale.rangeBand());

    svg.selectAll(".barScore")
        .data(values)
        .enter().append("text")
        .attr("x", function(d) { return xScale(d.value); })
        .attr("y", function(d) { return yScale(d.field) + yScale.rangeBand() / 2; })
        .attr("dx", 10)
        .attr("dy", ".36em")
        .attr("text-anchor", "start")
        .attr("class", "barScore")
        .text(function(d) { return d.value; });

    svg.selectAll(".barLabel")
        .data(values)
        .enter().append("text")
        .attr("x", -10)
        .attr("y", function(d) { return yScale(d.field) + yScale.rangeBand() / 2; } )
        .attr("dy", ".36em")
        .attr("text-anchor", "end")
        .attr('class', 'barLabel')
        .text(function(d) { return d.field; }); 
}
