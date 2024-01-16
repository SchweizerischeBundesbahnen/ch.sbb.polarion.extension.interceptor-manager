package ch.sbb.polarion.extension.interceptor.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScopeUtilsTest {

    private static Stream<Arguments> testValuesForGetProjectByScope() {
        return Stream.of(
                Arguments.of("", null),
                Arguments.of("project/elibrary/", "elibrary"),
                Arguments.of("projects/test/", null),
                Arguments.of("invalid", null)
        );
    }

    @ParameterizedTest
    @MethodSource("testValuesForGetProjectByScope")
    void getProjectByScope(String scope, String expected) {
        String projectFromScope = ScopeUtils.getProjectFromScope(scope);
        assertEquals(expected, projectFromScope);
    }
}