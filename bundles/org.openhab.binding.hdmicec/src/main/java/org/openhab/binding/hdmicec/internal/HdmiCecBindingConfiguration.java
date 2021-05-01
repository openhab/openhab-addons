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
package org.openhab.binding.hdmicec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HdmiCecBindingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author David Masshardt - Initial contribution
 * @author Sam Spencer - Conversion to OH3 and submission
 */

@NonNullByDefault
public class HdmiCecBindingConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    @Nullable
    public String deviceIndex;
    @Nullable
    public String address;
}
