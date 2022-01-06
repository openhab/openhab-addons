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
package org.openhab.binding.miele.internal;

/**
 * The {@link FullyQualifiedApplianceIdentifier} class represents a fully qualified appliance identifier.
 * Example: "hdm:ZigBee:0123456789abcdef#210"
 *
 * @author Jacob Laursen - Initial contribution
 */
public class FullyQualifiedApplianceIdentifier {
    private String uid;
    private String protocol;
    private String applianceId;

    public FullyQualifiedApplianceIdentifier(String uid) {
        this.uid = uid;

        int separatorPosition = this.uid.lastIndexOf(':') + 1;
        this.protocol = uid.substring(0, separatorPosition);
        this.applianceId = uid.substring(separatorPosition);
    }

    public FullyQualifiedApplianceIdentifier(String applianceId, String protocol) {
        this.uid = protocol + applianceId;
        this.protocol = protocol;
        this.applianceId = applianceId;
    }

    /**
     * @return UID of appliance (e.g. "hdm:ZigBee:0123456789abcdef#210")
     */
    public String getUid() {
        return this.uid;
    }

    /**
     * @return Appliance ID without protocol adapter information (e.g. "0123456789abcdef#210")
     */
    public String getApplianceId() {
        return this.applianceId;
    }

    public String getId() {
        return this.getApplianceId().replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /**
     * @return Protocol prefix of fully qualified appliance identifier (e.g. "hdmi:ZigBee:"")
     */
    public String getProtocol() {
        return this.protocol;
    }
}
