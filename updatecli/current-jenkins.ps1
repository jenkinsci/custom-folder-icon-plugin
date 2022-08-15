param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath
)

$regex = "\[ platform: '\w+', jdk: '\d{2}', jenkins: '(?<version>\d+.\d+.\d)' \]"

Write-Output "2.363"

if((Get-Content $JenkinsfilePath) -match $regex) {
  Write-Output $Matches.version
}
