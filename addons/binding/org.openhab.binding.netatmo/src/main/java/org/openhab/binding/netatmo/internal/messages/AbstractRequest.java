/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import static org.apache.commons.httpclient.util.URIUtil.encodeQuery;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhab.binding.netatmo.internal.NetatmoException;

/**
 * Base class for all Netatmo API requests.
 *
 * @author Andreas Brenk - Initial OH1 version
 * @author Gaël L'hopital - port to OH2
 *
 */
public abstract class AbstractRequest extends AbstractMessage {

    protected static final String API_BASE_URL = "https://api.netatmo.net/";
    private String resourceUrl;

    public AbstractRequest(String resourceUrl) {
        super();
        assert resourceUrl != null : "resourceUrl must not be null!";
        this.resourceUrl = resourceUrl;
    }

    protected StringBuilder getUrlBuilder() {
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL);
        urlBuilder.append(resourceUrl);

        return urlBuilder;
    }

    public String prepare() {
        String url = getUrlBuilder().toString();

        try {
            return encodeQuery(url);
        } catch (final URIException e) {
            throw new NetatmoException("Could not prepare " + resourceUrl + " request!", e);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder builder = createToStringBuilder();
        builder.appendSuper(super.toString());
        builder.append("resourceUrl", this.resourceUrl);
        return builder.toString();
    }

}
