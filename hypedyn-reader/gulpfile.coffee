gulp = require 'gulp'
plugins = require('gulp-load-plugins')
  rename:
    'gulp-ruby-sass': 'sass'
namespace = plugins.namespace
mainBowerFiles = require 'main-bower-files'

namespace gulp

swallowError = (err) ->
  console.log err.toString()
  this.emit 'end'

src_dir = 'src/main'
src =
  elm: "#{src_dir}/elm/**/*.elm"
  sass: "#{src_dir}/sass/**/*.sass"
  slim: "#{src_dir}/slim/**/*.slim"

build_dir = 'src/main/resources/org/narrativeandplay/hypedyn/reader'

gulp.namespace 'elm', ->
  gulp.task 'init', plugins.elm.init

  gulp.task 'compile', ['init'], ->
    gulp.src src.elm
      .pipe plugins.elm.bundle 'application.js'
      .on 'error', swallowError
      .pipe gulp.dest build_dir

gulp.namespace 'slim', ->
  gulp.task 'compile', ->
    gulp.src src.slim
      .pipe plugins.slim
        pretty: true
      .on 'error', swallowError
      .pipe gulp.dest build_dir

gulp.namespace 'sass', ->
  gulp.task 'compile', ->
    plugins.sass src.sass,
        loadPath: [
          'bower_components/foundation-sites/scss'
          'bower_components/foundation-icon-fonts'
          'bower_components/motion-ui/src'
        ]
        defaultEncoding: "UTF-8"
      .on 'error', plugins.sass.logError
      .pipe plugins.concat 'application.css'
      .pipe plugins.autoprefixer
        browsers: ['last 2 versions', 'ie >= 9', 'and_chr >= 2.3']
      .pipe gulp.dest build_dir

gulp.namespace 'bower', ->
  gulp.task 'compile', ->
    jsFilter = plugins.filter '**/*.js'

    gulp.src mainBowerFiles()
      .pipe jsFilter
      .pipe plugins.uglify()
      .pipe plugins.concat 'vendor.js'
      .pipe gulp.dest build_dir
