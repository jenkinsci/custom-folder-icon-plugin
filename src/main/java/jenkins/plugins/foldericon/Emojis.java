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

import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to work with emojis provided by the custom-folder-icon-plugin.
 *
 * @author strangelookingnerd
 */
public final class Emojis {

    private static final Logger LOGGER = Logger.getLogger(Emojis.class.getName());
    private static final String EMOJIS_LIST_RESOURCE_PATH = "jenkins/plugins/foldericon/EmojiFolderIcon/emojis.list";
    private static final String PLUGIN_NAME = "custom-folder-icon";
    private static final String EMOJI_PREFIX = "emoji_";
    private static final String ICON_CLASS_NAME_PATTERN = "symbol-" + EMOJI_PREFIX + "%s plugin-" + PLUGIN_NAME;
    private static final Emojis INSTANCE = new Emojis();

    private final Map<String, String> availableIcons = new LinkedHashMap<>();
    private Map<String, String> availableEmojis;

    private Emojis() {
        try {
            URL url = getClass().getClassLoader().getResource(EMOJIS_LIST_RESOURCE_PATH);
            if(url != null) {
                String entries = FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
                availableEmojis = Splitter.onPattern("\r?\n")
                        .withKeyValueSeparator(':')
                        .split(entries);
                availableEmojis.keySet().forEach(entry -> availableIcons.put(entry, getIconClassName(entry)));
            } else {
                availableEmojis = Map.of();
            }
        } catch (Exception ex) {
            availableEmojis = Map.of();
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
     * @return an immutable sorted map of available emojis with emoji name as key and the actual unicode emoji as value.
     */
    public static Map<String, String> getAvailableEmojis() {
        return INSTANCE.availableEmojis;
    }

}
