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
package org.openhab.binding.linky.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AuthData} holds authentication information
 *
 * @author Gaël L'hopital - Initial contribution
 */

public class AuthData {
    public class AuthDataCallBack {
        public class NameValuePair {
            public String name;
            public Object value;

            public @Nullable String valueAsString() {
                if (value instanceof String) {
                    return (String) value;
                }
                return null;
            }
        }

        public String type;

        public List<NameValuePair> output = new ArrayList<>();
        public List<NameValuePair> input = new ArrayList<>();
    }

    public String authId;
    public String template;
    public String stage;
    public String header;
    public List<AuthDataCallBack> callbacks = new ArrayList<>();
}
