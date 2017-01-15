/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.config;

import com.google.gson.JsonObject;

/**
 * The {@link WinkDeviceConfig} class represents the common configuration
 * parameters of a Wink Device
 *
 * @author Sebastien Marchand - Initial contribution
 *
 */
public class WinkDeviceConfig {
    private String deviceId;
    private String name;
    private String modelName;
    private String deviceManufacturer;
    private String pubnubSubscribeKey;
    private String pubnubChannel;

    public WinkDeviceConfig(String deviceID) {
        this.deviceId = deviceID;
    }

    public void readConfigFromJson(JsonObject jsonConfig) {
        this.name = jsonConfig.get("name").toString().replaceAll("\"", "");
        this.modelName = jsonConfig.get("model_name").toString().replaceAll("\"", "");
        this.deviceManufacturer = jsonConfig.get("device_manufacturer").toString().replaceAll("\"", "");
        JsonObject subscriptionBlob = jsonConfig.get("subscription").getAsJsonObject();
        if (subscriptionBlob != null) {
            JsonObject pubnubBlob = subscriptionBlob.get("pubnub").getAsJsonObject();
            if (pubnubBlob != null) {
                this.pubnubSubscribeKey = pubnubBlob.get("subscribe_key").toString().replaceAll("\"", "");
                this.pubnubChannel = pubnubBlob.get("channel").toString().replaceAll("\"", "");
            }
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getPubnubSubscribeKey() {
        return pubnubSubscribeKey;
    }

    public String getPubnubChannel() {
        return pubnubChannel;
    }
}