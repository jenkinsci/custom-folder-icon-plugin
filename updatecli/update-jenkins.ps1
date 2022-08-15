param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath,
  [Parameter(Position = 1)]
  [version] $NewVersion
)

[version]$CurrentVersion = (Get-Content $JenkinsfilePath) | Select-String -Pattern "\[ platform: 'linux', jdk: '17', jenkins: '(.+)' \]" | %{$_.Matches.Groups[1].value}

if ($null -ne $CurrentVersion -and $NewVersion -gt $CurrentVersion) {
  $NewContent = (Get-Content $JenkinsfilePath) -replace $CurrentVersion, $NewVersion
  Set-Content -Path $JenkinsfilePath -Value $NewContent
  Write-Output "$NewVersion"
}
