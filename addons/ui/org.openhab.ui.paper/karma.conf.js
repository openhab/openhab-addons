// Karma configuration
// Generated on Thu Jul 07 2016 15:30:16 GMT+0200 (CEST)

module.exports = function(config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath : '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks : [ 'angular', 'jasmine' ],
        angular : [ 'mocks' ],

        // list of files / patterns to load in the browser
        files : [ 'web/js/jquery.min.js', 'web/js/*.js', 'tests/*.js','web-src/**/*.spec.js' ],

        // list of files to exclude
        exclude : [],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors : {},

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters : [ 'spec', 'junit' ],

        junitReporter : {
            outputFile : 'PaperUI-tests.xml'
        },

        // web server port
        port : 9876,

        // enable / disable colors in the output (reporters and logs)
        colors : true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO ||
        // config.LOG_DEBUG
        logLevel : config.LOG_ERROR,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch : false,

        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers : [ 'PhantomJS' ],

        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun : false,

        // Concurrency level
        // how many browser should be started simultaneous
        concurrency : Infinity
    })
}
