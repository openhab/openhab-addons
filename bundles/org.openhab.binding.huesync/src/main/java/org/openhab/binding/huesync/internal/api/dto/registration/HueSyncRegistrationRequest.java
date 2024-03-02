/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.api.dto.registration;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
public class HueSyncRegistrationRequest {
    /** User recognizable name of registered application */
    public String appName;
    /** User recognizable name of application instance. */
    public String instanceName;
}
