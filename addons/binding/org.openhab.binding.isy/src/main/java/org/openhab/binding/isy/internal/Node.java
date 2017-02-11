package org.openhab.binding.isy.internal;

public class Node {

    enum MessageTypes {
        STATUS("ST"),
        DOF("DOF"),
        DON("DON");

        private String mCode;

        MessageTypes(String code) {
            this.mCode = code;
        }

        public String getCode() {
            return this.mCode;
        }
    }

    protected String flag;
    protected String name;
    protected String address;
    protected String type;

    public Node(String name, String address, String type) {
        this.name = name;
        this.address = address;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    // protected String uri;
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return new StringBuilder("Isy Node: name=").append(name).append(", flag=").append(flag).append(", address=")
                .append(address).append(", type=").append(getTypeReadable()).toString();
    }

    public String getTypeReadable() {
        String[] typeElements = type.split("\\.");
        return String.format("%02X", Integer.parseInt(typeElements[0])) + "."
                + String.format("%02X", Integer.parseInt(typeElements[1]));
    }
}
