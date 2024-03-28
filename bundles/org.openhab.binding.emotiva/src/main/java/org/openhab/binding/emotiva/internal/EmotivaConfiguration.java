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
package org.openhab.binding.emotiva.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EmotivaConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class EmotivaConfiguration {

    public static final BigDecimal MAX_VOLUME = new BigDecimal("15");

    public String ipAddress = "";
    public int controlPort = 7002;
    public int notifyPort = 7003;
    public int infoPort = 7004;
    public int setupPortTCP = 7100;
    public int menuNotifyPort = 7005;
    public String model = "";
    public String revision = "";
    public String dataRevision = "";
    public String protocolVersion = "2.0";
    public int keepAlive = 7500;
    private BigDecimal mainVolumeMax = MAX_VOLUME;

    public BigDecimal getMainVolumeMax() {
        return mainVolumeMax;
    }

    public void setMainVolumeMax(BigDecimal mainVolumeMax) {
        this.mainVolumeMax = mainVolumeMax;
    }
}
