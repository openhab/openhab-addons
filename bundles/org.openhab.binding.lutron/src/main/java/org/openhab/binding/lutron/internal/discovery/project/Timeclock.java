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
package org.openhab.binding.lutron.internal.discovery.project;

/**
 * A Timeclock subsystem in a Lutron RadioRA2 or HWQS controller
 *
 * @author Bob Adair - Initial contribution
 */
public class Timeclock {

    private String name;
    private Integer integrationId;

    public String getName() {
        return name;
    }

    public Integer getIntegrationId() {
        return integrationId;
    }
}
