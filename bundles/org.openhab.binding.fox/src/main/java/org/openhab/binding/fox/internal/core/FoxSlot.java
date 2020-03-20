package org.openhab.binding.fox.internal.core;

import java.math.BigInteger;
import java.util.ArrayList;

public class FoxSlot {

    protected FoxDevice parentDevice = null;

    public FoxSlot() {

    }

    protected int getIndex() {
        return parentDevice.getIndex(this);
    }

    protected void writeSet(Integer... args) throws FoxException {
        FoxMessageSet msg = new FoxMessageSet();
        msg.setDevice(parentDevice);
        msg.setSlotIndex(getIndex());
        ArrayList<Byte> argsList = new ArrayList<Byte>();
        for (Integer i : args) {
            byte[] bytes = BigInteger.valueOf(i).toByteArray();
            for (int bi = 0; bi < bytes.length; bi++) {
                byte b = bytes[(bytes.length - 1) - bi];
                if (bi < bytes.length - 1 || b != 0 || bytes.length == 1) {
                    argsList.add(b);
                }
            }
        }
        msg.setArgs(argsList);
        parentDevice.getParentSystem().write(msg);
    }

    protected Byte[] readGet() throws FoxException {
        FoxMessageGet msgGet = new FoxMessageGet();
        msgGet.setDevice(parentDevice);
        msgGet.setSlotIndex(getIndex());
        parentDevice.getParentSystem().write(msgGet);
        FoxMessageTake msgTake = new FoxMessageTake();
        parentDevice.getParentSystem().read(msgTake);
        if (msgTake.getDevice() != parentDevice.getAddress()) {
            throw new FoxException("Unexpected device address");
        }
        if (msgTake.getIndex() != getIndex()) {
            throw new FoxException("Unexpected slot index");
        }
        String[] strArray = msgTake.getArgs().split(" ");
        Byte[] intArray = new Byte[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = (byte) Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    protected Byte[] readGet(int minSize) throws FoxException {
        Byte[] bytes = readGet();
        if (bytes.length < minSize) {
            throw new FoxException("Read value list too short");
        }
        return bytes;
    }

    protected int convertArg(int value, int minValue, int maxValue, int scale) {
        return convertArg(value, minValue, maxValue) / scale;
    }

    protected int convertArg(int value, int minValue, int maxValue) {
        int v = value;
        if (v < minValue) {
            v = minValue;
        }
        if (v > maxValue) {
            v = maxValue;
        }
        return v;
    }

    private ArrayList<Integer> convertArgAligned(int value, int align) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < align; i++) {
            result.add((value >> 8 * i) & 0xff);
        }
        return result;
    }

    protected ArrayList<Integer> convertArgAligned(int value, int minValue, int maxValue, int scale, int align) {
        return convertArgAligned(convertArg(value, minValue, maxValue, scale), align);
    }

    protected ArrayList<Integer> convertArgAligned(int value, int minValue, int maxValue, int align) {
        return convertArgAligned(convertArg(value, minValue, maxValue), align);
    }

    void setParentDevice(FoxDevice device) {
        parentDevice = device;
    }

    public void label(int label) throws FoxException {
        if (parentDevice == null) {
            throw new FoxException("Null parent device");
        }
        parentDevice.getParentSystem().label(label, this);
    }

    public void label(String label) throws FoxException {
        if (parentDevice == null) {
            throw new FoxException("Null parent device");
        }
        parentDevice.getParentSystem().label(label, this);
    }
}
