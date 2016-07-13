module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
          //grunt task configuration will go here     

        ngAnnotate: {
            options: {
                singleQuotes: true
            },
            app: {
                files: {
                    './WebContent/min-safe/tracking.js': ['./WebContent/tracking.js']
                }
            }
        },
    
	    concat: {
	        js: { //target
	            src: ['./WebContent/min-safe/tracking.js'],
	            dest: './WebContent/min-safe/tracking.min.js'
	        }
	    },
	    
	    uglify: {
	        js: { //target
	            src: ['./WebContent/min-safe/tracking.js'],
	            dest: './WebContent/tracking.js'
	        }
	    },
	    clean: {
		folder: ['./WebContent/min-safe/']
	    },
	    war: {
	        target: {
	          options: {
	            war_dist_folder: '/Library/Tomcat/apache-tomcat-8.0.26/webapps',    /* Folder where to generate the WAR. */
	            war_name: 'SPEED_DATA'                    /* The name fo the WAR file (.war will be the extension) */
	          },
	          files: [
	            {
	              expand: true,
	              cwd: '/Users/SUReYeAh/.jenkins/jobs/SPEED_DATA/workspace/SPEED_DATA',
	              src: ['**'],
	              dest: ''
	            }
	          ]
	        }
	      }
    });

    
    //load grunt tasks
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-ng-annotate'); 
    grunt.loadNpmTasks('grunt-war');
    
    //register grunt default task
    grunt.registerTask('default', ['ngAnnotate', 'concat', 'uglify','clean','war']);
}