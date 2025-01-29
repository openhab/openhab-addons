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

import java.util.Hashtable;

import com.google.gson.annotations.SerializedName;

/**
 * Data object for Smartthings capabilities description
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class SmartthingsProperty {
    public String type;
    public int minimum;
    public int maximum;
    public String title;

    @SerializedName("enum")
    public String[] enumeration;

    public Hashtable<String, SmartthingsProperty> properties;
}
