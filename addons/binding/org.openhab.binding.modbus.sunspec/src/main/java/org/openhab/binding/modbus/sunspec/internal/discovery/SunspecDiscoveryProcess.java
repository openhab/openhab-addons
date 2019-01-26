/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.sunspec.internal.discovery;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants;
import org.openhab.binding.modbus.sunspec.internal.block.CommonModelBlock;
import org.openhab.binding.modbus.sunspec.internal.detector.ModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.CommonModelParser;
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusBitUtilities;
import org.openhab.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusSlaveErrorResponseException;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used by the SunspecDiscoveryParticipant to detect
 * the model blocks defined by the given device.
 * It scans trough the defined model items and notifies the
 * discovery service about the discovered devices
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
public class SunspecDiscoveryProcess {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SunspecDiscoveryProcess.class);

    /**
     * The handler instance for this device
     */
    @NonNull
    private final ModbusEndpointThingHandler handler;

    /**
     * The endpoint where we can reach the device
     */
    private final ModbusSlaveEndpoint endpoint;

    /**
     * Listener for the discovered devices. We get this
     * from the main discovery service, and it is used to
     * submit any discovered Sunspec devices
     */
    @NonNull
    private final ModbusDiscoveryListener listener;

    /**
     * The endpoint's slave id
     */
    private Integer slaveId;

    /**
     * Number of maximum retries
     */
    @NonNull
    private Integer maxTries = 3;

    /**
     * List of start addresses to try
     */
    @NonNull
    private List<Integer> possibleAddresses;

    /**
     * This is the base address where the next block should be searched for
     */
    private int baseAddress = 40000;

    /**
     * Count of valid Sunspec blocks found
     */
    private int blocksFound = 0;

    /**
     * Parser for commonblock
     */
    @NonNull
    private final CommonModelParser commonBlockParser;

    /**
     * The last common block found. This is used
     * to get the details of any found devices
     */
    private CommonModelBlock lastCommonBlock;

    /**
     * New instances of this class should get a reference to the handler
     *
     * @throws EndpointNotInitializedException
     */
    public SunspecDiscoveryProcess(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener) {
        this.handler = handler;
        this.endpoint = this.handler.asSlaveEndpoint();
        this.listener = listener;
        commonBlockParser = new CommonModelParser();
        possibleAddresses = new CopyOnWriteArrayList<>();
        // Preferred and alternate base registers
        // @see SunSpec Information Model Overview
        possibleAddresses.add(40000);
        possibleAddresses.add(50000);
        possibleAddresses.add(0);
    }

    /**
     * Set the maximum number of retries for operations
     *
     * @param num the new value to set
     * @return
     */
    public SunspecDiscoveryProcess setMaxTries(Integer num) {
        this.maxTries = num;
        return this;
    }

    /**
     * Start model detection
     *
     * @param uid the thing type to look for
     * @throws EndpointNotInitializedException
     */
    public void detectModel() throws EndpointNotInitializedException {

        slaveId = handler.getSlaveId();

        if (this.endpoint == null) {
            logger.debug("Endpoint is null, can not continue with discovery");
            throw new EndpointNotInitializedException();
        }

        if (possibleAddresses.size() < 1) {
            parsingFinished();
            return;
        }
        // Try the next address from the possibles
        baseAddress = possibleAddresses.get(0);
        logger.trace("Beginning scan for SunSpec device at address {}", baseAddress);

        BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, baseAddress, // Start address
                SunSpecBindingConstants.SUNSPEC_ID_SIZE, // number or words to return
                maxTries);

        PollTask task = new BasicPollTaskImpl(endpoint, request, new ModbusReadCallback() {

            @Override
            public void onRegisters(ModbusReadRequestBlueprint request, ModbusRegisterArray registers) {

                headerReceived(registers);

            }

            @Override
            public void onError(ModbusReadRequestBlueprint request, Exception error) {
                handleError(error);
            }

            @Override
            public void onBits(@Nullable ModbusReadRequestBlueprint request, @Nullable BitArray bits) {
                // don't care, we don't expect this result
            }
        });

        handler.getManagerRef().get().submitOneTimePoll(task);
    }

    /**
     * We received the first two words, that should equal to SunS
     */
    private void headerReceived(ModbusRegisterArray registers) {
        logger.trace("Received response from device {}", registers.toString());

        long id = ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT32).longValue();

        if (id != SunSpecBindingConstants.SUNSPEC_ID) {
            logger.debug("Could not find SunSpec DID at address {}, received: {}, expected: {}", baseAddress, id,
                    SunSpecBindingConstants.SUNSPEC_ID);
            possibleAddresses.remove(0);
            try {
                detectModel();
            } catch (EndpointNotInitializedException ex) {
                // This should not happen
                parsingFinished();
            }
            return;
        }

        logger.trace("Header looks correct");
        baseAddress += SunSpecBindingConstants.SUNSPEC_ID_SIZE;

        lookForModelBlock();
    }

    /**
     * Look for a valid model block at the current base address
     */
    private void lookForModelBlock() {
        BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, baseAddress, // Start address
                SunSpecBindingConstants.MODEL_HEADER_SIZE, // number or words to return
                maxTries);

        PollTask task = new BasicPollTaskImpl(endpoint, request, new ModbusReadCallback() {

            @Override
            public void onRegisters(ModbusReadRequestBlueprint request, ModbusRegisterArray registers) {

                modelBlockReceived(registers);

            }

            @Override
            public void onError(ModbusReadRequestBlueprint request, Exception error) {
                handleError(error);
            }

            @Override
            public void onBits(@Nullable ModbusReadRequestBlueprint request, @Nullable BitArray bits) {
                // don't care, we don't expect this result
            }
        });

        handler.getManagerRef().get().submitOneTimePoll(task);
    }

    /**
     * We received a model block header
     */
    private void modelBlockReceived(ModbusRegisterArray registers) {
        logger.debug("Received response from device {}", registers.toString());

        ModelBlock block = new ModelBlock();
        block.address = baseAddress;
        block.moduleID = ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT16).intValue();
        block.length = ModbusBitUtilities.extractStateFromRegisters(registers, 1, ValueType.UINT16).intValue()
                + SunSpecBindingConstants.MODEL_HEADER_SIZE;
        logger.debug("SunSpec detector found block {}", block);

        blocksFound++;

        if (block.moduleID == FINAL_BLOCK) {
            parsingFinished();
        } else {
            baseAddress += block.length;
            if (block.moduleID == COMMON_BLOCK) {
                readCommonBlock(block); // This is an asynchronous task
                return;
            } else {
                createDiscoveryResult(block);
                lookForModelBlock();
            }

        }
    }

    /**
     * Start reading common block
     *
     * @param block
     */
    private void readCommonBlock(ModelBlock block) {
        BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, block.address, // Start address
                block.length, // number or words to return
                maxTries);

        PollTask task = new BasicPollTaskImpl(endpoint, request, new ModbusReadCallback() {

            @Override
            public void onRegisters(ModbusReadRequestBlueprint request, ModbusRegisterArray registers) {

                parseCommonBlock(registers);

            }

            @Override
            public void onError(ModbusReadRequestBlueprint request, Exception error) {
                handleError(error);
            }

            @Override
            public void onBits(@Nullable ModbusReadRequestBlueprint request, @Nullable BitArray bits) {
                // don't care, we don't expect this result
            }
        });

        handler.getManagerRef().get().submitOneTimePoll(task);
    }

    /**
     * We've read the details of a common block now parse it, and
     * store for later use
     *
     * @param registers
     */
    private void parseCommonBlock(ModbusRegisterArray registers) {
        logger.trace("Got common block data: {}", registers);
        lastCommonBlock = commonBlockParser.parse(registers);
        lookForModelBlock(); // Continue parsing
    }

    /**
     * Create a discovery result from a model block
     *
     * @param block the block we've found
     */
    private void createDiscoveryResult(ModelBlock block) {
        if (!SUPPORTED_THING_TYPES_UIDS.containsKey(block.moduleID)) {
            logger.debug("ModuleID {} is not supported, skipping this block", block.moduleID);
            return;
        }

        if (lastCommonBlock == null) {
            logger.warn(
                    "Found model block without a preceding common block. Can't add device because details are unkown");
            return;
        }

        ThingUID thingUID = new ThingUID(SUPPORTED_THING_TYPES_UIDS.get(block.moduleID), handler.getUID(),
                Integer.toString(block.address));

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_VENDOR, lastCommonBlock.getManufacturer());
        properties.put(PROPERTY_MODEL, lastCommonBlock.getModel());
        properties.put(PROPERTY_SERIAL_NUMBER, lastCommonBlock.getSerialNumber());
        properties.put(PROPERTY_VERSION, lastCommonBlock.getVersion());
        properties.put(PROPERTY_BLOCK_ADDRESS, block.address);
        properties.put(PROPERTY_BLOCK_LENGTH, block.length);
        properties.put(PROPERTY_UNIQUE_ADDRESS, handler.getUID().getAsString() + ":" + block.address);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_UNIQUE_ADDRESS).withBridge(handler.getUID())
                .withLabel(lastCommonBlock.getManufacturer() + " " + lastCommonBlock.getModel()).build();

        listener.thingDiscovered(result);
    }

    /**
     * Parsing of model blocks finished
     * Now we have to report back to the handler the common block and the block we were looking for
     */
    private void parsingFinished() {
        listener.discoveryFinished();
    }

    /**
     * Handle errors received during communication
     */
    private void handleError(Exception error) {
        String msg = "";
        String cls = "";

        if (blocksFound > 1 && error instanceof ModbusSlaveErrorResponseException) {
            int code = ((ModbusSlaveErrorResponseException) error).getExceptionCode();
            if (code == ModbusSlaveErrorResponseException.ILLEGAL_DATA_ACCESS
                    || code == ModbusSlaveErrorResponseException.ILLEGAL_DATA_VALUE) {
                // It is very likely that the slave does not report an end block (0xffff) after the main blocks
                // so we treat this situation as normal.
                logger.debug(
                        "Seems like slave device does not report an end block. Continouing with the dectected blocks");
                parsingFinished();
                return;
            }
        }
        if (error != null) {

            cls = error.getClass().getName();
            msg = error.getMessage();
        }
        logger.warn("Error with read at address {}: {} {}", baseAddress, cls, msg);

        possibleAddresses.remove(0); // Drop the current base address, and continue with the next one
        try {
            detectModel();
        } catch (EndpointNotInitializedException ex) {
            // This should not happen, but if it does then give up the discovery
            parsingFinished();
        }
    }

}
