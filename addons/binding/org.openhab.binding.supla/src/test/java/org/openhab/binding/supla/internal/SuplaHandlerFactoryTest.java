package org.openhab.binding.supla.internal;


import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.openhab.binding.supla.handler.SuplaIoDeviceHandler;
import org.openhab.binding.supla.internal.discovery.SuplaDiscoveryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.supla.SuplaBindingConstants.BRIDGE_THING_TYPE;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_THING_TYPE;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class SuplaHandlerFactoryTest {
    private final SuplaHandlerFactory factory = new SuplaHandlerFactory();

    @Mock private BundleContext bundleContext;
    @Mock private Bundle bundle;

    @Mock private Thing thing;
    @Mock private Bridge bridge;

    @Before
    public void init() {
        factory.setBundleContext(bundleContext);
        given(bundleContext.getBundle()).willReturn(bundle);
        given(bundle.getBundleId()).willReturn(1L);
    }

    @Test
    public void shouldCreateHandlerForSuplaIoDevice() {

        // given
        given(thing.getThingTypeUID()).willReturn(SUPLA_IO_DEVICE_THING_TYPE);

        // when
        final ThingHandler handler = factory.createHandler(thing);

        // then
        assertThat(handler).isOfAnyClassIn(SuplaIoDeviceHandler.class);
    }

    @Test
    public void shouldCreateHandlerForSuplaCloudBridge() {

        // given
        given(bridge.getThingTypeUID()).willReturn(BRIDGE_THING_TYPE);

        // when
        final ThingHandler handler = factory.createHandler(bridge);

        // then
        assertThat(handler).isOfAnyClassIn(SuplaCloudBridgeHandler.class);
    }

    @Test
    public void shouldRegisterServiceDiscoveryWhenCreatingABridge() {

        // given
        given(bridge.getThingTypeUID()).willReturn(BRIDGE_THING_TYPE);

        // when
        factory.createHandler(bridge);

        // then
        verify(bundleContext).registerService(
                eq(DiscoveryService.class.getName()),
                any(SuplaDiscoveryService.class),
                eq(new Hashtable<>()));
    }

    @Test
    public void shouldReturnNullIfDontKnowThingType() {

        // given
        given(thing.getThingTypeUID()).willReturn(new ThingTypeUID("uuid:seg2"));

        // when
        final ThingHandler handler = factory.createHandler(thing);

        // then
        assertThat(handler).isNull();
    }
}
