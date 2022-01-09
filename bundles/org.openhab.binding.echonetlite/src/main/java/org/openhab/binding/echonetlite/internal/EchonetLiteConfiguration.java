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
package org.openhab.binding.echonetlite.internal;

/**
 * The {@link EchonetLiteConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Barker - Initial contribution
 */
public class EchonetLiteConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String hostname;
    public int port;
    public int groupCode;
    public int classCode;
    public int instance;
    public long pollIntervalMs;
    public long retryTimeoutMs;

    @Override
    public String toString() {
        return "EchonetLiteConfiguration{" + "hostname='" + hostname + '\'' + ", port=" + port + ", groupCode="
                + groupCode + ", classCode=" + classCode + ", instance=" + instance + ", pollIntervalMs="
                + pollIntervalMs + ", retryTimeoutMs=" + retryTimeoutMs + '}';
    }
}
