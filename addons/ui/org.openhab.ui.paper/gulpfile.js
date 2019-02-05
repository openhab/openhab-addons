'use strict';

var angularFilesort = require('gulp-angular-filesort'),
    browserSync = require('browser-sync'),
    concat = require('gulp-concat'),
    del = require('del'),
    gulp = require('gulp'),
    ngAnnotate = require('gulp-ng-annotate'),
    proxyMiddleware = require('http-proxy-middleware'),
    rename = require("gulp-rename"),
    uglify = require('gulp-uglify'),
    inject = require('gulp-inject'),
    util = require('gulp-util'),
    merge = require('merge-stream'),
    Server = require('karma').Server;
var isDevelopment = !!util.env.development;
var noMinify = util.env.noMinify

var paths = {
    scripts: [
        './web-src/js/app.js',
        './web-src/js/constants.js',
        './web-src/js/extensions.js',
        './web-src/js/main.js',
        './web-src/js/shared.properties.js',
        './web-src/js/controllers.module.js',
        './web-src/js/widget.multiselect.js',
        './web-src/js/search.filters.js',
        './web-src/js/utility.js'
    ],
    static: [
        './web-src/css/*.css',
        './web-src/img/*',
        './web-src/index.html'
    ],
    concat: [{
        'src': [
            './web-src/js/**/services*.js',
            './web-src/js/repositories/repositories-module.js',
            './web-src/js/repositories/repositories-services.js'
            ],
        'name': 'services.js'
    }, {
        'src': [
            './web-src/js/services/controller*.js',
            './web-src/js/bindings/bindings-module.js',
            './web-src/js/bindings/route-config.js',
            './web-src/js/bindings/binding-config-dialog.js',
            './web-src/js/items/items-module.js',
            './web-src/js/items/route-config.js',
            './web-src/js/items/service.items.metadata.js',
            './web-src/js/items/controllers.items.js',
            './web-src/js/items/directive.metadata-details.js',
            './web-src/js/items/directive.metadata-list.js',
            './web-src/js/items/controller.metadata-parameter.dialog.js',
            './web-src/js/system/controller*.js',
            './web-src/js/things/things-module.js',
            './web-src/js/things/route-config.js',
            './web-src/js/things/component*.js',
            './web-src/js/things/controller*.js',
            './web-src/js/extensions/controller*.js',
            './web-src/js/rules/controller*.js',
            './web-src/js/firmware/controller*.js',
            './web-src/js/control/control-module.js',
            './web-src/js/control/route-config.js',
            './web-src/js/control/service*.js',
            './web-src/js/control/**/component*.js',
            './web-src/js/control/controller*.js',
            './web-src/js/setup/controller*.js',
            './web-src/js/**/directive*.js',
            './web-src/js/filters/*.js',
            './web-src/js/controller*.js',
            '!./web-src/js/**/*.spec.js'],
        'name': 'controllers.js'
    }, {
        'src': [
            // @TODO Figure out why including angular in concat breaks stuff
            //'./web-src/bower_components/angular/angular.min.js',
            './node_modules/angular-route/angular-route.min.js',
            './node_modules/angular-resource/angular-resource.min.js',
            './node_modules/angular-animate/angular-animate.min.js',
            './node_modules/angular-aria/angular-aria.min.js',
            './node_modules/angular-material/angular-material.min.js',
            './node_modules/angular-messages/angular-messages.min.js',
            './node_modules/angular-sanitize/angular-sanitize.min.js',
            './node_modules/angular-ui-sortable/dist/sortable.min.js',
            './node_modules/angular-material-expansion-panel/dist/md-expansion-panel.min.js',
        ],
        'name': 'angular-bundle.js'
    }],
    partials: ['./web-src/js/**/*.html',
               './web-src/partials/*.html'],
    JSLibs: [
        './node_modules/jquery/dist/jquery.min.js',
        './node_modules/angular/angular.min.js',
        './node_modules/masonry-layout/dist/masonry.pkgd.min.js',
        './node_modules/sprintf-js/dist/sprintf.min.js',
        './node_modules/bootstrap/dist/js/bootstrap.min.js',
        './node_modules/tinycolor2/tinycolor.js',
        './node_modules/openlayers/ol-esh.js'
    ],
    JQUI: [{
        'src' : [
             './node_modules/jquery-ui/ui/core.js',
             './node_modules/jquery-ui/ui/widget.js',
             './node_modules/jquery-ui/ui/mouse.js',
             './node_modules/jquery-ui/ui/sortable.js',
        ],
        'name': 'jquery-ui.js'
    }],
    JSMisc: [
        './node_modules/eventsource-polyfill/dist/eventsource.js'  
    ],
    CSSLibs: [
        './node_modules/bootstrap/dist/css/bootstrap.min.css',
        './node_modules/angular-material/angular-material.min.css',
        './node_modules/angular-material-expansion-panel/dist/md-expansion-panel.min.css'
    ],
    FontLibs: [
        './node_modules/roboto-fontface/fonts/*.woff',
        './node_modules/material-design-icons/iconfont/MaterialIcons-Regular.woff'
    ]
};

gulp.task('default', ['test']);
gulp.task('build', ['uglify', 'concat', 'copyCSSLibs', 'copyFontLibs', 'copyJSLibs', 'copyJQUI', 'copyJSMisc', 'copyStatic', 'copyPartials']);

gulp.task('uglify', function () {
    return gulp.src(paths.scripts)
        .pipe(ngAnnotate())
        .pipe(uglify())
        .pipe(gulp.dest('./web/js/'));
});

