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
package org.openhab.binding.mail.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link POP3IMAPContentChannelConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class POP3IMAPContentChannelConfig {
    public @Nullable String folder;
    public String subject = "";
    public String sender = "";
    public @Nullable String transformation;

    public boolean markAsRead = false;
}
