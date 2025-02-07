/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link SenseEnergyApiException} exception class for any api exception
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyApiException extends Exception {
    private static final long serialVersionUID = -7059398508028583720L;
    private final boolean configurationIssue;

    public SenseEnergyApiException(String message, boolean configurationIssue) {
        super(message);
        this.configurationIssue = configurationIssue;
    }

    public boolean isConfigurationIssue() {
        return configurationIssue;
    }

    @Override
    public String toString() {
        return String.format("SenseEnergyApiException{message='%s', configurationIssue=%b}", getMessage(),
                configurationIssue);
    }
}
