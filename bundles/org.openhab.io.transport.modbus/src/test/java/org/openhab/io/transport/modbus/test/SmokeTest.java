/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.SocketImplFactory;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusResponse;
import org.openhab.io.transport.modbus.ModbusWriteCoilRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.io.transport.modbus.exception.ModbusConnectionException;
import org.openhab.io.transport.modbus.exception.ModbusSlaveErrorResponseException;
import org.openhab.io.transport.modbus.exception.ModbusSlaveIOException;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleCoilsRequest;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.BitVector;

/**
 * @author Sami Salonen - Initial contribution
 */
public class SmokeTest extends IntegrationTestSupport {

    private static final int COIL_EVERY_N_TRUE = 2;
    private static final int DISCRETE_EVERY_N_TRUE = 3;
    private static final int HOLDING_REGISTER_MULTIPLIER = 1;
    private static final int INPUT_REGISTER_MULTIPLIER = 10;
    private static final SpyingSocketFactory socketSpy = new SpyingSocketFactory();
    static {
        try {
            Socket.setSocketImplFactory(socketSpy);
        } catch (IOException e) {
            fail("Could not install socket spy in SmokeTest");
        }
    }

    /**
     * Whether tests are run in Continuous Integration environment, i.e. Jenkins or Travis CI
     *
     * Travis CI is detected using CI environment variable, see https://docs.travis-ci.com/user/environment-variables/
     * Jenkins CI is detected using JENKINS_HOME environment variable
     *
     * @return
     */
    private boolean isRunningInCI() {
        return "true".equals(System.getenv("CI")) || StringUtils.isNotBlank(System.getenv("JENKINS_HOME"));
    }

    private void generateData() {
        for (int i = 0; i < 100; i++) {
            spi.addRegister(new SimpleRegister(i * HOLDING_REGISTER_MULTIPLIER));
            spi.addInputRegister(new SimpleRegister(i * INPUT_REGISTER_MULTIPLIER));
            spi.addDigitalOut(new SimpleDigitalOut(i % COIL_EVERY_N_TRUE == 0));
            spi.addDigitalIn(new SimpleDigitalIn(i % DISCRETE_EVERY_N_TRUE == 0));
        }
    }

    private void testCoilValues(BitArray bits, int offsetInBitArray) {
        for (int i = 0; i < bits.size(); i++) {
            boolean expected = (i + offsetInBitArray) % COIL_EVERY_N_TRUE == 0;
            assertThat(String.format("i=%d, expecting %b, got %b", i, bits.getBit(i), expected), bits.getBit(i),
                    is(equalTo(expected)));
        }
    }

    private void testDiscreteValues(BitArray bits, int offsetInBitArray) {
        for (int i = 0; i < bits.size(); i++) {
            boolean expected = (i + offsetInBitArray) % DISCRETE_EVERY_N_TRUE == 0;
            assertThat(String.format("i=%d, expecting %b, got %b", i, bits.getBit(i), expected), bits.getBit(i),
                    is(equalTo(expected)));
        }
    }

    private void testHoldingValues(ModbusRegisterArray registers, int offsetInRegisters) {
        for (int i = 0; i < registers.size(); i++) {
            int expected = (i + offsetInRegisters) * HOLDING_REGISTER_MULTIPLIER;
            assertThat(String.format("i=%d, expecting %d, got %d", i, registers.getRegister(i).toUnsignedShort(),
                    expected), registers.getRegister(i).toUnsignedShort(), is(equalTo(expected)));
        }
    }

    private void testInputValues(ModbusRegisterArray registers, int offsetInRegisters) {
        for (int i = 0; i < registers.size(); i++) {
            int expected = (i + offsetInRegisters) * INPUT_REGISTER_MULTIPLIER;
            assertThat(String.format("i=%d, expecting %d, got %d", i, registers.getRegister(i).toUnsignedShort(),
                    expected), registers.getRegister(i).toUnsignedShort(), is(equalTo(expected)));
        }
    }

    @Before
    public void setUpSocketSpy() throws IOException {
        socketSpy.sockets.clear();
    }

