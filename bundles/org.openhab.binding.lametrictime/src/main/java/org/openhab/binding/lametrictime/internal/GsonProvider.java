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
package org.openhab.binding.lametrictime.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;

/**
 * JSON reader/writer for Jersey using GSON.
 *
 * @author Simon Kaufmann - Initial contribution
 *
 * @param <T>
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
public class GsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    private final Gson gson;

    public GsonProvider() {
        gson = GsonGenerator.create();
    }

    @Override
    public long getSize(T t, @Nullable Class<?> type, @Nullable Type genericType, Annotation @Nullable [] annotations,
            @Nullable MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(@Nullable Class<?> type, @Nullable Type genericType, Annotation @Nullable [] annotations,
            @Nullable MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(T object, @Nullable Class<?> type, @Nullable Type genericType,
            Annotation @Nullable [] annotations, @Nullable MediaType mediaType,
            @Nullable MultivaluedMap<String, Object> httpHeaders, @Nullable OutputStream entityStream)
            throws IOException, WebApplicationException {
        try (OutputStream stream = entityStream) {
            entityStream.write(gson.toJson(object).getBytes(StandardCharsets.UTF_8));
            entityStream.flush();
        }
    }

    @Override
    public boolean isReadable(@Nullable Class<?> type, @Nullable Type genericType, Annotation @Nullable [] annotations,
            @Nullable MediaType mediaType) {
        return true;
    }

    @Override
    public T readFrom(@Nullable Class<T> type, @Nullable Type genericType, Annotation @Nullable [] annotations,
            @Nullable MediaType mediaType, @Nullable MultivaluedMap<String, String> httpHeaders,
            @Nullable InputStream entityStream) throws IOException, WebApplicationException {
        try (InputStreamReader reader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }
}
