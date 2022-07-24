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
package org.openhab.binding.mybmw.internal.dto.status;

/**
 * The {@link CBSMessage} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CBSMessage {
    public String id;// ": "BrakeFluid",
    public String title;// ": "Brake fluid",
    public int iconId;// ": 60223,
    public String longDescription;// ": "Next service due by the specified date.",
    public String subtitle;// ": "Due in November 2023",
    public String criticalness;// ": "nonCritical"
}
