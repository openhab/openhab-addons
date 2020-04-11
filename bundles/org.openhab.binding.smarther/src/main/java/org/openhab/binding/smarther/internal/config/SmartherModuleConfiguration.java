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
package org.openhab.binding.smarther.internal.config;

/**
 * The {@link SmartherModuleConfiguration} is the class used to match the SmartherModuleHandler
 * configuration.
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

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public boolean isSettingsAutoupdate() {
        return settingsAutoupdate;
    }

    public void setSettingsAutoupdate(boolean settingsAutoupdate) {
        this.settingsAutoupdate = settingsAutoupdate;
    }

    public int getProgramsRefreshPeriod() {
        return programsRefreshPeriod;
    }

    public void setProgramsRefreshPeriod(int programsRefreshPeriod) {
        this.programsRefreshPeriod = programsRefreshPeriod;
    }

    public int getNumberOfEndDays() {
        return numberOfEndDays;
    }

    public void setNumberOfEndDays(int numberOfEndDays) {
        this.numberOfEndDays = numberOfEndDays;
    }

    public int getStatusRefreshPeriod() {
        return statusRefreshPeriod;
    }

    public void setStatusRefreshPeriod(int statusRefreshPeriod) {
        this.statusRefreshPeriod = statusRefreshPeriod;
    }

}
