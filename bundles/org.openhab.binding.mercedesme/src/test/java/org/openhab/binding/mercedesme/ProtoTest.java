package org.openhab.binding.mercedesme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.utils.UOMObserver;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleCommands.BatteryMaxSocConfigure;
import com.daimler.mbcarkit.proto.VehicleCommands.ChargeProgramConfigure.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleCommands.CommandRequest;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart.LightType;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart.SigposType;
import com.daimler.mbcarkit.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint.Zone;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdatesByVIN;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MapEntry;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;

class ProtoTest {

    @Test
    public void testPattern() {
        UOMObserver obersver = new UOMObserver(UOMObserver.LENGTH_KM_UNIT);
        System.out.println(obersver.getPattern(Constants.GROUP_RANGE));
        System.out.println(obersver.getPattern(Constants.GROUP_TRIP));
        if (obersver.getPattern(Constants.GROUP_RANGE).startsWith("%")) {
            System.out.println("True");
        } else {
            System.out.println("False");
        }
        System.out.println("50 %");
    }

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
    void testPrecondState() {
        try {
            // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
            FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-Precond-Active.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433").getAttributesMap();
            // System.out.println(m);
            VehicleAttributeStatus value = m.get("precondNow");
            System.out.println(value);
            value = m.get("precondState");
            System.out.println(value);

            UnknownFieldSet ufs = value.getUnknownFields();
            Field uf = ufs.getField(44);
            // System.out.println(uf.);
            Map<Integer, Field> unknownMap = ufs.asMap();
            Field f = unknownMap.get(44);
            System.out.println(unknownMap);
            System.out.println(f);
            List<ByteString> bs = f.getLengthDelimitedList();
            System.out.println(bs.get(0));
            System.out.println(bs.get(0).isValidUtf8());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void testJsonOutput() {
        // String test = Utils.proto2Json(null);
        // System.out.println(test);
        // System.out.println(test.toString());
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-charging.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            Map<String, VEPUpdate> m = pm.getVepUpdates().getUpdatesMap();
            m.forEach((key, value) -> {
                JSONObject jo1 = new JSONObject(Utils.proto2Json(value));
                System.out.println(jo1.length());
                JSONObject jo2 = new JSONObject(
                        FileReader.readFileInString("src/test/resources/EQA-Proto-Ladefehler.json"));
                System.out.println(jo2.length());
                Map combined = Utils.combineMaps(jo1.toMap(), jo2.toMap());
                System.out.println(combined.size());
                JSONObject jo3 = new JSONObject(combined);
                System.out.println(jo2);
                System.out.println(jo3);
                System.out.println(jo3.equals(jo2));
            });
            // Utils.proto2Json(pm.getVepUpdates().getUpdatesMap());
            // JSONObject jo;
            // jo.
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void testJson() {
        try {
            // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
            FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-charging2.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            System.out.println(pm.getVepUpdates().getAllFields().size());
            System.out.println(pm.getVepUpdates().getAllFields().keySet());
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            JSONObject jo = new JSONObject();
            Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("VIN12345678987654").getAttributesMap();
            m.forEach((key, value) -> {
                // System.out.println("Key: " + key);
                // System.out.println("Val: " + value);
                Map attMap = value.getAllFields();
                JSONObject joa = new JSONObject();
                attMap.forEach((aKey, aValue) -> {
                    // System.out.println(aValue.getClass());
                    if (aValue instanceof ChargeProgramsValue) {
                        System.out.println("Here! " + aValue);
                        ChargeProgramsValue cpv = (ChargeProgramsValue) aValue;
                        // JSONObject jocpc = new JSONObject(cpv.getAllFields());
                        System.out.println(cpv.getChargeProgramParametersCount());
                        // cpv.getChargeProgramParametersCount()
                    }
                    String[] bKey = aKey.toString().split("\\.");
                    if (bKey.length > 1) {
                        joa.put(bKey[bKey.length - 1], aValue);
                    } else {
                        joa.put(bKey[0], aValue);
                    }
                });
                jo.put(key, joa);
            });
            System.out.println(jo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void testMessages() {
        for (int i = 11; i < 12; i++) {
            try {
                // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
                FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-charging.blob");
                PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
                System.out.println(pm.getAllFields().toString());
                if (pm.hasVepUpdates()) {
                    VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
                    Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433")
                            .getAttributesMap();
                    m.forEach((key, value) -> {
                        if (value.hasPressureUnit()) {
                            System.out.println("Pressure Unit " + value.getPressureUnit());
                            System.out.println("Pressure Val " + value.getPressureUnitValue());
                        }
                        // System.out.println(key + " => " + Mapper.getChannelStateMap(key, value));
                        if (key.contains("hargeProgram")) {
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
    void testChargeProgram() {
        for (int i = 11; i < 12; i++) {
            try {
                // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
                FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-charging.blob");
                PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
                if (pm.hasVepUpdates()) {
                    VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
                    Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433")
                            .getAttributesMap();
                    ChargeProgramsValue cpv = m.get("chargePrograms").getChargeProgramsValue();
                    if (cpv.getChargeProgramParametersCount() > 0) {
                        System.out.println(cpv.getAllFields());
                        System.out.println(cpv.getChargeProgramParametersCount());
                        List<ChargeProgramParameters> chareProgeamParameters = cpv.getChargeProgramParametersList();
                        System.out.println(chareProgeamParameters.size());
                        chareProgeamParameters.forEach(program -> {
                            String programName = program.getChargeProgram().name();
                            int number = program.getChargeProgram().getNumber();
                            System.out.println(programName + " " + number);
                        });
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testEndChargeTime() {
        try {
            // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
            FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-Charging-1740-1915.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            // System.out.println(pm.getAllFields());
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433").getAttributesMap();
            VehicleAttributeStatus value = m.get("endofchargetime");
            System.out.println(value);
            System.out.println(value.getIntValue());
            System.out.println(value.getDisplayValue());
            Instant time = Instant.ofEpochMilli(value.getTimestampInMs());
            System.out.println(time);
            System.out.println(time.plusSeconds(value.getIntValue()));
            DateTimeType state = DateTimeType.valueOf(time.toString());
            System.out.println(state);
            LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
            state = DateTimeType.valueOf(ldt.toString());
            System.out.println(state);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    void testChargePrograms() {
        try {
            // FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
            FileInputStream fis = new FileInputStream("src/test/resources/proto/EQA-charging.blob");
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
            // System.out.println(pm.getAllFields());
            VehicleEvents.VEPUpdatesByVIN updates = pm.getVepUpdates();
            Map<String, VehicleAttributeStatus> m = updates.getUpdatesMap().get("W1N2437011J016433").getAttributesMap();
            VehicleAttributeStatus value = m.get("chargePrograms");
            System.out.println(value);
            ChargeProgramsValue cps = value.getChargeProgramsValue();
            List<ChargeProgramParameters> cppList = cps.getChargeProgramParametersList();
            System.out.println("All Programs");
            for (Iterator iterator = cppList.iterator(); iterator.hasNext();) {
                ChargeProgramParameters chargeProgramParameters = (ChargeProgramParameters) iterator.next();
                System.out.println(chargeProgramParameters.getChargeProgram().name());
            }
            System.out.println("Selected Program");
            ChargeProgramParameters chargeProgramParameters = cppList
                    .get((int) m.get("selectedChargeProgram").getIntValue());
            System.out.println(chargeProgramParameters.getChargeProgram());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testZoneValues() {
        Zone[] zone = Zone.values();
        System.out.println(zone.length);
        for (int i = 0; i < zone.length - 1; i++) {
            System.out.println(zone[i] + " " + zone[i].getNumber());
        }
        ChargeProgram[] programs = ChargeProgram.values();
        for (int i = 0; i < programs.length - 1; i++) {
            System.out.println(programs[i] + " " + programs[i].getNumber());
        }
    }

    @Test
    void testCommandRequest() {
        BatteryMaxSocConfigure batteryMax = BatteryMaxSocConfigure.newBuilder().setMaxSoc(60).build();
        CommandRequest cr = CommandRequest.newBuilder().setVin("abc").setRequestId(UUID.randomUUID().toString())
                .setBatteryMaxSoc(batteryMax).build();
        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
        System.out.println(cm.getAllFields());

        FieldDescriptor fd = SigPosStart.getDescriptor().getFields().get(4);
        SigPosStart sps = SigPosStart.newBuilder().setSigposType(SigposType.LIGHT_ONLY)
                .setLightType(LightType.DIPPED_HEAD_LIGHT).setSigposDuration(10).build();
        // sps = SigPosStart.getDefaultInstance();
        System.out.println(sps.getSigposTypeValue());
        System.out.println(sps.getAllFields());
        cr = CommandRequest.newBuilder().setVin("abc").setRequestId(UUID.randomUUID().toString()).setSigposStart(sps)
                .build();
        cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
        // System.out.println(cm.getAllFields());
        // System.out.println(cr.getAllFields());
        // ClientMessage.newBuilder().setCommandRequest(cr).build();
        // ZEVPreconditioningStart precond = ZEVPreconditioningStart.newBuilder().build();
        // ClientMessage cmm = ClientMessage.newBuilder().setCommandRequest(sps);
        System.out.println(cm.getAllFields());
    }

    @Test
    void testAssociatedVin() {
        for (int i = 1; i < 12; i++) {
            try {
                FileInputStream fis = new FileInputStream("src/test/resources/proto/message-" + i + ".blob");
                PushMessage pm = VehicleEvents.PushMessage.parseFrom(fis);
                System.out.println(pm.getAllFields().keySet());
                if (pm.hasApptwinPendingCommandRequest()) {
                    System.out.println(pm.getApptwinPendingCommandRequest().getAllFields());
                }
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
