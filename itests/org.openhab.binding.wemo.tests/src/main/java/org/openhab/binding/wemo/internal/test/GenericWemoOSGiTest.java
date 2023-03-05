/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wemo.internal.test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.jupnp.UpnpService;
import org.jupnp.mock.MockUpnpService;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.ServiceType;
import org.jupnp.model.types.UDN;
import org.mockito.Mockito;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.WemoHttpCallFactory;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Generic test class for all Wemo related tests that contains methods and constants used across the different test
 * classes
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public abstract class GenericWemoOSGiTest extends JavaOSGiTest {

    public static final String DEVICE_MANUFACTURER = "Belkin";

    // This port is included in the run configuration
    private static final int ORG_OSGI_SERVICE_HTTP_PORT = 9090;

    // Thing information
    protected static final String TEST_THING_ID = "TestThing";

    // UPnP Device information
    public static final String DEVICE_UDN = "Test-1_0-22124";

    private static final String DEVICE_TYPE = "Test";
    private static final int DEVICE_VERSION = 1;
    private static final String DEVICE_URL = "http://127.0.0.1:" + ORG_OSGI_SERVICE_HTTP_PORT;
    private static final String DEVICE_DESCRIPTION_PATH = "/setup.xml";

    protected static final String DEVICE_FRIENDLY_NAME = "WeMo Test";
    protected static final String DEVICE_CONTROL_PATH = "/upnp/control/";
    protected static final ChannelTypeUID DEFAULT_CHANNEL_TYPE_UID = new ChannelTypeUID(
            WemoBindingConstants.BINDING_ID + ":channelType");

    protected ManagedThingProvider managedThingProvider;
    protected UpnpIOService upnpIOService;
    protected ThingRegistry thingRegistry;

    protected WemoHttpCall mockCaller;
    protected MockUpnpService mockUpnpService;

    protected Thing thing;

    protected void setUpServices() throws IOException {
        // StorageService is required from the ManagedThingProvider
        VolatileStorageService volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        // Mock the UPnP Service, that is required from the UPnP IO Service
        mockUpnpService = new MockUpnpService(false, true);
        mockUpnpService.startup();
        registerService(mockUpnpService, UpnpService.class.getName());

        managedThingProvider = getService(ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));

        thingRegistry = getService(ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        // UPnP IO Service is required from the WemoDiscoveryService and WemoHandlerFactory
        upnpIOService = getService(UpnpIOService.class);
        assertThat(upnpIOService, is(notNullValue()));

        mockCaller = Mockito.spy(new WemoHttpCall());
        doReturn(true).when(mockCaller).probeURL(any());
        WemoHttpCallFactory wemoHttpCallFactory = () -> mockCaller;
        registerService(wemoHttpCallFactory, WemoHttpCallFactory.class.getName());

        ChannelTypeProvider channelTypeProvider = mock(ChannelTypeProvider.class);
        when(channelTypeProvider.getChannelType(any(ChannelTypeUID.class), nullable(Locale.class))).thenReturn(
                ChannelTypeBuilder.state(DEFAULT_CHANNEL_TYPE_UID, "label", CoreItemFactory.SWITCH).build());
        registerService(channelTypeProvider);
    }

    protected Thing createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN);

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        Channel channel = ChannelBuilder.create(channelUID, itemAcceptedType).withType(DEFAULT_CHANNEL_TYPE_UID)
                .withKind(ChannelKind.STATE).withLabel("label").build();

        thing = ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(configuration).withChannel(channel)
                .build();

        managedThingProvider.add(thing);
        return thing;
    }

    protected void addUpnpDevice(String serviceTypeID, String serviceNumber, String modelName)
            throws MalformedURLException, URISyntaxException, ValidationException {
        UDN udn = new UDN(DEVICE_UDN);
        URL deviceURL = new URL(DEVICE_URL + DEVICE_DESCRIPTION_PATH);

        RemoteDeviceIdentity identity = new RemoteDeviceIdentity(udn,
                WemoBindingConstants.SUBSCRIPTION_DURATION_SECONDS, deviceURL, new byte[1], null);
        DeviceType type = new DeviceType(DEVICE_MANUFACTURER, DEVICE_TYPE, DEVICE_VERSION);

        ManufacturerDetails manufacturerDetails = new ManufacturerDetails(DEVICE_MANUFACTURER);
        ModelDetails modelDetails = new ModelDetails(modelName);
        DeviceDetails details = new DeviceDetails(DEVICE_FRIENDLY_NAME, manufacturerDetails, modelDetails);

        ServiceType serviceType = new ServiceType(DEVICE_MANUFACTURER, serviceTypeID);
        ServiceId serviceId = new ServiceId(DEVICE_MANUFACTURER, serviceNumber);

        // Use the same URI for control, event subscription and device description
        URI mockURI = new URI(DEVICE_URL + DEVICE_DESCRIPTION_PATH);
        URI descriptorURI = mockURI;
        URI controlURI = mockURI;
        URI eventSubscriptionURI = mockURI;

        RemoteService service = new RemoteService(serviceType, serviceId, descriptorURI, controlURI,
                eventSubscriptionURI);

        RemoteDevice localDevice = new RemoteDevice(identity, type, details, service);
        mockUpnpService.getRegistry().addDevice(localDevice);
    }
}
