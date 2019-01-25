/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
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
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.handler.AbstractWemoHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;

/**
 * Generic test class for all Wemo related tests that contains methods and constants used across the different test
 * classes
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public abstract class GenericWemoOSGiTest extends JavaOSGiTest {

    public static MockUpnpService mockUpnpService;
    public static final String DEVICE_MANUFACTURER = "Belkin";

    // This port is included in the run configuration
    private final int ORG_OSGI_SERVICE_HTTP_PORT = 9090;

    // Thing information
    protected String TEST_THING_ID = "TestThing";

    // UPnP Device information
    public static String DEVICE_UDN = "Test-1_0-22124";

    private final String DEVICE_TYPE = "Test";
    private final int DEVICE_VERSION = 1;
    private final String DEVICE_URL = "http://127.0.0.1:" + ORG_OSGI_SERVICE_HTTP_PORT;
    private final String DEVICE_DESCRIPTION_PATH = "/setup.xml";

    protected final String DEVICE_FRIENDLY_NAME = "WeMo Test";
    protected final String DEVICE_CONTROL_PATH = "/upnp/control/";
    protected final ChannelTypeUID DEFAULT_CHANNEL_TYPE_UID = new ChannelTypeUID(
            WemoBindingConstants.BINDING_ID + ":channelType");

    protected ManagedThingProvider managedThingProvider;
    protected UpnpIOService upnpIOService;
    protected ThingRegistry thingRegistry;

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

        ChannelTypeProvider channelTypeProvider = mock(ChannelTypeProvider.class);
        when(channelTypeProvider.getChannelType(any(), any())).thenReturn(new ChannelType(DEFAULT_CHANNEL_TYPE_UID,
                false, "Switch", ChannelKind.STATE, "label", null, null, null, null, null, null));
        registerService(channelTypeProvider);
    }

    protected Thing createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType,
            WemoHttpCall wemoHttpCaller) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN);

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        Channel channel = ChannelBuilder.create(channelUID, itemAcceptedType).withType(DEFAULT_CHANNEL_TYPE_UID)
                .withKind(ChannelKind.STATE).withLabel("label").build();

        thing = ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(configuration).withChannel(channel)
                .build();

        managedThingProvider.add(thing);

        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            AbstractWemoHandler h = (AbstractWemoHandler) handler;
            h.setWemoHttpCaller(wemoHttpCaller);
        }

        return thing;
    }

    protected void addUpnpDevice(String serviceTypeID, String serviceNumber, String modelName)
            throws MalformedURLException, URISyntaxException, ValidationException {
        UDN udn = new UDN(DEVICE_UDN);
        URL deviceURL = new URL(DEVICE_URL + DEVICE_DESCRIPTION_PATH);

        RemoteDeviceIdentity identity = new RemoteDeviceIdentity(udn, WemoBindingConstants.SUBSCRIPTION_DURATION,
                deviceURL, new byte[1], null);
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
