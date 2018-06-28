/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.servelet;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NAWebhookCameraEvent} is responsible to hold
 * data given back by the Netatmo API when calling the webhook
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class KonnectedModuleEvent {

    @SerializedName("pin")
    String pin;

    public String getPin() {
        return pin;
    }

    @SerializedName("state")
    String state;

    public String getState() {
        return state;
    }

}
