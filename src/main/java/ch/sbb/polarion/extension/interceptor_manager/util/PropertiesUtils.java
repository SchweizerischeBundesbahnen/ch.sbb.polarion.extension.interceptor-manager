package ch.sbb.polarion.extension.interceptor_manager.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Generates comma-separated strings representing all possible combinations of the provided values in same order as they come in
     * the input array and a character '*', e.g. for the input values ['projectId', 'typeId', 'fieldId'] the result must be as follows:
     * <code>
     * projectId.typeId.fieldId
     * projectId.typeId.*
     * projectId.*.fieldId=
     * projectId.*.*
     * *.typeId.fieldId
     * *.typeId.*
     * *.*.fieldId
     * *.*.*
     * </code>
     *
     * @param selectors array of selectors
     * @return all possible combinations
     */
    public List<String> generateSelectorsCombinations(String... selectors) {
        if (selectors.length == 0) {
            return new ArrayList<>();
        }
        List<String> combinations = new ArrayList<>();
        generateSelectorsCombinationsRecursive(selectors, 0, "", combinations);
        return combinations;
    }

    private void generateSelectorsCombinationsRecursive(String[] values, int index, String current, List<String> combinations) {
        if (index == values.length) {
            combinations.add(current.substring(1)); // remove the leading "."
            return;
        }
        // add the current value
        generateSelectorsCombinationsRecursive(values, index + 1, current + "." + values[index], combinations);
        // add the wildcard '*'
        generateSelectorsCombinationsRecursive(values, index + 1, current + ".*", combinations);
    }
}
