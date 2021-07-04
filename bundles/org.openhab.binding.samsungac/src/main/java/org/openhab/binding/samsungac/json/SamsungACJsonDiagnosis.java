/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.json;

/**
 *
 * The {@link SamsungACJsonDevices} class defines the Alarm Structure Samsung Digital Inverter
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

public class SamsungACJsonDiagnosis {
    private String diagnosisStart;

    /**
     * @return the diagnosisStart
     */
    public String getDiagnosisStart() {
        return diagnosisStart;
    }

    /**
     * @param diagnosisStart the diagnosisStart to set
     */
    public void setDiagnosisStart(String diagnosisStart) {
        this.diagnosisStart = diagnosisStart;
    }

    public SamsungACJsonDiagnosis() {
    }
}
