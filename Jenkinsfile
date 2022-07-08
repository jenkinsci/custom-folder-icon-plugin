// Builds the plugin using https://github.com/jenkins-infra/pipeline-library
buildPlugin(useContainerAgent: true, configurations: [
  // Test the minimum required Jenkins version.
  [ platform: 'linux', jdk: '11', jenkins: null ],

  // Test the latest LTS release on both Linux and Windows.
  [ platform: 'linux', jdk: '11', jenkins: '2.357' ],
  [ platform: 'windows', jdk: '11', jenkins: '2.357' ],

  // Test the bleeding edge of the compatibility spectrum.
  [ platform: 'linux', jdk: '17', jenkins: '2.358' ],
])