gulp.task('copyStatic', function () {
    return gulp.src(paths.static, {base: 'web-src'})
        .pipe(gulp.dest('./web'));
});

gulp.task('copyPartials', function () {
    return gulp.src(paths.partials)
        .pipe(gulp.dest('./web/partials'));
});

gulp.task('copyJSLibs', function () {
    return gulp.src(paths.JSLibs)
        .pipe(gulp.dest('./web/js'));
});

gulp.task('copyJQUI', function() {
    var streams = merge();
    paths.JQUI.forEach(function (obj) {
        streams.add(gulp.src(obj.src)
            .pipe(concat(obj.name))
            .pipe(rename(function (path) {
                path.basename += '.min';
                return path;
            }))
            .pipe(uglify())
            .pipe(gulp.dest('./web/js')));
    });

    return streams;
});

gulp.task('copyJSMisc', function () {
    return gulp.src(paths.JSMisc)
        .pipe(rename(function (path) {
                path.basename += '.min';
                return path;
            }))
        .pipe(uglify())
        .pipe(gulp.dest('./web/js'));
});

gulp.task('copyCSSLibs', function () {
    return gulp.src(paths.CSSLibs)
        .pipe(gulp.dest('./web/css'));
});

gulp.task('copyFontLibs', function () {
    return gulp.src(paths.FontLibs)
        .pipe(gulp.dest('./web/fonts'));
});

gulp.task('concat', function () {
    var streams = merge();
    paths.concat.forEach(function (obj) {
        var result = gulp.src(obj.src)
            .pipe(angularFilesort())
            .pipe(concat(obj.name))
            .pipe(rename(function (path) {
                path.basename += '.min';
                return path;
            }));
        
        if (!noMinify) {
            result = result.pipe(ngAnnotate()).pipe(uglify());
        }
        result = result.pipe(gulp.dest('./web/js'));
            
        streams.add(result);
    });

    return streams;
});

gulp.task('clean', function () {
    return del([
        './web/'
      ]);
});

// Gulp Serve
function browserSyncInit(baseDir) {
    var server = {
        baseDir: baseDir,
        index: "index.html"
    };

    server.middleware = proxyMiddleware(['/rest','/icon','/audio'], {target: 'http://localhost:8080'});

    browserSync.instance = browserSync.init({
        startPath: '/',
        server: server,
        browser: 'default'
    });
}

gulp.task('serve', ['test'], function () {
    browserSyncInit(isDevelopment ? ['./web-src', './web'] : './web');
});


gulp.task('inject', ['build'], function () {
   var target = gulp.src('./web-src/index.html.template').pipe(rename('index.html'));

   // It's not necessary to read the files (will speed up things), we're only after their paths:
   var files;
   console.log("MODE: " + (isDevelopment ? "DEV" : "PROD"));
    if(!isDevelopment){
        files = [
                    'web/js/app.js',
                    'web/js/constants.js',
                    'web/js/services.min.js',
                    'web/js/controllers.min.js',
                    'web/js/extensions.js',
                    'web/js/main.js',
                    'web/js/shared.properties.js'
                    ];
        }
    else
    {
        files = [
                     './web-src/js/constants.js',
                     './web-src/js/services/controller*.js',
                     './web-src/js/bindings/bindings-module.js',
                     './web-src/js/bindings/route-config.js',
                     './web-src/js/bindings/binding-config-dialog.js',
                     './web-src/js/items/items-module.js',
                     './web-src/js/items/route-config.js',
                     './web-src/js/items/service.items.metadata.js',
                     './web-src/js/items/controllers.items.js',
                     './web-src/js/items/directive.metadata-details.js',
                     './web-src/js/items/directive.metadata-list.js',
                     './web-src/js/items/controller.metadata-parameter.dialog.js',
                     './web-src/js/system/controller*.js',
                     './web-src/js/things/things-module.js',
                     './web-src/js/things/route-config.js',
                     './web-src/js/things/component*.js',
                     './web-src/js/things/controller*.js',
                     './web-src/js/extensions/controller*.js',
                     './web-src/js/rules/controller*.js',
                     './web-src/js/firmware/controller*.js',
                     './web-src/js/control/control-module.js',
                     './web-src/js/control/route-config.js',
                     './web-src/js/control/service*.js',
                     './web-src/js/control/**/component*.js',
                     './web-src/js/control/controller*.js',
                     './web-src/js/setup/controller*.js',
                     './web-src/js/**/directive*.js',
                     './web-src/js/**/service*.js',
                     './web-src/js/repositories/repositories-module.js',
                     './web-src/js/repositories/repositories-services.js',
                     './web-src/js/filters/*',
                     './web-src/js/extensions.js',
                     './web-src/js/controller*.js',
                     './web-src/js/main.js',
                     './web-src/js/shared.properties.js',
                     './web-src/js/app.js',
                     '!./web-src/js/**/*.spec.js'
                     ]
    }    
    var sources = gulp.src(files, {read: false});
   
    return target.pipe(inject(sources,{
        addRootSlash : false,
        ignorePath : './web-src/js/*.js',
        transform : function ( filePath, file, i, length ) {
            
            var newPath = isDevelopment ? filePath.replace('web-src/','') : filePath.replace( 'web/', '' );
            console.log('inject script = '+ newPath);
            return '<script src="' + newPath  + '"></script>';
        }
    }))
      .pipe(isDevelopment ? gulp.dest('./web-src') : gulp.dest('./web'));
  });

gulp.task('test',['inject'], function (done) {
    return new Server({
      configFile: __dirname + '/karma.conf.js',
      singleRun: true
    }, done).start();
  });


