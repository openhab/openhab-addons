package org.openhab.binding.myuplink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ChannelType {
    ENERGY("kWh", "type-energy", "Number:Energy"),
    PRESSURE("bar", "type-pressure", "Number:Pressure"),
    PERCENT("%", "type-percent", "Number:Dimensionless"),
    TEMPERATURE("°C", "type-temperature", "Number:Temperature"),
    FREQUENCY("Hz", "type-frequency-unscaled", "Number:Frequency"),
    DEFAULT("", "type-number-scale10", "Number");

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

    public static ChannelType fromJsonUnit(String jsonUnit) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getJsonUnit().equals(jsonUnit)) {
                return channelType;
            }
        }
        return DEFAULT;
    }

    public static ChannelType fromAcceptedType(String acceptedType) {
        for (var channelType : ChannelType.values()) {
            if (channelType.getAcceptedType().equals(acceptedType)) {
                return channelType;
            }
        }
        return DEFAULT;
    }
}
