/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.avmfritz.handler.BoxHandler;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;

/**
 * Tests for {@link AVMFritzDiscoveryService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 *
 */
public class AVMFritzDiscoveryServiceTest {

    private static final ThingUID BRIGE_THING_ID = new ThingUID("avmfritz:fritzbox:1");

    @Mock
    private BoxHandler handler;

    private DiscoveryListener listener;
    private DiscoveryResult discoveryResult;

    private AVMFritzDiscoveryService discovery;

    @Before
    public void setUp() {
        initMocks(this);
        when(handler.getThing()).thenReturn(BridgeBuilder.create(BRIDGE_THING_TYPE, "1").build());
        discovery = new AVMFritzDiscoveryService(handler);
        listener = new DiscoveryListener() {
            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                discoveryResult = result;
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                return Collections.emptyList();
            }
        };
        discovery.addDiscoveryListener(listener);
    }

    @After
    public void cleanUp() {
        discoveryResult = null;
    }

    @Test
    public void correctSupportedTypes() {
        assertThat(discovery.getSupportedThingTypes().size(), is(7));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT100_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT200_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT210_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT300_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(DECT301_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(PL546E_THING_TYPE));
        assertTrue(discovery.getSupportedThingTypes().contains(COMETDECT_THING_TYPE));
    }

    @Test
    public void invalidDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><group identifier=\"F0:A3:7F-900\" id=\"20001\" functionbitmask=\"640\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>1000</masterdeviceid><members>20000</members></groupinfo></group></devicelist>";

        DevicelistModel devices = JAXBUtils.buildResult(xml);
        assertNotNull(devices);
        assertThat(devices.getDevicelist().size(), is(0));
    }

    @Test
    public void validDECTRepeater100Result() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0954669\" id=\"20\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device></devicelist>";

        DevicelistModel devices = JAXBUtils.buildResult(xml);
        assertNotNull(devices);
        assertThat(devices.getDevicelist().size(), is(1));

        DeviceModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("avmfritz:FRITZ_DECT_Repeater_100:1:087610954669")));
        assertThat(discoveryResult.getThingTypeUID(), is(DECT100_THING_TYPE));
        assertThat(discoveryResult.getBridgeUID(), is(BRIGE_THING_ID));
        assertThat(discoveryResult.getProperties().get(THING_AIN), is("087610954669"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_VENDOR), is("AVM"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_MODEL_ID), is("20"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER), is("087610954669"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION), is("03.86"));
        assertThat(discoveryResult.getRepresentationProperty(), is(THING_AIN));
    }

    @Test
    public void validDECT200DiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>45</power><energy>166</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device></devicelist>";

        DevicelistModel devices = JAXBUtils.buildResult(xml);
        assertNotNull(devices);
        assertThat(devices.getDevicelist().size(), is(1));

        DeviceModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("avmfritz:FRITZ_DECT_200:1:087610000434")));
        assertThat(discoveryResult.getThingTypeUID(), is(DECT200_THING_TYPE));
        assertThat(discoveryResult.getBridgeUID(), is(BRIGE_THING_ID));
        assertThat(discoveryResult.getProperties().get(THING_AIN), is("087610000434"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_VENDOR), is("AVM"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_MODEL_ID), is("17"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER), is("087610000434"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION), is("03.83"));
        assertThat(discoveryResult.getRepresentationProperty(), is(THING_AIN));
    }

    @Test
    public void validCometDECTDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\"><present>1</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>0</lock><devicelock>0</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device></devicelist>";

        DevicelistModel devices = JAXBUtils.buildResult(xml);
        assertNotNull(devices);
        assertThat(devices.getDevicelist().size(), is(1));

        DeviceModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(), is(new ThingUID("avmfritz:Comet_DECT:1:087610000435")));
        assertThat(discoveryResult.getThingTypeUID(), is(COMETDECT_THING_TYPE));
        assertThat(discoveryResult.getBridgeUID(), is(BRIGE_THING_ID));
        assertThat(discoveryResult.getProperties().get(THING_AIN), is("087610000435"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_VENDOR), is("AVM"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_MODEL_ID), is("18"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER), is("087610000435"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION), is("03.50"));
        assertThat(discoveryResult.getRepresentationProperty(), is(THING_AIN));
    }

    @Test
    public void validPowerline546EDiscoveryResult() throws JAXBException {
        String xml = "<devicelist version=\"1\"><device identifier=\"5C:49:79:F0:A3:84\" id=\"19\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter></device></devicelist>";

        DevicelistModel devices = JAXBUtils.buildResult(xml);
        assertNotNull(devices);
        assertThat(devices.getDevicelist().size(), is(1));

        DeviceModel device = devices.getDevicelist().get(0);
        assertNotNull(device);

        discovery.onDeviceAddedInternal(device);
        assertNotNull(discoveryResult);

        assertThat(discoveryResult.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(discoveryResult.getThingUID(),
                is(new ThingUID("avmfritz:FRITZ_Powerline_546E:1:5C_49_79_F0_A3_84")));
        assertThat(discoveryResult.getThingTypeUID(), is(PL546E_THING_TYPE));
        assertThat(discoveryResult.getBridgeUID(), is(BRIGE_THING_ID));
        assertThat(discoveryResult.getProperties().get(THING_AIN), is("5C:49:79:F0:A3:84"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_VENDOR), is("AVM"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_MODEL_ID), is("19"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_SERIAL_NUMBER), is("5C:49:79:F0:A3:84"));
        assertThat(discoveryResult.getProperties().get(PROPERTY_FIRMWARE_VERSION), is("06.92"));
        assertThat(discoveryResult.getRepresentationProperty(), is(THING_AIN));
    }
}
