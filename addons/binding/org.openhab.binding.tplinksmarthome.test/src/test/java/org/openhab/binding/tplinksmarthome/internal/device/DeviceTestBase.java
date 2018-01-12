/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNull;
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
public class DeviceTestBase {

    protected final Connection connection;
    protected final TPLinkSmartHomeConfiguration configuration = new TPLinkSmartHomeConfiguration();
    protected final DeviceState deviceState;

    @Mock
    private Socket socket;
    @Mock
    private OutputStream outputStream;

    /**
     * Constructor.
     *
     * @param deviceStateFilename name of the file to read the device state json from to use in tests
     * @throws IOException exception in case device not reachable
     */
    public DeviceTestBase(@NonNull String deviceStateFilename) throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(deviceStateFilename));
        configuration.ipAddress = "localhost";
        configuration.refresh = 30;
        configuration.transitionPeriod = 10;
        connection = new Connection(configuration.ipAddress) {
            @Override
            protected Socket createSocket() throws IOException {
                return socket;
            };
        };
    }

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        when(socket.getOutputStream()).thenReturn(outputStream);
    }

    /**
     * Sets the answer to return when the socket.getInputStream() is requested. This simulates the answer a real device
     * would give.
     *
     * @param responseFilename name of the file to read that contains the answer. It's the unencrypted json string
     * @throws IOException exception in case device not reachable
     */
    protected void setSocketReturnAssert(@NonNull String responseFilename) throws IOException {
        String stateResponse = ModelTestUtil.readJson(responseFilename);

        doAnswer(i -> {
            return new ByteArrayInputStream(CryptUtil.encryptWithLength(stateResponse));
        }).when(socket).getInputStream();

    }

    /**
     * Asserts the value passed to outputstream.write, which is the call that would be made to the actual device. This
     * checks if the value sent to the device is what is expected to be sent to the device.
     *
     * @param filename name of the file containing the reference json
     * @throws IOException exception in case device not reachable
     */
    protected void assertInput(@NonNull String filename) throws IOException {
        String json = ModelTestUtil.readJson(filename);

        doAnswer(i -> {
            byte[] input = (byte[]) i.getArguments()[0];
            assertEquals(filename, json, CryptUtil.decryptWithLength(new ByteArrayInputStream(input)));
            return null;
        }).when(outputStream).write(any());
    }
}
