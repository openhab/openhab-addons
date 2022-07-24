/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tplinksmarthome.internal.Connection;
import org.openhab.binding.tplinksmarthome.internal.CryptUtil;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeConfiguration;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Base class for tests that test classes extending {@link SmartHomeDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class DeviceTestBase<T extends SmartHomeDevice> {

    protected final T device;
    protected final Connection connection;
    protected final TPLinkSmartHomeConfiguration configuration = new TPLinkSmartHomeConfiguration();
    protected @NonNullByDefault({}) DeviceState deviceState;

    private final String deviceStateFilename;

    private @Mock @NonNullByDefault({}) Socket socket;
    private @Mock @NonNullByDefault({}) OutputStream outputStream;

    /**
     * Constructor.
     *
     * @param device Device under test
     * @param deviceStateFilename name of the file to read the device state json from to use in tests
     *
     * @throws IOException exception in case device not reachable
     */
    protected DeviceTestBase(final T device, final String deviceStateFilename) throws IOException {
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

    @BeforeEach
    public void setUp() throws IOException {
        lenient().when(socket.getOutputStream()).thenReturn(outputStream);
        deviceState = new DeviceState(ModelTestUtil.readJson(deviceStateFilename));
    }

    /**
     * Sets the answer to return when the socket.getInputStream() is requested. If multiple files are given they will
     * returned in order each time a call to socket.getInputStream() is done.
     *
     * @param responseFilenames names of the files to read that contains the answer. It's the unencrypted json string
     * @throws IOException exception in case device not reachable
     */
    protected void setSocketReturnAssert(final String... responseFilenames) throws IOException {
        final AtomicInteger index = new AtomicInteger();

        lenient().doAnswer(i -> {
            final String stateResponse = ModelTestUtil.readJson(responseFilenames[index.getAndIncrement()]);

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
    protected void assertInput(final String... filenames) throws IOException {
        assertInput(Function.identity(), Function.identity(), filenames);
    }

    protected void assertInput(final Function<String, String> jsonProcessor,
            final Function<String, String> expectedProcessor, final String... filenames) throws IOException {
        final AtomicInteger index = new AtomicInteger();

        lenient().doAnswer(arg -> {
            final String json = jsonProcessor.apply(ModelTestUtil.readJson(filenames[index.get()]));

            final byte[] input = (byte[]) arg.getArguments()[0];
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input)) {
                final String expectedString = expectedProcessor.apply(CryptUtil.decryptWithLength(inputStream));
                assertEquals(json, expectedString, filenames[index.get()]);
            }
            index.incrementAndGet();
            return null;
        }).when(outputStream).write(any());
    }
}
