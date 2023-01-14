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
package org.openhab.binding.vigicrues.internal.dto.opendatasoft;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VigiCruesFields} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesFields {
    @SerializedName("debit")
    private double flow = -1;
    @SerializedName("hauteur")
    private double height = -1;
    private @Nullable ZonedDateTime timestamp;

    public Optional<ZonedDateTime> getTimestamp() {
        return Optional.ofNullable(timestamp);
    }

    public double getFlow() {
        return flow;
    }

    public double getHeight() {
        return height;
    }
}
