/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.ProtoConverter;
import org.openhab.binding.mercedesme.internal.utils.Utils;

import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdatesByVIN;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;

/**
 * {@link ProtoTest} to check conversions made in the binding proto <-> json
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ProtoTest {
    public static final String VIN_ANON = "anonymous";

    void vinAndPositionAnaon() {
        /**
         * Make VIN and GPS data from proto blob anonymous
         */
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto-blob/EQA-Precond-Active.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            Map<String, VEPUpdate> updates = pm.getVepUpdates().getUpdatesMap();
            VEPUpdate vepUpdate = updates.get("REAL_VIN");
            VehicleAttributeStatus latStatus = VehicleAttributeStatus.newBuilder().setDoubleValue(1.23)
                    .setTimestampInMs(1692957336).build();
            VehicleAttributeStatus lonStatus = VehicleAttributeStatus.newBuilder().setDoubleValue(4.56)
                    .setTimestampInMs(1692957336).build();
            VEPUpdate vepUpdateAnon = VEPUpdate.newBuilder().mergeFrom(vepUpdate).setVin(VIN_ANON)
                    .putAttributes("positionLat", latStatus).putAttributes("positionLong", lonStatus).build();
            PushMessage pmAnon = PushMessage.newBuilder()
                    .setVepUpdates(VEPUpdatesByVIN.newBuilder().putUpdates(VIN_ANON, vepUpdateAnon).build()).build();
            try (FileOutputStream outputStream = new FileOutputStream("src/test/resources/proto-blob/anon.blob")) {
                pmAnon.writeTo(outputStream);
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testProtoDecoding() {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto-blob/proto-1.blob");
            byte[] data = fis.readAllBytes();
            boolean success = false;
            String previousMessage = "";
            for (int i = 0; i < data.length && !success; i++) {
                int newLength = data.length - i;
                byte[] dataOffset = new byte[newLength];
                System.arraycopy(data, i, dataOffset, 0, newLength);
                try {
                    PushMessage pm = VehicleEvents.PushMessage.parseFrom(dataOffset);
                    success = true;
                    assertTrue(VehicleEvents.PushMessage.MsgCase.VEPUPDATES.equals(pm.getMsgCase()));
                    break;
                } catch (IOException f) {
                    String message = f.getMessage();
                    assertNotNull(message);
                    if (!previousMessage.equals(message)) {
                        previousMessage = message;
                    }
                }
            }
            fis.close();
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testProtoBlob2Json() {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto-blob/MB-BEV-EQA-Charging.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            Map<String, VEPUpdate> updates = pm.getVepUpdates().getUpdatesMap();
            VEPUpdate vepUpdate = updates.get(VIN_ANON);
            if (vepUpdate != null) {
                String protoJson = Utils.proto2Json(vepUpdate, Constants.THING_TYPE_BEV);
                String referenceJson = FileReader
                        .readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging-Unformatted.json");
                assertEquals(referenceJson, protoJson, "Prto2Json compare");
                JSONObject protoJsonObject = new JSONObject(protoJson);
                // plus one due to added binding version
                assertEquals(vepUpdate.getAttributesCount() + 1, protoJsonObject.length(), "Attributes Count");
                // assure version is in
                assertTrue(protoJsonObject.has("bindingInfo"));
                VEPUpdate roundTrip = ProtoConverter.json2Proto(protoJsonObject.toString(), true);
                assertEquals(157, roundTrip.getAttributesCount(), "Roundtrip Count");
            } else {
                fail();
            }
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testEndChargeTime() {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto-blob/MB-BEV-EQA-Charging.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            VEPUpdate vepUpdate = updates.getUpdatesMap().get(VIN_ANON);
            assertNotNull(vepUpdate);
            Map<String, VehicleAttributeStatus> m = vepUpdate.getAttributesMap();
            VehicleAttributeStatus value = m.get("endofchargetime");
            assertNotNull(value);
            long minutesAfterMIdnight = value.getIntValue();
            long hours = minutesAfterMIdnight / 60;
            long minutes = minutesAfterMIdnight - hours * 60;
            assertEquals(value.getDisplayValue(), hours + ":" + minutes);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void testChargeProgramValues() {
        assertEquals(0, Utils.getChargeProgramNumber("DEFAULT_CHARGE_PROGRAM"), "Default Charge Program");
        assertEquals(2, Utils.getChargeProgramNumber("HOME_CHARGE_PROGRAM"), "Home Charge Program");
        assertEquals(3, Utils.getChargeProgramNumber("WORK_CHARGE_PROGRAM"), "Work Charge Program");
        assertEquals(-1, Utils.getChargeProgramNumber("whatever"), "Fail Value");
    }

    @Test
    void testTemperaturePointsValues() {
        assertEquals(3, Utils.getZoneNumber("frontCenter"), "Front Center Zone");
        assertEquals(1, Utils.getZoneNumber("frontLeft"), "Front Left Zone");
        assertEquals(2, Utils.getZoneNumber("frontRight"), "Front Right Zone");
        assertEquals(-1, Utils.getZoneNumber("whatever"), "Fail Value");
    }
}
