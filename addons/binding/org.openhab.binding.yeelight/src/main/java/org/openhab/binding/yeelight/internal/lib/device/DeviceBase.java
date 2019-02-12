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
package org.openhab.binding.yeelight.internal.lib.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.yeelight.internal.lib.device.connection.ConnectionBase;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceMode;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.openhab.binding.yeelight.internal.lib.enums.MethodAction;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceConnectionStateListener;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceStatusChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link DeviceBase} is a generic class for all devices.
 *
 * @author Coaster Li - Initial contribution
 * @author Daniel Walters - Correct handling of brightness
 * @author Joe Ho - Added duration to some commands
 */
public abstract class DeviceBase {
    private final Logger logger = LoggerFactory.getLogger(DeviceBase.class);

    private static final String TAG = DeviceBase.class.getSimpleName();

    protected String mDeviceId;
    protected String mDeviceName;
    protected DeviceType mDeviceType;
    protected String mAddress;
    private String[] mSupportProps;
    protected int mPort;
    private int mFwVersion;
    protected boolean bIsOnline;
    protected boolean bIsAutoConnect;
    protected ConnectionBase mConnection;
    protected ConnectState mConnectState = ConnectState.DISCONNECTED;
    protected DeviceStatus mDeviceStatus;
    private Map<String, Object> mBulbInfo = null;
    protected List<String> mQueryList = new ArrayList<>();

    protected int mMinCt, mMaxCt;

    List<DeviceConnectionStateListener> mConnectionListeners = new ArrayList<>();
    List<DeviceStatusChangeListener> mStatusChangeListeners = new ArrayList<>();

    public DeviceBase(String id) {
        mDeviceId = id;
        mDeviceStatus = new DeviceStatus();
    }

    public DeviceBase(String id, boolean isAutoConnect) {
        mDeviceId = id;
        this.bIsAutoConnect = isAutoConnect;
    }

