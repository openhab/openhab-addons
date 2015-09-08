package org.openhab.binding.modbus.handler;

/**
 * The {@link SlaveConnector} interface defines methods used
 * for interaction between modbus slave register pool and
 * endpoint registers
 *
 * @author Dmitry Krasnov - Initial contribution
 */
public interface SlaveConnector {

    void setCoil(boolean b, int readRegister, int writeRegister);

    void setRegister(int value, int readRegister, int writeRegister);

}
