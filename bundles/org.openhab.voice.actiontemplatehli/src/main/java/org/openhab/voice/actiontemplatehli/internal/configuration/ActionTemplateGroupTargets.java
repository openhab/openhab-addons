/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.voice.actiontemplatehli.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ActionTemplateGroupTargets} class filters the item targets when targeting an item group.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateGroupTargets {
    @JsonProperty("itemName")
    public String itemName = "";
    @JsonProperty("itemType")
    public String itemType = "";
    @JsonProperty("requiredTags")
    public String[] requiredItemTags = new String[] {};
    @JsonProperty("mergeState")
    public boolean mergeState = false;
    @JsonProperty("recursive")
    public boolean recursive = true;
}
