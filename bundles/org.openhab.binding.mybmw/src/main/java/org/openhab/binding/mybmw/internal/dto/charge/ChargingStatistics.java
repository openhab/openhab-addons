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
 * The {@link ChargingStatistics} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactoring
 */
public class ChargingStatistics {
    private int totalEnergyCharged;// ": 173,
    private String totalEnergyChargedSemantics;// ": "Insgesamt circa 173 Kilowattstunden geladen",
    private String symbol;// ": "~",
    private int numberOfChargingSessions;// ": 13,
    private String numberOfChargingSessionsSemantics;// ": "13 Ladevorgänge"

    /**
     * @return the totalEnergyCharged
     */
    public int getTotalEnergyCharged() {
        return totalEnergyCharged;
    }

    /**
     * @param totalEnergyCharged the totalEnergyCharged to set
     */
    public void setTotalEnergyCharged(int totalEnergyCharged) {
        this.totalEnergyCharged = totalEnergyCharged;
    }

    /**
     * @return the totalEnergyChargedSemantics
     */
    public String getTotalEnergyChargedSemantics() {
        return totalEnergyChargedSemantics;
    }

    /**
     * @param totalEnergyChargedSemantics the totalEnergyChargedSemantics to set
     */
    public void setTotalEnergyChargedSemantics(String totalEnergyChargedSemantics) {
        this.totalEnergyChargedSemantics = totalEnergyChargedSemantics;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return the numberOfChargingSessions
     */
    public int getNumberOfChargingSessions() {
        return numberOfChargingSessions;
    }

    /**
     * @param numberOfChargingSessions the numberOfChargingSessions to set
     */
    public void setNumberOfChargingSessions(int numberOfChargingSessions) {
        this.numberOfChargingSessions = numberOfChargingSessions;
    }

    /**
     * @return the numberOfChargingSessionsSemantics
     */
    public String getNumberOfChargingSessionsSemantics() {
        return numberOfChargingSessionsSemantics;
    }

    /**
     * @param numberOfChargingSessionsSemantics the numberOfChargingSessionsSemantics to set
     */
    public void setNumberOfChargingSessionsSemantics(String numberOfChargingSessionsSemantics) {
        this.numberOfChargingSessionsSemantics = numberOfChargingSessionsSemantics;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "ChargingStatistics [totalEnergyCharged=" + totalEnergyCharged + ", totalEnergyChargedSemantics="
                + totalEnergyChargedSemantics + ", symbol=" + symbol + ", numberOfChargingSessions="
                + numberOfChargingSessions + ", numberOfChargingSessionsSemantics=" + numberOfChargingSessionsSemantics
                + "]";
    }
}
