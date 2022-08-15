param(
  [Parameter(Position = 0)]
  [string] $JenkinsfilePath
)

(Get-Content $JenkinsfilePath) | Select-String -Pattern "\[ platform: 'linux', jdk: '17', jenkins: '(.+)' \]" | %{$_.Matches.Groups[1].value}
