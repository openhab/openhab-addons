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
package org.openhab.binding.foobot.internal.json;

import java.util.List;

/**
 * The {@link FoobotJsonData} is responsible for storing the "datapoints" from the foobot.io JSON response
 *
 * @author Divya Chauhan - Initial contribution
 */
public class FoobotJsonData {

    private String uuid;
    private long start;
    private long end;
    private List<String> sensors;
    private List<String> units;
    private List<List<String>> datapoints;

    public String getUuid() {
        return uuid;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public List<String> getSensors() {
        return sensors;
    }

    public List<String> getUnits() {
        return units;
    }

    public List<List<String>> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<List<String>> datapoints) {
        this.datapoints = datapoints;
    }
}
