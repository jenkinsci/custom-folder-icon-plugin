# The MIT License
#
# Copyright (c) 2024 strangelookingnerd
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

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
    SearchAndReplace -regexPattern " E\d+.\d+ " -replacement ":"

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
    SearchAndReplace -regexPattern "___" -replacement "_"
    SearchAndReplace -regexPattern "__" -replacement "_"
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
    $nonEmptyLines | Set-Content -Path $EmojiList -Encoding UTF8NoBOM
}

"Done. Parsed Emojis in " + $runtime.Milliseconds + " ms" | Out-Host
