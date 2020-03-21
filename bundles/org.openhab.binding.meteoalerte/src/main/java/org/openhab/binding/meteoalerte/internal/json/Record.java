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
package org.openhab.binding.meteoalerte.internal.json;

/**
 * The {@link Record} is the Java class used to map the JSON
 * response to the webservice request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Record {
    private String datasetid;
    private String recordid;
    private String record_timestamp;
    private Fields fields;

    public String getDatasetDd() {
        return datasetid;
    }

    public String getRecordDd() {
        return recordid;
    }

    public String getRecordTimestamp() {
        return record_timestamp;
    }

    public Fields getFields() {
        return fields;
    }
}
