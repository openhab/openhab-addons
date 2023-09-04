package org.openhab.binding.mercedesme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.proto.Client.ClientMessage;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.CommandRequest;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.DoorsUnlock;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.ZEVPreconditioningStart;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.PushMessage;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VEPUpdate;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VEPUpdatesByVIN;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VehicleAttributeStatus;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MapEntry;

class ProtoTest {

    @Test
    void test() {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto/message-11.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            // System.out.println(pm.toString());
            // System.out.println(pm.hasField("soc"));
            // pm.getField(FieldDescriptor.);
            // System.out.println(pm.getField(Protos.AssignedVehicles.));
            Map m = pm.getAllFields();
            // FieldDescriptor fd = new FieldDescriptor()
            Set keys = m.keySet();
            for (java.util.Iterator iterator = keys.iterator(); iterator.hasNext();) {
                Object object = iterator.next();
                System.out.println(object);
                System.out.println(m.get(object).getClass());
                // VEPUpdatesByVIN update = (VEPUpdatesByVIN) m.get(object);
                VEPUpdatesByVIN update = pm.getVepUpdates();
                // update.System.out.println(update.getAllFields().size());
                // System.out.println(update);
                Set keys2 = update.getAllFields().keySet();
                for (java.util.Iterator iterator2 = keys2.iterator(); iterator2.hasNext();) {
                    Object key2 = iterator2.next();
                    System.out.println(key2);
                    Object values2 = update.getField((FieldDescriptor) key2);
                    System.out.println(values2.getClass());
                    List list = (List) values2;
                    System.out.println(list.get(0).getClass());
                    MapEntry protoMapEntry = (MapEntry) list.get(0);
                    System.out.println(protoMapEntry.getAllFields().size());
                    System.out.println(protoMapEntry.getKey());
                    System.out.println(protoMapEntry.getValue().getClass());
                    VEPUpdate vepUpdate = (VEPUpdate) protoMapEntry.getValue();
                    System.out.println(vepUpdate.getAttributes().size());
                    Map attributes = vepUpdate.getAttributesMap();
                    for (Iterator iterator3 = attributes.keySet().iterator(); iterator3.hasNext();) {
                        Object object2 = iterator3.next();
                        // System.out.println(object2);
                    }
                    System.out.println(attributes.get("soc"));
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void testMessages() {
        for (int i = 11; i < 12; i++) {
            try {
                FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
                PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
                System.out.println(pm.getAllFields());
                if (pm.hasVepUpdates()) {
                    VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
                    Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433")
                            .getAttributesMap();
                    m.forEach((key, value) -> {
                        // System.out.println(key + " => " + Mapper.getChannelStateMap(key, value));
                        if (key.contains("evRangeAssistDriveOnSOC")) {
                            // System.out.println(Mapper.getChannelStateMap(key, value));
                            System.out.println(key + ":" + value);
                            // System.out.println(
                            // "a:" + value.getTemperaturePointsValue().getTemperaturePoints(0).getTemperature());
                        }
                    });
                }
                System.out.println(i + " Size " + pm.getAllFields().size());
                System.out.println(pm.hasVepUpdate() + " : " + pm.hasVepUpdates());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testCommandRequest() {
        ClientMessage cm = ClientMessage.getDefaultInstance();
        System.out.println(cm.getAllFields());
        CommandRequest cr = CommandRequest.newBuilder().setVin("abc").setRequestId("xyz").build();
        System.out.println(cr.getAllFields());
        ClientMessage.newBuilder().setCommandRequest(cr).build();
        ZEVPreconditioningStart precond = ZEVPreconditioningStart.newBuilder().build();
        DoorsUnlock du = DoorsUnlock.newBuilder().getDefaultInstanceForType();
        ClientMessage cmm = ClientMessage.newBuilder().setCommandRequest(cr).build();
        // cm.writeTo(null);
    }

    @Test
    void testAssociatedVin() {
        for (int i = 1; i < 12; i++) {
            try {
                FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
                PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
                System.out.println(pm.hasAssignedVehicles());
                if (pm.hasAssignedVehicles()) {
                    System.out.println(pm.getAssignedVehicles().getVinsCount());
                    System.out.println(pm.getAssignedVehicles().getVins(0));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testTirePressure() {
        System.out.println(Double.valueOf("3.0"));
        System.out.println(QuantityType.valueOf(Double.valueOf("3.0"), Units.BAR));
    }
}
