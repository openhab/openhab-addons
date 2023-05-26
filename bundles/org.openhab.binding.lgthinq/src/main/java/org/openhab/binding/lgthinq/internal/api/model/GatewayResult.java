/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayResult} class
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class GatewayResult extends HeaderResult {
    private final String rtiUri;
    private final String thinq1Uri;
    private final String thinq2Uri;
    private final String empUri;
    private final String empTermsUri;
    private final String oauthUri;
    private final String empSpxUri;

    public GatewayResult(String resultCode, String resultMessage, String rtiUri, String thinq1Uri, String thinq2Uri,
            String empUri, String empTermsUri, String oauthUri, String empSpxUri) {
        super(resultCode, resultMessage);
        this.rtiUri = rtiUri;
        this.thinq1Uri = thinq1Uri;
        this.thinq2Uri = thinq2Uri;
        this.empUri = empUri;
        this.empTermsUri = empTermsUri;
        this.oauthUri = oauthUri;
        this.empSpxUri = empSpxUri;
    }

    public String getRtiUri() {
        return rtiUri;
    }

    public String getEmpTermsUri() {
        return empTermsUri;
    }

    public String getEmpSpxUri() {
        return empSpxUri;
    }

    public String getThinq1Uri() {
        return thinq1Uri;
    }

    public String getThinq2Uri() {
        return thinq2Uri;
    }

    public String getEmpUri() {
        return empUri;
    }

    public String getOauthUri() {
        return oauthUri;
    }
}
