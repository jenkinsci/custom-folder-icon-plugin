param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath,
  [Parameter(Position = 1)]
  [version] $NewVersion
)

$changed = $false
if ($null -eq $ENV:DRY_RUN) {
  $ENV:DRY_RUN = $false
}

[version]$CurrentVersion = (Get-Content $JenkinsfilePath) | Select-String -Pattern "\[ platform: 'linux', jdk: '17', jenkins: '(.+)' \]" | %{$_.Matches.Groups[1].value}

if ($null -ne $CurrentVersion -and $NewVersion -gt $CurrentVersion) {
  $changed = $true
}

if ($changed) {
  Write-Output "$NewVersion"
  if ($ENV:DRY_RUN -eq $false) {
    $NewContent = (Get-Content $JenkinsfilePath) -replace $CurrentVersion, $NewVersion
    Set-Content -Path $JenkinsfilePath -Value $NewContent
    Write-Output "(Get-Content $JenkinsfilePath)"
  }
}
