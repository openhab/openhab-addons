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
package org.openhab.binding.netatmo.internal.webhook;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NAWebhookCameraEventPerson} is responsible to hold
 * data given back by the Netatmo API when calling the webhook
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class NAWebhookCameraEventPerson {
    @SerializedName("id")
    String id;

    public String getId() {
        return id;
    }

    @SerializedName("face_id")
    String faceId;

    public String getFaceId() {
        return faceId;
    }

    @SerializedName("face_key")
    String faceKey;

    public String getFaceKey() {
        return faceKey;
    }

    @SerializedName("is_known")
    Boolean isKnown;

    public Boolean isKnown() {
        return isKnown;
    }
}
