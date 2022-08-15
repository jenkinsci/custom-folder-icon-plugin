param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath
)

if((Get-Content $JenkinsfilePath) -match "\[ platform: '\w+', jdk: '\d{2}', jenkins: '(?<version>\d+.\d+.\d)' \]") {
  $Matches.version
} else {
  "Why is it not matching!?"
}
