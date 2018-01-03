/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import static org.junit.Assert.*;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link DevicelistModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 *
 */
public class AVMFritzModelTest {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzModelTest.class);

    private DevicelistModel devices;

    @Before
    public void setUp() {
        String xml = "<devicelist version=\"1\">"
                + "<group identifier=\"F0:A3:7F-900\" id=\"20001\" functionbitmask=\"640\" fwversion=\"1.0\" manufacturer=\"AVM\" productname=\"\"><present>1</present><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter><groupinfo><masterdeviceid>1000</masterdeviceid><members>20000</members></groupinfo></group>"
                + "<device identifier=\"08761 0000434\" id=\"17\" functionbitmask=\"2944\" fwversion=\"03.83\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 200\"><present>1</present><name>FRITZ!DECT 200 #1</name><switch><state>1</state><mode>manuell</mode><lock>0</lock><devicelock>0</devicelock></switch><powermeter><power>45</power><energy>166</energy></powermeter><temperature><celsius>255</celsius><offset>0</offset></temperature></device>"
                + "<device identifier=\"08761 0000437\" id=\"20\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 300\"><present>0</present><name>FRITZ!DECT 300 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>"
                + "<device identifier=\"08761 0000436\" id=\"21\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"FRITZ!DECT 301\"><present>0</present><name>FRITZ!DECT 301 #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>"
                + "<device identifier=\"08761 0000435\" id=\"22\" functionbitmask=\"320\" fwversion=\"03.50\" manufacturer=\"AVM\" productname=\"Comet DECT\"><present>0</present><name>Comet DECT #1</name><temperature><celsius>220</celsius><offset>-10</offset></temperature><hkr><tist>44</tist><tsoll>42</tsoll><absenk>28</absenk><komfort>42</komfort><lock>1</lock><devicelock>1</devicelock><errorcode>0</errorcode><batterylow>0</batterylow><nextchange><endperiod>1484341200</endperiod><tchange>28</tchange></nextchange></hkr></device>"
                + "<device identifier=\"5C:49:79:F0:A3:84\" id=\"30\" functionbitmask=\"640\" fwversion=\"06.92\" manufacturer=\"AVM\" productname=\"FRITZ!Powerline 546E\"><present>1</present><name>FRITZ!Powerline 546E #1</name><switch><state>0</state><mode>manuell</mode><lock>0</lock><devicelock>1</devicelock></switch><powermeter><power>0</power><energy>2087</energy></powermeter></device>"
                + "<device identifier=\"08761 0954669\" id=\"40\" functionbitmask=\"1280\" fwversion=\"03.86\" manufacturer=\"AVM\" productname=\"FRITZ!DECT Repeater 100\"><present>1</present><name>FRITZ!DECT Repeater 100 #5</name><temperature><celsius>230</celsius><offset>0</offset></temperature></device>"
                + "</devicelist>";

        try {
            devices = JAXBUtils.buildResult(xml);
        } catch (JAXBException e) {
            logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
        }
    }

    @Test
    public void validateDevicelistModel() {
        assertNotNull(devices);
        assertEquals(devices.getDevicelist().size(), 6);
        assertEquals(devices.getXmlApiVersion(), "1");
    }

    @Test
    public void validateDECTRepeater100Model() {
        Optional<DeviceModel> optionalDevice = findDevice("FRITZ!DECT Repeater 100");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("FRITZ!DECT Repeater 100", device.getProductName());
        assertEquals("087610954669", device.getIdentifier());
        assertEquals("40", device.getDeviceId());
        assertEquals("03.86", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT Repeater 100 #5", device.getName());

        assertTrue(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(new BigDecimal("23.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());
    }

    @Test
    public void validateDECT200Model() {
        Optional<DeviceModel> optionalDevice = findDevice("FRITZ!DECT 200");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("FRITZ!DECT 200", device.getProductName());
        assertEquals("087610000434", device.getIdentifier());
        assertEquals("17", device.getDeviceId());
        assertEquals("03.83", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!DECT 200 #1", device.getName());

        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNotNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(SwitchModel.ON, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getDevicelock());

        assertEquals(new BigDecimal("25.5"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("0.0"), device.getTemperature().getOffset());

        assertEquals(new BigDecimal("0.045"), device.getPowermeter().getPower());
        assertEquals(new BigDecimal("0.166"), device.getPowermeter().getEnergy());
    }

    @Test
    public void validateDECT300Model() {
        Optional<DeviceModel> optionalDevice = findDevice("FRITZ!DECT 300");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("FRITZ!DECT 300", device.getProductName());
        assertEquals("087610000437", device.getIdentifier());
        assertEquals("20", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("FRITZ!DECT 300 #1", device.getName());

        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNotNull(device.getHkr());

        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertEquals(new BigDecimal(44), device.getHkr().getTist());
        assertEquals(new BigDecimal(42), device.getHkr().getTsoll());
        assertEquals(new BigDecimal(28), device.getHkr().getAbsenk());
        assertEquals(new BigDecimal(42), device.getHkr().getKomfort());
        assertEquals(BigDecimal.ONE, device.getHkr().getLock());
        assertEquals(BigDecimal.ONE, device.getHkr().getDevicelock());
        assertEquals(HeatingModel.BATTERY_OFF, device.getHkr().getBatterylow());
        assertEquals(MODE_AUTO, device.getHkr().getMode());
        assertEquals(MODE_COMFORT, device.getHkr().getRadiatorMode());

        assertNotNull(device.getHkr().getNextchange());

        assertEquals(1484341200, device.getHkr().getNextchange().getEndperiod());
        assertEquals(new BigDecimal(28), device.getHkr().getNextchange().getTchange());
    }

    @Test
    public void validateDECT301Model() {
        Optional<DeviceModel> optionalDevice = findDevice("FRITZ!DECT 301");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("FRITZ!DECT 301", device.getProductName());
        assertEquals("087610000436", device.getIdentifier());
        assertEquals("21", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("FRITZ!DECT 301 #1", device.getName());

        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNotNull(device.getHkr());

        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertEquals(new BigDecimal(44), device.getHkr().getTist());
        assertEquals(new BigDecimal(42), device.getHkr().getTsoll());
        assertEquals(new BigDecimal(28), device.getHkr().getAbsenk());
        assertEquals(new BigDecimal(42), device.getHkr().getKomfort());
        assertEquals(BigDecimal.ONE, device.getHkr().getLock());
        assertEquals(BigDecimal.ONE, device.getHkr().getDevicelock());
        assertEquals(HeatingModel.BATTERY_OFF, device.getHkr().getBatterylow());
        assertEquals(MODE_AUTO, device.getHkr().getMode());
        assertEquals(MODE_COMFORT, device.getHkr().getRadiatorMode());

        assertNotNull(device.getHkr().getNextchange());

        assertEquals(1484341200, device.getHkr().getNextchange().getEndperiod());
        assertEquals(new BigDecimal(28), device.getHkr().getNextchange().getTchange());
    }

    @Test
    public void validateCometDECTModel() {
        Optional<DeviceModel> optionalDevice = findDevice("Comet DECT");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("Comet DECT", device.getProductName());
        assertEquals("087610000435", device.getIdentifier());
        assertEquals("22", device.getDeviceId());
        assertEquals("03.50", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(0, device.getPresent());
        assertEquals("Comet DECT #1", device.getName());

        assertFalse(device.isDectRepeater());
        assertFalse(device.isSwitchableOutlet());
        assertTrue(device.isTempSensor());
        assertFalse(device.isPowermeter());
        assertTrue(device.isHeatingThermostat());

        assertNull(device.getSwitch());
        assertNotNull(device.getTemperature());
        assertNull(device.getPowermeter());
        assertNotNull(device.getHkr());

        assertEquals(new BigDecimal("22.0"), device.getTemperature().getCelsius());
        assertEquals(new BigDecimal("-1.0"), device.getTemperature().getOffset());

        assertEquals(new BigDecimal(44), device.getHkr().getTist());
        assertEquals(new BigDecimal(42), device.getHkr().getTsoll());
        assertEquals(new BigDecimal(28), device.getHkr().getAbsenk());
        assertEquals(new BigDecimal(42), device.getHkr().getKomfort());
        assertEquals(BigDecimal.ONE, device.getHkr().getLock());
        assertEquals(BigDecimal.ONE, device.getHkr().getDevicelock());
        assertEquals(HeatingModel.BATTERY_OFF, device.getHkr().getBatterylow());
        assertEquals(MODE_AUTO, device.getHkr().getMode());
        assertEquals(MODE_COMFORT, device.getHkr().getRadiatorMode());

        assertNotNull(device.getHkr().getNextchange());

        assertEquals(1484341200, device.getHkr().getNextchange().getEndperiod());
        assertEquals(new BigDecimal(28), device.getHkr().getNextchange().getTchange());
    }

    @Test
    public void validatePowerline546EModel() {
        Optional<DeviceModel> optionalDevice = findDevice("FRITZ!Powerline 546E");
        assertTrue(optionalDevice.isPresent());

        DeviceModel device = optionalDevice.get();
        assertEquals("FRITZ!Powerline 546E", device.getProductName());
        assertEquals("5C:49:79:F0:A3:84", device.getIdentifier());
        assertEquals("30", device.getDeviceId());
        assertEquals("06.92", device.getFirmwareVersion());
        assertEquals("AVM", device.getManufacturer());

        assertEquals(1, device.getPresent());
        assertEquals("FRITZ!Powerline 546E #1", device.getName());

        assertFalse(device.isDectRepeater());
        assertTrue(device.isSwitchableOutlet());
        assertFalse(device.isTempSensor());
        assertTrue(device.isPowermeter());
        assertFalse(device.isHeatingThermostat());

        assertNotNull(device.getSwitch());
        assertNull(device.getTemperature());
        assertNotNull(device.getPowermeter());
        assertNull(device.getHkr());

        assertEquals(SwitchModel.OFF, device.getSwitch().getState());
        assertEquals(MODE_MANUAL, device.getSwitch().getMode());
        assertEquals(BigDecimal.ZERO, device.getSwitch().getLock());
        assertEquals(BigDecimal.ONE, device.getSwitch().getDevicelock());

        assertEquals(new BigDecimal("0.000"), device.getPowermeter().getPower());
        assertEquals(new BigDecimal("2.087"), device.getPowermeter().getEnergy());
    }

    private Optional<DeviceModel> findDevice(@NonNull String name) {
        return devices.getDevicelist().stream().filter(it -> name.equals(it.getProductName())).findFirst();
    }
}
