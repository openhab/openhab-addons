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
package org.openhab.binding.roborock.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

@NonNullByDefault
public class Login {
    public String code = "";
    public String message = "";
    public @NonNullByDefault({}) LoginData data;

    public class LoginData {
        public String uid = "";
        public String tokentype = "";
        public String token = "";
        public String rruid = "";
        public String region = "";
        public String nickname = "";
        public @NonNullByDefault({}) Rriot rriot;
        public String tuyaDeviceState = "";
        public String avatarurl = "";
    }

    public class Rriot {
        public String u = "";
        public String s = "";
        public String h = "";
        public String k = "";
        public @NonNullByDefault({}) R r;
    }

    public class R {
        public String r = "";
        public String a = "";
        public String m = "";
        public String l = "";
    }

    public Login() {
    }
}
