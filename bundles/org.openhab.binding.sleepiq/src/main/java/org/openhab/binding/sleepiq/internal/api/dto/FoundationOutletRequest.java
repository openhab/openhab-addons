/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutlet;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutletOperation;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationOutletRequest} is used to control an outlet on the bed foundation.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationOutletRequest {
    @SerializedName("outletId")
    private FoundationOutlet outlet;

    @SerializedName("setting")
    private FoundationOutletOperation operation;

    public FoundationOutlet getFoundationOutlet() {
        return outlet;
    }

    public void setFoundationOutlet(FoundationOutlet outlet) {
        this.outlet = outlet;
    }

    public FoundationOutletRequest withFoundationOutlet(FoundationOutlet outlet) {
        setFoundationOutlet(outlet);
        return this;
    }

    public FoundationOutletOperation getFoundationOutletOperation() {
        return operation;
    }

    public void setFoundationOutletOperation(FoundationOutletOperation operation) {
        this.operation = operation;
    }

    public FoundationOutletRequest withFoundationOutletOperation(FoundationOutletOperation operation) {
        setFoundationOutletOperation(operation);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [outlet=");
        builder.append(outlet);
        builder.append(", operation=");
        builder.append(operation);
        builder.append("]");
        return builder.toString();
    }
}
