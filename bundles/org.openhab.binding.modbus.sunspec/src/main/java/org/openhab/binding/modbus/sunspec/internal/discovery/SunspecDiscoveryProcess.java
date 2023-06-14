/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.sunspec.internal.dto.CommonModelBlock;
import org.openhab.binding.modbus.sunspec.internal.dto.ModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.CommonModelParser;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.exception.ModbusSlaveErrorResponseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
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
@NonNullByDefault
public class SunspecDiscoveryProcess {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SunspecDiscoveryProcess.class);

    /**
     * The handler instance for this device
     */
    private final ModbusEndpointThingHandler handler;

    /**
     * Listener for the discovered devices. We get this
     * from the main discovery service, and it is used to
     * submit any discovered Sunspec devices
     */
    private final ModbusDiscoveryListener listener;

    /**
     * The endpoint's slave id
     */
    private int slaveId;

    /**
     * Number of maximum retries
     */
    private static final int maxTries = 3;

    /**
     * List of start addresses to try
     */
    private Queue<Integer> possibleAddresses;

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
    private final CommonModelParser commonBlockParser;

    /**
     * The last common block found. This is used
     * to get the details of any found devices
     */
    private @Nullable CommonModelBlock lastCommonBlock = null;

    /**
     * Communication interface to the endpoint
     */
    private ModbusCommunicationInterface comms;

    /**
     * New instances of this class should get a reference to the handler
     *
     * @throws EndpointNotInitializedException
     */
    public SunspecDiscoveryProcess(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener)
            throws EndpointNotInitializedException {
        this.handler = handler;

        ModbusCommunicationInterface localComms = handler.getCommunicationInterface();
        if (localComms != null) {
            this.comms = localComms;
        } else {
            throw new EndpointNotInitializedException();
        }
        slaveId = handler.getSlaveId();
        this.listener = listener;
        commonBlockParser = new CommonModelParser();
        possibleAddresses = new ConcurrentLinkedQueue<>();
        // Preferred and alternate base registers
        // @see SunSpec Information Model Overview
        possibleAddresses.add(40000);
        possibleAddresses.add(50000);
        possibleAddresses.add(0);
    }

    /**
     * Start model detection
     *
     * @param uid the thing type to look for
     * @throws EndpointNotInitializedException
     */
    public void detectModel() {
        if (possibleAddresses.isEmpty()) {
            parsingFinished();
            return;
        }
        // Try the next address from the possibles
        baseAddress = possibleAddresses.poll();
        logger.trace("Beginning scan for SunSpec device at address {}", baseAddress);

        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, baseAddress, // Start address
                SUNSPEC_ID_SIZE, // number or words to return
                maxTries);

        comms.submitOneTimePoll(request, result -> result.getRegisters().ifPresent(this::headerReceived),
                this::handleError);
    }

    /**
     * We received the first two words, that should equal to SunS
     */
    private void headerReceived(ModbusRegisterArray registers) {
        logger.trace("Received response from device {}", registers.toString());

        Optional<DecimalType> id = ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT32);

        if (!id.isPresent() || id.get().longValue() != SUNSPEC_ID) {
            logger.debug("Could not find SunSpec DID at address {}, received: {}, expected: {}", baseAddress, id,
                    SUNSPEC_ID);
            detectModel();
            return;
        }

        logger.trace("Header looks correct");
        baseAddress += SUNSPEC_ID_SIZE;

        lookForModelBlock();
    }

    /**
     * Look for a valid model block at the current base address
     */
    private void lookForModelBlock() {
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, baseAddress, // Start address
                MODEL_HEADER_SIZE, // number or words to return
                maxTries);

        comms.submitOneTimePoll(request, result -> result.getRegisters().ifPresent(this::modelBlockReceived),
                this::handleError);
    }

    /**
     * We received a model block header
     */
    private void modelBlockReceived(ModbusRegisterArray registers) {
        logger.debug("Received response from device {}", registers.toString());

        Optional<DecimalType> moduleID = ModbusBitUtilities.extractStateFromRegisters(registers, 0, ValueType.UINT16);
        Optional<DecimalType> blockLength = ModbusBitUtilities.extractStateFromRegisters(registers, 1,
                ValueType.UINT16);

        if (!moduleID.isPresent() || !blockLength.isPresent()) {
            logger.info("Could not find valid module id or block length field.");
            parsingFinished();
            return;
        }
        ModelBlock block = new ModelBlock();
        block.address = baseAddress;
        block.moduleID = moduleID.get().intValue();
        block.length = blockLength.get().intValue() + MODEL_HEADER_SIZE;
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
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, block.address, // Start address
                block.length, // number or words to return
                maxTries);

        comms.submitOneTimePoll(request, result -> result.getRegisters().ifPresent(this::parseCommonBlock),
                this::handleError);
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

        CommonModelBlock commonBlock = lastCommonBlock;

        if (commonBlock == null) {
            logger.warn(
                    "Found model block without a preceding common block. Can't add device because details are unkown");
            return;
        }

        ThingTypeUID thingTypeUID = SUPPORTED_THING_TYPES_UIDS.get(block.moduleID);
        if (thingTypeUID == null) {
            logger.warn("Found model block but no corresponding thing type UID present: {}", block.moduleID);
            return;
        }
        ThingUID thingUID = new ThingUID(thingTypeUID, handler.getUID(), Integer.toString(block.address));

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_VENDOR, commonBlock.manufacturer);
        properties.put(PROPERTY_MODEL, commonBlock.model);
        properties.put(PROPERTY_SERIAL_NUMBER, commonBlock.serialNumber);
        properties.put(PROPERTY_VERSION, commonBlock.version);
        properties.put(PROPERTY_BLOCK_ADDRESS, block.address);
        properties.put(PROPERTY_BLOCK_LENGTH, block.length);
        properties.put(PROPERTY_UNIQUE_ADDRESS, handler.getUID().getAsString() + ":" + block.address);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_UNIQUE_ADDRESS).withBridge(handler.getUID())
                .withLabel(commonBlock.manufacturer + " " + commonBlock.model).build();

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
    private void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        if (blocksFound > 1 && failure.getCause() instanceof ModbusSlaveErrorResponseException) {
            int code = ((ModbusSlaveErrorResponseException) failure.getCause()).getExceptionCode();
            if (code == ModbusSlaveErrorResponseException.ILLEGAL_DATA_ACCESS
                    || code == ModbusSlaveErrorResponseException.ILLEGAL_DATA_VALUE) {
                // It is very likely that the slave does not report an end block (0xffff) after the main blocks
                // so we treat this situation as normal.
                logger.debug(
                        "Seems like slave device does not report an end block. Continuing with the dectected blocks");
                parsingFinished();
                return;
            }
        }

        String cls = failure.getCause().getClass().getName();
        String msg = failure.getCause().getMessage();

        logger.warn("Error with read at address {}: {} {}", baseAddress, cls, msg);

        detectModel();
    }
}
