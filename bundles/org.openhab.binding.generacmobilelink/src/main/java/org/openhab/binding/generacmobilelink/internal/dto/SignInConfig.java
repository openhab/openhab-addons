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
package org.openhab.binding.generacmobilelink.internal.dto;

import java.util.Map;

/**
 * /**
 * The {@link SignInConfig} represents the SignInConfig object used in login
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SignInConfig {
    public String remoteResource;
    public int retryLimit;
    public boolean trimSpacesInPassword;
    public String api;
    public String csrf;
    public String transId;
    public String pageViewId;
    public boolean suppressElementCss;
    public boolean isPageViewIdSentWithHeader;
    public boolean allowAutoFocusOnPasswordField;
    public int pageMode;
    public Map<String, String> config;
    public Map<String, String> hosts;
    public Locale locale;
    public XhrSettings xhrSettings;

    public class Locale {
        public String lang;
    }

    public class XhrSettings {
        public boolean retryEnabled;
        public int retryMaxAttempts;
        public int retryDelay;
        public int retryExponent;
        public String[] retryOn;
    }
}
