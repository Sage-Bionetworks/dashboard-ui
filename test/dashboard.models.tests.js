window = require('jsdom').jsdom().createWindow();
jQuery = require('jquery');
d3 = require('d3');
dashboard = require('../public/javascripts/dashboard.js');
require('../public/javascripts/dashboard.models.js');
var assert = require('assert');
describe('dashboard.models', function() {
  describe('unpack()', function() {
    var input = {
      xLabel: null,
      yLabel: 'count',
      xHeaders: [ 'timestamp', 'id', 'name', 'url' ],
      xValues: [
        [ '1362182400000', '1328918400000', '1352419200000' ],
        [ '123', '456', '789' ],
        [ 'lora', 'maya', 'noah' ],
        [ 'synapse.org/user/123', 'synapse.org/user/456', 'synapse.org/user/789' ]
      ],
      yHeaders: [ 'web', 'python' ],
      yValues: [
        [ '389', '211', '507'],
        [ '9', '52', '0']
      ]
    };
    it('should do the very basic unpacking', function() {
      var expected, output;
      expected = {
        yLabel: 'count',
        xHeaders: [ 'timestamp', 'id', 'name', 'url' ],
        yHeaders: [ 'web', 'python' ],
        xSeries: {
          header: 'timestamp',
          values: [ '1362182400000', '1328918400000', '1352419200000' ]
        },
        ySeries: [
          {
            x:
              [ { header: 'timestamp', value: '1362182400000' },
                { header: 'id', value: '123' },
                { header: 'name', value: 'lora' },
                { header: 'url', value: 'synapse.org/user/123' } ],
             y:
              [ { header: 'web', value: 389 },
                { header: 'python', value: 9 } ]
          },
          {
            x:
              [ { header: 'timestamp', value: '1328918400000' },
                { header: 'id', value: '456' },
                { header: 'name', value: 'maya' },
                { header: 'url', value: 'synapse.org/user/456' } ],
             y:
              [ { header: 'web', value: 211 },
                { header: 'python', value: 52 } ]
          },
          {
            x:
              [ { header: 'timestamp', value: '1352419200000' },
                { header: 'id', value: '789' },
                { header: 'name', value: 'noah' },
                { header: 'url', value: 'synapse.org/user/789' } ],
             y:
              [ { header: 'web', value: 507 },
                { header: 'python', value: 0 } ]
          }
        ]
      };
      output = dashboard.models.unpack(input);
      assert.deepEqual(output, expected);
    });
    it('should unpack the time series', function() {
      var expected, output;
      expected = {
        yLabel: 'count',
        xHeaders: [ 'timestamp', 'id', 'name', 'url' ],
        yHeaders: [ 'web', 'python' ],
        xSeries: {
          header: 'timestamp',
          values: [
            new Date('Mar 2, 2013 GMT'),
            new Date('Feb 11, 2012 GMT'),
            new Date('Nov 9, 2012 GMT')
          ]
        },
        ySeries: [
                  {
                    x:
                      [ { header: 'timestamp', value: new Date('Mar 2, 2013 GMT') },
                        { header: 'id', value: '123' },
                        { header: 'name', value: 'lora' },
                        { header: 'url', value: 'synapse.org/user/123' } ],
                     y:
                      [ { header: 'web', value: 389 },
                        { header: 'python', value: 9 } ]
                  },
                  {
                    x:
                      [ { header: 'timestamp', value: new Date('Feb 11, 2012 GMT') },
                        { header: 'id', value: '456' },
                        { header: 'name', value: 'maya' },
                        { header: 'url', value: 'synapse.org/user/456' } ],
                     y:
                      [ { header: 'web', value: 211 },
                        { header: 'python', value: 52 } ]
                  },
                  {
                    x:
                      [ { header: 'timestamp', value: new Date('Nov 9, 2012 GMT') },
                        { header: 'id', value: '789' },
                        { header: 'name', value: 'noah' },
                        { header: 'url', value: 'synapse.org/user/789' } ],
                     y:
                      [ { header: 'web', value: 507 },
                        { header: 'python', value: 0 } ]
                  }
                ]
      };
      output = dashboard.models.unpack(input, {timeSeries: true});
      assert.deepEqual(output, expected);
    });
    it('should set min and max', function() {
      var output = dashboard.models.unpack(input, {timeSeries: true, xMinMax: true, yMinMax: true});
      assert.deepEqual(output.xMin, new Date('Feb 11, 2012 GMT'));
      assert.deepEqual(output.xMax, new Date('Mar 2, 2013 GMT'));
      assert.deepEqual(output.yMin, 0);
      assert.deepEqual(output.yMax, 507);
    });
  });
});
