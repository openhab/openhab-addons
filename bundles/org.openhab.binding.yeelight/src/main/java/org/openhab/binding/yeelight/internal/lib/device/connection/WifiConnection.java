/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.lib.device.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.openhab.binding.yeelight.internal.lib.device.ConnectState;
import org.openhab.binding.yeelight.internal.lib.device.DeviceBase;
import org.openhab.binding.yeelight.internal.lib.device.DeviceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiang on 16/10/25.
 *
 * @author Coaster Li - Initial contribution
 */

public class WifiConnection implements ConnectionBase {

    private final Logger logger = LoggerFactory.getLogger(WifiConnection.class);

    private static final String TAG = WifiConnection.class.getSimpleName();

    private Socket mSocket;
    private BufferedReader mReader;
    private BufferedOutputStream mWriter;
    private Thread mConnectThread;
    private DeviceBase mDevice;
    private boolean mCmdRun = false;

    public WifiConnection(DeviceBase device) {
        mDevice = device;
    }

    @Override
    public boolean invoke(DeviceMethod method) {
        if (mWriter != null) {
            try {
                mWriter.write(method.getParamsStr().getBytes());
                mWriter.flush();
                logger.debug("{}: Write Success!", TAG);
            } catch (Exception e) {
                logger.debug("{}: write exception, set device to disconnected!", TAG);
                logger.debug("Exception", e);
                mDevice.setConnectionState(ConnectState.DISCONNECTED);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean invokeCustom(DeviceMethod method) {
        if (mWriter != null) {
            try {
                mWriter.write(method.getCustomParamsStr().getBytes());
                mWriter.flush();
                logger.debug("{}: Write Success!", TAG);
            } catch (Exception e) {
                logger.debug("{}: write exception, set device to disconnected!", TAG);
                logger.debug("Exception", e);
                mDevice.setConnectionState(ConnectState.DISCONNECTED);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean connect() {
        logger.debug("{}: connect() entering!", TAG);
        if (mSocket != null && mSocket.isConnected()) {
            logger.debug("{}: socket not null, return!", TAG);
            return true;
        }
        mConnectThread = new Thread(() -> {
            try {
                mCmdRun = true;
                logger.debug("{}: connect device! {}, {}", TAG, mDevice.getAddress(), mDevice.getPort());
                mSocket = new Socket(mDevice.getAddress(), mDevice.getPort());
                mSocket.setKeepAlive(true);
                mWriter = new BufferedOutputStream(mSocket.getOutputStream());
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mDevice.setConnectionState(ConnectState.CONNECTED);
                while (mCmdRun) {
                    try {
                        String value = mReader.readLine();
                        logger.debug("{}: get response: {}", TAG, value);
                        if (value == null) {
                            mCmdRun = false;
                        } else {
                            mDevice.onNotify(value);
                        }
                    } catch (Exception e) {
                        logger.debug("Exception", e);
                        mCmdRun = false;
                    }
                }
                mSocket.close();
            } catch (Exception e) {
                logger.debug("{}: connect device! ERROR! {}", TAG, e.getMessage());
                logger.debug("Exception", e);
            } finally {
                mDevice.setConnectionState(ConnectState.DISCONNECTED);
                mSocket = null;
            }
        });
        mConnectThread.start();
        return false;
    }

    @Override
    public boolean disconnect() {
        mDevice.setAutoConnect(false);
        mCmdRun = false;
        try {
            if (mConnectThread != null) {
                mConnectThread.interrupt();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (Exception e) {
            logger.debug("Exception while terminating connection", e);
        } finally {
            mSocket = null;
            mDevice.setConnectionState(ConnectState.DISCONNECTED);
        }
        return true;
    }
}
