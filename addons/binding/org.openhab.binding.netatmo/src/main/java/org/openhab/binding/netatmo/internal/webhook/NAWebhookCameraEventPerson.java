/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    public Boolean IsKnown() {
        return isKnown;
    }
}
