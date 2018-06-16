/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.lib.device.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.openhab.binding.yeelight.lib.CommonLogger;
import org.openhab.binding.yeelight.lib.device.ConnectState;
import org.openhab.binding.yeelight.lib.device.DeviceBase;
import org.openhab.binding.yeelight.lib.device.DeviceMethod;

/**
 * Created by jiang on 16/10/25.
 */

public class WifiConnection implements ConnectionBase {

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
                CommonLogger.debug(TAG + ": Write Success!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                CommonLogger.debug(TAG + ": write exception, set device to disconnected!");
                e.printStackTrace();
                mDevice.setConnectionState(ConnectState.DISCONNECTED);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean connect() {
        CommonLogger.debug(TAG + ": connect() entering!");
        if (mSocket != null && mSocket.isConnected()) {
            CommonLogger.debug(TAG + ": socket not null, return!");
            return true;
        }
        mConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCmdRun = true;
                    CommonLogger.debug(TAG + ": connect device!" + mDevice.getAddress() + ", " + mDevice.getPort());
                    mSocket = new Socket(mDevice.getAddress(), mDevice.getPort());
                    mSocket.setKeepAlive(true);
                    mWriter = new BufferedOutputStream(mSocket.getOutputStream());
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    mDevice.setConnectionState(ConnectState.CONNECTED);
                    while (mCmdRun) {
                        try {
                            String value = mReader.readLine();
                            CommonLogger.debug(TAG + ": get response:" + value);
                            if (value == null) {
                                mCmdRun = false;
                            } else {
                                mDevice.onNotify(value);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mCmdRun = false;
                        }
                    }
                    mSocket.close();
                } catch (Exception e) {
                    CommonLogger.debug(TAG + ": connect device! ERROR!" + e.getMessage());
                    e.printStackTrace();
                } finally {
                    mDevice.setConnectionState(ConnectState.DISCONNECTED);
                    mSocket = null;
                }
            }
        });
        mConnectThread.start();
        return false;
    }

    @Override
    public boolean disconnect() {
        mDevice.setAutoConnect(false);
        return false;
    }

}
