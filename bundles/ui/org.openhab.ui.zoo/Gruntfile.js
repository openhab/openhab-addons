// Created by Sebastian Janzen <sebastian.janzen@innoq.com>
'use strict';
module.exports = function(grunt) {

  var config = {
    app: 'web/js'
  };

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    uglify: {
      options: {},
      build: {
        src: 'web/js/<%= pkg.name %>.js',
        dest: 'web/build/<%= pkg.name %>.min.js'
      }
    },

    jshint: {
      files: ['Gruntfile.js', 'src/**/*.js', 'test/**/*.js'],
      options: {
        globals: {
          jQuery: true
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-uglify'); // Uglify/Minify task

  grunt.registerTask('default', ['uglify']);

};