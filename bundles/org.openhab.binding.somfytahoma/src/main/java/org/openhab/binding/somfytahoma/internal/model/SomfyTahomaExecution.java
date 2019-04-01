/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaExecution} holds information about currently
 * running execution (moving device).
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaExecution {

    private String id = "";
    private SomfyTahomaActionGroup actionGroup = new SomfyTahomaActionGroup();

    public String getId() {
        return id;
    }

    public SomfyTahomaActionGroup getActionGroup() {
        return actionGroup;
    }
}
