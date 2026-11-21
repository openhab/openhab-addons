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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.rest.dto.ErrorDTO;
import org.openhab.binding.knx.internal.rest.dto.GroupAddressDetailDTO;
import org.openhab.core.auth.Role;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * Checks what the REST resource itself does: at activation it empties the upload folder of what a crash left, the
 * stream method sets core's headers and answers every failure with an {@code end} event, and deactivation ends the
 * streams.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxResourceTest {
    private @TempDir Path folder = Path.of("");

    @Test
    void activationEmptiesTheUploadFolderWithoutFollowingALinkOutOfIt() throws Exception {
        Path knx = folder.resolve("knx");
        Path upload = knx.resolve("upload");
        Files.createDirectories(upload.resolve("left").resolve("deeper"));
        Files.writeString(upload.resolve("left").resolve("upload.knxproj"), "made-up", StandardCharsets.UTF_8);
        Files.writeString(upload.resolve("left").resolve("deeper").resolve("x"), "made-up", StandardCharsets.UTF_8);
        Files.writeString(upload.resolve(".hidden"), "made-up", StandardCharsets.UTF_8);
        Path outside = Files.createDirectories(folder.resolve("outside"));
        Path kept = Files.writeString(outside.resolve("keep.txt"), "made-up", StandardCharsets.UTF_8);
        Files.createSymbolicLink(upload.resolve("link"), outside);
        Files.createSymbolicLink(upload.resolve("file-link"), kept);
        Path stored = Files.writeString(knx.resolve("project-knx_ip_home.json"), "{}", StandardCharsets.UTF_8);

        new KnxResource(mock(KNXHandlerFactory.class), mock(KnxProjectStore.class), mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class), knx, folder.resolve("conf"),
                new ManualTimer());

        try (Stream<Path> left = Files.list(upload)) {
            assertEquals(List.of(), left.toList());
        }
        assertTrue(Files.exists(kept));
        assertTrue(Files.exists(stored));
    }

    @Test
    void activationWithoutAnUploadFolderMakesNone() {
        Path knx = folder.resolve("knx");

        new KnxResource(mock(KNXHandlerFactory.class), mock(KnxProjectStore.class), mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class), knx, folder.resolve("conf"),
                new ManualTimer());

        assertFalse(Files.exists(knx));
    }

    private final SseEventSink sink = mock(SseEventSink.class);
    private final Sse sse = mock(Sse.class);
    private final OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private KnxResource resource(KNXHandlerFactory factory) {
        when(sse.newEventBuilder()).thenReturn(builder);
        when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
        doReturn(new CompletableFuture<>()).when(sink).send(any());
        return new KnxResource(factory, new KnxProjectStore(folder.resolve("store"), Clock.systemUTC()),
                mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class),
                folder.resolve("knx"), folder.resolve("conf"), new ManualTimer());
    }

    private void stream(KnxResource resource, String bridge) {
        resource.stream(sink, sse, response, bridge, null, null, null, null, null, null, null, null, null, null, null);
    }

    private ErrorDTO endEvent() {
        verify(builder).name("end");
        ArgumentCaptor<Object> data = ArgumentCaptor.forClass(Object.class);
        verify(builder).data(data.capture());
        return (ErrorDTO) data.getValue();
    }

    @Test
    void theStreamSetsTheHeadersOfCoreAndAnswersAFailureWithAnEndEvent() throws Exception {
        stream(resource(RestFixtures.factory()), "knx:ip:gone");

        verify(response).setHeader("X-Accel-Buffering", "no");
        verify(response).setHeader("Content-Encoding", "identity");
        verify(response).flushBuffer();
        assertEquals("unknown-bridge", endEvent().code);
        verify(sink).close();
    }

    @Test
    void anUnexpectedFailureOfTheStreamIsAnEndEventInternal() {
        KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
        when(factory.getBridges()).thenThrow(new IllegalStateException("made-up failure"));

        stream(resource(factory), "knx:ip:home");

        ErrorDTO end = endEvent();
        assertEquals("internal", end.code);
        assertEquals(KnxRestError.INTERNAL.text(), end.text);
        verify(sink).close();
    }

    @Test
    void oneGroupAddressIsAskedForByQueryUnderTheProjectOfItsBridgeByAdminsOnly() throws Exception {
        Method method = KnxResource.class.getMethod("groupAddress", String.class, String.class);

        assertNotNull(method.getAnnotation(GET.class));
        javax.ws.rs.Path path = method.getAnnotation(javax.ws.rs.Path.class);
        assertNotNull(path);
        assertEquals("bridges/{b}/project/group-address", path.value());
        RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
        assertNotNull(roles);
        assertArrayEquals(new String[] { Role.ADMIN }, roles.value());
        Parameter[] parameters = method.getParameters();
        PathParam bridge = parameters[0].getAnnotation(PathParam.class);
        assertNotNull(bridge);
        assertEquals("b", bridge.value());
        QueryParam address = parameters[1].getAnnotation(QueryParam.class);
        assertNotNull(address);
        assertEquals("address", address.value());
    }

    @Test
    void oneGroupAddressAnswersItsDetailAndAnErrorWithItsStatus() throws Exception {
        KnxProjectStore store = new KnxProjectStore(folder.resolve("store"), Clock.systemUTC());
        store.importProject(RestFixtures.uid("home"), ProjectTestFiles.plain(), null, null, "house.knxproj");
        KnxResource resource = new KnxResource(
                RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())), store,
                mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class),
                folder.resolve("knx"), folder.resolve("conf"), new ManualTimer());

        Response found = resource.groupAddress("knx:ip:home", "31/7/1");

        assertEquals(200, found.getStatus());
        GroupAddressDetailDTO detail = (GroupAddressDetailDTO) found.getEntity();
        assertEquals("31/7/1", detail.address);
        assertEquals(Boolean.FALSE, detail.configured);
        assertEquals(List.of(), detail.usedBy);
        Response unknown = resource.groupAddress("knx:ip:home", "31/0/99");
        assertEquals(404, unknown.getStatus());
        assertEquals("unknown-group-address", ((ErrorDTO) unknown.getEntity()).code);
        Response missing = resource.groupAddress("knx:ip:home", null);
        assertEquals(400, missing.getStatus());
        assertEquals("bad-input", ((ErrorDTO) missing.getEntity()).code);
        assertEquals(404, resource.groupAddress("knx:ip:gone", "31/7/1").getStatus());
    }

    @Test
    void deactivationEndsTheOpenStreams() {
        KnxResource resource = resource(RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())));
        stream(resource, "knx:ip:home");
        verify(builder).name("open");

        resource.deactivate();

        verify(builder).name("end");
        verify(sink).close();
    }
}
