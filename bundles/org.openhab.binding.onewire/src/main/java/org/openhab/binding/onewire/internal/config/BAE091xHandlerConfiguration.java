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
package org.openhab.binding.onewire.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwBindingConstants;

/**
 * The {@link BAE091xHandlerConfiguration} is a helper class for the BAE091x thing handler configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BAE091xHandlerConfiguration extends BaseHandlerConfiguration {
    public String pin1 = OwBindingConstants.CONFIG_BAE_PIN_DISABLED;
    public String pin2 = OwBindingConstants.CONFIG_BAE_PIN_DISABLED;
    public String pin6 = OwBindingConstants.CONFIG_BAE_PIN_DISABLED;
    public String pin7 = OwBindingConstants.CONFIG_BAE_PIN_DISABLED;
    public String pin8 = OwBindingConstants.CONFIG_BAE_PIN_DISABLED;
}
