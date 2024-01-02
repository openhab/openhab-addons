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
package org.openhab.binding.bticinosmarther.internal.config;

/**
 * The {@code SmartherModuleConfiguration} class defines the internal configuration of a {@code SmartherModuleHandler}
 * instance.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class SmartherModuleConfiguration {

    private String plantId;
    private String moduleId;
    private boolean settingsAutoupdate;
    private int programsRefreshPeriod;
    private int numberOfEndDays;
    private int statusRefreshPeriod;

    /**
     * Returns the location plant identifier.
     *
     * @return a string containing the plant identifier
     */
    public String getPlantId() {
        return plantId;
    }

    /**
     * Sets the location plant identifier.
     *
     * @param plantId
     *            the new plant identifier
     */
    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    /**
     * Returns the chronothermostat module identifier.
     *
     * @return a string containing the module identifier
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Sets the chronothermostat module identifier.
     *
     * @param moduleId
     *            the new module identifier
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * Tells whether the Module settings are updated with its status.
     *
     * @return {@code true} if the settings are updated whenever the module status is updated, {@code false} if the
     *         settings are updated only upon module initialization
     */
    public boolean isSettingsAutoupdate() {
        return settingsAutoupdate;
    }

    /**
     * Sets whether the Module settings are updated with its status.
     *
     * @param settingsAutoupdate
     *            {@code true} if the settings are updated whenever the module status is updated, {@code false} if the
     *            settings are updated only upon module initialization
     */
    public void setSettingsAutoupdate(boolean settingsAutoupdate) {
        this.settingsAutoupdate = settingsAutoupdate;
    }

    /**
     * Returns the automatic mode programs refresh period (in hours).
     *
     * @return the automatic mode programs refresh period
     */
    public int getProgramsRefreshPeriod() {
        return programsRefreshPeriod;
    }

    /**
     * Sets the automatic mode programs refresh period (in hours).
     *
     * @param programsRefreshPeriod
     *            the new automatic mode programs refresh period
     */
    public void setProgramsRefreshPeriod(int programsRefreshPeriod) {
        this.programsRefreshPeriod = programsRefreshPeriod;
    }

    /**
     * Returns the number of end days to be displayed in manual mode.
     *
     * @return the number of end days to be displayed
     */
    public int getNumberOfEndDays() {
        return numberOfEndDays;
    }

    /**
     * Sets the number of end days to be displayed in manual mode.
     *
     * @param numberOfEndDays
     *            the new number of end days to be displayed
     */
    public void setNumberOfEndDays(int numberOfEndDays) {
        this.numberOfEndDays = numberOfEndDays;
    }

    /**
     * Returns the Module status refresh period (in minutes).
     *
     * @return the Module status refresh period
     */
    public int getStatusRefreshPeriod() {
        return statusRefreshPeriod;
    }

    /**
     * Sets the Module status refresh period (in minutes).
     *
     * @param statusRefreshPeriod
     *            the new Module status refresh period
     */
    public void setStatusRefreshPeriod(int statusRefreshPeriod) {
        this.statusRefreshPeriod = statusRefreshPeriod;
    }
}
