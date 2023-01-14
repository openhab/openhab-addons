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
package org.openhab.binding.tr064.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * The{@link ChannelConfigException} is a catched Exception that is thrown during channel configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ChannelConfigException extends Exception {
    private static final long serialVersionUID = 1L;

    public ChannelConfigException(String message) {
        super(message);
    }
}
