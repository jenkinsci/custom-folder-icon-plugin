package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Emojis Tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmojisTest {

    /**
     * Test count of entries in 'emojis.list' vs. existing SVG files.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(0)
    void testSVG() throws Exception {
        File emojiList = new File("./src/main/resources/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list");
        File svgFolder = new File("./src/main/resources/images/symbols");

        String[] folderContents = svgFolder.list();
        assertNotNull(folderContents);
        assertEquals(folderContents.length, Emojis.getAvailableIcons().size());
        assertEquals(folderContents.length, Emojis.getAvailableEmojis().size());

        List<String> entries = Files.readAllLines(emojiList.toPath(), StandardCharsets.UTF_8);
        assertEquals(folderContents.length, entries.size());

        for (String svg : folderContents) {
            String emoji = Emojis.getAvailableIcons()
                    .get(svg.replaceFirst("emoji_", "").replaceFirst(".svg", ""));
            assertNotNull(emoji);
        }

        for (String entry : entries) {
            String[] content = entry.split(":");
            assertEquals(2, content.length);

            assertTrue(content[0].matches("^[a-z0-9_]+$"));

            String iconClassName = Emojis.getAvailableIcons().get(content[0]);
            assertNotNull(iconClassName);
            assertEquals(iconClassName, Emojis.getIconClassName(content[0]));

            String emoji = Emojis.getAvailableEmojis().get(content[0]);
            assertNotNull(emoji);
            assertEquals(emoji, content[1]);

            String svg = Files.readString(
                    new File(svgFolder, "emoji_" + content[0] + ".svg").toPath(), StandardCharsets.UTF_8);
            assertTrue(svg.contains(content[1]));
        }
    }

    /**
     * Test missing 'emojis.list'.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(1)
    void testMissingResource() throws Exception {
        File emojiList = new File("./target/classes/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list");
        File backup = new File("./target/classes/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list.backup");

        try {
            assertTrue(emojiList.renameTo(backup));

            validateEmojisInstance();
        } finally {
            assertTrue(backup.renameTo(emojiList));
        }
    }

    /**
     * Test invalid 'emojis.list'.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    @Order(2)
    void testInvalidResource() throws Exception {
        File emojiList = new File("./target/classes/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list");
        File backup = new File("./target/classes/jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list.backup");

        try {
            assertTrue(emojiList.renameTo(backup));
            assertTrue(emojiList.createNewFile());
            Files.writeString(emojiList.toPath(), "invalid", StandardCharsets.UTF_8);

            validateEmojisInstance();
        } finally {
            assertTrue(emojiList.delete());
            assertTrue(backup.renameTo(emojiList));
        }
    }

    private static void validateEmojisInstance() throws Exception {
        Constructor<Emojis> ctor = Emojis.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Emojis emojis = ctor.newInstance();

        Field availableIconsField = emojis.getClass().getDeclaredField("availableIcons");
        availableIconsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableIcons = (Map<String, String>) availableIconsField.get(emojis);

        assertNotNull(availableIcons);
        assertTrue(availableIcons.isEmpty());

        Field availableEmojisField = emojis.getClass().getDeclaredField("availableEmojis");
        availableEmojisField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableEmojis = (Map<String, String>) availableEmojisField.get(emojis);

        assertNotNull(availableEmojis);
        assertTrue(availableEmojis.isEmpty());
    }
}
