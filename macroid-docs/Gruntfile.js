module.exports = function(grunt) {
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    gitbook: {
      build: {
        input: ".",
        output: "target"
      }
    },
    'gh-pages': {
      options: {
        repo: "git@github.com:macroid/macroid.github.io.git",
        branch: 'master',
        message: "Update documentation",
        push: false
      },
      src: ["target/**"]
    }
  });

  grunt.loadNpmTasks('grunt-gitbook');
  grunt.loadNpmTasks('grunt-gh-pages');

  grunt.registerTask('build', ['gitbook']);
  grunt.registerTask('publish', ['gh-pages']);
};
