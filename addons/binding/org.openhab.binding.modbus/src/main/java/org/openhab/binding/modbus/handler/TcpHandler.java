package org.openhab.binding.modbus.handler;

import static org.openhab.binding.modbus.ModbusBindingConstants.THING_TYPE_TCP;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.net.TCPMasterConnection;

public class TcpHandler extends BaseBridgeHandler implements BridgeConnector {
    private Logger logger = LoggerFactory.getLogger(TcpHandler.class);

    private static final String PROP_ADDRESS = "address";
    private static final String PROP_PORT = "port";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_TCP);
    private InetAddress address = null;
    private int port = 502;

    private TCPMasterConnection connection = null;
    private ModbusTCPTransaction transaction = new ModbusTCPTransaction();

    public TcpHandler(Bridge thing) {
        super(thing);
        try {
            address = InetAddress.getByName((String) thing.getConfiguration().get(PROP_ADDRESS));
            port = ((BigDecimal) thing.getConfiguration().get(PROP_PORT)).intValue();
            // id = ((BigDecimal) thing.getConfiguration().get(PROP_ID)).intValue();
            // start = ((BigDecimal) thing.getConfiguration().get(PROP_START)).intValue();
            // length = ((BigDecimal) thing.getConfiguration().get(PROP_LENGTH)).intValue();
            // String sType = (String) thing.getConfiguration().get(PROP_TYPE);
            // if (DATA_TYPE_COIL.equalsIgnoreCase(sType)) {
            // type = DATA_TYPES.COIL;
            // } else if (DATA_TYPE_DISCRETE.equalsIgnoreCase(sType)) {
            // type = DATA_TYPES.DISCRETE;
            // } else if (DATA_TYPE_HOLDING.equalsIgnoreCase(sType)) {
            // type = DATA_TYPES.HOLDING;
            // } else if (DATA_TYPE_INPUT.equalsIgnoreCase(sType)) {
            // type = DATA_TYPES.INPUT;
            // }
        } catch (Exception e) {
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        boolean isValid = true;
        if (!connect()) {
            isValid = false;
        }
        updateStatus(isValid ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // // TODO: handle command
        // }
    }

    @Override
    public boolean isConnected() {
        return connection == null ? false : connection.isConnected();
    }

    /**
     * Establishes connection to the device
     */
    @Override
    public boolean connect() {
        if (connection == null)
            connection = new TCPMasterConnection(address);
        if (!connection.isConnected())
            try {
                connection.setPort(port);
                connection.connect();
                (transaction).setConnection(connection);
                (transaction).setReconnecting(false);
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.debug("ModbusSlave: Error connecting to master: " + e.getMessage());
                updateStatus(ThingStatus.OFFLINE);
                return false;
            }
        return true;
    }

    @Override
    public void resetConnection() {
        connection = null;
    }

    @Override
    public ModbusTransaction getTransaction() {
        return transaction;
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

}