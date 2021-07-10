package org.openhab.binding.threema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.threema.internal.ThreemaConfiguration;
import org.openhab.binding.threema.internal.ThreemaHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;

import ch.threema.apitool.APIConnector;

class ThreemaHandlerTest {

    private static final String GATEWAY_ID = "*TESTID1";
    private static final List<String> RECEPIENTS = List.of("FIRST", "SECOND", "THIRD");
    private ThreemaHandler threemaHandler;
    private Thing thing;
    private APIConnector apiConnector;
    private ThreemaConfiguration threemaConfig;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        threemaConfig = mock(ThreemaConfiguration.class);
        when(threemaConfig.getGatewayId()).thenReturn(GATEWAY_ID);

        Configuration config = mock(Configuration.class);
        when(config.as(ThreemaConfiguration.class)).thenReturn(threemaConfig);

        thing = mock(Thing.class);
        when(thing.getConfiguration()).thenReturn(config);

        apiConnector = mock(APIConnector.class);
    }

    @AfterEach
    void tearDown() {
        threemaHandler.dispose();
    }

    @Test
    void testInitialize() throws IOException, InterruptedException {
        // GIVEN
        BlockingQueue<ThingStatusInfo> stati = new ArrayBlockingQueue<ThingStatusInfo>(2);
        when(apiConnector.lookupCredits()).thenReturn(1);

        threemaHandler = new ThreemaHandler(thing, apiConnector);

        threemaHandler.setCallback(new DefaultThingHandlerCallback() {

            @Override
            public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
                assertThat(thing).isEqualTo(ThreemaHandlerTest.this.thing);
                stati.add(thingStatus);
            }
        });

        // WHEN
        threemaHandler.initialize();

        // THEN
        verify(threemaConfig).getGatewayId();
        assertThat(stati.take().getStatus()).isEqualTo(ThingStatus.UNKNOWN);
        assertThat(stati.take().getStatus()).isEqualTo(ThingStatus.ONLINE);
        assertThat(stati).isEmpty();
    }

    @Test
    void testSendTextMessageSimpleStringString() throws IOException {
        // GIVEN
        when(apiConnector.sendTextMessageSimple(Mockito.anyString(), Mockito.anyString())).thenReturn("12343");
        threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.initialize();

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isTrue();
        verify(apiConnector, atMost(1)).sendTextMessageSimple("*THREEMA", "Hello World");
    }

    @Test
    void testSendTextMessageSimpleString() throws IOException {
        // GIVEN
        when(threemaConfig.getRecipientIds()).thenReturn(RECEPIENTS);
        when(apiConnector.sendTextMessageSimple(Mockito.anyString(), Mockito.anyString())).thenReturn("12343");
        threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.initialize();

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("Hello World");

        // THEN
        assertThat(suceeded).isTrue();
        for (String id : RECEPIENTS) {
            verify(apiConnector).sendTextMessageSimple(id, "Hello World");
        }
    }
}
