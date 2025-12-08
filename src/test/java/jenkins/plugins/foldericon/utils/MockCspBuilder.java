package jenkins.plugins.foldericon.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jenkins.security.csp.CspBuilder;

/**
 * Mocked {@link CspBuilder} for testing.
 */
public class MockCspBuilder extends CspBuilder {

    private String directive;
    private String[] values;

    @Override
    public CspBuilder add(String directive, String... values) {
        this.directive = directive;
        this.values = values;
        return this;
    }

    public void assertDirective(String expected) {
        assertEquals(expected, directive);
    }

    public void assertValues(String[] expected) {
        assertArrayEquals(expected, values);
    }

    /**
     * Reset the mock.
     */
    public void reset() {
        directive = null;
        values = null;
    }
}
