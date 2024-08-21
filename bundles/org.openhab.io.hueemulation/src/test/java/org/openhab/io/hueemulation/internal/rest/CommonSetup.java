/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.rest.mocks.ConfigStoreWithoutMetadata;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyMetadataRegistry;
import org.openhab.io.hueemulation.internal.rest.mocks.DummyUsersStorage;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * We have no OSGi framework in the background. This class resolves
 * dependencies between the different classes and mocks common services like the configAdmin.
 * <p>
 * The {@link UserManagement} rest components is always
 * setup and started in this common test setup, because all other rest components require
 * user authentication.
 *
 * @author David Graeff - Initial contribution
 */
public class CommonSetup {

    public String basePath;
    public HttpClient client;
    public ConfigStore cs;
    public HttpServer server;

    UserManagement userManagement;

    AutoCloseable mocksCloseable;

    @Mock
    EventPublisher eventPublisher;

    @Mock
    ConfigurationAdmin configAdmin;

    @Mock
    ScheduledExecutorService scheduler;

    @Mock
    org.osgi.service.cm.Configuration configAdminConfig;

    @Mock
    NetworkAddressService networkAddressService;

    MetadataRegistry metadataRegistry = new DummyMetadataRegistry();

    StorageService storageService = new StorageService() {
        @Override
        public <T> Storage<T> getStorage(String name, ClassLoader classLoader) {
            return getStorage(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Storage<T> getStorage(String name) {
            if ("hueEmulationUsers".equals(name)) {
                return (Storage<T>) new DummyUsersStorage();
            }
            throw new IllegalStateException();
        }
    };

    public CommonSetup(boolean withMetadata) throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(configAdmin.getConfiguration(anyString())).thenReturn(configAdminConfig);
        when(configAdmin.getConfiguration(anyString(), any())).thenReturn(configAdminConfig);
        Dictionary<String, Object> mockProperties = new Hashtable<>();
        when(configAdminConfig.getProperties()).thenReturn(mockProperties);
        when(networkAddressService.getPrimaryIpv4HostAddress()).thenReturn("127.0.0.1");

        // If anything is scheduled, immediately run it instead
        when(scheduler.schedule(any(Runnable.class), anyLong(), any())).thenAnswer(answer -> {
            ((Runnable) answer.getArgument(0)).run();
            return null;
        });

        if (withMetadata) {
            cs = new ConfigStore(networkAddressService, configAdmin, metadataRegistry, scheduler);
        } else {
            cs = new ConfigStoreWithoutMetadata(networkAddressService, configAdmin, scheduler);
        }
        cs.activate(Map.of("uuid", "a668dc9b-7172-49c3-832f-acb07dda2a20"));
        cs.switchFilter = Set.of("Switchable");
        cs.whiteFilter = Set.of("Switchable");
        cs.colorFilter = Set.of("ColorLighting");

        userManagement = Mockito.spy(new UserManagement(storageService, cs));

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(0));
            basePath = "http://localhost:" + serverSocket.getLocalPort() + "/api";
        }
    }

    /**
     * Start the http server to serve all registered jax-rs resources. Also setup a client for testing, see
     * {@link #client}.
     *
     * @param rc A resource config. Add objects and object instance resources to your needs. Example:
     *            "new ResourceConfig().registerInstances(configurationAccess)"
     */
    public void start(ResourceConfig resourceConfig) {
        ResourceConfig rc = resourceConfig.registerInstances(userManagement).register(new LoggingFeature(
                Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.OFF, Verbosity.HEADERS_ONLY, 10));

        Logger log2 = Logger.getLogger("org.glassfish");
        log2.setLevel(Level.OFF);

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(basePath), rc);
        client = new HttpClient();
        try {
            client.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start HttpClient", e);
        }
    }

    public void dispose() throws Exception {
        if (client != null) {
            client.stop();
        }
        if (server != null) {
            server.shutdownNow();
        }

        mocksCloseable.close();
    }

    public ContentResponse sendDelete(String path) throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath + path).method(HttpMethod.DELETE).send();
    }

    public ContentResponse sendGet() throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath).method(HttpMethod.GET).send();
    }

    public ContentResponse sendGet(String path) throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath + path).method(HttpMethod.GET).send();
    }

    public ContentResponse sendPost(String content) throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath).method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(content)).send();
    }

    public ContentResponse sendPost(String path, String content)
            throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath + path).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(new StringContentProvider(content)).send();
    }

    public ContentResponse sendPut(String path, String content)
            throws InterruptedException, TimeoutException, ExecutionException {
        return client.newRequest(basePath + path).method(HttpMethod.PUT)
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(new StringContentProvider(content)).send();
    }
}
