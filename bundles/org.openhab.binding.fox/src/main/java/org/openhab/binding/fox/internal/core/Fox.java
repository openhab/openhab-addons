package org.openhab.binding.fox.internal.core;

import java.util.ArrayList;
import java.util.TreeMap;

import org.openhab.binding.fox.internal.devices.FoxDeviceDimm;
import org.openhab.binding.fox.internal.devices.FoxDeviceLed;
import org.openhab.binding.fox.internal.devices.FoxDeviceNet;
import org.openhab.binding.fox.internal.devices.FoxDeviceOut;
import org.openhab.binding.fox.internal.devices.FoxDeviceSet;
import org.openhab.binding.fox.internal.devices.FoxDeviceTouch;

public class Fox {

    public final static int maxDevicesCount = 32;
    public final static int minDeviceAddress = 0;
    public final static int maxDeviceAddress = maxDevicesCount - 1;

    private TreeMap<Integer, FoxDevice> devices;
    private TreeMap<Integer, FoxSlot> numberLabels;
    private TreeMap<String, FoxSlot> textLabels;
    private FoxMessenger messenger = null;

    public Fox() {
        devices = new TreeMap<Integer, FoxDevice>();
        numberLabels = new TreeMap<Integer, FoxSlot>();
        textLabels = new TreeMap<String, FoxSlot>();
    }

    public void setMessenger(FoxMessenger messenger) {
        this.messenger = messenger;
    }

    public FoxDevice addDevice(FoxDevice dev) throws FoxException {
        if (dev == null) {
            throw new FoxException("Null device");
        }
        if (devices.size() >= maxDevicesCount) {
            throw new FoxException(String.format("Cannot add more than %d devices", maxDevicesCount));
        }
        if (getDevice(dev.getAddress()) != null) {
            throw new FoxException(String.format("Device with address %d already added", dev.getAddress()));
        }
        dev.setParentSystem(this);
        devices.put(dev.getAddress(), dev);
        return dev;
    }

    public FoxDevice[] addDevices(FoxDevice[] devs) throws FoxException {
        for (FoxDevice dev : devs) {
            addDevice(dev);
        }
        return devs;
    }

    public FoxDevice getDevice(int address) {
        return devices.get(address);
    }

    public FoxDevice[] getDevices() {
        ArrayList<FoxDevice> devices = new ArrayList<FoxDevice>();
        for (FoxDevice dev : this.devices.values()) {
            devices.add(dev);
        }
        FoxDevice[] array = new FoxDevice[devices.size()];
        return devices.toArray(array);
    }

    public FoxDevice[] searchDevices() throws FoxException {
        ArrayList<FoxDevice> devices = new ArrayList<FoxDevice>();
        for (int i = minDeviceAddress; i <= maxDeviceAddress; i++) {
            FoxDevice device = new FoxDevice(i);

            FoxMessageHello msgTx = new FoxMessageHello();
            msgTx.setDevice(device);
            write(msgTx);

            FoxMessageMe msgRx = new FoxMessageMe();
            read(msgRx);
            String type = msgRx.getType();

            device = null;
            if (type.equals("nxw.fox.out")) {
                device = new FoxDeviceOut(i);
            } else if (type.equals("nxw.fox.dimm")) {
                device = new FoxDeviceDimm(i);
            } else if (type.equals("nxw.fox.led")) {
                device = new FoxDeviceLed(i);
            } else if (type.equals("nxw.fox.net")) {
                device = new FoxDeviceNet(i);
            } else if (type.equals("nxw.fox.touch")) {
                device = new FoxDeviceTouch(i);
            } else if (type.equals("nxw.fox.set")) {
                device = new FoxDeviceSet(i);
            }

            if (device != null) {
                devices.add(device);
            }
        }
        FoxDevice[] array = new FoxDevice[devices.size()];
        return devices.toArray(array);
    }

    public void reboot() throws FoxException {
        FoxMessageBoot msg = new FoxMessageBoot();
        write(msg);
    }

    public void doTask(int id) throws FoxException {
        FoxMessageDoTask msg = new FoxMessageDoTask();
        msg.setTaskId(id);
        write(msg);
    }

    public String noticeResult() throws FoxException {
        FoxMessageNoticeResult msg = new FoxMessageNoticeResult();
        read(msg);
        return msg.getResult();
    }

    public void commandLine(String line) throws FoxException {
        if (messenger == null) {
            throw new FoxException("Null messenger");
        }
        messenger.write(line.trim());
    }

    void write(FoxMessage msg) throws FoxException {
        if (msg == null) {
            throw new FoxException("Null message");
        }
        if (messenger == null) {
            throw new FoxException("Null messenger");
        }
        messenger.write(msg.prepare().trim());
    }

    void read(FoxMessage msg) throws FoxException {
        if (messenger == null) {
            throw new FoxException("Null messenger");
        }
        msg.interpret(messenger.read().trim());
    }

    void label(int label, FoxSlot slot) throws FoxException {
        if (slot == null) {
            throw new FoxException("Slot cannot be null");
        }
        if (numberLabels.containsKey(label)) {
            throw new FoxException(String.format("Label (%d) must be unique", label));
        }
        numberLabels.put(label, slot);
    }

    void label(String label, FoxSlot slot) throws FoxException {
        if (label == null) {
            throw new FoxException("Label cannot be null");
        }
        if (label.isEmpty()) {
            throw new FoxException("Label cannot be empty");
        }
        if (slot == null) {
            throw new FoxException("Slot cannot be null");
        }
        if (textLabels.containsKey(label)) {
            throw new FoxException(String.format("Label (%s) must be unique", label));
        }
        textLabels.put(label, slot);
    }

    public FoxSlot get(int label) {
        return numberLabels.get(label);
    }

    public FoxSlot get(String label) {
        return textLabels.get(label);
    }
}
