/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.xmppclient.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link XMPPClientConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Pavel Gololobov - Initial contribution
 */
@NonNullByDefault
public class XMPPClientConfiguration {
    public @Nullable String host;
    public Integer port = 5222;
    public String username = "";
    public String password = "";
    public String domain = "";
}
