/*
 * The MIT License
 *
 * Copyright (c) 2023 strangelookingnerd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.foldericon;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Emojis Tests
 *
 * @author strangelookingnerd
 */

class EmojisTest {

    /**
     * Test count of entries in 'emojis.list' vs. existing SVG files.
     *
     * @throws Exception
     */
    @Test
    void testSVG() throws Exception {
        File emojiList = new File("./src/main/resources/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list");
        File svgFolder = new File("./src/main/resources/images/symbols");

        String[] folderContents = svgFolder.list();
        assertNotNull(folderContents);
        assertEquals(folderContents.length, Emojis.getAvailableIcons().size());
        assertEquals(folderContents.length, Emojis.getAvailableEmojis().size());

        List<String> entries = FileUtils.readLines(emojiList, StandardCharsets.UTF_8);
        assertEquals(folderContents.length, entries.size());

        for (String entry : entries) {
            String[] content = entry.split(":");
            assertEquals(2, content.length);

            String iconClassName = Emojis.getAvailableIcons().get(content[0]);
            assertNotNull(iconClassName);
            assertEquals(iconClassName, Emojis.getIconClassName(content[0]));

            List<String> files = Arrays.stream(folderContents).filter(file -> file.equals("emoji_" + content[0] + ".svg")).collect(Collectors.toList());
            assertEquals(1, files.size());

            String emoji = Emojis.getAvailableEmojis().get(content[0]);
            assertNotNull(emoji);

            String svg = FileUtils.readFileToString(new File(svgFolder, files.get(0)), StandardCharsets.UTF_8);
            assertTrue(svg.contains(emoji));
        }
    }

}
