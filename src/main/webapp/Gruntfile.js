module.exports = function (grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        aws: grunt.file.readJSON('aws-deploy-keys.json'), // Load deploy variables
        aws_s3: {
            options: {
                accessKeyId: '<%= aws.AWSAccessKeyId %>',
                secretAccessKey: '<%= aws.AWSSecretKey %>',
                region: '<%= aws.AWSRegion %>',
                uploadConcurrency: 5, // 5 simultaneous uploads
                downloadConcurrency: 5 // 5 simultaneous downloads
            },
            production: {
                options: {
                    bucket: 'witbooking.static',
                    gzipRename: 'ext'
                },
                files: [
                    {expand: true, cwd: 'dist', src: ['*.js.gz'],  dest: '/js'},
                    {expand: true, cwd: 'dist', src: ['*.css.gz'], dest: '/css'},
                    {expand: true, cwd: 'dist/sipay', src: ['*.js.gz'],  dest: '/js/sipay'},
                    {expand: true, cwd: 'dist/sipay', src: ['*.css.gz'], dest: '/css/sipay'}
                ]
            },
            devel: {
                options: {
                    bucket: 'witbooking.static',
                    gzipRename: 'ext'
                },
                files: [
                    {expand: true, cwd: 'dist', src: ['*.js.gz'],  dest: '/dev/js'},
                    {expand: true, cwd: 'dist', src: ['*.css.gz'], dest: '/dev/css'},
                    {expand: true, cwd: 'dist/sipay', src: ['*.js.gz'],  dest: '/dev/js/sipay'},
                    {expand: true, cwd: 'dist/sipay', src: ['*.css.gz'], dest: '/dev/css/sipay'}
                ]
            }
        },
        concat: {
            css: {
                src: [
                    'css/**.css',
                    'js/jquery.cookiebar/jquery.cookiebar.css'
                ],
                dest: 'style.css'
            },
            js: {
                src: [
                    "js/components/angular-dynamic-locale/src/tmhDynamicLocale.js",
                    "js/lib/extra/witchart.js",
                    "js/app.js",
                    "js/templates.js",
                    "js/services.js",
                    "js/paymentService.js",
                    "js/controllers.js",
                    "js/filters.js",
                    "js/directives.js",
                    "js/jquery.cookiebar/jquery.cookiebar.js",
                    "js/jquery.cookiebar/witcookie.js",
                    "js/components/spin.js/spin.js",
                    "js/components/angular-cache-buster/angular-cache-buster.js"
                ],
                dest: 'js/app.min.js'

            },
            sipaycss: {
                src: [
                    'sipay/styles/**.css'
                ],
                dest: 'dist/sipay/style.min.css'
            },
            sipayjs: {
                src: [
                    "sipay/scripts/vendor.js",
                    "sipay/scripts/scripts.js"
                ],
                dest: 'dist/sipay/app.min.js'
            },
            minifiedVendors: {
                src: [
                    "js/lib/angular/angular-route.min.js",
                    "js/lib/angular/angular-resource.min.js",
                    "js/lib/angular/angular-sanitize.min.js",
                    "js/components/angular-animate/angular-animate.min.js",
                    "js/components/jquery-ui/ui/minified/jquery.ui.core.min.js",
                    "js/components/jquery-ui/ui/minified/jquery.ui.datepicker.min.js",
                    "js/components/angular-translate/angular-translate.min.js",
                    "js/components/angular-translate-loader-url/angular-translate-loader-url.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-es.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-en-GB.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-fr.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-it.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-ru.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-ca.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-zh-CN.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-de.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-nl.min.js",
                    "js/components/jquery-ui/ui/minified/i18n/jquery.ui.datepicker-pt.min.js",
                    "js/components/angular-bootstrap/ui-bootstrap-tpls.min.js",
                    "js/components/angular-ui-router/release/angular-ui-router.min.js",
                    "js/placeholders.min.js",
                    "js/components/iframe-resizer/js/iframeResizer.contentWindow.min.js",
                    "js/components/angular-md5/angular-md5.min.js",
                    "js/app.min.js"
                ],
                dest: 'dist/app.min.js'
            }
        },

        uglify: {
            options: {
                banner: '',
                compress: true,
                preserveComments: false
            },
            js: {
                src: 'js/app.min.js',
                dest: 'js/app.min.js'
            },
            jsall: {
                src: 'dist/app.min.js',
                dest: 'dist/app.min.js'
            }
        },
        cssmin: {
            css: {
                options: {
                    keepSpecialComments: 0
                },
                src: 'style.css',
                dest: 'css/style.min.css'
            }
        },
        copy: {
            dist: {
                src:'css/style.min.css',
                dest:'dist/style.min.css'
            }
        },
        compress: {
            main: {
                options: {
                    mode: 'gzip'
                },
                expand: true,
                files: [
                    {  cwd: 'dist/', expand: true, src: ['*.js'], dest: 'dist/', ext: '.min.js.gz'},
                    {  cwd: 'dist/', expand: true, src: ['*.css'], dest: 'dist/', ext: '.min.css.gz'},
                    {  cwd: 'dist/sipay/', expand: true, src: ['*.js'], dest: 'dist/sipay/', ext: '.min.js.gz'},
                    {  cwd: 'dist/sipay/', expand: true, src: ['*.css'], dest: 'dist/sipay/', ext: '.min.css.gz'}
                ]
            }
        },
        clean: {
            deleteOldMins: ['css/style.min.css', 'js/app.min.js', 'dist/app.min.js', 'sipay/style.min.css', 'sipay/app.min.js'],
            delete: ['style.css', 'sipay/style.css', 'sipay/app.js'],
            deletetemplates: ['templates.js', 'style.css' ]
        },

        ngtemplates: {
            witbooker: {
                src: ['js/partials/**/**.html'],
                dest: 'js/templates.js',
                options: {
                    htmlmin: {
                        collapseBooleanAttributes:      true,
                        collapseWhitespace:             true,
                        removeAttributeQuotes:          true,
                        removeComments:                 true, // Only if you don't use comment directives!
                        removeEmptyAttributes:          true,
                        removeRedundantAttributes:      true,
                        removeScriptTypeAttributes:     true,
                        removeStyleLinkTypeAttributes:  true
                    }
                }
            }
        }
    });



    //————————————————————————————
    //  PLUGINS
    //————————————————————————————
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-aws-s3');
    //————————————————————————————
    //  TASKS
    //————————————————————————————
    grunt.registerTask('sipay', ['concat:sipaycss', 'concat:sipayjs'  ]);
    grunt.registerTask('default', ['clean:deleteOldMins', 'ngtemplates:witbooker', 'concat:js', 'uglify:js', 'concat:minifiedVendors', 'uglify:jsall','concat:css', 'cssmin:css','copy:dist', 'sipay','compress:main','clean:deletetemplates']);
    grunt.registerTask('deploy',  ['clean:deleteOldMins', 'ngtemplates:witbooker', 'concat:js', 'uglify:js', 'concat:minifiedVendors', 'uglify:jsall','concat:css', 'cssmin:css','copy:dist','sipay','compress:main','clean:deletetemplates','aws_s3']);
    grunt.registerTask('deploydev',  ['clean:deleteOldMins', 'ngtemplates:witbooker', 'concat:js', 'uglify:js', 'concat:minifiedVendors', 'uglify:jsall','concat:css', 'cssmin:css','copy:dist','sipay','compress:main','clean:deletetemplates','aws_s3:devel']);
};
