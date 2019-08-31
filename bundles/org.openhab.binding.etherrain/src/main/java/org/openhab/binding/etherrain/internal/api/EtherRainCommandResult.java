/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.etherrain.internal.api;

/**
 * The {@link EtherRainCommandResult} is the response packet for Command Result
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public enum EtherRainCommandResult {
    RESULT_OK("OK"),
    RESULT_INTERRUPTED_RAIN("RN"),
    RESULT_INTERUPPTED_SHORT("SH"),
    RESULT_INCOMPLETE("NC");

    protected String result;

    EtherRainCommandResult(String result) {
        this.result = result;
    }

    public static EtherRainCommandResult fromString(String text) {
        for (EtherRainCommandResult b : EtherRainCommandResult.values()) {
            if (b.result.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
