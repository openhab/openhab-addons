package org.openhab.binding.modbus.handler;

import net.wimpi.modbus.io.ModbusTransaction;

/**
 * The {@link BridgeConnector} interface defines methods used
 * for interaction between tcp/servial connection and
 * modbus slave register pool
 *
 * @author Dmitry Krasnov - Initial contribution
 */

public interface BridgeConnector {
    public boolean isConnected();

    public boolean connect();

    public void resetConnection();

    public ModbusTransaction getTransaction();

    public boolean isHeadless();
}
