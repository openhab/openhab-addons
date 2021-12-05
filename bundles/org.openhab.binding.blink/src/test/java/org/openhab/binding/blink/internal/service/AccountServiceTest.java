package org.openhab.binding.blink.internal.service;

import java.util.stream.IntStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class AccountServiceTest {

    AccountService accountService;
    private @NonNullByDefault({}) HttpClient httpClient;
    private @NonNullByDefault({}) Gson gson;

    @BeforeEach
    void setup() {
        this.accountService = new AccountService(httpClient, gson);
    }

    @Test
    void testGenerateClientId() {
        String format = "BlinkCamera_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{6}";
        assertThat(accountService.generateClientId(), matchesPattern(format));
    }

    @Test
    void testRandomNumberSuccessful() {
        IntStream.range(1, 10)
                .forEach(i -> assertThat(accountService.randomNumber(i), matchesPattern("\\d{" + i + "}")));
    }

    @Test
    void testRandomNumberExceptions() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.randomNumber(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> accountService.randomNumber(10));
    }
}