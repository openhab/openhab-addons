package org.openhab.binding.modbus.handler;

public interface SlaveConnector {

    void setCoil(boolean b, int readRegister, int writeRegister);

    void setRegister(int value, int readRegister, int writeRegister);

}
