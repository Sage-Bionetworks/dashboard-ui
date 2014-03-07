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
    var results, xValues, yValues, xRows, yRows;
    // Set the headers
    results = {
        xHeaders: data.xHeaders,
        yHeaders: data.yHeaders,
    };
    // Set up the labels
    if (data.xLabel) {
      results.xLabel = data.xLabel;
    }
    if (data.yLabel) {
      results.yLabel = data.yLabel;
    }
    // Process the x values. Convert time stamps.
    xValues = data.xValues.map(function(xRow, i) {
      var isTimestamp = ("timestamp" === data.xHeaders[i]);
      return xRow.map(function(xValue) {
        if (isTimestamp) {
          return new Date(Number(xValue));
        }
        return xValue;
      });
    });
    // Process the y values. Convert to numbers.
    yValues = data.yValues.map(function(yRow) {
      return yRow.map(function(yValue) {
        return Number(yValue);
      });
    });
    // Extract the first row of the x values as the x series
    // Data of the y series are plotted against this single x series
    results.xSeries = {
      header: data.xHeaders[0],
      values: xValues[0]
    };
    if (options) {
      // Min and max
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
      // Include y series. This can be multi-series.
      if (options.ySeries) {
        results.ySeries = yValues.map(function(yRow, i) {
          var yHeader = data.yHeaders[i];
          return {
            header: yHeader,
            values: yRow
          };
        });
      }
      // Present the data as rows
      if (options.rows) {
        xRows = transpose(attachHeaders(data.xHeaders, xValues));
        yRows = transpose(attachHeaders(data.yHeaders, yValues));
        results.rows = xRows.map(function(xRow, i) {
          return {
            x: xRow,
            y: yRows[i]
          };
        });
      }
    }
    return results;
  };

  return { unpack: unpack };
})();

