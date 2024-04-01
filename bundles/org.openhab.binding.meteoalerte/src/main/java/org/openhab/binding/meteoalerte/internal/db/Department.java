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
package org.openhab.binding.meteoalerte.internal.db;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Department} is a DTO for departments.json database.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class Department {
    public String id = "";
    public String name = "";
    public double northestLat;
    public double southestLat;
    public double eastestLon;
    public double westestLon;
    public String shape = "";
}
