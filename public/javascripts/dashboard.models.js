dashboard.models = (function() {

  var getSeries, getTimeSeries, groupByX, unpack;

  getSeries = function(data) {

    var xLabel, yLabel, headers, xSeries, xMin, xMax, ySeries, yMin, yMax;

    xLabel = data.headers[0];
    yLabel = data.name;

    headers = data.headers.filter(function(header) { return header !== xLabel; });

    xSeries = data.values.map(function(row) { return row[0]; });
    xMin = d3.min(xSeries),
    xMax = d3.max(xSeries);

    ySeries = headers.map(function(header, index) {
      return {
        header: header,
        values: data.values.map(function(row) {
          return {
            x: row[0],
            y: Number(row[index + 1])
          };
        })
      };
    });

    yMin = d3.min(ySeries, function(series) {
      return d3.min(series.values, function(d) {
        return d.y;
      });
    });

    yMax = d3.max(ySeries, function(series) {
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
  };

  getTimeSeries = function(data) {

    var dataSeries, dateFormat;

    dataSeries = getSeries(data);

    dateFormat = null;
    if ('dateFormat' in data) {
      dateFormat = d3.time.format(data.dateFormat);
    }

    dataSeries.xSeries = dataSeries.xSeries.map(function(dateStr) {
      if (dateFormat != null) {
        return dateFormat.parse(dateStr);
      }
      return new Date(Number(dateStr));
    });

    dataSeries.xMin = d3.min(dataSeries.xSeries);
    dataSeries.xMax = d3.max(dataSeries.xSeries);

    dataSeries.ySeries = dataSeries.ySeries.map(function(series) {
      return {
        header: series.header,
        values: series.values.map(function(d) {
          var xVal = null;
          if (dateFormat != null) {
            xVal = dateFormat.parse(d.x);
          } else {
            xVal = new Date(Number(d.x));
          }
          return {
            x: xVal,
            y: d.y
          };
        })
      };
    });

    return dataSeries;
  };

  groupByX = function(dataSeries) {
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
  };

  createDataSeries = function (data) {
    var xValues = data.xValues.filter(function(val, i) {
      return i === data.xHeaders.indexOf(data.xHeader);
    });
    return {
      xLabel: data.xLabel, // What if this is null?
      yLabel: data.yLabel, // What if this is null?
      x: {
        header: data.xHeader,
        values: xValues
      }
    }
  }

  /**
   * Converts CSV/TSV-like JSON data to annotated series of name-value pairs.
   * Compact CSV-like JSON makes data transfers more efficient and yet does not have
   * the parsing overhead of CSVs.
   *
   * @param data  CSV/TSV-like JSON data. The 1st row must be the headers.
   * @returns Annotated series of name-value pairs.
   */
  unpack = function(data, options) {
    
    if (options) {
      if (options.xMinMax) {
        
      }
      if (options.yMinMax) {
        
      }
      if (options.xSlice) {
        
      }
      if (options.ySlice) {
        
      }
    }
  };

  return { unpack: unpack };
})();

