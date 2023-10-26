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
package org.openhab.binding.freeboxos.internal.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.ui.icon.AbstractResourceIconProvider;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxOsIconProvider} delivers icons provided by FreeboxOS
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { IconProvider.class })
public class FreeboxOsIconProvider extends AbstractResourceIconProvider {

    private final Logger logger = LoggerFactory.getLogger(FreeboxOsIconProvider.class);
    private static final int REQUEST_TIMEOUT_MS = 8000;

    private final HttpClient httpClient;
    private final UriBuilder uriBuilder;

    @Activate
    public FreeboxOsIconProvider(final @Reference TranslationProvider i18nProvider,
            final @Reference HttpClientFactory httpClientFactory) {
        super(i18nProvider);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.uriBuilder = UriBuilder.fromPath("/").scheme("http").host(FreeboxTlsCertificateProvider.DEFAULT_NAME)
                .path("resources/images/home/pictos");
    }

    @Override
    public Set<IconSet> getIconSets(@Nullable Locale locale) {
        return Set.of();
    }

    @Override
    protected Integer getPriority() {
        return 4;
    }

    @Override
    protected @Nullable InputStream getResource(String iconSetId, String resourceName) {
        URI uri = uriBuilder.clone().path(resourceName).build();
        Request request = httpClient.newRequest(uri).method(HttpMethod.GET).timeout(REQUEST_TIMEOUT_MS,
                TimeUnit.MILLISECONDS);

        try {
            ContentResponse response = request.send();
            if (HttpStatus.getCode(response.getStatus()) == Code.OK) {
                return new ByteArrayInputStream(response.getContent());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting icon {}: {}", resourceName, e.getMessage());
        }
        return null;
    }

    @Override
    protected boolean hasResource(String iconSetId, String resourceName) {
        return resourceName.contains(".png") && getResource(iconSetId, resourceName) != null;
    }
}
