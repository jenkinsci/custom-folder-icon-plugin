param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath
)

$regex = "\[ platform: '\w+', jdk: '\d{2}', jenkins: '(?<version>\d+.\d+.\d)' \]"

Write-Output (Get-Content $JenkinsfilePath)

if((Get-Content $JenkinsfilePath) -match $regex) {
  Write-Output $Matches.version
}
