package jenkins.plugins.foldericon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to work with emojis provided by the custom-folder-icon-plugin.
 */
public final class Emojis {

    private static final Logger LOGGER = Logger.getLogger(Emojis.class.getName());
    private static final String EMOJIS_LIST_RESOURCE_PATH = "jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list";
    private static final String PLUGIN_NAME = "custom-folder-icon";
    private static final String EMOJI_PREFIX = "emoji_";
    private static final String ICON_CLASS_NAME_PATTERN = "symbol-" + EMOJI_PREFIX + "%s plugin-" + PLUGIN_NAME;
    private static final Emojis INSTANCE = new Emojis();

    private final Map<String, String> availableIcons = new LinkedHashMap<>();
    private final Map<String, String> availableEmojis = new LinkedHashMap<>();

    private Emojis() {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(EMOJIS_LIST_RESOURCE_PATH)) {
            if (stream != null) {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] splitLine = line.split(":");
                        String entry = splitLine[0];
                        String emoji = splitLine[1];
                        availableEmojis.put(entry, emoji);
                        availableIcons.put(entry, getIconClassName(entry));
                    }
                }
            } else {
                LOGGER.warning("Unable to read available emojis: Resource unavailable.");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Unable to read available emojis: Resource unavailable.", ex);
        }
    }

    /**
     * Takes an emoji name and generates an icon class name from it.
     *
     * @param emoji the emoji name
     * @return the icon class name
     */
    public static String getIconClassName(String emoji) {
        return String.format(ICON_CLASS_NAME_PATTERN, emoji);
    }

    /**
     * Get all available icons provided by the custom-folder-icon-plugin.
     *
     * @return a sorted map of available emojis with emoji name as key and the icon class name as value.
     */
    public static Map<String, String> getAvailableIcons() {
        return INSTANCE.availableIcons;
    }

    /**
     * Get all available emojis provided by the custom-folder-icon-plugin.
     *
     * @return a sorted map of available emojis with emoji name as key and the actual Unicode emoji as value.
     */
    public static Map<String, String> getAvailableEmojis() {
        return INSTANCE.availableEmojis;
    }
}
