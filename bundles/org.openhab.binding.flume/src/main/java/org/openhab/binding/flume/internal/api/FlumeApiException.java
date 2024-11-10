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
package org.openhab.binding.flume.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link FlumeApiException} exception class for any api exception
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeApiException extends Exception {
    private static final long serialVersionUID = -7050804598914012847L;
    private int code;
    private boolean configurationIssue;

    public FlumeApiException(String message, int code, boolean configurationIssue) {
        super(message);
        this.code = code;
        this.configurationIssue = configurationIssue;
    }

    public int getCode() {
        return code;
    }

    public boolean isConfigurationIssue() {
        return configurationIssue;
    }

    @Override
    public @Nullable String getMessage() {
        return super.getMessage();
    }
}
