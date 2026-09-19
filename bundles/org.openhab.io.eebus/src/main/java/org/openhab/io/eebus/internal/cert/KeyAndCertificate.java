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
package org.openhab.io.eebus.internal.cert;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A private key paired with its self-signed certificate, independent of jEEBus's own
 * {@code CertificateInfo} type so callers generating a certificate don't need that dependency.
 *
 * @author openHAB EEBus Binding Contributors - Initial contribution
 */
@NonNullByDefault
public record KeyAndCertificate(PrivateKey privateKey, X509Certificate certificate) {
}
