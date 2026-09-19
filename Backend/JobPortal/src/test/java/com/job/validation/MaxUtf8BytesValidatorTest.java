package com.job.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plain unit test (no Spring context) for MaxUtf8BytesValidator's UTF-8 byte counting. */
class MaxUtf8BytesValidatorTest {

    private static class Holder {
        @MaxUtf8Bytes(72)
        String value;
    }

    private MaxUtf8BytesValidator validator;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("value");
        MaxUtf8Bytes annotation = field.getAnnotation(MaxUtf8Bytes.class);
        validator = new MaxUtf8BytesValidator();
        validator.initialize(annotation);
    }

    @Test
    void seventyTwoAsciiCharsIsValid() {
        assertTrue(validator.isValid("a".repeat(72), null));
    }

    @Test
    void seventyThreeAsciiCharsIsInvalid() {
        assertFalse(validator.isValid("a".repeat(73), null));
    }

    @Test
    void thirtySixTwoByteCharsIsValid() {
        // 'e with acute' (U+00E9) is 2 bytes in UTF-8: 36 * 2 = 72 bytes, exactly at the limit
        assertTrue(validator.isValid("é".repeat(36), null));
    }

    @Test
    void thirtySevenTwoByteCharsIsInvalid() {
        // 37 * 2 = 74 bytes
        assertFalse(validator.isValid("é".repeat(37), null));
    }

    @Test
    void fourByteEmojiCase() {
        // U+1F600 (grinning face) is 4 bytes in UTF-8: 18 * 4 = 72 (valid), 19 * 4 = 76 (invalid)
        String emoji = "😀";
        assertTrue(validator.isValid(emoji.repeat(18), null));
        assertFalse(validator.isValid(emoji.repeat(19), null));
    }

    @Test
    void nullIsValid() {
        assertTrue(validator.isValid(null, null));
    }
}
