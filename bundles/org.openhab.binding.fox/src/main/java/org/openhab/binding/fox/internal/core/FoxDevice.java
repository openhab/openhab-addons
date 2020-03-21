package org.openhab.binding.fox.internal.core;

import java.util.ArrayList;

public class FoxDevice {

    protected int address = 0;
    protected String type = "";
    protected Fox parentSystem = null;
    protected ArrayList<FoxSlot> slots;

    public FoxDevice(int address) throws FoxException {
        slots = new ArrayList<FoxSlot>();
        setAddress(address);
    }

    public void setAddress(int address) throws FoxException {
        if (address < Fox.minDeviceAddress || address > Fox.maxDeviceAddress) {
            throw new FoxException("Address out of range");
        }
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public String getTypeString() {
        return type;
    }

    public void reboot() throws FoxException {
        FoxMessageBoot msg = new FoxMessageBoot();
        msg.setDevice(this);
        parentSystem.write(msg);
    }

    public FoxSlot getSlot(int index) throws FoxException {
        try {
            return slots.get(index);
        } catch (IndexOutOfBoundsException ex) {
            throw new FoxException("Index out of range");
        }
    }

    public FoxSlot[] getSlots() {
        ArrayList<FoxSlot> slots = new ArrayList<FoxSlot>();
        for (FoxSlot s : this.slots) {
            slots.add(s);
        }
        FoxSlot[] array = new FoxSlot[slots.size()];
        return slots.toArray(array);
    }

    protected void addSlot(FoxSlot slot) {
        slot.setParentDevice(this);
        slots.add(slot);
    }

    protected FoxSlot findSlot(Class<?> slotClass, int index) {
        int i = index;
        for (FoxSlot slot : slots) {
            if (slot.getClass().equals(slotClass)) {
                i--;
            }
            if (i == -1) {
                return slot;
            }
        }
        return null;
    }

    void setParentSystem(Fox system) {
        parentSystem = system;
    }

    Fox getParentSystem() {
        return parentSystem;
    }

    int getIndex(FoxSlot slot) {
        return slots.indexOf(slot);
    }
}
