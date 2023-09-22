package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.handler.ProtoConverter;
import org.openhab.binding.mercedesme.internal.utils.Utils;

import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdatesByVIN;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;

class ProtoTest {
    public static final String VIN_ANON = "anonymous";

    void vinAndPositionAnaon() {
        /**
         * Make VIN and GPS data from proto blob anonymous
         */
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto-blob/EQA-Precond-Active.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            Map<String, VEPUpdate> updates = pm.getVepUpdates().getUpdates();
            VEPUpdate vepUpdate = updates.get("REAL_VIN");
            VehicleAttributeStatus latStatus = VehicleAttributeStatus.newBuilder().setDoubleValue(1.23)
                    .setTimestamp(1692957336).setTimestampInMs(1692957336).build();
            VehicleAttributeStatus lonStatus = VehicleAttributeStatus.newBuilder().setDoubleValue(4.56)
                    .setTimestamp(1692957336).setTimestampInMs(1692957336).build();
            VEPUpdate vepUpdateAnon = VEPUpdate.newBuilder().mergeFrom(vepUpdate).setVin(VIN_ANON)
                    .putAttributes("positionLat", latStatus).putAttributes("positionLong", lonStatus).build();
            PushMessage pmAnon = PushMessage.newBuilder()
                    .setVepUpdates(VEPUpdatesByVIN.newBuilder().putUpdates(VIN_ANON, vepUpdateAnon).build()).build();
            System.out.println(pmAnon.getAllFields());

            try (FileOutputStream outputStream = new FileOutputStream("src/test/resources/proto-blob/anon.blob")) {
                pmAnon.writeTo(outputStream);
            }
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
                String protoJson = Utils.proto2Json(vepUpdate);
                String referenceJson = FileReader
                        .readFileInString("src/test/resources/proto-json/MB-BEV-EQA-Charging-Unformatted.json");
                assertEquals(referenceJson, protoJson, "Prto2Json compare");
                JSONObject protoJsonObject = new JSONObject(protoJson);
                assertEquals(vepUpdate.getAttributesCount(), protoJsonObject.length(), "Attributes Count");
                VEPUpdate roundTrip = ProtoConverter.json2Proto(protoJsonObject.toString(), true);
                assertEquals(156, roundTrip.getAttributesCount(), "Roundtrip Count");
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
            // System.out.println(pm.getAllFields());
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get(VIN_ANON).getAttributesMap();
            VehicleAttributeStatus value = m.get("endofchargetime");
            long minutesAfterMIdnight = value.getIntValue();
            long hours = minutesAfterMIdnight / 60;
            long minutes = minutesAfterMIdnight - hours * 60;
            assertEquals(value.getDisplayValue(), hours + ":" + minutes);
        } catch (Throwable e) {
            fail();
        }
    }
}
