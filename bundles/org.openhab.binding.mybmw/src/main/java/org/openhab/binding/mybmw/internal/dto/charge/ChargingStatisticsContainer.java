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
package org.openhab.binding.mybmw.internal.dto.charge;

/**
 * The {@link ChargingStatisticsContainer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChargingStatisticsContainer {
    private String description;// ": "Dezember 2021",
    private String optStateType;// ": "OPT_IN_WITH_SESSIONS",
    private ChargingStatistics statistics;// ": {

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the optStateType
     */
    public String getOptStateType() {
        return optStateType;
    }

    /**
     * @param optStateType the optStateType to set
     */
    public void setOptStateType(String optStateType) {
        this.optStateType = optStateType;
    }

    /**
     * @return the statistics
     */
    public ChargingStatistics getStatistics() {
        return statistics;
    }

    /**
     * @param statistics the statistics to set
     */
    public void setStatistics(ChargingStatistics statistics) {
        this.statistics = statistics;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "ChargingStatisticsContainer [description=" + description + ", optStateType=" + optStateType
                + ", statistics=" + statistics + "]";
    }
}
