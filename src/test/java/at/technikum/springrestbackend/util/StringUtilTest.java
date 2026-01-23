package at.technikum.springrestbackend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    // ==================== getNonBlank ====================

    @Test
    void getNonBlank_validString_returnsOptionalWithTrimmedValue() {
        Optional<String> result = StringUtil.getNonBlank("  hello  ");

        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }


    @Test
    void getNonBlank_validStringNoSpaces_returnsOptionalWithValue() {
        Optional<String> result = StringUtil.getNonBlank("hello");

        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
    }

    @Test
    void getNonBlank_null_returnsEmpty() {
        Optional<String> result = StringUtil.getNonBlank(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getNonBlank_emptyString_returnsEmpty() {
        Optional<String> result = StringUtil.getNonBlank("");

        assertTrue(result.isEmpty());
    }

    @Test
    void getNonBlank_blankString_returnsEmpty() {
        Optional<String> result = StringUtil.getNonBlank("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void getNonBlank_tabsAndNewlines_returnsEmpty() {
        Optional<String> result = StringUtil.getNonBlank("\t\n  \r");

        assertTrue(result.isEmpty());
    }

    // ==================== isBlank ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
    void isBlank_blankValues_returnsTrue(String value) {
        assertTrue(StringUtil.isBlank(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "hello", "  x  ", "123"})
    void isBlank_nonBlankValues_returnsFalse(String value) {
        assertFalse(StringUtil.isBlank(value));
    }
}