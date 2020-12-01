/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a current external input status
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class CurrentExternalInputsStatus_1_1 extends CurrentExternalInputsStatus_1_0 {

    /** The uri identifying the input */
    private @Nullable String status;

    /**
     * Constructor used for deserialization only
     */
    public CurrentExternalInputsStatus_1_1() {
        super();
    }

    /**
     * Gets the status of the input
     *
     * @return the status of the input
     */
    public @Nullable String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CurrentExternalInputsStatus_1_1 [uri=" + getUri() + ", title=" + getTitle() + ", connection="
                + isConnection() + ", label=" + getLabel() + ", icon=" + getIcon() + ", status=" + status + "]";
    }
}
