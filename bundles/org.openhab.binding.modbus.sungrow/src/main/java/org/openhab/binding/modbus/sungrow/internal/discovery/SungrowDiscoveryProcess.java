/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal.discovery;

import static org.openhab.binding.modbus.sungrow.internal.SungrowConstants.*;

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
import org.openhab.binding.modbus.sungrow.internal.SungrowDeviceType;
import org.openhab.binding.modbus.sungrow.internal.dto.CommonModelBlock;
import org.openhab.binding.modbus.sungrow.internal.dto.ModelBlock;
import org.openhab.binding.modbus.sungrow.internal.parser.CommonModelParser;
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
 * This class is used by the SungrowDiscoveryParticipant to detect
 * the model blocks defined by the given device.
 * It scans trough the defined model items and notifies the
 * discovery service about the discovered devices
 *
 * @author Nagy Attila Gabor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 */
@NonNullByDefault
public class SungrowDiscoveryProcess {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SungrowDiscoveryProcess.class);

    /**
     * The handler instance for this device
     */
    private final ModbusEndpointThingHandler handler;

    /**
     * Listener for the discovered devices. We get this
     * from the main discovery service, and it is used to
     * submit any discovered Sungrow devices
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
     * Count of valid Sungrow blocks found
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
     * Communication interface to the endpointmvn spotless:apply
     */
    private ModbusCommunicationInterface comms;

    /**
     * New instances of this class should get a reference to the handler
     *
     * @throws EndpointNotInitializedException
     */
    public SungrowDiscoveryProcess(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener)
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
        // copied from SunSpec
        possibleAddresses.add(40000);
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
        logger.trace("Beginning scan for Sungrow device at address {}", baseAddress);

        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, baseAddress, // Start address
                SUNGROW_ID_SIZE, // number or words to return
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

        if (!id.isPresent() || id.get().longValue() != SUNGROW_ID) {
            logger.debug("Could not find Sungrow DID at address {}, received: {}, expected: {}", baseAddress, id,
                    SUNGROW_ID);
            detectModel();
            return;
        }

        logger.trace("Header looks correct");
        baseAddress += SUNGROW_ID_SIZE;

        lookForSunSpecBlock();
    }

    /**
     * Look for a valid model block at the current base address
     */
    private void lookForSunSpecBlock() {

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
        logger.debug("Sungrow detector found block {}", block);

        blocksFound++;

        if (block.moduleID == FINAL_BLOCK) {
            parsingFinished();
        } else {
            baseAddress += block.length;
            if (block.moduleID == COMMON_BLOCK) {
                readCommonBlock(block); // This is an asynchronous task
                return;
            } else {
                // createDiscoveryResult(block);
                // lookForSunSpecBlock();
                parsingFinished();
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
        CommonModelBlock commonBlock = lastCommonBlock;
        logger.debug("Found common block:\n{}", commonBlock.toString());
        logger.debug("SUPPORTED_THING_TYPES_UIDS:\n{}", SUPPORTED_THING_TYPES_UIDS.keySet().toString());
        logger.debug("SUPPORTED_THING_TYPES_UIDS:\n{}", SUPPORTED_THING_TYPES_UIDS.keySet().toString());
        logger.debug("commonBlock.manufacturer: '{}'", commonBlock.manufacturer);
        logger.debug("commonBlock.manufacturer.equals('SUNGROW'): {}", commonBlock.manufacturer.equals("SUNGROW"));
        logger.debug("SUPPORTED_THING_TYPES_UIDS.containsKey(commonBlock.model)': {}",
                (SUPPORTED_THING_TYPES_UIDS.containsKey(commonBlock.model)));
        if (commonBlock.manufacturer.equals("SUNGROW") && SUPPORTED_THING_TYPES_UIDS.containsKey(commonBlock.model)) {
            readDeviceType();
        }
        parsingFinished();
        // lookForSunSpecBlock(); // Continue parsing
    }

    /**
     * Start reading device type
     *
     * @param block
     */
    private void readDeviceType() {
        logger.trace("Try to read device Type.");
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, 4999, // Start address
                1, // number or words to return
                maxTries);

        comms.submitOneTimePoll(request, result -> result.getRegisters().ifPresent(this::parseDeviceType),
                this::handleError);
    }

    /**
     * We've read the device type now parse it
     *
     * @param registers
     */
    private void parseDeviceType(ModbusRegisterArray registers) {
        logger.trace("Got device type data: {}", registers);
        SungrowDeviceType deviceType = SungrowDeviceType.getByCode(registers.getRegister(0));
        logger.debug("Found device: {}", deviceType);
        if (deviceType != SungrowDeviceType.UNKNOWN) {
            createDiscoveryResult(deviceType);
        } else {
            logger.debug("Device Type {} is not supported", registers.getRegister(0));
        }
        parsingFinished();
        // lookForSunSpecBlock(); // Continue parsing
    }

    /**
     * Create a discovery result from a model block
     *
     * @param SungrowDeviceType the device we have found
     */
    private void createDiscoveryResult(SungrowDeviceType deviceType) {
        if (deviceType == SungrowDeviceType.UNKNOWN) {
            logger.debug("Device Type {} is not supported", deviceType);
            return;
        }

        CommonModelBlock commonBlock = lastCommonBlock;

        if (commonBlock == null) {
            logger.warn(
                    "Found model block without a preceding common block. Can't add device because details are unkown");
            return;
        }

        ThingTypeUID thingTypeUID = SUPPORTED_THING_TYPES_UIDS.get(commonBlock.model);
        if (thingTypeUID == null) {
            logger.warn("Found model but no corresponding thing type UID present: {}", commonBlock.model);
            return;
        }
        ThingUID thingUID = new ThingUID(thingTypeUID, handler.getUID(), deviceType.name());

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_VENDOR, commonBlock.manufacturer);
        properties.put(PROPERTY_MODEL, commonBlock.model);
        properties.put(PROPERTY_DEVICE_TYPE, deviceType.name());
        properties.put(PROPERTY_SERIAL_NUMBER, commonBlock.serialNumber);
        properties.put(PROPERTY_VERSION, commonBlock.version);

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withRepresentationProperty(PROPERTY_SERIAL_NUMBER).withBridge(handler.getUID())
                .withLabel(commonBlock.manufacturer + " " + commonBlock.model + " " + deviceType.name()).build();

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
        logger.trace("We received an error.");
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
