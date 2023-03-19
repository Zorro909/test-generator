package de.zorro909.test;

import java.lang.IllegalArgumentException;
import java.lang.String;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UnitToBeTestedTest {
    @InjectMocks
    UnitToBeTested uut;

    /**
     * Simple ping test without Input
     */
    @Test
    void pingSimpleTest() {
        // Input Parameters

        // Expected Output
        String expectedOutput = "Hallo";
        // Test
        String returnObject = this.uut.ping();
        Assertions.assertThat(returnObject).isEqualTo(expectedOutput);
    }

    /**
     * Ping with World as Input
     */
    @Test
    void pingNormalInputTest() {
        // Input Parameters
        String content = "World";

        // Expected Output
        String expectedOutput = "Hallo World";
        // Test
        String returnObject = this.uut.ping(content);
        Assertions.assertThat(returnObject).isEqualTo(expectedOutput);
    }

    /**
     * Ping with illegal input
     */
    @Test
    void pingNullExceptionTest() {
        // Input Parameters
        String content = null;

        Assertions.assertThatCode(() -> uut.ping(content)).isInstanceOf(IllegalArgumentException.class);
    }
}
