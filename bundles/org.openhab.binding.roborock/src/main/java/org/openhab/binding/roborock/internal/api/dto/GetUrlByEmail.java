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
package org.openhab.binding.roborock.internal.api;

/**
 * Class for holding the set of parameters used by the GetUrlByEmail response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

public class GetUrlByEmail {
    public String code = "";
    public String message = "";
    public GetUrlByEmailData data;

    public class GetUrlByEmailData {
        public String url = "";
        public String countrycode = "";
        public String country = "";
    }

    public GetUrlByEmail() {
    }
}
