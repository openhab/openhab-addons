/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.to_be_moved;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides a TrustManager for supported endpoints
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public interface EndpointTrustManager {
    /**
     * Does this class support the given endpoint
     *
     * @param endpoint the endpoint for which a TrustManager was requested
     * @return whether this implementation supports the given endpoints
     */
    boolean supports(String endpoint);

    /**
     * A TrustManager should always be returned if supports returned true
     *
     * @return
     */
    TrustManager getTrustManager();
}
