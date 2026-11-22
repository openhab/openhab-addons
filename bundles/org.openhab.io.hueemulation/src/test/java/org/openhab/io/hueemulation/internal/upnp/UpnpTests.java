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
package org.openhab.io.hueemulation.internal.upnp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.osgi.httpservice.OSGiMainHandler;
import org.glassfish.grizzly.osgi.httpservice.util.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.io.hueemulation.internal.rest.CommonSetup;
import org.openhab.io.hueemulation.internal.rest.LightsAndGroups;
import org.ops4j.pax.web.service.http.HttpService;
import org.osgi.framework.Bundle;
import org.osgi.util.tracker.ServiceTracker;

import jakarta.ws.rs.client.ClientBuilder;

/**
 * Tests the upnp server part if the description.xml is available and if the udp thread comes online
 *
 * @author David Graeff - Initial contribution
 */
public class UpnpTests {
    protected static CommonSetup commonSetup = null;
    protected UpnpServer subject;
    protected static OSGiMainHandler mainHttpHandler;
    private static HttpService httpServiceMock;
    private static String descriptionPath;

    // grizzly-httpservice 2.4.4 uses javax.servlet; UpnpServer uses jakarta.servlet.
    // Register one shared handler, updated per test via this static reference.
    private static volatile @Nullable UpnpServer activeSubject = null;

    LightsAndGroups lightsAndGroups = new LightsAndGroups();

    @BeforeAll
    public static void setupHttpParts() throws IOException {
        commonSetup = new CommonSetup(true);
        commonSetup.start(new ResourceConfig());

        descriptionPath = commonSetup.basePath.replace("/api", "/description.xml");

        Logger logger = new org.glassfish.grizzly.osgi.httpservice.util.Logger(mock(ServiceTracker.class));

        mainHttpHandler = new OSGiMainHandler(logger, mock(Bundle.class));
        commonSetup.server.getServerConfiguration().addHttpHandler(mainHttpHandler, "/");

        // Register a single Grizzly handler that delegates to the current test's subject.
        // httpService.registerServlet() is mocked as no-op because grizzly-httpservice 2.4.4
        // uses javax.servlet which is incompatible with UpnpServer's jakarta.servlet.
        commonSetup.server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                UpnpServer s = activeSubject;
                String xml = s != null ? s.xmlDocWithAddress : null;
                if (xml == null || xml.isEmpty()) {
                    response.setStatus(404, "Not Found");
                } else {
                    response.setContentType("application/xml");
                    response.getWriter().write(xml);
                }
            }
        }, UpnpServer.DISCOVERY_FILE);

        httpServiceMock = mock(HttpService.class);
    }

    @BeforeEach
    public void setup() {
        Executor executor = mock(Executor.class);
        Mockito.doAnswer(a -> {
            ((Runnable) a.getArgument(0)).run();
            return null;
        }).when(executor).execute(any());
        subject = new UpnpServer(executor);
        subject.clientBuilder = ClientBuilder.newBuilder();
        subject.httpService = httpServiceMock;
        subject.cs = commonSetup.cs;
        activeSubject = subject;
        subject.overwriteReadyToFalse = true;
        subject.activate(); // don't execute handleEvent()
        subject.overwriteReadyToFalse = false;
    }

    @AfterEach
    public void tearDown() {
        subject.deactivate();
        activeSubject = null;
    }

    @AfterAll
    public static void tearDownHttp() throws Exception {
        mainHttpHandler.unregisterAll();
        commonSetup.dispose();
    }

    @Test
    public void descriptionWithoutAddress() throws Exception {
        ContentResponse response = commonSetup.client.newRequest(descriptionPath).send();
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void descriptionWithAddress()
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        HueEmulationConfigWithRuntime r = subject.createConfiguration(null);
        r = subject.performAddressTest(r);
        subject.applyConfiguration(r);
        ContentResponse response = commonSetup.client.newRequest(descriptionPath).send();
        assertThat(response.getStatus(), is(200));
        String body = response.getContentAsString();
        assertThat(body, is(subject.xmlDocWithAddress));

        if (r == null) {
            throw new IllegalStateException();
        }

        // UDP thread started?
        r.startNow().get(5, TimeUnit.SECONDS);
        assertThat(subject.upnpAnnouncementThreadRunning(), is(true));

        // Send M-SEARCH UPNP "packet" and check if the result contains our bridge ID
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            sendSocket.setSoTimeout(700);
            byte[] bytes = "M-SEARCH".getBytes();
            sendSocket.send(new DatagramPacket(bytes, bytes.length, subject.MULTI_ADDR_IPV4, UpnpServer.UPNP_PORT));
            byte[] buffer = new byte[1000];
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            sendSocket.receive(p);
            String received = new String(buffer);
            assertThat(received, CoreMatchers.startsWith("HTTP/1.1 200 OK"));
            assertThat(received, CoreMatchers.containsString("hue-bridgeid: A668DC9B7172"));
        }

        r.dispose();
        assertThat(subject.upnpAnnouncementThreadRunning(), is(false));
    }

    @Test
    public void handEventTest() throws InterruptedException, ExecutionException, TimeoutException {
        subject.handleEvent(null);
        subject.configChangeFuture.get(5, TimeUnit.SECONDS);
        assertThat(subject.upnpAnnouncementThreadRunning(), is(true));

        subject.deactivate();
        assertThat(subject.upnpAnnouncementThreadRunning(), is(false));
    }
}
