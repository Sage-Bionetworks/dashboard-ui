dashboard.models = (function() {

  var attachHeaders, transpose, unpack;

  // Attaches headers to values to get name-value pairs.
  attachHeaders = function(headers, values) {
    return values.map(function(row, i) {
      var header = headers[i];
      return row.map(function(value) {
        return {
          header: header,
          value: value
        };
      });
    });
  };

  // Transposes a 2-D array.
  transpose = function(array) {
    return array[0].map(function(col, j) {
      return array.map(function(row) {
        return row[j];
      });
    });
  }

  /**
   * Converts CSV/TSV-like JSON data to annotated series of name-value pairs.
   * Compact CSV-like JSON makes data transfers more efficient and yet does not have
   * the parsing overhead of CSVs. But they are not proper for rendering charts.
   *
   * @param data  CSV/TSV-like JSON data. The 1st row must be the headers.
   * @returns Annotated series of name-value pairs.
   */
  unpack = function(data, options) {
    var results, timeSeries, xValues, yValues, xRows, yRows;
    // Get the headers first
    results = {
        xHeaders: data.xHeaders,
        yHeaders: data.yHeaders,
    };
    // Set up labels
    if (data.xLabel) {
      results.xLabel = data.xLabel;
    }
    if (data.yLabel) {
      results.yLabel = data.yLabel;
    }
    // Convert time series
    timeSeries = (options && options.timeSeries);
    xValues = data.xValues.map(function(row, i) {
      timeSeries = timeSeries && i === 0;
      return row.map(function(value) {
        if (timeSeries) {
          return new Date(Number(value));
        }
        return value;
      });
    });
    // Extract the first row of the x values as the x series
    // Data of the y series are plotted against this x series
    results.xSeries = {
      header: data.xHeaders[0],
      values: xValues[0]
    };
    // Convert all the y values to numbers
    yValues = data.yValues.map(function(row) {
      return row.map(function(value) {
        return Number(value);
      });
    });
    // Get the y series
    xRows = transpose(attachHeaders(data.xHeaders, xValues));
    yRows = transpose(attachHeaders(data.yHeaders, yValues));
    results.ySeries = xRows.map(function(xRow, i) {
      return {
        x: xRow,
        y: yRows[i]
      };
    });
    // Min and max
    if (options) {
      if (options.xMinMax) {
        results.xMin = d3.min(results.xSeries.values, function(value) {
          return value;
        });
        results.xMax = d3.max(results.xSeries.values, function(value) {
          return value;
        });
      }
      if (options.yMinMax) {
        results.yMin = d3.min(yValues, function(row) {
          return d3.min(row, function(val) {
            return Number(val);
          });
        });
        results.yMax = d3.max(yValues, function(row) {
          return d3.max(row, function(val) {
            return Number(val);
          });
        });
      }
    }
    return results;
  };

  return { unpack: unpack };
})();

