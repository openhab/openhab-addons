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
package org.openhab.binding.vigicrues.internal.json;

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
    private @Nullable Double flow;
    @SerializedName("hauteur")
    private @Nullable Double height;
    private @Nullable ZonedDateTime timestamp;

    public Optional<ZonedDateTime> getTimestamp() {
        ZonedDateTime timestamp = this.timestamp;
        if (timestamp != null) {
            return Optional.of(timestamp);
        }
        return Optional.empty();
    }

    public Optional<Double> getFlow() {
        Double flow = this.flow;
        if (flow != null) {
            return Optional.of(flow);
        }
        return Optional.empty();
    }

    public Optional<Double> getHeight() {
        Double height = this.height;
        if (height != null) {
            return Optional.of(height);
        }
        return Optional.empty();
    }

}
