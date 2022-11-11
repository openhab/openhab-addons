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
package org.openhab.binding.elroconnects.internal;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ElroConnectsMessage} represents the JSON messages exchanged with the ELRO Connects K1 Connector. This
 * class is used to serialize/deserialize the messages. The class also maps cmdId's from older firmware to the newer
 * codes and will encode/decode fields that are encoded in the messages with newer firmware versions.
 *
 * @author Mark Herwege - Initial contribution
 */
@SuppressWarnings("unused") // Suppress warning on serialized fields
@NonNullByDefault
public class ElroConnectsMessage {

    private transient boolean legacyFirmware = false; // legacy firmware uses different cmd id's and will not encode
                                                      // device id when sending messages

    private static class Data {
        private int cmdId;

        @SerializedName(value = "device_ID")
        private @Nullable Integer deviceId;

        @SerializedName(value = "device_name")
        private @Nullable String deviceName;

        @SerializedName(value = "device_status")
        private @Nullable String deviceStatus;

        @SerializedName(value = "answer_content")
        private @Nullable String answerContent;

        @SerializedName(value = "sence_group")
        private @Nullable Integer sceneGroup;

        @SerializedName(value = "scene_type")
        private @Nullable Integer sceneType;

        @SerializedName(value = "scene_content")
        private @Nullable String sceneContent;
    }

    private static class Params {
        private String devTid = "";
        private String ctrlKey = "";
        private Data data = new Data();
    }

    private int msgId;
    private String action = "appSend";
    private Params params = new Params();

    public ElroConnectsMessage(int msgId, String devTid, String ctrlKey, int cmdId) {
        this(msgId, devTid, ctrlKey, cmdId, false);
    }

    public ElroConnectsMessage(int msgId, String devTid, String ctrlKey, int cmdId, boolean legacyFirmware) {
        this.msgId = msgId;
        params.devTid = devTid;
        params.ctrlKey = ctrlKey;
        params.data.cmdId = legacyFirmware ? ELRO_LEGACY_MESSAGES.getOrDefault(cmdId, cmdId) : cmdId;

        this.legacyFirmware = legacyFirmware;
    }

    public ElroConnectsMessage withDeviceStatus(String deviceStatus) {
        params.data.deviceStatus = deviceStatus;
        return this;
    }

    public ElroConnectsMessage withDeviceId(int deviceId) {
        params.data.deviceId = isLegacy() ? deviceId : ElroConnectsUtil.encode(deviceId);
        return this;
    }

    public ElroConnectsMessage withSceneType(int sceneType) {
        params.data.sceneType = isLegacy() ? sceneType : ElroConnectsUtil.encode(sceneType);
        return this;
    }

    public ElroConnectsMessage withDeviceName(String deviceName) {
        params.data.deviceName = deviceName;
        return this;
    }

    public ElroConnectsMessage withSceneGroup(int sceneGroup) {
        params.data.sceneGroup = isLegacy() ? sceneGroup : ElroConnectsUtil.encode(sceneGroup);
        return this;
    }

    public ElroConnectsMessage withSceneContent(String sceneContent) {
        params.data.sceneContent = sceneContent;
        return this;
    }

    public ElroConnectsMessage withAnswerContent(String answerContent) {
        params.data.answerContent = answerContent;
        return this;
    }

    private boolean isLegacy() {
        return ELRO_NEW_MESSAGES.containsKey(params.data.cmdId) || legacyFirmware;
    }

    public int getMsgId() {
        return msgId;
    }

    public String getAction() {
        return action;
    }

    public int getCmdId() {
        return ELRO_NEW_MESSAGES.getOrDefault(params.data.cmdId, params.data.cmdId);
    }

    public String getDeviceStatus() {
        return ElroConnectsUtil.stringOrEmpty(params.data.deviceStatus);
    }

    public int getSceneGroup() {
        int sceneGroup = ElroConnectsUtil.intOrZero(params.data.sceneGroup);
        return isLegacy() ? sceneGroup : ElroConnectsUtil.decode(sceneGroup, msgId);
    }

    public int getSceneType() {
        int sceneType = ElroConnectsUtil.intOrZero(params.data.sceneType);
        return isLegacy() ? sceneType : ElroConnectsUtil.decode(sceneType, msgId);
    }

    public String getSceneContent() {
        return ElroConnectsUtil.stringOrEmpty(params.data.sceneContent);
    }

    public String getAnswerContent() {
        return ElroConnectsUtil.stringOrEmpty(params.data.answerContent);
    }

    public int getDeviceId() {
        int deviceId = ElroConnectsUtil.intOrZero(params.data.deviceId);
        return isLegacy() ? deviceId : ElroConnectsUtil.decode(deviceId, msgId);
    }

    public String getDeviceName() {
        return ElroConnectsUtil.stringOrEmpty(params.data.deviceName);
    }
}
