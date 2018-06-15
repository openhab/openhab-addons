/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

/**
 * The {@link AvailableServices} is responsible for listing all available services.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class AvailableServices {

    private String houseCleaning;
    private String spotCleaning;
    private String manualCleaning;
    private String easyConnect;
    private String schedule;
    private String generalInfo;

    public String getGeneralInfo() {
        return generalInfo;
    }

    public void setGenenralInfo(String generalInfo) {
        this.generalInfo = generalInfo;
    }

    public String getHouseCleaning() {
        return houseCleaning;
    }

    public void setHouseCleaning(String houseCleaning) {
        this.houseCleaning = houseCleaning;
    }

    public String getSpotCleaning() {
        return spotCleaning;
    }

    public void setSpotCleaning(String spotCleaning) {
        this.spotCleaning = spotCleaning;
    }

    public String getManualCleaning() {
        return manualCleaning;
    }

    public void setManualCleaning(String manualCleaning) {
        this.manualCleaning = manualCleaning;
    }

    public String getEasyConnect() {
        return easyConnect;
    }

    public void setEasyConnect(String easyConnect) {
        this.easyConnect = easyConnect;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

}
