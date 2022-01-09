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
package org.openhab.binding.mybmw.internal.dto.charge;

/**
 * The {@link ChargeSession} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class ChargeSession {
    public String id;// ": "2021-12-26T16:57:20Z_128fa4af",
    public String title;// ": "Gestern 17:57",
    public String subtitle;// ": "Uferstraße 4B • 7h 45min • -- EUR",
    public String energyCharged;// ": "~ 31 kWh",
    public String sessionStatus;// ": "FINISHED",
    public String issues;// ": "2 Probleme",
    public String isPublic;// ": false
}
