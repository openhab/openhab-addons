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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.mockito.Mock;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.CryptUtil;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Base class for tests that test classes extending {@link SmartHomeDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DeviceTestBase<T extends SmartHomeDevice> {

    protected final T device;
    protected final Connection connection;
    protected final TPLinkSmartHomeConfiguration configuration = new TPLinkSmartHomeConfiguration();
    protected @NonNullByDefault({}) DeviceState deviceState;

    private final String deviceStateFilename;

    @Mock
    private @NonNullByDefault({}) Socket socket;
    @Mock
    private @NonNullByDefault({}) OutputStream outputStream;

    /**
     * Constructor.
     *
     * @param device Device under test
     * @param deviceStateFilename name of the file to read the device state json from to use in tests
     *
     * @throws IOException exception in case device not reachable
     */
    protected DeviceTestBase(T device, String deviceStateFilename) throws IOException {
        this.device = device;
        this.deviceStateFilename = deviceStateFilename;
        configuration.ipAddress = "localhost";
        configuration.refresh = 30;
        configuration.transitionPeriod = 10;
        connection = new Connection(configuration.ipAddress) {
            @Override
            protected Socket createSocket() throws IOException {
                return socket;
            }
        };
        device.initialize(connection, configuration);
    }

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        when(socket.getOutputStream()).thenReturn(outputStream);
        deviceState = new DeviceState(ModelTestUtil.readJson(deviceStateFilename));
    }

    /**
     * Sets the answer to return when the socket.getInputStream() is requested. If multiple files are given they will
     * returned in order each time a call to socket.getInputStream() is done.
     *
     * @param responseFilenames names of the files to read that contains the answer. It's the unencrypted json string
     * @throws IOException exception in case device not reachable
     */
    protected void setSocketReturnAssert(String... responseFilenames) throws IOException {
        AtomicInteger index = new AtomicInteger();

        doAnswer(i -> {
            String stateResponse = ModelTestUtil.readJson(responseFilenames[index.getAndIncrement()]);

            return new ByteArrayInputStream(CryptUtil.encryptWithLength(stateResponse));
        }).when(socket).getInputStream();

    }

    /**
     * Asserts the value passed to outputstream.write, which is the call that would be made to the actual device. This
     * checks if the value sent to the device is what is expected to be sent to the device. If multiple files are given
     * they will be used to check in order each time a call outputstream.write is done.
     *
     * @param filenames names of the files containing the reference json
     * @throws IOException exception in case device not reachable
     */
    protected void assertInput(String... filenames) throws IOException {
        assertInput(Function.identity(), Function.identity(), filenames);
    }

    protected void assertInput(Function<String, String> jsonProcessor, Function<String, String> expectedProcessor,
            String... filenames) throws IOException {
        AtomicInteger index = new AtomicInteger();

        doAnswer(arg -> {
            String json = jsonProcessor.apply(ModelTestUtil.readJson(filenames[index.get()]));

            byte[] input = (byte[]) arg.getArguments()[0];
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input)) {
                String expectedString = expectedProcessor.apply(CryptUtil.decryptWithLength(inputStream));
                assertEquals(filenames[index.get()], json, expectedString);
            }
            index.incrementAndGet();
            return null;
        }).when(outputStream).write(any());
    }
}
