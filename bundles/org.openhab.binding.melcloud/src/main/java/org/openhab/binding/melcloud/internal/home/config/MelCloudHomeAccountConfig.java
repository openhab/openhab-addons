/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal.home.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Config class for the {@code melcloudhomeaccount} bridge.
 *
 * <p>
 * The {@link #username}/{@link #password} pair is used to run the real MELCloud Home login (see ADR-002): the full
 * OAuth 2.0 Authorization Code + PKCE flow, including the Pushed Authorization Request step and the AWS
 * Cognito-federated credential submission, runs entirely server-side — no interactive browser step is required.
 * This supersedes the developer-only {@code accessToken} stopgap from ADR-001.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAccountConfig {

    public String username = "";
    public String password = "";

    @Override
    public String toString() {
        return "[username=" + (username.isEmpty() ? "<empty>" : username) + ", password="
                + (password.isEmpty() ? "<empty>" : "<redacted>") + "]";
    }
}
