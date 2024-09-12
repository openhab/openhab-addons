/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Record} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Record {
    @SerializedName("datasetid")
    private String datasetId = "";
    @SerializedName("recordid")
    private String recordId = "";
    @SerializedName("record_timestamp")
    private String recordTimestamp = "";
    private @Nullable VigiCruesFields fields;

    public String getDatasetId() {
        return datasetId;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getRecordTimestamp() {
        return recordTimestamp;
    }

    public Optional<VigiCruesFields> getFields() {
        VigiCruesFields fields = this.fields;
        if (fields != null) {
            return Optional.of(fields);
        }
        return Optional.empty();
    }
}
