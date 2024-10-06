# uses the emojis.list and converts it to svgs

$SymbolsPath = $PSScriptRoot + "\src\main\resources\images\symbols\"
$EmojiList = $PSScriptRoot + "\src\main\resources\jenkins\plugins\foldericon\EmojiFolderIcon\emojis.list"
$SVG = ".svg"
$EmojiPrefix = "emoji_"
$Placeholder = "PLACEHOLDER"
$SVGTemplate = "<svg xmlns=`"http://www.w3.org/2000/svg`" class=`"emoji`" viewBox=`"0 0 100 100`">"`
             +      "<text "`
             +          "font-size=`"80`" "`
             +          "dominant-baseline=`"middle`" "`
             +          "text-anchor=`"middle`" "`
             +          "x=`"50%`" "`
             +          "y=`"50%`" "`
             +          "dy=`"0.1em`" "`
             +          "style=`"user-select: none;`">"`
             +          $Placeholder`
             +      "</text>"`
             + "</svg>"


"Creating SVGs from " + $EmojiList | Out-Host

$counter = 0

$runtime = Measure-Command {
    foreach ($line in Get-Content -Encoding UTF8NoBOM $EmojiList)
    {
        $splitLine = $line.Split(":")
        $name = $splitLine[0]
        $emoji = $splitLine[1]

        $content = $SVGTemplate.Replace($Placeholder, $emoji)
        $filename = $SymbolsPath + $EmojiPrefix + $name + $SVG

        "Writing " + $filename | Out-Host
        $content | Out-File -NoNewline -Encoding UTF8NoBOM -FilePath $filename

        $counter++
    }
}

"Done. Created " + $counter + " SVGs in " + $runtime.Milliseconds + " ms" | Out-Host