    public void onNotify(String response) {
        boolean needNotify = true;
        JsonObject message = new JsonParser().parse(response).getAsJsonObject();
        try {
            String updateProp = "";
            if (message.has("method")) {
                String method = message.get("method").toString().replace("\"", "");
                if (method.equals("props")) {// Property notify
                    String params = message.get("params").toString();
                    JsonObject propsObject = new JsonParser().parse(params).getAsJsonObject();
                    Set<Entry<String, JsonElement>> props = propsObject.entrySet();
                    Iterator<Entry<String, JsonElement>> iterator = props.iterator();
                    while (iterator.hasNext()) {
                        Entry<String, JsonElement> prop = iterator.next();
                        if (prop.getKey().equals("power")) {
                            updateProp += " power";
                            if (prop.getValue().toString().equals("\"off\"")) {
                                mDeviceStatus.setPowerOff(true);
                            } else if (prop.getValue().toString().equals("\"on\"")) {
                                mDeviceStatus.setPowerOff(false);
                            }
                        } else if (prop.getKey().equals("bright")) {
                            updateProp += " bright";
                            mDeviceStatus.setBrightness(prop.getValue().getAsInt());
                        } else if (prop.getKey().equals("ct")) {
                            updateProp += " ct";
                            mDeviceStatus.setCt(prop.getValue().getAsInt());
                            mDeviceStatus.setMode(DeviceMode.MODE_SUNHINE);
                        } else if (prop.getKey().equals("rgb")) {
                            updateProp += " rgb";
                            mDeviceStatus.setMode(DeviceMode.MODE_COLOR);
                            int color = prop.getValue().getAsInt();
                            mDeviceStatus.setColor(color);
                            mDeviceStatus.setR((color >> 16) & 0xFF);
                            mDeviceStatus.setG((color >> 8) & 0xFF);
                            mDeviceStatus.setB(color & 0xFF);
                        } else if (prop.getKey().equals("hue")) {
                            updateProp += " hue";
                            mDeviceStatus.setMode(DeviceMode.MODE_HSV);
                            mDeviceStatus.setHue(prop.getValue().getAsInt());
                        } else if (prop.getKey().equals("sat")) {
                            updateProp += " sat";
                            mDeviceStatus.setMode(DeviceMode.MODE_HSV);
                            mDeviceStatus.setSat(prop.getValue().getAsInt());
                        } else if (prop.getKey().equals("color_mode")) {
                            updateProp += " color_mode";
                            switch (prop.getValue().getAsInt()) {
                                case DeviceStatus.MODE_COLOR:
                                    mDeviceStatus.setMode(DeviceMode.MODE_COLOR);
                                    break;
                                case DeviceStatus.MODE_COLORTEMPERATURE:
                                    mDeviceStatus.setMode(DeviceMode.MODE_SUNHINE);
                                    break;
                                case DeviceStatus.MODE_HSV:
                                    mDeviceStatus.setMode(DeviceMode.MODE_HSV);
                                    break;
                                default:
                                    break;
                            }
                        } else if (prop.getKey().equals("flowing")) {
                            updateProp += " flowing";
                            mDeviceStatus.setIsFlowing(prop.getValue().getAsInt() == 1);
                        } else if (prop.getKey().equals("flow_params")) {
                            updateProp += " flow_params";
                            // {"method":"props","params":{"flow_params":"0,0,1000,1,15935488,31,1000,1,13366016,31,1000,1,62370,31,1000,1,7995635,31"}}
                            String[] flowStrs = prop.getValue().toString().replace("\"", "").split(",");
                            if (flowStrs.length > 2 && (flowStrs.length - 2) % 4 == 0) {
                                mDeviceStatus.setFlowCount(Integer.parseInt(flowStrs[0]));
                                mDeviceStatus.setFlowEndAction(Integer.parseInt(flowStrs[1]));
                                if (mDeviceStatus.getFlowItems() == null) {
                                    mDeviceStatus.setFlowItems(new ArrayList<>());
                                }
                                mDeviceStatus.getFlowItems().clear();
                                for (int i = 0; i < ((flowStrs.length - 2) / 4); i++) {
                                    ColorFlowItem item = new ColorFlowItem();
                                    item.duration = Integer.valueOf(flowStrs[4 * i + 2]);
                                    item.mode = Integer.valueOf(flowStrs[4 * i + 3]);
                                    item.value = Integer.valueOf(flowStrs[4 * i + 4]);
                                    item.brightness = Integer.valueOf(flowStrs[4 * i + 5]);
                                    mDeviceStatus.getFlowItems().add(item);
                                }
                            }
                        } else if (prop.getKey().equals("delayoff")) {
                            updateProp += " delayoff";
                            int delayOff = prop.getValue().getAsInt();
                            if (delayOff > 0 && delayOff <= 60) {
                                mDeviceStatus.setDelayOff(delayOff);
                            } else {
                                mDeviceStatus.setDelayOff(DeviceStatus.DEFAULT_NO_DELAY);
                            }
                        } else if (prop.getKey().equals("music_on")) {
                            updateProp += " music_on";
                            mDeviceStatus.setMusicOn(prop.getValue().getAsInt() == 1);
                        } else if (prop.getKey().equals("name")) {
                            updateProp += " name";
                            mDeviceName = prop.getValue().toString();
                        }
                    }
                }

            } else if (message.has("id") && message.has("result")) {
                // no method, but result : ["ok"]
                JsonArray result = message.get("result").getAsJsonArray();
                if (result.get(0).toString().equals("\"ok\"")) {
                    logger.info("######### this is control command response, don't need to notify status change!");
                    needNotify = false;
                }
            }

            if (needNotify) {
                logger.info("status = {}", mDeviceStatus.toString());
                for (DeviceStatusChangeListener statusChangeListener : mStatusChangeListeners) {
                    statusChangeListener.onStatusChanged(updateProp.trim(), mDeviceStatus);
                }
            }
        } catch (Exception e) {
            logger.debug("Exception: {}", e);
        }
    }

    public void open(int duration) {
        mConnection.invoke(
                new DeviceMethod(MethodAction.SWITCH, new Object[] { "on", DeviceMethod.EFFECT_SMOOTH, duration }));
    }

    public void close(int duration) {
        mConnection.invoke(
                new DeviceMethod(MethodAction.SWITCH, new Object[] { "off", DeviceMethod.EFFECT_SMOOTH, duration }));
    }

