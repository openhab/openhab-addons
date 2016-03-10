// Created by Sebastian Janzen <sebastian.janzen@innoq.com>
'use strict';

module.exports = function(grunt) {

  var config = {
    app: 'web',
    dist: 'web_dist'
  };


  require('load-grunt-tasks')(grunt);

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    config: config,

    watch: {
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
      html: {
        files: ['<%= config.app %>/*.html', '<%= config.app %>/partials/{,*/}*.html'],
        tasks: ['htmlmin:dev', 'copy:devHtml']
      },
      js: {
        files: ['<%= config.app %>/js/{,*/}*.js'],
        tasks: ['newer:jshint:all', 'newer:copy:devJs']
      },
      css: {
        files: ['<%= config.app %>/css/{,*/}*.css'],
        tasks: ['newer:autoprefixer:dev']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      }
      //template: {
      //  files: ['<%= config.app %>/{,*}*.ejs'],
      //  tasks: ['template']
      //},
    },

    ngAnnotate: {
      options: {
        singleQuotes: true
      },
      dist: {
        files: [{
          expand: true,
          cwd: '<%= config.app %>',
          src: 'js/{,*/}*.js',
          dest: '.tmp/'
        }]
      }
    },

    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: {
        src: ['Gruntfile.js', 'web/js/{,*/}*.js', '!web/js/{,*/}*.min.js'],
        force: true
      },
      ci: {
        options: {
          reporter: require('jshint-junit-reporter'),
          reporterOutput: 'jshint-results.xml',
          force: true // Report errors but continue grunt tasks
        },
        src: '<%= jshint.all.src %>'
      }
    },

    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= config.dist %>/{,*/}*',
            '!<%= config.dist %>/.git{,*/}*'
          ]
        }]
      }
    },

    htmlmin: {
      dist: {
        options: {
          removeComments: false,
          collapseWhitespace: false
        },
        files: [{
          expand: true,
          cwd: '<%= config.app %>',
          src: ['partials/{,*/}*.html', '*.html'],
          dest: '<%= config.dist %>'
        }]
      },
      dev: {
        files: '<%= htmlmin.dist.files %>'
      }
    },

    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= config.app %>',
          src: 'img/{,*/}*.{png,jpg,jpeg,gif}',
          dest: '<%= config.dist %>'
        }]
      }
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 1 version', 'ie >= 10', 'android >= 4']
      },
      dist: {
        files: [{
          expand: true,
          cwd: '<%= config.app %>',
          src: 'css/{,*/}*.css',
          dest: '.tmp'
        }]
      },
      dev: {
        src: '<%= autoprefixer.dist.src %>',
        dest: '<%= config.dist %>/css/'
      }
    },

    // Add script and style tags for bower installed components
    // automatically to index.html
    wiredep: {
      dist: {
        src: '<%= config.app %>/index.html'
        //ignorePath:  /\.\.\//
        //exclude: [/bootstrap-sass-official\//]
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= config.app %>/index.html',
      options: {
        dest: '<%= config.dist %>'

      }
    },

    filerev: {
      dist: {
        src: [
          '<%= config.dist %>/js/{,*/}*.js',
          '<%= config.dist %>/css/{,*/}*.css',
          '<%= config.dist %>/img/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },

    // Performs rewrites based on filerev and the useminPrepare configuration
    usemin: {
      html: '<%= config.dist %>/index.html'
      //css: ['<%= config.dist %>/css/*.css'],
      /*options: {
        assetsDirs: ['<%= config.dist %>','<%= config.dist %>/img', 'bower_components']
      }*/
    },

    copy: {
      devHtml: {
        files: [{
          expand: true,
          cwd: '<%= config.app %>',
          dest: '<%= config.dist %>',
          src: [
            '*.html',
            'partials/{,*/}*.html'
          ]
        }]
      },
      devCss: {
        expand: true,
        cwd: '<%= config.app %>',
        dest: '<%= config.dist %>',
        src: 'css/{,*/}*.css'
      },
      devJs: {
        expand: true,
        cwd: '<%= config.app %>',
        dest: '<%= config.dist %>',
        src: ['js/**', 'bower_components/**']
      },
      devImg: {
        expand: true,
        cwd: '<%= config.app %>',
        dest: '<%= config.dist %>',
        src: 'img/**'
      },
      staticFiles: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= config.app %>',
          dest: '<%= config.dist %>',
          src: 'fonts/{,*/}*.*'
        }, {
          '<%= config.dist %>/favicon.ico' : '<%= config.app %>/img/favicon.ico'
        }, {
          expand: true,
          cwd: '<%= config.app %>/bower_components/fontawesome/fonts',
          dest:'<%= config.dist %>/fonts/',
          src: '*'
        }]
      }
    }


  });

  grunt.registerTask('build', [
    //'jshint',
    'clean:dist',

    // Refresh index.html css and js tags based on bower
    'wiredep',

    // Generate tasks for js/css concat and min
    'useminPrepare',

    // Optimize images
    // >>> Images now in dest
    //'imagemin',
    'copy:devImg',

    // Minification-safe angular DI
    // >>> JS now in .tmp/js/
    'ngAnnotate:dist',

    // CSS for old browsers ready
    // >>> CSS now in .tmp/css/
    'autoprefixer:dist',

    // Minify HTML
    // >>> HTML now in dest
    'htmlmin:dist',

    // Run conact and min
    // >>> JS now in dest
    // >>> CSS now in dest
    // >>> HTML now in dest
    'concat:generated',
    'cssmin:generated',
    'uglify:generated',
    //'filerev',
    'usemin',

    // Copy fonts, to dest
    'copy:staticFiles'
  ]);

  // TODO CoffeeScript

  grunt.registerTask('dev', [
    'wiredep',
    'clean:dist',
    'copy',
    'watch'

  ]);

};