    /**
     * Test handling of slave error responses. In this case, error code = 2, illegal data address, since no data.
     *
     * @throws Exception
     */
    @Test
    public void testSlaveReadErrorResponse() throws Exception {
        ModbusSlaveEndpoint endpoint = getEndpoint();
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Exception> lastError = new AtomicReference<>();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 0, 5, 1), result -> {
                        assert result.getRegisters().isPresent();
                        okCount.incrementAndGet();
                        callbackCalled.countDown();
                    }, failure -> {
                        errorCount.incrementAndGet();
                        lastError.set(failure.getCause());
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(okCount.get(), is(equalTo(0)));
            assertThat(errorCount.get(), is(equalTo(1)));
            assertTrue(lastError.toString(), lastError.get() instanceof ModbusSlaveErrorResponseException);
        }
    }

    /**
     * Test handling of connection error responses.
     *
     * @throws Exception
     */
    @Test
    public void testSlaveConnectionError() throws Exception {
        // In the test we have non-responding slave (see http://stackoverflow.com/a/904609), and we use short connection
        // timeout
        ModbusSlaveEndpoint endpoint = new ModbusTCPSlaveEndpoint("10.255.255.1", 9999);
        EndpointPoolConfiguration configuration = new EndpointPoolConfiguration();
        configuration.setConnectTimeoutMillis(100);

        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Exception> lastError = new AtomicReference<>();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint,
                configuration)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 0, 5, 1), result -> {
                        assert result.getRegisters().isPresent();
                        okCount.incrementAndGet();
                        callbackCalled.countDown();
                    }, failure -> {
                        errorCount.incrementAndGet();
                        lastError.set(failure.getCause());
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(okCount.get(), is(equalTo(0)));
            assertThat(errorCount.get(), is(equalTo(1)));
            assertTrue(lastError.toString(), lastError.get() instanceof ModbusConnectionException);
        }
    }

    /**
     * Have super slow connection response, eventually resulting as timeout (due to default timeout of 3 s in
     * net.wimpi.modbus.Modbus.DEFAULT_TIMEOUT)
     *
     * @throws Exception
     */
    @Test
    public void testIOError() throws Exception {
        artificialServerWait = 60000;
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Exception> lastError = new AtomicReference<>();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 0, 5, 1), result -> {
                        assert result.getRegisters().isPresent();
                        okCount.incrementAndGet();
                        callbackCalled.countDown();
                    }, failure -> {
                        errorCount.incrementAndGet();
                        lastError.set(failure.getCause());
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(15, TimeUnit.SECONDS));
            assertThat(okCount.get(), is(equalTo(0)));
            assertThat(lastError.toString(), errorCount.get(), is(equalTo(1)));
            assertTrue(lastError.toString(), lastError.get() instanceof ModbusSlaveIOException);
        }
    }

    public void testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode functionCode, int count) throws Exception {
        assertThat(functionCode, is(anyOf(equalTo(ModbusReadFunctionCode.READ_INPUT_DISCRETES),
                equalTo(ModbusReadFunctionCode.READ_COILS))));
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Object> lastData = new AtomicReference<>();

        final int offset = 1;

        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID, functionCode, offset, count, 1),
                    result -> {
                        Optional<@NonNull BitArray> bitsOptional = result.getBits();
                        if (bitsOptional.isPresent()) {
                            lastData.set(bitsOptional.get());
                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            BitArray bits = (BitArray) lastData.get();
            assertThat(bits, notNullValue());
            assertThat(bits.size(), is(equalTo(count)));
            if (functionCode == ModbusReadFunctionCode.READ_INPUT_DISCRETES) {
                testDiscreteValues(bits, offset);
            } else {
                testCoilValues(bits, offset);
            }
        }
    }

    @Test
    public void testOneOffReadWithDiscrete1() throws Exception {
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_INPUT_DISCRETES, 1);
    }

    @Test
    public void testOneOffReadWithDiscrete7() throws Exception {
        // less than byte
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_INPUT_DISCRETES, 7);
    }

    @Test
    public void testOneOffReadWithDiscrete8() throws Exception {
        // exactly one byte
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_INPUT_DISCRETES, 8);
    }

    @Test
    public void testOneOffReadWithDiscrete13() throws Exception {
        // larger than byte, less than word (16 bit)
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_INPUT_DISCRETES, 13);
    }

    @Test
    public void testOneOffReadWithDiscrete18() throws Exception {
        // larger than word (16 bit)
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_INPUT_DISCRETES, 18);
    }

    @Test
    public void testOneOffReadWithCoils1() throws Exception {
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_COILS, 1);
    }

    @Test
    public void testOneOffReadWithCoils7() throws Exception {
        // less than byte
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_COILS, 7);
    }

    @Test
    public void testOneOffReadWithCoils8() throws Exception {
        // exactly one byte
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_COILS, 8);
    }

    @Test
    public void testOneOffReadWithCoils13() throws Exception {
        // larger than byte, less than word (16 bit)
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_COILS, 13);
    }

    @Test
    public void testOneOffReadWithCoils18() throws Exception {
        // larger than word (16 bit)
        testOneOffReadWithDiscreteOrCoils(ModbusReadFunctionCode.READ_COILS, 18);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testOneOffReadWithHolding() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Object> lastData = new AtomicReference<>();

        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 15, 1), result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            lastData.set(registersOptional.get());
                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            ModbusRegisterArray registers = (ModbusRegisterArray) lastData.get();
            assertThat(registers.size(), is(equalTo(15)));
            testHoldingValues(registers, 1);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testOneOffReadWithInput() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Object> lastData = new AtomicReference<>();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_INPUT_REGISTERS, 1, 15, 1), result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            lastData.set(registersOptional.get());
                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            ModbusRegisterArray registers = (ModbusRegisterArray) lastData.get();
            assertThat(registers.size(), is(equalTo(15)));
            testInputValues(registers, 1);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testOneOffWriteMultipleCoil() throws Exception {
        LoggerFactory.getLogger(this.getClass()).error("STARTING MULTIPLE");
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        AtomicReference<Object> lastData = new AtomicReference<>();

        BitArray bits = new BitArray(true, true, false, false, true, true);
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimeWrite(new ModbusWriteCoilRequestBlueprint(SLAVE_UNIT_ID, 3, bits, true, 1), result -> {
                lastData.set(result.getResponse());
            }, failure -> {
                unexpectedCount.incrementAndGet();
            });
            waitForAssert(() -> {
                assertThat(unexpectedCount.get(), is(equalTo(0)));
                assertThat(lastData.get(), is(notNullValue()));

                ModbusResponse response = (ModbusResponse) lastData.get();
                assertThat(response.getFunctionCode(), is(equalTo(15)));

                assertThat(modbustRequestCaptor.getAllReturnValues().size(), is(equalTo(1)));
                ModbusRequest request = modbustRequestCaptor.getAllReturnValues().get(0);
                assertThat(request.getFunctionCode(), is(equalTo(15)));
                assertThat(((WriteMultipleCoilsRequest) request).getReference(), is(equalTo(3)));
                assertThat(((WriteMultipleCoilsRequest) request).getBitCount(), is(equalTo(bits.size())));
                BitVector writeRequestCoils = ((WriteMultipleCoilsRequest) request).getCoils();
                BitArray writtenBits = new BitArray(BitSet.valueOf(writeRequestCoils.getBytes()), bits.size());
                assertThat(writtenBits, is(equalTo(bits)));
            }, 6000, 10);
        }
        LoggerFactory.getLogger(this.getClass()).error("ENDINGMULTIPLE");
    }

    /**
     * Write is out-of-bounds, slave should return error
     *
     * @throws Exception
     */
    @Test
    public void testOneOffWriteMultipleCoilError() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Exception> lastError = new AtomicReference<>();

        BitArray bits = new BitArray(500);
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimeWrite(new ModbusWriteCoilRequestBlueprint(SLAVE_UNIT_ID, 3, bits, true, 1), result -> {
                unexpectedCount.incrementAndGet();
                callbackCalled.countDown();
            }, failure -> {
                lastError.set(failure.getCause());
                callbackCalled.countDown();
            });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            assertTrue(lastError.toString(), lastError.get() instanceof ModbusSlaveErrorResponseException);

            assertThat(modbustRequestCaptor.getAllReturnValues().size(), is(equalTo(1)));
            ModbusRequest request = modbustRequestCaptor.getAllReturnValues().get(0);
            assertThat(request.getFunctionCode(), is(equalTo(15)));
            assertThat(((WriteMultipleCoilsRequest) request).getReference(), is(equalTo(3)));
            assertThat(((WriteMultipleCoilsRequest) request).getBitCount(), is(equalTo(bits.size())));
            BitVector writeRequestCoils = ((WriteMultipleCoilsRequest) request).getCoils();
            BitArray writtenBits = new BitArray(BitSet.valueOf(writeRequestCoils.getBytes()), bits.size());
            assertThat(writtenBits, is(equalTo(bits)));
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testOneOffWriteSingleCoil() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Object> lastData = new AtomicReference<>();

        BitArray bits = new BitArray(true);
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimeWrite(new ModbusWriteCoilRequestBlueprint(SLAVE_UNIT_ID, 3, bits, false, 1), result -> {
                lastData.set(result.getResponse());
                callbackCalled.countDown();
            }, failure -> {
                unexpectedCount.incrementAndGet();
                callbackCalled.countDown();
            });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            ModbusResponse response = (ModbusResponse) lastData.get();
            assertThat(response.getFunctionCode(), is(equalTo(5)));

            assertThat(modbustRequestCaptor.getAllReturnValues().size(), is(equalTo(1)));
            ModbusRequest request = modbustRequestCaptor.getAllReturnValues().get(0);
            assertThat(request.getFunctionCode(), is(equalTo(5)));
            assertThat(((WriteCoilRequest) request).getReference(), is(equalTo(3)));
            assertThat(((WriteCoilRequest) request).getCoil(), is(equalTo(true)));
        }
    }

    /**
     *
     * Write is out-of-bounds, slave should return error
     *
     * @throws Exception
     */
    @Test
    public void testOneOffWriteSingleCoilError() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(1);
        AtomicReference<Exception> lastError = new AtomicReference<>();

        BitArray bits = new BitArray(true);
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.submitOneTimeWrite(new ModbusWriteCoilRequestBlueprint(SLAVE_UNIT_ID, 300, bits, false, 1),
                    result -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    }, failure -> {
                        lastError.set(failure.getCause());
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            assertThat(unexpectedCount.get(), is(equalTo(0)));
            assertTrue(lastError.toString(), lastError.get() instanceof ModbusSlaveErrorResponseException);

            assertThat(modbustRequestCaptor.getAllReturnValues().size(), is(equalTo(1)));
            ModbusRequest request = modbustRequestCaptor.getAllReturnValues().get(0);
            assertThat(request.getFunctionCode(), is(equalTo(5)));
            assertThat(((WriteCoilRequest) request).getReference(), is(equalTo(300)));
            assertThat(((WriteCoilRequest) request).getCoil(), is(equalTo(true)));
        }
    }

    /**
     * Testing regular polling of coils
     *
     * Amount of requests is timed, and average poll period is checked
     *
     * @throws Exception
     */
    @Test
    public void testRegularReadEvery150msWithCoil() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(5);
        AtomicInteger dataReceived = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.registerRegularPoll(
                    new ModbusReadRequestBlueprint(SLAVE_UNIT_ID, ModbusReadFunctionCode.READ_COILS, 1, 15, 1), 150, 0,
                    result -> {
                        Optional<@NonNull BitArray> bitsOptional = result.getBits();
                        if (bitsOptional.isPresent()) {
                            BitArray bits = bitsOptional.get();
                            dataReceived.incrementAndGet();
                            try {
                                assertThat(bits.size(), is(equalTo(15)));
                                testCoilValues(bits, 1);
                            } catch (AssertionError e) {
                                unexpectedCount.incrementAndGet();
                            }
                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));

            long end = System.currentTimeMillis();
            assertPollDetails(unexpectedCount, dataReceived, start, end, 145, 500);
        }
    }

    /**
     * Testing regular polling of holding registers
     *
     * Amount of requests is timed, and average poll period is checked
     *
     * @throws Exception
     */
    @Test
    public void testRegularReadEvery150msWithHolding() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(5);
        AtomicInteger dataReceived = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.registerRegularPoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 15, 1), 150, 0, result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            ModbusRegisterArray registers = registersOptional.get();
                            dataReceived.incrementAndGet();
                            try {
                                assertThat(registers.size(), is(equalTo(15)));
                                testHoldingValues(registers, 1);
                            } catch (AssertionError e) {
                                unexpectedCount.incrementAndGet();
                            }
                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));
            long end = System.currentTimeMillis();
            assertPollDetails(unexpectedCount, dataReceived, start, end, 145, 500);
        }
    }

    @Test
    public void testRegularReadFirstErrorThenOK() throws Exception {
        generateData();
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(5);
        AtomicInteger dataReceived = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.registerRegularPoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 15, 1), 150, 0, result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            ModbusRegisterArray registers = registersOptional.get();
                            dataReceived.incrementAndGet();
                            try {
                                assertThat(registers.size(), is(equalTo(15)));
                                testHoldingValues(registers, 1);
                            } catch (AssertionError e) {
                                unexpectedCount.incrementAndGet();
                            }

                        } else {
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        unexpectedCount.incrementAndGet();
                        callbackCalled.countDown();
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));
            long end = System.currentTimeMillis();
            assertPollDetails(unexpectedCount, dataReceived, start, end, 145, 500);
        }
    }

    /**
     *
     * @param unexpectedCount number of unexpected callback calls
     * @param callbackCalled number of callback calls (including unexpected)
     * @param dataReceived number of expected callback calls (onBits or onRegisters)
     * @param pollStartMillis poll start time in milliepoch
     * @param expectedPollAverageMin average poll period should be at least greater than this
     * @param expectedPollAverageMax average poll period less than this
     * @throws InterruptedException
     */
    private void assertPollDetails(AtomicInteger unexpectedCount, AtomicInteger expectedCount, long pollStartMillis,
            long pollEndMillis, int expectedPollAverageMin, int expectedPollAverageMax) throws InterruptedException {
        int responses = expectedCount.get();
        assertThat(unexpectedCount.get(), is(equalTo(0)));
        assertTrue(responses > 1);

        // Rest of the (timing-sensitive) assertions are not run in CI
        assumeFalse("Running in CI! Will not test timing-sensitive details", isRunningInCI());
        float averagePollPeriodMillis = ((float) (pollEndMillis - pollStartMillis)) / (responses - 1);
        assertTrue(String.format(
                "Measured avarage poll period %f ms (%d responses in %d ms) is not withing expected limits [%d, %d]",
                averagePollPeriodMillis, responses, pollEndMillis - pollStartMillis, expectedPollAverageMin,
                expectedPollAverageMax),
                averagePollPeriodMillis > expectedPollAverageMin && averagePollPeriodMillis < expectedPollAverageMax);
    }

    @Test
    public void testUnregisterPollingOnClose() throws Exception {
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch successfulCountDownLatch = new CountDownLatch(3);
        AtomicInteger expectedReceived = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            comms.registerRegularPoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 15, 1), 200, 0, result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            expectedReceived.incrementAndGet();
                            successfulCountDownLatch.countDown();
                        } else {
                            // bits
                            unexpectedCount.incrementAndGet();
                        }
                    }, failure -> {
                        if (spi.getDigitalInCount() > 0) {
                            // No errors expected after server filled with data
                            unexpectedCount.incrementAndGet();
                        } else {
                            expectedReceived.incrementAndGet();
                            errorCount.incrementAndGet();
                            generateData();
                            successfulCountDownLatch.countDown();
                        }
                    });
            // Wait for N successful responses before proceeding with assertions of poll rate
            assertTrue(successfulCountDownLatch.await(60, TimeUnit.SECONDS));

            long end = System.currentTimeMillis();
            assertPollDetails(unexpectedCount, expectedReceived, start, end, 190, 600);

            // wait some more and ensure nothing comes back
            Thread.sleep(500);
            assertThat(unexpectedCount.get(), is(equalTo(0)));
        }
    }

    @Test
    public void testUnregisterPollingExplicit() throws Exception {
        ModbusSlaveEndpoint endpoint = getEndpoint();

        AtomicInteger unexpectedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();
        CountDownLatch callbackCalled = new CountDownLatch(3);
        AtomicInteger expectedReceived = new AtomicInteger();

        long start = System.currentTimeMillis();
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, null)) {
            PollTask task = comms.registerRegularPoll(new ModbusReadRequestBlueprint(SLAVE_UNIT_ID,
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 1, 15, 1), 200, 0, result -> {
                        Optional<@NonNull ModbusRegisterArray> registersOptional = result.getRegisters();
                        if (registersOptional.isPresent()) {
                            expectedReceived.incrementAndGet();
                        } else {
                            // bits
                            unexpectedCount.incrementAndGet();
                        }
                        callbackCalled.countDown();
                    }, failure -> {
                        if (spi.getDigitalInCount() > 0) {
                            // No errors expected after server filled with data
                            unexpectedCount.incrementAndGet();
                        } else {
                            expectedReceived.incrementAndGet();
                            errorCount.incrementAndGet();
                            generateData();
                        }
                    });
            assertTrue(callbackCalled.await(60, TimeUnit.SECONDS));
            long end = System.currentTimeMillis();
            assertPollDetails(unexpectedCount, expectedReceived, start, end, 190, 600);

            // Explicitly unregister the regular poll
            comms.unregisterRegularPoll(task);

            // wait some more and ensure nothing comes back
            Thread.sleep(500);
            assertThat(unexpectedCount.get(), is(equalTo(0)));
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testPoolConfigurationWithoutListener() throws Exception {
        EndpointPoolConfiguration defaultConfig = modbusManager.getEndpointPoolConfiguration(getEndpoint());
        assertThat(defaultConfig, is(notNullValue()));

        EndpointPoolConfiguration newConfig = new EndpointPoolConfiguration();
        newConfig.setConnectMaxTries(5);
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(getEndpoint(),
                newConfig)) {
            // Sets configuration for the endpoint implicitly
        }

        assertThat(modbusManager.getEndpointPoolConfiguration(getEndpoint()).getConnectMaxTries(), is(equalTo(5)));
        assertThat(modbusManager.getEndpointPoolConfiguration(getEndpoint()), is(not(equalTo(defaultConfig))));

        // Reset config
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(getEndpoint(), null)) {
            // Sets configuration for the endpoint implicitly
        }
        // Should match the default
        assertThat(modbusManager.getEndpointPoolConfiguration(getEndpoint()), is(equalTo(defaultConfig)));
    }

    @Test
    public void testConnectionCloseAfterLastCommunicationInterfaceClosed() throws IllegalArgumentException, Exception {
        assumeFalse("Running in CI! Will not test timing-sensitive details", isRunningInCI());
        ModbusSlaveEndpoint endpoint = getEndpoint();
        assumeTrue("Connection closing test supported only with TCP slaves",
                endpoint instanceof ModbusTCPSlaveEndpoint);

        // Generate server data
        generateData();

        EndpointPoolConfiguration config = new EndpointPoolConfiguration();
        config.setReconnectAfterMillis(9_000_000);

        // 1. capture open connections at this point
        long openSocketsBefore = getNumberOfOpenClients(socketSpy);
        assertThat(openSocketsBefore, is(equalTo(0L)));

        // 2. make poll, binding opens the tcp connection
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, config)) {
            {
                CountDownLatch latch = new CountDownLatch(1);
                comms.submitOneTimePoll(new ModbusReadRequestBlueprint(1, ModbusReadFunctionCode.READ_COILS, 0, 1, 1),
                        response -> {
                            latch.countDown();
                        }, failure -> {
                            latch.countDown();
                        });
                assertTrue(latch.await(60, TimeUnit.SECONDS));
            }
            waitForAssert(() -> {
                // 3. ensure one open connection
                long openSocketsAfter = getNumberOfOpenClients(socketSpy);
                assertThat(openSocketsAfter, is(equalTo(1L)));
            });
            try (ModbusCommunicationInterface comms2 = modbusManager.newModbusCommunicationInterface(endpoint,
                    config)) {
                {
                    CountDownLatch latch = new CountDownLatch(1);
                    comms.submitOneTimePoll(
                            new ModbusReadRequestBlueprint(1, ModbusReadFunctionCode.READ_COILS, 0, 1, 1), response -> {
                                latch.countDown();
                            }, failure -> {
                                latch.countDown();
                            });
                    assertTrue(latch.await(60, TimeUnit.SECONDS));
                }
                assertThat(getNumberOfOpenClients(socketSpy), is(equalTo(1L)));
                // wait for moment (to check that no connections are closed)
                Thread.sleep(1000);
                // no more than 1 connection, even though requests are going through
                assertThat(getNumberOfOpenClients(socketSpy), is(equalTo(1L)));
            }
            Thread.sleep(1000);
            // Still one connection open even after closing second connection
            assertThat(getNumberOfOpenClients(socketSpy), is(equalTo(1L)));

        } // 4. close (the last) comms
          // ensure that open connections are closed
          // (despite huge "reconnect after millis")
        waitForAssert(() -> {
            long openSocketsAfterClose = getNumberOfOpenClients(socketSpy);
            assertThat(openSocketsAfterClose, is(equalTo(0L)));
        });
    }

    @Test
    public void testConnectionCloseAfterOneOffPoll() throws IllegalArgumentException, Exception {
        assumeFalse("Running in CI! Will not test timing-sensitive details", isRunningInCI());
        ModbusSlaveEndpoint endpoint = getEndpoint();
        assumeTrue("Connection closing test supported only with TCP slaves",
                endpoint instanceof ModbusTCPSlaveEndpoint);

        // Generate server data
        generateData();

        EndpointPoolConfiguration config = new EndpointPoolConfiguration();
        config.setReconnectAfterMillis(2_000);

        // 1. capture open connections at this point
        long openSocketsBefore = getNumberOfOpenClients(socketSpy);
        assertThat(openSocketsBefore, is(equalTo(0L)));

        // 2. make poll, binding opens the tcp connection
        try (ModbusCommunicationInterface comms = modbusManager.newModbusCommunicationInterface(endpoint, config)) {
            {
                CountDownLatch latch = new CountDownLatch(1);
                comms.submitOneTimePoll(new ModbusReadRequestBlueprint(1, ModbusReadFunctionCode.READ_COILS, 0, 1, 1),
                        response -> {
                            latch.countDown();
                        }, failure -> {
                            latch.countDown();
                        });
                assertTrue(latch.await(60, TimeUnit.SECONDS));
            }
            // Right after the poll we should have one connection open
            waitForAssert(() -> {
                // 3. ensure one open connection
                long openSocketsAfter = getNumberOfOpenClients(socketSpy);
                assertThat(openSocketsAfter, is(equalTo(1L)));
            });
            // 4. Connection should close itself by the commons pool eviction policy (checking for old idle connection
            // every now and then)
            waitForAssert(() -> {
                // 3. ensure one open connection
                long openSocketsAfter = getNumberOfOpenClients(socketSpy);
                assertThat(openSocketsAfter, is(equalTo(0L)));
            }, 60_000, 50);

        }
    }

    private long getNumberOfOpenClients(SpyingSocketFactory socketSpy) {
        final InetAddress testServerAddress;
        try {
            testServerAddress = localAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return socketSpy.sockets.stream().filter(socketImpl -> {
            Socket socket = getSocketOfSocketImpl(socketImpl);
            return socket.getPort() == tcpModbusPort && socket.isConnected()
                    && socket.getLocalAddress().equals(testServerAddress);
        }).count();
    }

    /**
     * Spy all sockets that are created
     *
     * @author Sami Salonen
     *
     */
    private static class SpyingSocketFactory implements SocketImplFactory {

        Queue<SocketImpl> sockets = new ConcurrentLinkedQueue<SocketImpl>();

        @Override
        public SocketImpl createSocketImpl() {
            SocketImpl socket = newSocksSocketImpl();
            sockets.add(socket);
            return socket;
        }
    }

    private static SocketImpl newSocksSocketImpl() {
        try {
            Class<?> defaultSocketImpl = Class.forName("java.net.SocksSocketImpl");
            Constructor<?> constructor = defaultSocketImpl.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (SocketImpl) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get Socket corresponding to SocketImpl using reflection
     */
    private static Socket getSocketOfSocketImpl(SocketImpl impl) {
        try {
            Method getSocket = SocketImpl.class.getDeclaredMethod("getSocket");
            getSocket.setAccessible(true);
            return (Socket) getSocket.invoke(impl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
