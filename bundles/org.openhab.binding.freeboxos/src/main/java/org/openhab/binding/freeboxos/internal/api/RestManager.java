/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;

/**
 * Base class for all various rest managers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RestManager {
    // public static Permission associatedPermission() {
    // return Permission.SETTINGS;
    // }

    protected final ApiHandler apiHandler;

    public RestManager(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }
}
