package org.openhab.binding.enera.internal.model;

import java.math.BigInteger;

public class RealtimeDataMessageDateTime {
    private BigInteger value;
    private String scale;
    private String kind;
    private String formatted;

    /**
     * @return the value
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * @return the scale
     */
    public String getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(String scale) {
        this.scale = scale;
    }

    /**
     * @return the kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * @return the formatted
     */
    public String getFormatted() {
        return formatted;
    }

    /**
     * @param formatted the formatted to set
     */
    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }
}
