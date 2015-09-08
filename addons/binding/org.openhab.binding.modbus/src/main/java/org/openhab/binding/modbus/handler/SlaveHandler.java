/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import static org.openhab.binding.modbus.ModbusBindingConstants.THING_TYPE_SLAVE;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.util.BitVector;

/**
 * The {@link SlaveHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author vores8 - Initial contribution
 */
public class SlaveHandler extends BaseBridgeHandler implements SlaveConnector {

    private Logger logger = LoggerFactory.getLogger(SlaveHandler.class);
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SLAVE);

    static final String PROP_ID = "id";
    static final String PROP_START = "start";
    static final String PROP_LENGTH = "length";
    final static String PROP_TYPE = "type";
    final static String PROP_REFRESH = "refresh";

    final static String DATA_TYPE_COIL = "coil";
    final static String DATA_TYPE_DISCRETE = "discrete";
    final static String DATA_TYPE_HOLDING = "holding";
    final static String DATA_TYPE_INPUT = "input";

    enum DATA_TYPES {
        COIL,
        DISCRETE,
        HOLDING,
        INPUT
    };

    int id = 1;
    int start = 0;
    int length = 0;
    DATA_TYPES type = null;
    int refresh = 1000;

    private ScheduledFuture<?> pollingJob;
    private Object storage = null;
    private BridgeConnector connector = null;
    private boolean writeMultipleRegisters = false;

    void setConnector(BridgeConnector c) {
        this.connector = c;
    }

    public SlaveHandler(Bridge thing) {
        super(thing);
        try {
            refresh = ((BigDecimal) thing.getConfiguration().get(PROP_REFRESH)).intValue();
            id = ((BigDecimal) thing.getConfiguration().get(PROP_ID)).intValue();
            start = ((BigDecimal) thing.getConfiguration().get(PROP_START)).intValue();
            length = ((BigDecimal) thing.getConfiguration().get(PROP_LENGTH)).intValue();
            String sType = (String) thing.getConfiguration().get(PROP_TYPE);
            if (DATA_TYPE_COIL.equalsIgnoreCase(sType)) {
                type = DATA_TYPES.COIL;
            } else if (DATA_TYPE_DISCRETE.equalsIgnoreCase(sType)) {
                type = DATA_TYPES.DISCRETE;
            } else if (DATA_TYPE_HOLDING.equalsIgnoreCase(sType)) {
                type = DATA_TYPES.HOLDING;
            } else if (DATA_TYPE_INPUT.equalsIgnoreCase(sType)) {
                type = DATA_TYPES.INPUT;
            }

        } catch (Exception e) {
        }

    }

    @Override
    public void initialize() {
        super.initialize();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (connector == null) {
                    return;
                }
                if (!connector.connect()) {
                    connector.resetConnection();
                    logger.info("ModbusSlave not connected");
                    return;
                }

                try {

                    Object local = null;

                    if (type == DATA_TYPES.COIL) {
                        ModbusRequest request = new ReadCoilsRequest(start, length);
                        if (connector.isHeadless()) {
                            request.setHeadless();
                        }
                        // if (this instanceof SerialHandler) {
                        // request.setHeadless();
                        // }
                        request.setUnitID(id);
                        ReadCoilsResponse responce = (ReadCoilsResponse) getModbusData(request);
                        local = responce.getCoils();
                    } else if (type == DATA_TYPES.DISCRETE) {
                        ModbusRequest request = new ReadInputDiscretesRequest(start, length);
                        ReadInputDiscretesResponse responce = (ReadInputDiscretesResponse) getModbusData(request);
                        local = responce.getDiscretes();
                    } else if (type == DATA_TYPES.HOLDING) {
                        ModbusRequest request = new ReadMultipleRegistersRequest(start, length);
                        ReadMultipleRegistersResponse responce = (ReadMultipleRegistersResponse) getModbusData(request);
                        local = responce.getRegisters();
                    } else if (type == DATA_TYPES.INPUT) {
                        ModbusRequest request = new ReadInputRegistersRequest(start, length);
                        ReadInputRegistersResponse responce = (ReadInputRegistersResponse) getModbusData(request);
                        local = responce.getRegisters();
                    }
                    if (storage == null)
                        storage = local;
                    else {
                        synchronized (storage) {
                            storage = local;
                        }
                    }
                    for (Thing t : getThing().getThings()) {
                        EndpointHandler h = (EndpointHandler) t.getHandler();
                        if (type == DATA_TYPES.COIL || type == DATA_TYPES.DISCRETE) {
                            h.update((BitVector) storage);
                        } else if (type == DATA_TYPES.HOLDING || type == DATA_TYPES.INPUT) {
                            h.update((InputRegister[]) storage);
                        }
                    }
                    // Collection<String> items = binding.getItemNames();
                    // for (String item : items) {
                    // updateItem(binding, item);
                    // }
                } catch (Exception e) {
                    connector.resetConnection();
                    logger.info("ModbusSlave error getting responce from slave");
                }
            }
        };
        connector = (BridgeConnector) getBridge().getHandler();
        Bridge b = getBridge();
        ThingHandler h = b.getHandler();

        pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.MILLISECONDS);
        boolean online = connector.isConnected();
        updateStatus(online ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

    /**
     * Executes Modbus transaction that reads data from the device and returns response data
     *
     * @param request describes what data are requested from the device
     * @return response data
     */
    private ModbusResponse getModbusData(ModbusRequest request) {
        request.setUnitID(id);
        connector.getTransaction().setRequest(request);

        try {
            connector.getTransaction().execute();
        } catch (Exception e) {
            logger.debug("ModbusSlave:" + e.getMessage());
            return null;
        }

        ModbusResponse r = connector.getTransaction().getResponse();
        if ((r.getTransactionID() != connector.getTransaction().getTransactionID()) && !r.isHeadless()) {
            return null;
        }

        return r;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
        for (Thing t : this.getBridge().getThings()) {
            EndpointHandler h = (EndpointHandler) t.getHandler();
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCoil(boolean b, int readRegister, int writeRegister) {
        synchronized (storage) {
            if (((BitVector) storage).getBit(readRegister) != b) {
                if (b) {
                    doSetCoil(start + writeRegister, true);
                } else {
                    doSetCoil(start + writeRegister, readRegister == writeRegister ? false : true);
                }
            }
        }
    }

    /**
     * Sends boolean (bit) data to the device using Modbus FC05 function
     *
     * @param writeRegister
     * @param b
     */
    public void doSetCoil(int writeRegister, boolean b) {
        if (!connector.connect()) {
            logger.info("ModbusSlave not connected");
            return;
        }
        ModbusRequest request = new WriteCoilRequest(writeRegister, b);
        request.setUnitID(id);
        connector.getTransaction().setRequest(request);
        try {
            logger.debug("ModbusSlave: FC05 ref=" + writeRegister + " value=" + b);
            connector.getTransaction().execute();
        } catch (Exception e) {
            logger.debug("ModbusSlave:" + e.getMessage());
            return;
        }
    }

    @Override
    public void setRegister(int value, int readRegister, int writeRegister) {
        /**
         * Performs physical write to device when slave type is "holding" using Modbus FC06 function
         *
         * @param command command received from OpenHAB
         * @param readRegister reference to the register that stores current value
         * @param writeRegister register reference to write data to
         */
        if (!connector.isConnected()) {
            return;
        }

        Register newValue = null;
        synchronized (storage) {
            newValue = (Register) ((InputRegister[]) storage)[readRegister];
        }

        // if (command instanceof IncreaseDecreaseType) {
        // if (command.equals(IncreaseDecreaseType.INCREASE))
        // newValue.setValue(newValue.getValue() + 1);
        // else if (command.equals(IncreaseDecreaseType.DECREASE))
        // newValue.setValue(newValue.getValue() - 1);
        // } else if (command instanceof UpDownType) {
        // if (command.equals(UpDownType.UP))
        // newValue.setValue(newValue.getValue() + 1);
        // else if (command.equals(UpDownType.DOWN))
        // newValue.setValue(newValue.getValue() - 1);
        // } else if (command instanceof DecimalType) {
        // newValue.setValue(((DecimalType)command).intValue());
        // } else if (command instanceof OnOffType) {
        // if (command.equals(OnOffType.ON))
        // newValue.setValue(1);
        // else if (command.equals(OnOffType.OFF))
        // newValue.setValue(0);
        // }
        newValue.setValue(value);
        ModbusRequest request = null;
        if (writeMultipleRegisters) {
            Register[] regs = new Register[1];
            regs[0] = newValue;
            request = new WriteMultipleRegistersRequest(writeRegister, regs);
        } else {
            request = new WriteSingleRegisterRequest(writeRegister, newValue);
        }
        request.setUnitID(id);
        connector.getTransaction().setRequest(request);

        try {
            logger.debug("ModbusSlave: FC" + request.getFunctionCode() + " ref=" + writeRegister + " value="
                    + newValue.getValue());
            connector.getTransaction().execute();
        } catch (Exception e) {
            logger.debug("ModbusSlave:" + e.getMessage());
            return;
        }
    }

}
