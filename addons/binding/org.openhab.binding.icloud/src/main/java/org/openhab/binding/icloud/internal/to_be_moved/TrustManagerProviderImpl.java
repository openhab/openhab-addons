/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.to_be_moved;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides a TrustManager for https://fmipmobile.icloud.com
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@Component
@NonNullByDefault
public class TrustManagerProviderImpl implements TrustManagerProvider {
    private final List<EndpointTrustManager> endpointTrustManagers = new CopyOnWriteArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addEndpointTrustManager(EndpointTrustManager endpointTrustManager) {
        endpointTrustManagers.add(endpointTrustManager);
    }

    void removeEndpointTrustManager(EndpointTrustManager endpointTrustManager) {
        endpointTrustManagers.remove(endpointTrustManager);
    }

    @Override
    public Stream<TrustManager> getTrustManagers(String endpoint) {
        return endpointTrustManagers.stream().filter(tm -> tm.supports(endpoint))
                .map(EndpointTrustManager::getTrustManager);
    }

}
