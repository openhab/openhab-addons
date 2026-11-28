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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.StoreFailedException;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Number 17: reads an uploaded ETS project for a bridge and stores the result. The file lies in a private
 * temporary folder only while it is read and is never kept; the password is wiped when the upload ends.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectUpload {
    static final long MAX_BYTES = 200L * 1024 * 1024;
    static final String DEFAULT_NAME = "upload.knxproj";
    static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_LANGUAGE_LENGTH = 35;
    private static final Pattern LANGUAGE = Pattern.compile("[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*");
    private static final Pattern NOT_IN_NAME = Pattern.compile("[^A-Za-z0-9._ -]");

    private final Logger logger = LoggerFactory.getLogger(ProjectUpload.class);
    private final KnxRestService service;
    private final KnxProjectStore store;
    private final Uploads uploads;
    private final long maxBytes;

    ProjectUpload(KnxRestService service, KnxProjectStore store, Uploads uploads) {
        this(service, store, uploads, MAX_BYTES);
    }

    ProjectUpload(KnxRestService service, KnxProjectStore store, Uploads uploads, long maxBytes) {
        this.service = service;
        this.store = store;
        this.uploads = uploads;
        this.maxBytes = maxBytes;
    }

    /**
     * @param contentLength the declared length, null without one
     * @param password the header {@link Uploads#PASSWORD_HEADER}, null without one; never copied
     * @param language the language of the texts, such as {@code de-DE}
     * @param name the file name to keep as the project's source; cut to its last part and cleaned
     */
    public ProjectDTO upload(@Nullable String bridge, @Nullable InputStream body, @Nullable String contentLength,
            @Nullable String password, @Nullable String language, @Nullable String name) {
        ThingUID uid = service.handler(bridge).getThing().getUID();
        String readLanguage = language(language);
        String fileName = cleanName(name);
        Uploads.checkLength(contentLength, maxBytes);
        uploads.begin();
        try (Uploads.Password secret = uploads.password(password)) {
            Path folder = uploads.newFolder();
            try {
                Path file = uploads.receive(body, folder, fileName, maxBytes);
                StoredProject stored = store.importProject(uid, file, secret.chars(), readLanguage, fileName);
                return KnxDtoMapper.project(FileState.LOADED, stored);
            } catch (KnxProjectException e) {
                throw failed(reasonError(e.getReason()), e);
            } catch (StoreFailedException e) {
                throw failed(KnxRestError.STORE_FAILED, e);
            } catch (IOException e) {
                throw failed(KnxRestError.READ_FAILED, e);
            } finally {
                uploads.delete(folder);
            }
        } finally {
            uploads.end();
        }
    }

    private KnxRestException failed(KnxRestError error, Exception e) {
        logger.warn("KNX REST project upload: {} ({})", error.code(), e.getClass().getName());
        return new KnxRestException(error);
    }

    private static KnxRestError reasonError(KnxProjectException.Reason reason) {
        return switch (reason) {
            case NOT_A_PROJECT -> KnxRestError.NOT_A_PROJECT;
            case PASSWORD_REQUIRED -> KnxRestError.PASSWORD_REQUIRED;
            case WRONG_PASSWORD -> KnxRestError.WRONG_PASSWORD;
            case UNSUPPORTED_PROTECTION -> KnxRestError.UNSUPPORTED_PROTECTION;
            case LIMIT_EXCEEDED -> KnxRestError.LIMIT_EXCEEDED;
            case MALFORMED -> KnxRestError.MALFORMED;
        };
    }

    private static @Nullable String language(@Nullable String language) {
        if (language == null || language.isEmpty()) {
            return null;
        }
        if (language.length() > MAX_LANGUAGE_LENGTH || !LANGUAGE.matcher(language).matches()) {
            throw KnxRestException.badInput("language", "not a language tag such as de or de-DE");
        }
        return language;
    }

    /**
     * {@return the last part of the name after {@code /} or {@code \}, every character outside {@code A-Z a-z 0-9 .
     * _ -} and space as {@code _}, at most 100 characters; {@link #DEFAULT_NAME} for none, an empty one, {@code .}
     * or {@code ..}}
     */
    static String cleanName(@Nullable String name) {
        if (name == null) {
            return DEFAULT_NAME;
        }
        String last = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        String clean = NOT_IN_NAME.matcher(last).replaceAll("_");
        if (clean.length() > MAX_NAME_LENGTH) {
            clean = clean.substring(0, MAX_NAME_LENGTH);
        }
        if (clean.isEmpty() || ".".equals(clean) || "..".equals(clean)) {
            return DEFAULT_NAME;
        }
        return clean;
    }
}
