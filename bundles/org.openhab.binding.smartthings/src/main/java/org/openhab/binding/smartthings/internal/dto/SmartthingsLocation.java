/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings app description
 *
 * @author Laurent ARNAL - Initial contribution
 */

public class SmartthingsLocation {
    public String locationId;
    public String name;
    public String[] allowed;
    public String countryCode;
    public String latitude;
    public String longitude;
    public int regionRadius;
    public String temperatureScale;
    public String timeZoneId;
    public String locale;
    public String backgroundImage;
    public String created;
    public String lastModified;

    public record parent(String type, String id) {
    }

    public parent parent;
}
