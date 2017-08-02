package org.openhab.binding.supla.internal;


import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.handler.SuplaCloudBridgeHandler;
import org.openhab.binding.supla.handler.SuplaIoDeviceHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.openhab.binding.supla.SuplaBindingConstants.BRIDGE_THING_TYPE;
import static org.openhab.binding.supla.SuplaBindingConstants.SUPLA_IO_DEVICE_THING_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class SuplaHandlerFactoryTest {
    private final SuplaHandlerFactory factory = new SuplaHandlerFactory();

    @Mock private BundleContext bundleContext;
    @Mock private Bundle bundle;

    @Mock private Thing thing;
    @Mock private Bridge bridge;

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
        factory.setBundleContext(bundleContext);
        given(bundleContext.getBundle()).willReturn(bundle);
        given(bundle.getBundleId()).willReturn(1L);

        // when
        final ThingHandler handler = factory.createHandler(bridge);

        // then
        assertThat(handler).isOfAnyClassIn(SuplaCloudBridgeHandler.class);
    }
}
