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
package org.openhab.binding.ntfy.internal.network;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NtfyMessageHeaderBuilder}.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyMessageHeaderBuilderTest {

    private static Request createRequestProxy(Map<String, String> headers) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public @Nullable Object invoke(@Nullable Object proxy, @Nullable Method method,
                    @Nullable Object @Nullable [] args) throws Throwable {
                if (method == null) {
                    return null;
                }
                if ("header".equals(method.getName()) && args != null && args.length == 2) {
                    Object nameObj = args[0];
                    Object valueObj = args[1];
                    if (nameObj == null || valueObj == null) {
                        return proxy;
                    }
                    String name = (String) nameObj;
                    String value = (String) valueObj;
                    headers.put(name, value);
                    return proxy;
                }
                if (method.getReturnType().isPrimitive()) {
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                }
                return null;
            }
        };

        return (Request) Proxy.newProxyInstance(Request.class.getClassLoader(), new Class[] { Request.class }, handler);
    }

    /**
     * Verifies that the default priority is written to the X-Priority header.
     */
    @Test
    public void buildSetsPriority() {
        NtfyMessage message = new NtfyMessage();
        Map<String, String> headers = new HashMap<>();
        Request request = createRequestProxy(headers);

        new NtfyMessageHeaderBuilder(message, request).build();

        assertEquals("3", headers.get("X-Priority"), () -> "X-Priority header was: " + headers.get("X-Priority"));
    }

    /**
     * Verifies that click action, tags, icon, attachment, actions and sequence id
     * are correctly added to the request headers.
     */
    @Test
    public void buildWithClickTagsIconAttachmentSequenceAndAction() throws Exception {
        NtfyMessage message = new NtfyMessage();
        message.setPriority(5);
        message.setClickAction("https://example.org/click");
        message.addTag("tag1");
        message.addTag("tag2");
        message.setIcon("https://example.org/icon.png");
        message.setAttachment("https://example.org/file.bin", "file.bin");
        message.addCopyAction("CopyLabel", Boolean.TRUE, "somevalue");
        message.setSequenceId("seq-123");

        Map<String, String> headers = new HashMap<>();
        Request request = createRequestProxy(headers);

        new NtfyMessageHeaderBuilder(message, request).build();

        new NtfyMessageHeaderBuilder(message, request).build();

        assertEquals("5", headers.get("X-Priority"), () -> "X-Priority header was: " + headers.get("X-Priority"));
        assertEquals("https://example.org/click", headers.get("X-Click"),
                () -> "X-Click header was: " + headers.get("X-Click"));
        String tags = headers.get("X-Tags");
        assertNotNull(tags, () -> "X-Tags header is missing");
        // set order is not guaranteed
        Set<String> tagSet = new HashSet<>();
        for (String t : tags.split(",")) {
            tagSet.add(t.trim());
        }
        assertTrue(tagSet.contains("tag1"), () -> "Expected tags to contain 'tag1', actual: " + tags);
        assertTrue(tagSet.contains("tag2"), () -> "Expected tags to contain 'tag2', actual: " + tags);

        assertEquals("https://example.org/icon.png", headers.get("X-Icon"),
                () -> "X-Icon header was: " + headers.get("X-Icon"));
        assertEquals("https://example.org/file.bin", headers.get("X-Attach"),
                () -> "X-Attach header was: " + headers.get("X-Attach"));
        assertEquals("file.bin", headers.get("X-Filename"),
                () -> "X-Filename header was: " + headers.get("X-Filename"));

        String actions = headers.get("X-Actions");
        assertNotNull(actions, () -> "X-Actions header is missing");
        assertTrue(actions.contains("copy"), () -> "X-Actions did not contain 'copy', actual: " + actions);
        assertTrue(actions.contains("CopyLabel"), () -> "X-Actions did not contain 'CopyLabel', actual: " + actions);

        assertEquals("seq-123", headers.get("X-Sequence-ID"),
                () -> "X-Sequence-ID header was: " + headers.get("X-Sequence-ID"));
    }

    /**
     * Verifies that when an attachment filename is blank the X-Filename header is not set.
     */
    @Test
    public void buildWithBlankFilenameDoesNotSetFilename() throws URISyntaxException {
        NtfyMessage message = new NtfyMessage();
        message.setAttachment("https://example.org/file.bin", "   ");

        Map<String, String> headers = new HashMap<>();
        Request request = createRequestProxy(headers);

        new NtfyMessageHeaderBuilder(message, request).build();

        assertEquals("https://example.org/file.bin", headers.get("X-Attach"),
                () -> "X-Attach header was: " + headers.get("X-Attach"));
        assertNull(headers.get("X-Filename"),
                () -> "Expected X-Filename to be absent or blank but was: " + headers.get("X-Filename"));
    }
}
