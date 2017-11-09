/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import static org.junit.Assert.*;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;

import org.junit.Test;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.util.JAXBtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link DevicelistModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AVMFritzModelTest {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzModelTest.class);

    @Test
    public void validateDevicelistModel() {
        String xml = "<devicelist version=\"1\">"
                + "<group identifier=\"F0:A3:7F-900\" id=\"20001\" functionbitmask=\"640\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>1000</masterdeviceid><members>20000</members></groupinfo></group>"
                + "<device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>45</power><energy>166</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>"
                + "<device identifier=\"08761 0000435\" id=\"18\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\"><present>0</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>"
                + "<device identifier=\"5C:49:79:F0:A3:84\" id=\"19\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter></device>"
                + "<device identifier=\"08761 0954669\" id=\"20\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device>"
                + "</devicelist>";

        DevicelistModel devices = JAXBtUtils.buildResult(xml);
        assertNotNull(devices);
        assertEquals(devices.getDevicelist().size(), 4);
        assertEquals(devices.getXmlApiVersion(), "1");

        DeviceModel device1 = devices.getDevicelist().stream()
                .filter(it -> "FRITZ!DECT Repeater 100".equals(it.getProductName())).findFirst().orElse(null);
        assertNotNull(device1);
        validateDECTRepeater100Model(device1);

        DeviceModel device2 = devices.getDevicelist().stream()
                .filter(it -> "FRITZ!DECT 200".equals(it.getProductName())).findFirst().orElse(null);
        assertNotNull(device2);
        validateDECT200Model(device2);

        DeviceModel device3 = devices.getDevicelist().stream().filter(it -> "Comet DECT".equals(it.getProductName()))
                .findFirst().orElse(null);
        assertNotNull(device3);
        validateCometDECTModel(device3);

        DeviceModel device4 = devices.getDevicelist().stream()
                .filter(it -> "FRITZ!Powerline 546E".equals(it.getProductName())).findFirst().orElse(null);
        assertNotNull(device4);
        validatePowerline546EModel(device4);
    }

    private void validateDECTRepeater100Model(DeviceModel device) {
        assertEquals(device.getProductName(), "FRITZ!DECT Repeater 100");
        assertEquals(device.getIdentifier(), "087610954669");
        assertEquals(device.getDeviceId(), "20");
        assertEquals(device.getFirmwareVersion(), "03.86");
        assertEquals(device.getManufacturer(), "AVM");

        assertEquals(device.getPresent(), 1);
        assertEquals(device.getName(), "FRITZ!DECT Repeater 100 #5");

        assertTrue(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(device.getTemperature().getCelsius(), new BigDecimal("23.0"));
        assertEquals(device.getTemperature().getOffset(), new BigDecimal("0.0"));
    }

    private void validateDECT200Model(DeviceModel device) {
        assertEquals(device.getProductName(), "FRITZ!DECT 200");
        assertEquals(device.getIdentifier(), "087610000434");
        assertEquals(device.getDeviceId(), "17");
        assertEquals(device.getFirmwareVersion(), "03.83");
        assertEquals(device.getManufacturer(), "AVM");

        assertEquals(device.getPresent(), 1);
        assertEquals(device.getName(), "FRITZ!DECT 200 #1");

        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNotNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(device.getSwitch().getState(), SwitchModel.ON);
        assertEquals(device.getSwitch().getMode(), MODE_MANUAL);
        assertEquals(device.getSwitch().getLock(), BigDecimal.ZERO);
        assertEquals(device.getSwitch().getDevicelock(), BigDecimal.ZERO);

        assertEquals(device.getTemperature().getCelsius(), new BigDecimal("25.5"));
        assertEquals(device.getTemperature().getOffset(), new BigDecimal("0.0"));

        assertEquals(device.getPowermeter().getPower(), new BigDecimal("0.045"));
        assertEquals(device.getPowermeter().getEnergy(), new BigDecimal("0.166"));
    }

    private void validateCometDECTModel(DeviceModel device) {
        assertEquals(device.getProductName(), "Comet DECT");
        assertEquals(device.getIdentifier(), "087610000435");
        assertEquals(device.getDeviceId(), "18");
        assertEquals(device.getFirmwareVersion(), "03.50");
        assertEquals(device.getManufacturer(), "AVM");

        assertEquals(device.getPresent(), 0);
        assertEquals(device.getName(), "Comet DECT #1");

        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNotNull(device.getHkr());

        assertEquals(device.getTemperature().getCelsius(), new BigDecimal("22.0"));
        assertEquals(device.getTemperature().getOffset(), new BigDecimal("-1.0"));

        assertEquals(device.getHkr().getTist(), new BigDecimal(44));
        assertEquals(device.getHkr().getTsoll(), new BigDecimal(42));
        assertEquals(device.getHkr().getAbsenk(), new BigDecimal(28));
        assertEquals(device.getHkr().getKomfort(), new BigDecimal(42));
        assertEquals(device.getHkr().getLock(), BigDecimal.ONE);
        assertEquals(device.getHkr().getDevicelock(), BigDecimal.ONE);
        assertEquals(device.getHkr().getBatterylow(), HeatingModel.BATTERY_OFF);
        assertEquals(device.getHkr().getMode(), MODE_AUTO);
        assertEquals(device.getHkr().getRadiatorMode(), MODE_COMFORT);

        assertNotNull(device.getHkr().getNextchange());

        assertEquals(device.getHkr().getNextchange().getEndperiod(), 1484341200);
        assertEquals(device.getHkr().getNextchange().getTchange(), new BigDecimal(28));
    }

    private void validatePowerline546EModel(DeviceModel device) {
        assertEquals(device.getProductName(), "FRITZ!Powerline 546E");
        assertEquals(device.getIdentifier(), "5C:49:79:F0:A3:84");
        assertEquals(device.getDeviceId(), "19");
        assertEquals(device.getFirmwareVersion(), "06.92");
        assertEquals(device.getManufacturer(), "AVM");

        assertEquals(device.getPresent(), 1);
        assertEquals(device.getName(), "FRITZ!Powerline 546E #1");

        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getSwitch());
        assertNull(device.getTemperature());
        assertNotNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(device.getSwitch().getState(), SwitchModel.OFF);
        assertEquals(device.getSwitch().getMode(), MODE_MANUAL);
        assertEquals(device.getSwitch().getLock(), BigDecimal.ZERO);
        assertEquals(device.getSwitch().getDevicelock(), BigDecimal.ONE);

        assertEquals(device.getPowermeter().getPower(), new BigDecimal("0.000"));
        assertEquals(device.getPowermeter().getEnergy(), new BigDecimal("2.087"));
    }

    @Test
    public void validateTemperatureConversion() {
        assertEquals(HeatingModel.fromCelsius(null), BigDecimal.ZERO);
        assertEquals(HeatingModel.fromCelsius(BigDecimal.ONE), HeatingModel.TEMP_FRITZ_MIN);
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(7.5)), HeatingModel.TEMP_FRITZ_MIN);
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(8)), new BigDecimal(16));
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(14)), new BigDecimal(28));
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(14.5)), new BigDecimal(29));
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(28)), new BigDecimal(56));
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(28.5)), HeatingModel.TEMP_FRITZ_MAX);
        assertEquals(HeatingModel.fromCelsius(new BigDecimal(30)), HeatingModel.TEMP_FRITZ_MAX);

        assertEquals(HeatingModel.toCelsius(null), BigDecimal.ZERO);
        assertEquals(HeatingModel.toCelsius(new BigDecimal(28)), new BigDecimal("14.0"));
        assertEquals(HeatingModel.toCelsius(new BigDecimal(29)), new BigDecimal("14.5"));
        assertEquals(HeatingModel.toCelsius(new BigDecimal(253)), new BigDecimal("6.0"));
        assertEquals(HeatingModel.toCelsius(new BigDecimal(254)), new BigDecimal("30.0"));
    }
}
