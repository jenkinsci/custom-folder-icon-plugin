// Builds the plugin using https://github.com/jenkins-infra/pipeline-library
buildPlugin(
  forkCount: '1C',
  useContainerAgent: true,
  configurations: [
    // Test the minimum required Jenkins Version.
    [ platform: 'linux', jdk: '17', jenkins: null ],
    [ platform: 'windows', jdk: '21', jenkins: null ]
])
