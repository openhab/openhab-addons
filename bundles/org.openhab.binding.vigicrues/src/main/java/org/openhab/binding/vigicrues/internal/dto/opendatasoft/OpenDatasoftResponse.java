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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link OpenDatasoftResponse} is the Java class used to map the JSON
 * response to an opendatasoft endpoint request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenDatasoftResponse {
    @SerializedName("nhits")
    private int nHits;
    private @Nullable Parameters parameters;
    private List<Record> records = new ArrayList<>();

    public int getNHits() {
        return nHits;
    }

    public Optional<Parameters> getParameters() {
        Parameters parameters = this.parameters;
        if (parameters != null) {
            return Optional.of(parameters);
        }
        return Optional.empty();
    }

    public Optional<VigiCruesFields> getFirstRecord() {
        return records.stream().findFirst().flatMap(Record::getFields);
    }
}
