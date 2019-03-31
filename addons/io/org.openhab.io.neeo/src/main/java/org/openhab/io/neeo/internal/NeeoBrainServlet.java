/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.models.BrainStatus;
import org.openhab.io.neeo.internal.servletservices.NeeoBrainSearchService;
import org.openhab.io.neeo.internal.servletservices.NeeoBrainService;

/**
 * This implementation of {@link AbstractServlet} will handle any requests from the NEEO Brain. The brain will ask for
 * any search results (performed by {@link NeeoBrainSearchService}) and requests state values, set
 * state values (performed by {@Link NeeoBrainService})
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoBrainServlet extends AbstractServlet {

    /** The serial UID */
    private static final long serialVersionUID = 4697853008321468041L;

    /** The NEEO API to use. Can be null if not a NEEO servlet */
    private final NeeoApi api;

    /**
     * Create a servlet to handle integration duties with the NEEO brain
     *
     * @param context the non-null service context
     * @param servletUrl the non-null, non-empty servlet URL
     * @param api the non-null API
     */
    private NeeoBrainServlet(ServiceContext context, String servletUrl, NeeoApi api) {
        super(context, servletUrl, new NeeoBrainSearchService(context), new NeeoBrainService(api, context));

        Objects.requireNonNull(context, "context cannot be null");
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be empty");
        Objects.requireNonNull(api, "api cannot be null");

        this.api = api;
    }

    /**
     * Creates the servlet from the context, URL and ID/IpAddress of the brain
     *
     * @param context the non-null service context
     * @param servletUrl the non-null, non-empty servlet URL
     * @param brainId the non-null, non-empty brainID
     * @param ipAddress the non-null, IP Address
     * @return a non-null brain servlet
     * @throws IOException when an exception occurs contacting the brain
     */
    public static NeeoBrainServlet create(ServiceContext context, String servletUrl, String brainId,
            InetAddress ipAddress) throws IOException {
        Objects.requireNonNull(context, "context cannot be null");
        NeeoUtil.requireNotEmpty(servletUrl, "servletUrl cannot be empty");
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(ipAddress, "ipAddress cannot be null");

        final NeeoApi api = new NeeoApi(ipAddress.getHostAddress(), brainId, context);
        api.start();

        return new NeeoBrainServlet(context, servletUrl, api);
    }

    /**
     * Returns the status of the brain
     *
     * @return a non-null {@link BrainStatus}
     */
    public BrainStatus getBrainStatus() {
        return new BrainStatus(api.getBrainId(), api.getBrainName(), api.getBrainUrl(),
                NeeoUtil.getServletUrl(api.getBrainId()), api.getSystemInfo().getFirmwareVersion(), api.isConnected());
    }

    /**
     * Returns the {@link NeeoApi} related to the brain
     *
     * @return a {@link NeeoApi}
     */
    public NeeoApi getBrainApi() {
        return api;
    }

    /**
     * Returns the device keys used by the brain
     *
     * @return a non-null {@link NeeoDeviceKeys}
     */
    public NeeoDeviceKeys getDeviceKeys() {
        return api.getDeviceKeys();
    }

    /**
     * Simply closes the API and each of the services
     */
    @Override
    public void close() {
        super.close();
        NeeoUtil.close(api);
    }
}
