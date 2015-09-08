package org.openhab.binding.modbus.handler;

import static org.openhab.binding.modbus.ModbusBindingConstants.THING_TYPE_SERIAL;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;

/**
 * The {@link SerialHandler} class is responsible
 * for connection to Modbus device using
 * serial communications
 *
 * @author Dmitry Krasnov - Initial contribution
 */
public class SerialHandler extends BaseBridgeHandler implements BridgeConnector {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SERIAL);
    private static final Object PROP_PORTNAME = "port";
    private static final Object PROP_BAUD = "baud";
    private static final Object PROP_DATABITS = "databits";
    private static final Object PROP_STOPBITS = "stopbits";
    private static final Object PROP_PARITY = "parity";
    private static final Object PROP_ENCODING = "encoding";
    private Logger logger = LoggerFactory.getLogger(SerialHandler.class);

    private SerialParameters parameters = new SerialParameters();
    private ModbusSerialTransaction transaction = null;

    private static SerialConnection connection = null;

    /**
     * {@inheritDoc}
     */
    public SerialHandler(Bridge thing) {
        super(thing);
        parameters.setPortName(thing.getConfiguration().get(PROP_PORTNAME).toString());
        parameters.setBaudRate(((BigDecimal) thing.getConfiguration().get(PROP_BAUD)).intValue());
        parameters.setDatabits(((BigDecimal) thing.getConfiguration().get(PROP_DATABITS)).intValue());
        parameters.setStopbits(thing.getConfiguration().get(PROP_STOPBITS).toString());
        parameters.setParity(thing.getConfiguration().get(PROP_PARITY).toString());
        parameters.setEncoding(thing.getConfiguration().get(PROP_ENCODING).toString());
        transaction = new ModbusSerialTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connect() {
        try {
            if (connection == null) {
                logger.debug("connection was null, going to create a new one");
                connection = new SerialConnection(parameters);
            }
            if (!connection.isOpen()) {
                connection.open();
            }
            transaction.setSerialConnection(connection);
        } catch (Exception e) {
            logger.error("ModbusSlave: Error connecting to master: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetConnection() {
        if (connection != null) {
            connection.close();
        }
        connection = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connection != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusTransaction getTransaction() {
        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadless() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

}
