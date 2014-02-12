window = require('jsdom').jsdom().createWindow();
jQuery = require('jquery');
d3 = require('d3');
dashboard = require('../public/javascripts/dashboard.js');
require('../public/javascripts/dashboard.models.js');
var assert = require('assert');
describe('dashboard.models', function() {
  describe('unpack()', function() {
    it('should unpack the data matrix', function() {
      var input, expected, output;
      input = {
        name: "Count",
        headers: ["Client", "All", "Public", "Non-Sage", "Public, Non-Sage"],
        values: [
          ["web", 151, 121, "93", 77],
          ["python", 95, "62", 79, 39],
          ["r", 55, 32, 29, 6]
        ]
      };
      expected = {
        xLabel: "Client",
        yLabel: "Count",
        headers: ["All", "Public", "Non-Sage", "Public, Non-Sage"],
        xSeries: ["web", "python", "r"],
        ySeries: [
          {
            header: "All",
            values: [
              {x: "web", y: 151},
              {x: "python", y: 95},
              {x: "r", y: 55}
            ]
          },{
            header: "Public",
            values: [
              {x: "web", y: 121},
              {x: "python", y: 62},
              {x: "r", y: 32}
            ]
          },{
            header: "Non-Sage",
            values: [
              {x: "web", y: 93},
              {x: "python", y: 79},
              {x: "r", y: 29}
            ]
          },{
            header: "Public, Non-Sage",
            values: [
              {x: "web", y: 77},
              {x: "python", y: 39},
              {x: "r", y: 6}
            ]
          }
        ],
        xMin: "python",
        xMax: "web",
        yMin: 6,
        yMax: 151
      };
      output = dashboard.models.unpack(input);
      assert.deepEqual(output, expected);
    });
    it('should unpack the time series', function() {
      var input, expected, output;
      input = {
        dateFormat: "%Y-%m",
        name: "Temperature",
        headers: ["Month", "Austin", "New York", "Seattle"],
        values: [
          ["2013-01", "50", "33", "47"],
          ["2013-02", 52, 35, 51],
          ["2013-03", 55, 42, 56]
        ]
      };
      expected = {
        xLabel: "Month",
        yLabel: "Temperature",
        xMin: new Date(2013, 0),
        xMax: new Date(2013, 2),
        yMin: 33,
        yMax: 56,
        headers: ["Austin", "New York", "Seattle"],
        xSeries: [new Date(2013, 0), new Date(2013, 1), new Date(2013, 2)],
        ySeries: [
          {
            header: "Austin",
            values: [
              {x: new Date(2013, 0), y: 50},
              {x: new Date(2013, 1), y: 52},
              {x: new Date(2013, 2), y: 55}
            ]
          },{
            header: "New York",
            values: [
              {x: new Date(2013, 0), y: 33},
              {x: new Date(2013, 1), y: 35},
              {x: new Date(2013, 2), y: 42}
            ]
          },{
            header: "Seattle",
            values: [
              {x: new Date(2013, 0), y: 47},
              {x: new Date(2013, 1), y: 51},
              {x: new Date(2013, 2), y: 56}
            ]
          }
        ]
      };
      output = dashboard.models.unpack(input, { timeSeries: true });
      assert.deepEqual(output, expected);
    });
    it('should group by X', function() {
      var input, expected, output;
      input = {
        dateFormat: "%Y-%m",
        name: "Temperature",
        headers: ["Month", "Austin", "New York", "Seattle"],
        values: [
          ["2013-01", "50", "33", "47"],
          ["2013-02", 52, 35, 51],
          ["2013-03", 55, 42, 56]
        ]
      };
      expected = [
        {
          x: new Date(2013, 0),
          values: [
            {header: "Austin", x: new Date(2013, 0), y: 50},
            {header: "New York", x: new Date(2013, 0), y: 33},
            {header: "Seattle", x: new Date(2013, 0), y: 47}
          ]
        }, {
          x: new Date(2013, 1),
          values: [
            {header: "Austin", x: new Date(2013, 1), y: 52},
            {header: "New York", x: new Date(2013, 1), y: 35},
            {header: "Seattle", x: new Date(2013, 1), y: 51}
          ]
        }, {
          x: new Date(2013, 2),
          values: [
            {header: "Austin", x: new Date(2013, 2), y: 55},
            {header: "New York", x: new Date(2013, 2), y: 42},
            {header: "Seattle", x: new Date(2013, 2), y: 56}
          ]
        }
      ];
      output = dashboard.models.unpack(input, { timeSeries: true, groupByX: true });
      assert.deepEqual(output.xGroups, expected);
    });
  });
});
