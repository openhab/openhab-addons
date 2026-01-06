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
package org.openhab.automation.java223.internal.strategy.jarloader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JarFileObject} is a custom {@link JavaFileObject} implementation
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class JarFileObject implements JavaFileObject {
    private final URI uri;
    private final Kind kind;

    private JarFileObject(URI uri, Kind kind) {
        this.uri = uri;
        this.kind = kind;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public String getName() {
        return uri.toString();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return uri.toURL().openStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(Path.of(uri).toFile());
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new StringReader(getCharContent(ignoreEncodingErrors).toString());
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            try (InputStream inputStream = openInputStream()) {
                result.write(inputStream.readAllBytes());
                return result.toString(StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isNameCompatible(@Nullable String simpleName, @Nullable Kind kind) {
        if (simpleName == null || kind == null) {
            return false;
        }
        String baseName = simpleName + kind.extension;
        return kind.equals(getKind()) && toUri().getPath().endsWith(baseName);
    }

    @Override
    public @Nullable NestingKind getNestingKind() {
        return null;
    }

    @Override
    public @Nullable Modifier getAccessLevel() {
        return null;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    /**
     * create a new {@link JavaFileObject} of kind SOURCE
     *
     * @param uri the URI of the file object
     * @return the corresponding {@link JavaFileObject}
     */
    public static JavaFileObject sourceFileObject(URI uri) {
        return new JarFileObject(uri, Kind.SOURCE);
    }

    /**
     * create a new {@link JavaFileObject} of kind CLASS
     *
     * @param uri the URI of the file object
     * @return the corresponding {@link JavaFileObject}
     */
    public static JavaFileObject classFileObject(URI uri) {
        return new JarFileObject(uri, Kind.CLASS);
    }
}
