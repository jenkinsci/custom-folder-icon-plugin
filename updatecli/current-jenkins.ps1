param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath
)

if((Get-Content $JenkinsfilePath) -match "\[ platform: '\w+', jdk: '\d{2}', jenkins: '(?<version>\d+.\d+.\d)' \]") {
  Write-Output $Matches.version
} else {
  Write-Output "Why is it not matching!?"
}
