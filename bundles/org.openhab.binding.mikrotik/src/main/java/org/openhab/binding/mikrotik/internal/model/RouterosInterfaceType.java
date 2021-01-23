package org.openhab.binding.mikrotik.internal.model;

public enum RouterosInterfaceType {
    ETHERNET ("ether"),
    BRIDGE ("bridge"),
    CAP ("cap"),
    PPPOE_CLIENT ("pppoe-out"),
    L2TP_SERVER ("l2tp-in");

    private final String typeName;

    RouterosInterfaceType(String routerosType) {
        this.typeName = routerosType;
    }

    public boolean equalsName(String otherType) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return typeName.equals(otherType);
    }

    public String toString() {
        return this.typeName;
    }

    public static RouterosInterfaceType resolve(String routerosType){
        for(RouterosInterfaceType current : RouterosInterfaceType.values()) {
            if(current.typeName.equals(routerosType)) {
                return current;
            }
        }
        return null;
    }

}
