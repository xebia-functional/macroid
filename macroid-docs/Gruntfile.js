module.exports = function(grunt) {
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    gitbook: {
      build: {
        input: ".",
        output: "target/gitbook"
      }
    }
  });

  grunt.loadNpmTasks('grunt-gitbook');

  grunt.registerTask('build', ['gitbook']);
};
