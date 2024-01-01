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
 * The {@link ChargingSession} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChargingSession {
    private String id;// ": "2021-12-26T16:57:20Z_128fa4af",
    private String title;// ": "Gestern 17:57",
    private String subtitle;// ": "Uferstraße 4B • 7h 45min • -- EUR",
    private String energyCharged;// ": "~ 31 kWh",
    private String sessionStatus;// ": "FINISHED",
    private String issues;// ": "2 Probleme",
    private String isPublic;// ": false

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @return the energyCharged
     */
    public String getEnergyCharged() {
        return energyCharged;
    }

    /**
     * @return the sessionStatus
     */
    public String getSessionStatus() {
        return sessionStatus;
    }

    /**
     * @return the issues
     */
    public String getIssues() {
        return issues;
    }

    /**
     * @return the isPublic
     */
    public String getIsPublic() {
        return isPublic;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "ChargingSession [id=" + id + ", title=" + title + ", subtitle=" + subtitle + ", energyCharged="
                + energyCharged + ", sessionStatus=" + sessionStatus + ", issues=" + issues + ", isPublic=" + isPublic
                + "]";
    }
}
