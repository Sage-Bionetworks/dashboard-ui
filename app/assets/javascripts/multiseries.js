/**
 * Converts CSV/TSV-like JSON data to annotated series of name-value pairs.
 * Compact CSV-like JSON makes data transfers more efficient and yet does not have
 * the parsing overhead of CSVs.
 *
 * @param data  CSV/TSV-like JSON data. The 1st row must be the headers and the 1st column must be the x-column.
 * @returns Annotated series of name-value pairs.
 */
function getSeries(data) {

    var xLabel = data.headers[0],
        yLabel = data.name;

    var headers = data.headers.filter(function(header) { return header !== xLabel; });

    var xSeries = data.values.map(function(row) { return row[0]; });

    var xMin = d3.min(xSeries),
        xMax = d3.max(xSeries);

    var ySeries = headers.map(function(header, index) {
        return {
            header: header,
            values: data.values.map(function(row) {
                return {
                    x: row[0],
                    y: Number(row[index + 1])
                };
            }
        )};
    });

    var yMin = d3.min(ySeries, function(series) {
        return d3.min(series.values, function(d) {
            return d.y;
        });
    });

    var yMax = d3.max(ySeries, function(series) {
        return d3.max(series.values, function(d) {
            return d.y;
        });
    });

    return {
        xLabel: xLabel,
        yLabel: yLabel,
        xMin: xMin,
        xMax: xMax,
        yMin: yMin,
        yMax: yMax,
        headers: headers,
        xSeries: xSeries,
        ySeries: ySeries
    };
}

/**
 * Similar to getSeries(data) except that the x-column is treated as timestamps.
 */
function getTimeSeries(data) {

    var dataSeries = getSeries(data);

    var dateFormat = d3.time.format(data.dateFormat);

    dataSeries.xSeries = dataSeries.xSeries.map(function(dateStr) {
        return dateFormat.parse(dateStr);
    });

    dataSeries.xMin = d3.min(dataSeries.xSeries);
    dataSeries.xMax = d3.max(dataSeries.xSeries);

    dataSeries.ySeries = dataSeries.ySeries.map(function(series) {
        return {
            header: series.header,
            values: series.values.map(function(d) {
                return {
                  x: dateFormat.parse(d.x),
                  y: d.y
                };
            })
        };
    });

    return dataSeries;
}

/**
 * Group by X values.
 */
function groupByX(dataSeries) {
    return dataSeries.xSeries.map(function(x, i) {
        return {
            x: x,
            values: dataSeries.ySeries.map(function(series) {
                return {
                    header: series.header,
                    x: x,
                    y: Number(series.values[i].y)
                };
            })
        };
    });
}
