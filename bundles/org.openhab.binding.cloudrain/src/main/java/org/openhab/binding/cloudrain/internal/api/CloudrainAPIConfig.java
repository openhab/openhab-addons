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
package org.openhab.binding.cloudrain.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link CloudrainAPIConfig} defines the configuration parameters expected by the CloudrainAPI.
 *
 * @author Till Koellmann - Initial contribution
 *
 */
@NonNullByDefault
public interface CloudrainAPIConfig {

    /**
     * Returns the connection timeout to the Cloudrain system.
     */
    public Integer getConnectionTimeout();

    /**
     * Returns the mode for operation. True for test mode.
     */
    public Boolean getTestMode();
}
