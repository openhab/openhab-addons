/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mail.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link POP3IMAPConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public class POP3IMAPConfig extends BaseConfig {
    public int refresh = 60;
    
    public String debugPrint() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Configuration details:\r\n");
        buffer.append("  Hostname = " + hostname + "\r\n");
        buffer.append("      Port = " + port + "\r\n");
        buffer.append("  Username = " + username + "\r\n");
        buffer.append("  Password = (redacted)\r\n");
        buffer.append("  Security = " + security + "\r\n");
        buffer.append("   Refresh = " + refresh + "\r\n");
        return buffer.toString();
    }
}
