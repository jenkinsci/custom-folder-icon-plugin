// Builds the plugin using https://github.com/jenkins-infra/pipeline-library
buildPlugin(useContainerAgent: true, configurations: [
  // Test the minimum required Jenkins version.
  [ platform: 'linux', jdk: '11', jenkins: null ],
  [ platform: 'windows', jdk: '11', jenkins: null ],

  // Test the latest LTS release version.
  // [ platform: 'linux', jdk: '11', jenkins: 'null' ],
  // [ platform: 'windows', jdk: '11', jenkins: 'null' ],

  // Test the bleeding edge of the compatibility spectrum.
  [ platform: 'linux', jdk: '17', jenkins: '2.363' ],
])
