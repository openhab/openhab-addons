package org.openhab.binding.threema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.threema.internal.ThreemaHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;

class ThreemaHandlerIntegrationTest {

    private ThreemaHandler threemaHandler;
    private Thing thing;
    private Configuration config;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        config = TestUtils.readThingConfiguration();

        thing = mock(Thing.class);
        when(thing.getConfiguration()).thenReturn(config);

        threemaHandler = new ThreemaHandler(thing);

        CountDownLatch latch = new CountDownLatch(1);
        threemaHandler.initialize();
        threemaHandler.setCallback(new DefaultThingHandlerCallback() {
            @Override
            public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
                assertThat(thing).isEqualTo(ThreemaHandlerIntegrationTest.this.thing);
                assertThat(thingStatus.getStatus()).isEqualTo(ThingStatus.ONLINE);
                latch.countDown();
            }
        });
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
    }

    @AfterEach
    void tearDown() {
        threemaHandler.dispose();
    }

    @Test
    void testSendTextMessageSimpleStringString() {
        String[] recipientIds = Optional.ofNullable(config.get("recipientIds")).map(String.class::cast)
                .map(str -> str.split("'")).orElseThrow();
        assertThat(threemaHandler.sendTextMessageSimple(recipientIds[0], "Hello World")).isTrue();
    }

    @Test
    void testSendTextMessageSimpleString() {
        assertThat(threemaHandler.sendTextMessageSimple("Hello World")).isTrue();
    }
}
