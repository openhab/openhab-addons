/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.to_be_moved;

import java.security.KeyStore;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides a TrustManager for supported endpoints
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public interface EndpointKeyStore {
    /**
     * hostname for which this TrustManager is intended
     *
     * @return
     */
    String getHostName();

    /**
     * A X509ExtendedTrustManager for the specified hostname
     *
     * @return
     */
    KeyStore getKeyStore();
}
