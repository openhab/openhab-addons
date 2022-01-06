/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AMSHandlerConfiguration} is a helper class for the mstx thing handler configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class AMSHandlerConfiguration extends BaseHandlerConfiguration {
    public int digitalRefresh = 10;
}
