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
package org.openhab.binding.wled.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WledApi} is the json Api methods for different firmware versions
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public interface WledApi {
    public abstract void update() throws ApiException;

    public abstract int getFirmwareVersion() throws ApiException;
}
