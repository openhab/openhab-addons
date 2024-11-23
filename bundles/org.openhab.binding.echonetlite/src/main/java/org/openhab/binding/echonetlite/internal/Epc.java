/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.ON_OFF_CODEC_30_31;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.ON_OFF_CODEC_41_42;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.echonetlite.internal.StateCodec.Option;
import org.openhab.binding.echonetlite.internal.StateCodec.OptionCodec;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public interface Epc {
    int code();

    String name();

    @Nullable
    default String type() {
        return null;
    }

    default String channelId() {
        return LangUtil.constantToVariable(name());
    }

    @Nullable
    default StateDecode decoder() {
        return null;
    }

    @Nullable
    default StateEncode encoder() {
        return null;
    }

    static Epc lookup(int groupCode, int classCode, int epcCode) {
        return EpcLookupTable.INSTANCE.resolve(groupCode, classCode, epcCode);
    }

    // ECHONET SPECIFICATION
    // APPENDIX Detailed Requirements for ECHONET Device objects
    // Table 2-1
    enum Device implements Epc {
        // @formatter:off
        OPERATION_STATUS(0x80, ON_OFF_CODEC_30_31),

        INSTALLATION_LOCATION(0x81, new OptionCodec(
                new Option("Not specified", 0b00000_000),
                
                new Option("Living Room", 0b00001_000),
                new Option("Living Room 1", 0b00001_001),
                new Option("Living Room 2", 0b00001_010),
                new Option("Living Room 3", 0b00001_011),
                new Option("Living Room 4", 0b00001_100),
                new Option("Living Room 5", 0b00001_101),
                new Option("Living Room 6", 0b00001_110),
                new Option("Living Room 7", 0b00001_111),
                
                new Option("Dining Room", 0b00010_000),
                new Option("Dining Room 1", 0b00010_001),
                new Option("Dining Room 2", 0b00010_010),
                new Option("Dining Room 3", 0b00010_011),
                new Option("Dining Room 4", 0b00010_100),
                new Option("Dining Room 5", 0b00010_101),
                new Option("Dining Room 6", 0b00010_110),
                new Option("Dining Room 7", 0b00010_111),
               
                new Option("Kitchen", 0b00011_000),
                new Option("Kitchen 1", 0b00011_001),
                new Option("Kitchen 2", 0b00011_010),
                new Option("Kitchen 3", 0b00011_011),
                new Option("Kitchen 4", 0b00011_100),
                new Option("Kitchen 5", 0b00011_101),
                new Option("Kitchen 6", 0b00011_110),
                new Option("Kitchen 7", 0b00011_111),
                
                new Option("Lavatory", 0b00100_000),
                new Option("Lavatory 1", 0b00100_001),
                new Option("Lavatory 2", 0b00100_010),
                new Option("Lavatory 3", 0b00100_011),
                new Option("Lavatory 4", 0b00100_100),
                new Option("Lavatory 5", 0b00100_101),
                new Option("Lavatory 6", 0b00100_110),
                new Option("Lavatory 7", 0b00100_111),
                
                new Option("Washroom/changing room", 0b00101_000),
                new Option("Washroom/changing room 1", 0b00101_001),
                new Option("Washroom/changing room 2", 0b00101_010),
                new Option("Washroom/changing room 3", 0b00101_011),
                new Option("Washroom/changing room 4", 0b00101_100),
                new Option("Washroom/changing room 5", 0b00101_101),
                new Option("Washroom/changing room 6", 0b00101_110),
                new Option("Washroom/changing room 7", 0b00101_111),
                
                new Option("Passageway", 0b00111_000),
                new Option("Passageway  1", 0b00111_001),
                new Option("Passageway  2", 0b00111_010),
                new Option("Passageway  3", 0b00111_011),
                new Option("Passageway  4", 0b00111_100),
                new Option("Passageway  5", 0b00111_101),
                new Option("Passageway  6", 0b00111_110),
                new Option("Passageway  7", 0b00111_111),
                
                new Option("Room", 0b01000_000),
                new Option("Room 1", 0b01000_001),
                new Option("Room 2", 0b01000_010),
                new Option("Room 3", 0b01000_011),
                new Option("Room 4", 0b01000_100),
                new Option("Room 5", 0b01000_101),
                new Option("Room 6", 0b01000_110),
                new Option("Room 7", 0b01000_111),
                
                new Option("Stairway", 0b01001_000),
                new Option("Stairway 1", 0b01001_001),
                new Option("Stairway 2", 0b01001_010),
                new Option("Stairway 3", 0b01001_011),
                new Option("Stairway 4", 0b01001_100),
                new Option("Stairway 5", 0b01001_101),
                new Option("Stairway 6", 0b01001_110),
                new Option("Stairway 7", 0b01001_111),
                
                new Option("Front door", 0b01010_000),
                new Option("Front door 1", 0b01010_001),
                new Option("Front door 2", 0b01010_010),
                new Option("Front door 3", 0b01010_011),
                new Option("Front door 4", 0b01010_100),
                new Option("Front door 5", 0b01010_101),
                new Option("Front door 6", 0b01010_110),
                new Option("Front door 7", 0b01010_111),
                
                new Option("Storeroom", 0b01011_000),
                new Option("Storeroom 1", 0b01011_001),
                new Option("Storeroom 2", 0b01011_010),
                new Option("Storeroom 3", 0b01011_011),
                new Option("Storeroom 4", 0b01011_100),
                new Option("Storeroom 5", 0b01011_101),
                new Option("Storeroom 6", 0b01011_110),
                new Option("Storeroom 7", 0b01011_111),
                
                new Option("Garden/perimeter", 0b01100_000),
                new Option("Garden/perimeter 1", 0b01100_001),
                new Option("Garden/perimeter 2", 0b01100_010),
                new Option("Garden/perimeter 3", 0b01100_011),
                new Option("Garden/perimeter 4", 0b01100_100),
                new Option("Garden/perimeter 5", 0b01100_101),
                new Option("Garden/perimeter 6", 0b01100_110),
                new Option("Garden/perimeter 7", 0b01100_111),
                
                new Option("Garage", 0b01101_000),
                new Option("Garage 1", 0b01101_001),
                new Option("Garage 2", 0b01101_010),
                new Option("Garage 3", 0b01101_011),
                new Option("Garage 4", 0b01101_100),
                new Option("Garage 5", 0b01101_101),
                new Option("Garage 6", 0b01101_110),
                new Option("Garage 7", 0b01101_111),
                
                new Option("Veranda/balcony", 0b01110_000),
                new Option("Veranda/balcony 1", 0b01110_001),
                new Option("Veranda/balcony 2", 0b01110_010),
                new Option("Veranda/balcony 3", 0b01110_011),
                new Option("Veranda/balcony 4", 0b01110_100),
                new Option("Veranda/balcony 5", 0b01110_101),
                new Option("Veranda/balcony 6", 0b01110_110),
                new Option("Veranda/balcony 7", 0b01110_111),
                
                new Option("Others", 0b01111_000),
                new Option("Others 1", 0b01111_001),
                new Option("Others 2", 0b01111_010),
                new Option("Others 3", 0b01111_011),
                new Option("Others 4", 0b01111_100),
                new Option("Others 5", 0b01111_101),
                new Option("Others 6", 0b01111_110),
                new Option("Others 7", 0b01111_111))),
        
        STANDARD_VERSION_INFORMATION(0x82, StateCodec.StandardVersionInformationCodec.INSTANCE, null),
        IDENTIFICATION_NUMBER(0x83, StateCodec.HexStringCodec.INSTANCE, null),
        MEASURED_INSTANTANEOUS_POWER_CONSUMPTION(0x84),
        MEASURED_CUMULATIVE_POWER_CONSUMPTION(0x85),
        MANUFACTURER_FAULT_CODE(0x86, StateCodec.HexStringCodec.INSTANCE, null),
        CURRENT_LIMIT_SETTING(0x87),
        FAULT_STATUS(0x88, ON_OFF_CODEC_41_42, null),
        FAULT_DESCRIPTION(0x89, StateCodec.HexStringCodec.INSTANCE, null),
        MANUFACTURER_CODE(0x8A, StateCodec.HexStringCodec.INSTANCE, null),
        BUSINESS_FACILITY_CODE(0x8B, StateCodec.HexStringCodec.INSTANCE, null),
        PRODUCT_CODE(0x8C),
        PRODUCTION_NUMBER(0x8D),
        PRODUCTION_DATE(0x8E),
        POWER_SAVING_OPERATION_SETTING(0x8F, ON_OFF_CODEC_41_42),
        REMOTE_CONTROL_SETTING(0x93),
        CURRENT_TIME_SETTING(0x97),
        CURRENT_DATE_SETTING(0x98),
        POWER_LIMIT_SETTING(0x99),
        CUMULATIVE_OPERATING_TIME(0x9A, StateCodec.OperatingTimeDecode.INSTANCE, null),
        SETM_PROPERTY_MAP(0x9B),
        GETM_PROPERTY_MAP(0x9C),
        STATUS_CHANGE_ANNOUNCEMENT_PROPERTY_MAP(0x9D),
        SET_PROPERTY_MAP(0x9E),
        GET_PROPERTY_MAP(0x9F);
        // @formatter:on

        public final int code;
        @Nullable
        public final StateDecode stateDecode;
        @Nullable
        public final StateEncode stateEncode;

        Device(int code) {
            this(code, null, null);
        }

        Device(int code, @Nullable StateDecode stateDecode, @Nullable StateEncode stateEncode) {
            this.code = code;
            this.stateDecode = stateDecode;
            this.stateEncode = stateEncode;
        }

        Device(int code, StateCodec stateCodec) {
            this(code, stateCodec, stateCodec);
        }

        @Override
        public int code() {
            return code;
        }

        @Nullable
        @Override
        public StateDecode decoder() {
            return stateDecode;
        }

        @Nullable
        @Override
        public StateEncode encoder() {
            return stateEncode;
        }
    }

    enum AcGroup implements Epc {
        // @formatter:off
        AIR_FLOW_RATE(0xA0, new OptionCodec(
                new Option("Auto", 0x41), 
                new Option("Rate 1", 0x31), 
                new Option("Rate 2", 0x32),
                new Option("Rate 3", 0x33),
                new Option("Rate 4", 0x34),
                new Option("Rate 5", 0x35),
                new Option("Rate 6", 0x36),
                new Option("Rate 7", 0x37),
                new Option("Rate 8", 0x38))),
        
        AUTOMATIC_CONTROL_OF_AIR_FLOW_DIRECTION(0xA1, new OptionCodec(
                new Option("Automatic", 0x41),
                new Option("Non-automatic", 0x42),
                new Option("Automatic (vertical)", 0x43),
                new Option("Automatic (horizontal)", 0x44))),
        
        AUTOMATIC_SWING_OF_AIR_FLOW(0xA3, new OptionCodec(
                new Option("Not used", 0x31),
                new Option("Used (vertical)", 0x41),
                new Option("Used (horizontal)", 0x42),
                new Option("Used (vertical and horizontal)", 0x43))),

        AIR_FLOW_DIRECTION_VERTICAL(0xA4, new OptionCodec(
                new Option("Uppermost", 0x41),
                new Option("Lowermost", 0x42),
                new Option("Mid-uppermost", 0x43),
                new Option("Mid-lowermost", 0x44),
                new Option("Central", 0x45))),

        AIR_FLOW_DIRECTION_HORIZONTAL(0xA5, new OptionCodec(
                new Option("XXXOO", 0x41),
                new Option("OOXXX", 0x42),
                new Option("XOOOX", 0x43),
                new Option("OOXOO", 0x44),
                new Option("XXXXO", 0x51),
                new Option("XXXOX", 0x52),
                new Option("XXOXX", 0x54),
                new Option("XXOXO", 0x55),
                new Option("XXOOX", 0x56),
                new Option("XXOOO", 0x57),
                new Option("XOXXX", 0x58),
                new Option("XOXXO", 0x59),
                new Option("XOXOX", 0x5A),
                new Option("XOXOO", 0x5B),
                new Option("XOOXX", 0x5C),
                new Option("XOOXO", 0x5D),
                new Option("XOOOO", 0x5F),
                new Option("OXXXX", 0x60),
                new Option("OXXXO", 0x61),
                new Option("OXXOX", 0x62),
                new Option("OXXOO", 0x63),
                new Option("OXOXX", 0x64),
                new Option("OXOXO", 0x65),
                new Option("OXOOX", 0x66),
                new Option("OXOOO", 0x67),
                new Option("OOXXO", 0x69),
                new Option("OOXOX", 0x6A),
                new Option("OOOXX", 0x6C),
                new Option("OOOXO", 0x6D),
                new Option("OOOOX", 0x6E),
                new Option("OOOOO", 0x6F))),

        SPECIAL_STATE(0xAA),
        NON_PRIORITY_STATE(0xAB),
        OPERATION_MODE(0xB0, new OptionCodec(
                new Option("Automatic", 0x41),
                new Option("Cooling", 0x42),
                new Option("Heating", 0x43),
                new Option("Dry", 0x44),
                new Option("Fan", 0x45),
                new Option("Other", 0x40))),

        AUTOMATIC_TEMPERATURE_CONTROL(0xB1),
        NORMAL_HIGH_SPEED_SILENT_OPERATION(0xB2),
        SET_TEMPERATURE(0xB3, StateCodec.Temperature8bitCodec.INSTANCE),
        SET_RELATIVE_HUMIDITY(0xB4),
        SET_TEMPERATURE_COOLING_MODE(0xB5),
        SET_TEMPERATURE_HEATING_MODE(0xB6),
        SET_TEMPERATURE_DEHUMIDIFYING_MODE(0xB7),
        RATED_POWER_CONSUMPTION(0xB8),
        MEASURED_CURRENT_CONSUMPTION(0xB9),
        MEASURED_ROOM_RELATIVE_HUMIDITY(0xBA),
        MEASURED_ROOM_TEMPERATURE(0xBB, StateCodec.Temperature8bitCodec.INSTANCE, null),
        SET_TEMPERATURE_USER_REMOTE_CONTROL(0xBC),
        MEASURED_COOLED_AIR_TEMPERATURE(0xBD),
        MEASURED_OUTDOOR_TEMPERATURE(0xBE, StateCodec.Temperature8bitCodec.INSTANCE, null),
        RELATIVE_TEMPERATURE(0xBF);
        // @formatter:on

        public final int code;

        @Nullable
        public final StateDecode stateDecode;

        @Nullable
        public final StateEncode stateEncode;

        AcGroup(int code) {
            this(code, null, null);
        }

        AcGroup(int code, @Nullable StateDecode stateDecode, @Nullable StateEncode stateEncode) {
            this.code = code;
            this.stateDecode = stateDecode;
            this.stateEncode = stateEncode;
        }

        AcGroup(int code, StateCodec stateCodec) {
            this(code, stateCodec, stateCodec);
        }

        @Override
        public int code() {
            return code;
        }

        @Nullable
        @Override
        public StateDecode decoder() {
            return stateDecode;
        }

        @Nullable
        @Override
        public StateEncode encoder() {
            return stateEncode;
        }
    }

    enum HomeAc implements Epc {
        VENTILATION_FUNCTION(0xC0),
        HUMIDIFIER_FUNCTION(0xC1),
        VENTILATION_AIR_FLOW_RATE(0xC3);

        public final int code;

        HomeAc(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    enum Profile implements Epc {
        OPERATING_STATUS(0x80, new OptionCodec(new Option("Booting", 0x30), new Option("Not booting", 0x31))),
        VERSION_INFORMATION(0x82),
        NODE_IDENTIFICATION_NUMBER(0x83),
        FAULT_CONTENT(0x89);

        public final int code;

        @Nullable
        public final StateDecode stateDecode;
        @Nullable
        public final StateEncode stateEncode;

        Profile(int code) {
            this(code, null, null);
        }

        Profile(int code, @Nullable StateDecode stateDecode, @Nullable StateEncode stateEncode) {
            this.code = code;
            this.stateDecode = stateDecode;
            this.stateEncode = stateEncode;
        }

        Profile(int code, StateCodec stateCodec) {
            this(code, stateCodec, stateCodec);
        }

        @Override
        public int code() {
            return code;
        }

        @Nullable
        @Override
        public StateDecode decoder() {
            return stateDecode;
        }

        @Nullable
        @Override
        public StateEncode encoder() {
            return stateEncode;
        }
    }

    enum ProfileGroup implements Epc {
        UNIQUE_IDENTIFIER_CODE(0xBF);

        public final int code;

        ProfileGroup(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    enum NodeProfile implements Epc {
        EA(0xE0),
        NET_ID(0xE1),
        NODE_D(0xE2),
        DEFAULT_ROUTER_DATA(0xE3),
        ALL_ROUTER_DATA(0xE4),
        LOCK_CONTROL_STATUS(0xEE),
        LOCK_CONTROL_DATA(0xEF),
        SECURE_COMMUNICATION_COMMON_KEY_SETUP_USER_KEY(0xC0),
        SECURE_COMMUNICATION_COMMON_KEY_SETUP_SERVICE_PROVIDER_KEY(0xC1),
        SECURE_COMMUNICATION_COMMON_KEY_SWITCHOVER_SETUP_USER_KEY(0xC2),
        SECURE_COMMUNICATION_COMMON_KEY_SWITCHOVER_SETUP_SERVICE_PROVIDER_KEY(0xC3),
        SECURE_COMMUNICATION_COMMON_KEY_SERIAL_KEY(0xC4),
        SELF_NODE_INSTANCE_LIST_PAGE(0xD0),
        SELF_NODE_CLASS_LIST(0xD2),
        SELF_NODE_INSTANCE_COUNT(0xD3),
        SELF_NODE_CLASS_COUNT(0xD4),
        INSTANCE_CHANGE_CLASS_COUNT(0xD5),
        SELF_NODE_INSTANCE_LIST_S(0xD6),
        SELF_NODE_CLASS_LIST_S(0xD7),
        RELATED_TO_OTHER_NODE_EA_LIST(0xD8),
        RELATED_TO_OTHER_NODE_EA_COUNT(0xD9),
        GROUP_BROADCAST_NUMBER(0xDA),;

        public final int code;

        NodeProfile(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }
}
