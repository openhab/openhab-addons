/**
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.config;

<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxTlsCertificateProvider;

/**
 * The {@link FreeboxOsConfiguration} is responsible for holding configuration informations needed to access the Freebox
 * API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsConfiguration {
    public static final String API_DOMAIN = "apiDomain";
    public static final String APP_TOKEN = "appToken";
    public static final String HTTPS_PORT = "httpsPort";
    public static final String HTTPS_AVAILABLE = "httpsAvailable";

    private String apiDomain = FreeboxTlsCertificateProvider.DEFAULT_NAME;
    public String appToken = "";
    public boolean discoverNetDevice;

    private int httpsPort = 15682;
    private boolean httpsAvailable;

    private String getScheme() {
        return httpsAvailable ? "https" : "http";
    }

    private int getPort() {
        return httpsAvailable ? httpsPort : 80;
    }

    public UriBuilder getUriBuilder(String path) {
        return UriBuilder.fromPath("/").scheme(getScheme()).port(getPort()).host(apiDomain).path(path).clone();
=======
 * Copyright (c) 2010-2022 Contributors to the openHAB project
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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
package org.openhab.binding.freeboxos.internal.config;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.DEFAULT_FREEBOX_NAME;
=======
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.DEFAULT_FREEBOX_NAME;
>>>>>>> e4ef5cc Switching to Java 17 records

=======
>>>>>>> a6d34ed Adding IliadBox compatibility
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxTlsCertificateProvider;

/**
 * The {@link FreeboxOsConfiguration} is responsible for holding configuration informations needed to access the Freebox
 * API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsConfiguration {
    public static final String API_DOMAIN = "apiDomain";
    public static final String APP_TOKEN = "appToken";
    public static final String HTTPS_PORT = "httpsPort";
    public static final String HTTPS_AVAILABLE = "httpsAvailable";

    private String apiDomain = FreeboxTlsCertificateProvider.DEFAULT_NAME;
    public String appToken = "";
    public boolean discoverNetDevice;

    private int httpsPort = 15682;
    private boolean httpsAvailable;

    private String getScheme() {
        return httpsAvailable ? "https" : "http";
    }

    private int getPort() {
        return httpsAvailable ? httpsPort : 80;
>>>>>>> 46dadb1 SAT warnings handling
    }

    public UriBuilder getUriBuilder(String path) {
        return UriBuilder.fromPath("/").scheme(getScheme()).port(getPort()).host(apiDomain).path(path).clone();
    }
}
