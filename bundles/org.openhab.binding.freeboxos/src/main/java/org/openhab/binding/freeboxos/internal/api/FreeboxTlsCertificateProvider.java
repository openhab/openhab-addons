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
package org.openhab.binding.freeboxos.internal.api;

<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsCertificateProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a CertificateManager for the Freebox SSL certificate
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component
@NonNullByDefault
public class FreeboxTlsCertificateProvider implements TlsCertificateProvider {

    private static final String CERTIFICATE_NAME = "freeboxECCRootCA.crt";

    public static final String DEFAULT_NAME = "mafreebox.freebox.fr";

    @Override
    public String getHostName() {
        return DEFAULT_NAME;
    }

    @Override
    public URL getCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(CERTIFICATE_NAME);
        if (resource != null) {
            return resource;
        }
        throw new IllegalStateException("Certificate '%s' not found or not accessible".formatted(CERTIFICATE_NAME));
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
package org.openhab.binding.freeboxos.internal.api;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.DEFAULT_FREEBOX_NAME;
=======
import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.DEFAULT_FREEBOX_NAME;
>>>>>>> e4ef5cc Switching to Java 17 records

=======
>>>>>>> a6d34ed Adding IliadBox compatibility
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsCertificateProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a CertificateManager for the Freebox SSL certificate
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component
@NonNullByDefault
public class FreeboxTlsCertificateProvider implements TlsCertificateProvider {

    private static final String CERTIFICATE_NAME = "freeboxECCRootCA.crt";

    public static final String DEFAULT_NAME = "mafreebox.freebox.fr";

    @Override
    public String getHostName() {
        return DEFAULT_NAME;
    }

    @Override
    public URL getCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(CERTIFICATE_NAME);
        if (resource != null) {
            return resource;
        }
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
        throw new IllegalStateException("Certifcate resource not found or not accessible");
>>>>>>> 46dadb1 SAT warnings handling
=======
        throw new IllegalStateException("Certificate resource not found or not accessible");
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
=======
        throw new IllegalStateException("Certificate '%s' not found or not accessible".formatted(CERTIFICATE_NAME));
>>>>>>> a6d34ed Adding IliadBox compatibility
    }
}
