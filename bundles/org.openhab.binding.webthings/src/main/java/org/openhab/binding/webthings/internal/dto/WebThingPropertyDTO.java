/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.dto;

/**
 * The {@link WebThingPropertyDTO}
 *
 * @author Sven Schneider - Initial contribution
 */
public class WebThingPropertyDTO {
    private String type;
    private String title;
    private String description;
    private long minimum;
    private long maximum;
    private String unit;

    public String getType() { return type; }
    public void setType(String value) { this.type = value; }

    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public long getMinimum() { return minimum; }
    public void setMinimum(long value) { this.minimum = value; }

    public long getMaximum() { return maximum; }
    public void setMaximum(long value) { this.maximum = value; }

    public String getUnit() { return unit; }
    public void setUnit(String value) { this.unit = value; } 
}
