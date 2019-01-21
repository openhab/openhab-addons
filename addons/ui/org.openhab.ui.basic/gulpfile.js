(function() {
	"use strict";

	var
		gulp = require("gulp"),
		sass = require("gulp-sass"),
		uglify = require("gulp-uglify"),
		eslint = require("gulp-eslint");

	var
		sources = {
			js: "web-src/smarthome.js",
			sass: "web-src/smarthome.scss"
		};

	
	var paths = {
	        FontLibs: [
                './node_modules/roboto-fontface/fonts/Roboto-Medium.*',
                './node_modules/roboto-fontface/fonts/Roboto-Regular.*',
                './node_modules/material-design-icons/iconfont/MaterialIcons-Regular.*'
	        ]
	    };
	
	gulp.task("copyFontLibs", function () {
	    return gulp.src(paths.FontLibs)
	        .pipe(gulp.dest('./web/fonts'));
	});

	gulp.task("css", function() {
		return gulp.src(sources.sass)
			.pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError))
			.pipe(gulp.dest("web"));
	});

	gulp.task("eslint", function() {
		return gulp.src(sources.js)
			.pipe(eslint({
				configFile: "eslint.json"
			}))
			.pipe(eslint.format())
			.pipe(eslint.failAfterError());
	});

	gulp.task("js", function() {
		return gulp.src(sources.js)
			.pipe(uglify())
			.pipe(gulp.dest("web"));
	});

	gulp.task("default", [ "css", "copyFontLibs", "eslint", "js" ]);
})();
