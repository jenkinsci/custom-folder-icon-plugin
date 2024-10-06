# downloads emoji specification from unicode.org and converts it to be used in the plugin

$EmojiList = $PSScriptRoot + "\src\main\resources\jenkins\plugins\foldericon\EmojiFolderIcon\emojis.list"
$EmojiUrl = "https://unicode.org/Public/emoji/16.0/emoji-test.txt"

function SearchAndReplace {
    param (
        $regexPattern,
        $replacement
    )
    $fileContent = Get-Content -Path $EmojiList -Encoding UTF8NoBOM
    $modifiedContent = $fileContent -replace $regexPattern, $replacement
    $modifiedContent | Set-Content -Path $EmojiList -Encoding UTF8NoBOM
}

"Parsing Emojis from " + $EmojiUrl | Out-Host

$runtime = Measure-Command {
    # download file
    Invoke-WebRequest -Uri $EmojiUrl -OutFile $EmojiList

    # remove unused content
    SearchAndReplace -regexPattern "^(?!.+; fully-qualified     # |.+; component           # ).+$" -replacement ""
    SearchAndReplace -regexPattern "^(.*; fully-qualified     # |.*; component           # )" -replacement ""
    SearchAndReplace -regexPattern ": " -replacement "_"
    SearchAndReplace -regexPattern " E\d+\.\d+ " -replacement ":"

    # various replacements
    SearchAndReplace -regexPattern "&" -replacement "and"
    SearchAndReplace -regexPattern "u\.s\." -replacement "us"
    SearchAndReplace -regexPattern "keycap_#" -replacement "keycap_hashtag"
    SearchAndReplace -regexPattern "keycap_\*" -replacement "keycap_asterisk"
    SearchAndReplace -regexPattern "ñ" -replacement "n"
    SearchAndReplace -regexPattern "å" -replacement "a"
    SearchAndReplace -regexPattern "é" -replacement "e"
    SearchAndReplace -regexPattern "ô" -replacement "o"
    SearchAndReplace -regexPattern "ç" -replacement "c"
    SearchAndReplace -regexPattern "ã" -replacement "a"
    SearchAndReplace -regexPattern "í" -replacement "i"
    SearchAndReplace -regexPattern "ü" -replacement "u"
    SearchAndReplace -regexPattern "[\-\. ,()’!]" -replacement "_"
    SearchAndReplace -regexPattern "[\u201C\u201D]" -replacement "_"
    SearchAndReplace -regexPattern "_{2,}" -replacement "_"
    SearchAndReplace -regexPattern "_$" -replacement ""

    # switch name and emoji
    SearchAndReplace -regexPattern "(.+):(.+)" -replacement '$2:$1'

    # to lowercase
    $fileContent = Get-Content -Path $EmojiList -Encoding UTF8NoBOM
    $lowercaseContent = $fileContent | ForEach-Object { $_.ToLower() }
    $lowercaseContent | Set-Content -Path $EmojiList -Encoding UTF8NoBOM

    # remove empty lines
    $fileContent = Get-Content -Path $EmojiList -Encoding UTF8NoBOM
    $nonEmptyLines = $fileContent | Where-Object { $_.Trim() -ne "" }
    $nonEmptyLines -join "`r`n" | Set-Content -Path $EmojiList -Encoding UTF8NoBOM -NoNewline
}

"Done. Parsed Emojis in " + $runtime.Milliseconds + " ms" | Out-Host
