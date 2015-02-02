module.exports = function(grunt) {

  grunt.initConfig({
    src: 'public/javascripts',
    pkg: grunt.file.readJSON('package.json'),
    mochaTest: {
      test: {
        src: ['test/**/*.js']
      }
    },
    uglify: {
      build: {
        files: {
          '<%= src %>/<%= pkg.name %>.min.js': [
            '<%= src %>/dashboard.js',
            '<%= src %>/dashboard.models.js',
            '<%= src %>/dashboard.charts.js'
          ]
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-mocha-test');

  grunt.registerTask('default', ['mochaTest', 'uglify']);

};
