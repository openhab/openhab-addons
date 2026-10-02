/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RouterosKidData} is a model class for the RouterOS IP Kid Control and used as to bridge thing property
 * values.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RouterosKidData extends RouterosBaseData {
    public RouterosKidData(Map<String, String> props) {
        super(props);
    }

    public boolean isEnabled() {
        return "false".equals(getProp("disabled", ""));
    }

    public boolean isPaused() {
        return "true".equals(getProp("paused", ""));
    }
}
