package org.openhab.binding.myuplink.internal.model;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ChannelType {
    ENERGY("kWh", "type-energy", "Number:Energy"),
    PRESSURE("bar", "type-pressure", "Number:Pressure"),
    PERCENT("%", "type-percent", "Number:Dimensionless"),
    TEMPERATURE("°C", "type-temperature", "Number:Temperature"),
    FREQUENCY("Hz", "type-frequency", "Number:Frequency"),
    FLOW("l/m", "type-flow", "Number:Dimensionless"),
    ELECTRIC_CURRENT("A", "type-electric-current", "Number:ElectricCurrent"),
    TIME("h", "type-time", "Number:Time"),
    INTEGER("NUMBER", "type-number-integer", "Number"),
    DOUBLE("NUMBER", "type-number-double", "Number");

    private final String jsonUnit;
    private final String typeName;
    private final String acceptedType;

    ChannelType(String jsonUnit, String typeName, String acceptedType) {
        this.jsonUnit = jsonUnit;
        this.typeName = typeName;
        this.acceptedType = acceptedType;
    }

    /**
     * @return the jsonUnit
     */
    public String getJsonUnit() {
        return jsonUnit;
    }

    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return the acceptedType
     */
    public String getAcceptedType() {
        return acceptedType;
    }

    public static ChannelType fromJsonData(String jsonUnit, String jsonStrVal) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getJsonUnit().equals(jsonUnit)) {
                return channelType;
            }
        }
        if (jsonStrVal.contains(JSON_VAL_DECIMAL_SEPARATOR)) {
            return DOUBLE;
        } else {
            return INTEGER;
        }
    }

    public static ChannelType fromTypeName(String typeName) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getTypeName().equals(typeName)) {
                return channelType;
            }
        }
        return DOUBLE;
    }
}
