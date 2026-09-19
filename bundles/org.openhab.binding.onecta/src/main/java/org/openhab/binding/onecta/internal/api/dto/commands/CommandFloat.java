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
package org.openhab.binding.onecta.internal.api.dto.commands;

/**
 * @author Alexander Drent - Initial contribution
 */
public class CommandFloat {
    public float value;
    public String path;

    public CommandFloat(float value) {
        this.value = value;
    }

    public CommandFloat(float value, String path) {
        this.value = value;
        this.path = path;
    }
}
