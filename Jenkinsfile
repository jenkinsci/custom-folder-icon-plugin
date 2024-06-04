// Builds the plugin using https://github.com/jenkins-infra/pipeline-library
buildPlugin(useContainerAgent: true, configurations: [
  // Test the minimum required Jenkins Version.
  [ platform: 'linux', jdk: '11', jenkins: null ],
  [ platform: 'windows', jdk: '17', jenkins: null ],

  // Test latest Jenkins Version.
  [ platform: 'linux', jdk: '21', jenkins: '2.461' ]
])
