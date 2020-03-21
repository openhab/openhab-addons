package org.openhab.binding.fox.internal.core;

abstract class FoxMessage {

    protected String devToken;
    protected String appToken;
    protected String message;

    public FoxMessage() {
        reset();
    }

    void reset() {
        appToken = FoxDefinitions.appToken;
        setDeviceAll();
        message = "";
    }

    void setDevice(FoxDevice dev) {
        devToken = String.format("x%02x", dev.getAddress());
    }

    void setDeviceAll() {
        devToken = "all";
    }

    int getDevice() {
        if (devToken.startsWith("x") && devToken.length() == 3) {
            return Integer.parseInt(devToken.substring(1), 16);
        }
        return -1;
    }

    abstract protected void prepareMessage();

    abstract protected void interpretMessage();

    String prepare() {
        prepareMessage();
        return String.format("@%s:%s %s", devToken, appToken, message);
    }

    void interpret(String data) {
        reset();
        if (data.matches("@[^ ]+:[^ ]+ .+")) {
            appToken = data.substring(data.indexOf("@") + 1, data.indexOf(":"));
            devToken = data.substring(data.indexOf(":") + 1, data.indexOf(" "));
            message = data.substring(data.indexOf(" ") + 1);
        }
        interpretMessage();
    }
}
