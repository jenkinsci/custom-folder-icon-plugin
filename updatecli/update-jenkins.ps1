param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath,
  [Parameter(Position = 1)]
  [version] $NewVersion
)

$regex = "\[ platform: '\w+', jdk: '\d{2}', jenkins: '(?<version>\d+.\d+.\d)' \]"

if((Get-Content $JenkinsfilePath) -match $regex) {
  [version]$CurrentVersion = $Matches.version

  if ($null -ne $CurrentVersion -and $NewVersion -gt $CurrentVersion) {
    $NewContent = (Get-Content $JenkinsfilePath) -replace $CurrentVersion, $NewVersion
    Set-Content -Path $JenkinsfilePath -Value $NewContent
    Write-Output "$NewVersion"
  }
}