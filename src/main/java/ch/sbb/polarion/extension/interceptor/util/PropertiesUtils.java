package ch.sbb.polarion.extension.interceptor.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertiesUtils {

    /**
     * Generates property string.
     * Treats each pair of items as a property key and value respectively.
     * E.g. arguments {'key1', 'value1', 'key2', 'value2'} will generate:
     * <code>
     * key1=value1
     * key2=value2
     * </code>
     */
    public String build(String... entries) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.length; i++) {
            builder.append(entries[i]).append(i % 2 == 0 ? "=" : (i == entries.length - 1 ? "" : System.lineSeparator()));
        }
        return builder.toString();
    }

    /**
     * Generates property string.
     * Treats each first item as a property entry description and second and third item as a property key and value respectively.
     * E.g. arguments {'first', 'key1', 'value1', 'second', 'key2', 'value2'} will generate:
     * <code>
     * #first
     * key1=value1
     * #second
     * key2=value2
     * </code>
     */
    public static String buildWithDescription(String... entries) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < entries.length; ++i) {
            switch (i % 3) {
                case 0 -> builder.append("# ").append(entries[i]).append(System.lineSeparator());
                case 1 -> builder.append(entries[i]).append("=");
                default -> builder.append(entries[i]).append(System.lineSeparator());
            }
        }

        return builder.toString();
    }
}
