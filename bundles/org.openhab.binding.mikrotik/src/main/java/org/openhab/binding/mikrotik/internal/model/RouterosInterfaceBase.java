package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosInstance.PROP_ID_KEY;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import org.joda.time.DateTime;
import org.openhab.binding.mikrotik.internal.util.Converter;

public abstract class RouterosInterfaceBase {
    protected Map<String, String> propMap;
    protected RouterosInterfaceType type;

    public RouterosInterfaceBase(Map<String, String> props) {
        this.propMap = props;
        this.type = RouterosInterfaceType.resolve(getType());
    }

    protected abstract RouterosInterfaceType[] getDesignedTypes();

    public void mergeProps(Map<String, String> otherProps) {
        this.propMap.putAll(otherProps);
    }

    public boolean validate() {
        return Arrays.stream(getDesignedTypes()).anyMatch(ifaceType -> ifaceType == this.type);
    }

    public String getId() {
        return propMap.get(PROP_ID_KEY);
    }

    public String getType() {
        return propMap.get("type");
    }

    public String getName() {
        return propMap.get("name");
    }

    public String getComment() {
        return propMap.get("comment");
    }

    public String getMacAddress() {
        return propMap.get("mac-address");
    }

    public boolean isEnabled() {
        return propMap.get("disabled").equals("false");
    }

    public boolean isConnected() {
        return propMap.get("running").equals("true");
    }

    public int getLinkDowns() {
        return Integer.parseInt(propMap.get("link-downs"));
    }

    public DateTime getLastLinkDownTime() {
        return Converter.fromRouterosTime(propMap.get("last-link-down-time"));
    }

    public DateTime getLastLinkUpTime() {
        return Converter.fromRouterosTime(propMap.get("last-link-up-time"));
    }

    public BigInteger getTxBytes() {
        return new BigInteger(propMap.get("tx-byte"));
    }

    public BigInteger getRxBytes() {
        return new BigInteger(propMap.get("rx-byte"));
    }

    public BigInteger getTxPackets() {
        return new BigInteger(propMap.get("tx-packet"));
    }

    public BigInteger getRxPackets() {
        return new BigInteger(propMap.get("rx-packet"));
    }

    public BigInteger getTxDrops() {
        return new BigInteger(propMap.get("tx-drop"));
    }

    public BigInteger getRxDrops() {
        return new BigInteger(propMap.get("rx-drop"));
    }

    public BigInteger getTxErrors() {
        return new BigInteger(propMap.get("tx-error"));
    }

    public BigInteger getRxErrors() {
        return new BigInteger(propMap.get("rx-error"));
    }
}
