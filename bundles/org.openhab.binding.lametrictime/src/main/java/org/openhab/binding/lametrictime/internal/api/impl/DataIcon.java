/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation class for data icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class DataIcon extends AbstractDataIcon {
    public DataIcon(String mimeType, byte[] data) {
        setType(mimeType);
        setData(data);
    }

    @Override
    protected void configure() {
        // noop
    }

    @Override
    protected void populateFields() {
        // noop
    }
}
