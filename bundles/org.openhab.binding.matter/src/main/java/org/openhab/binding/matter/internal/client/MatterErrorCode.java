/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.matter.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Error codes and translation keys for Matter errors events.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum MatterErrorCode {
    COMMISSIONING("commissioning", "matterjs.error.commissioning"),
    MAXIMUM_COMMISSIONED_FABRICS_REACHED("maximum-commissioned-fabrics-reached",
            "matterjs.error.maximum-commissioned-fabrics-reached"),
    COMMISSIONING_TIMEOUT("commissioning-timeout", "matterjs.error.commissioning-timeout"),
    DEVICE_ALREADY_COMMISSIONED_TO_THIS_FABRIC("device-already-commissioned-to-this-fabric",
            "matterjs.error.device-already-commissioned-to-this-fabric"),
    FABRIC_LABEL_CONFLICT("fabric-label-conflict", "matterjs.error.fabric-label-conflict"),
    WIFI_OR_THREAD_NETWORK_CREDENTIALS_NOT_CONFIGURED("wifi-or-thread-network-credentials-not-configured",
            "matterjs.error.wifi-or-thread-network-credentials-not-configured"),
    WIFI_NETWORK_SETUP_FAILED("wifi-network-setup-failed", "matterjs.error.wifi-network-setup-failed"),
    THREAD_NETWORK_SETUP_FAILED("thread-network-setup-failed", "matterjs.error.thread-network-setup-failed"),
    NODE_ID_CONFLICT("node-id-conflict", "matterjs.error.node-id-conflict"),
    COMMISSIONABLE_DEVICE_DISCOVERY_FAILED("commissionable-device-discovery-failed",
            "matterjs.error.commissionable-device-discovery-failed"),
    OPERATIVE_CONNECTION_FAILED("operative-connection-failed", "matterjs.error.operative-connection-failed");

    private final String errorId;
    private final String translationKey;

    MatterErrorCode(String errorId, String translationKey) {
        this.errorId = errorId;
        this.translationKey = translationKey;
    }

    public String getErrorId() {
        return errorId;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static @Nullable MatterErrorCode fromErrorId(String errorId) {
        for (MatterErrorCode error : values()) {
            if (error.errorId.equals(errorId)) {
                return error;
            }
        }
        return null;
    }
}