    public void decreaseBrightness(int duration) {
        int bright = getDeviceStatus().getBrightness() - 10;
        if (bright <= 0) {
            close(duration);
        } else {
            setBrightness(bright, duration);
        }
    }

    public void increaseBrightness(int duration) {
        int bright = getDeviceStatus().getBrightness() + 10;
        if (bright > 100) {
            bright = 100;
        }
        setBrightness(bright, duration);
    }

    public void setBrightness(int brightness, int duration) {
        mConnection.invoke(MethodFactory.buildBrightnessMethd(brightness, DeviceMethod.EFFECT_SMOOTH, duration));
    }

    public void setColor(int color, int duration) {
        mConnection.invoke(MethodFactory.buildRgbMethod(color, DeviceMethod.EFFECT_SMOOTH, duration));
    }

    public void increaseCt(int duration) {
        int ct = getDeviceStatus().getCt() - ((mMaxCt - mMinCt) / 10);
        if (ct < mMinCt) {
            ct = mMinCt;
        }
        setCT(ct, duration);
    }

    public void decreaseCt(int duration) {
        int ct = getDeviceStatus().getCt() + ((mMaxCt - mMinCt) / 10);
        if (ct > mMaxCt) {
            ct = mMaxCt;
        }
        setCT(ct, duration);
    }

    public void setCT(int ct, int duration) {
        mConnection.invoke(MethodFactory.buildCTMethod(ct, DeviceMethod.EFFECT_SMOOTH, duration));
    }

    public void connect() {
        setConnectionState(ConnectState.CONNECTTING);
        mConnection.connect();
    }

    public void setConnectionState(ConnectState connectState) {
        logger.debug("{}: set connection state to: {}", TAG, connectState.name());
        if (connectState == ConnectState.DISCONNECTED) {
            setOnline(false);
        }
        if (mConnectState != connectState) {
            mConnectState = connectState;
            if (mConnectionListeners != null) {
                for (DeviceConnectionStateListener listener : mConnectionListeners) {
                    listener.onConnectionStateChanged(mConnectState);
                }
            }
        }
    }

    // ===================== setter and getter=====================

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceName(String name) {
        mDeviceName = name;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public String getDeviceModel() {
        return mDeviceType.name();
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public boolean isAutoConnect() {
        return bIsAutoConnect;
    }

    public int getFwVersion() {
        return mFwVersion;
    }

    public void setFwVersion(int fwVersion) {
        this.mFwVersion = fwVersion;
    }

    public Map<String, Object> getBulbInfo() {
        return mBulbInfo;
    }

    public void setBulbInfo(Map<String, Object> bulbInfo) {
        this.mBulbInfo = bulbInfo;
    }

    public String[] getSupportProps() {
        return mSupportProps;
    }

    public void setSupportProps(String[] supportProps) {
        this.mSupportProps = supportProps;
    }

    public void setAutoConnect(boolean isAutoConnect) {
        if (bIsAutoConnect != isAutoConnect) {
            this.bIsAutoConnect = isAutoConnect;
            checkAutoConnect();
        }
    }

    public DeviceStatus getDeviceStatus() {
        return mDeviceStatus;
    }

    public void registerConnectStateListener(DeviceConnectionStateListener listener) {
        mConnectionListeners.add(listener);

    }

    public void registerStatusChangedListener(DeviceStatusChangeListener listener) {
        mStatusChangeListeners.add(listener);
    }

    public void setOnline(boolean isOnline) {
        bIsOnline = isOnline;
        checkAutoConnect();
    }

    public boolean isOnline() {
        return bIsOnline;
    }

    public ConnectState getConnectionState() {
        return mConnectState;
    }

    public void queryStatus() {
        DeviceMethod cmd = MethodFactory.buildQuery(this);
        mQueryList.add(cmd.getCmdId());
        mConnection.invoke(cmd);
    }

    private void checkAutoConnect() {
        logger.debug(
                "{}: CheckAutoConnect: online: {}, autoConnect: {}, connection state: {}, device = {}, device id: {}",
                TAG, bIsOnline, bIsAutoConnect, mConnectState.name(), this, this.getDeviceId());
        if (bIsOnline && bIsAutoConnect && mConnectState == ConnectState.DISCONNECTED) {
            connect();
        }
    }

}
