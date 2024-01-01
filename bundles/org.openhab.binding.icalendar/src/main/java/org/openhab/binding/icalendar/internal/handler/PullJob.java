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
package org.openhab.binding.icalendar.internal.handler;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.HTTP_TIMEOUT_SECS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.icalendar.internal.logic.AbstractPresentableCalendar;
import org.openhab.binding.icalendar.internal.logic.CalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Job for pulling an update of a calendar. Fires
 * {@link CalendarUpdateListener#onCalendarUpdated()} after successful update.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Michael Wodniok - Added better descriptions for some errors while
 *         downloading calendar
 */
@NonNullByDefault
class PullJob implements Runnable {
    private static final String TMP_FILE_PREFIX = "icalendardld";

    private final Authentication.@Nullable Result authentication;
    private final File destination;
    private final HttpClient httpClient;
    private final CalendarUpdateListener listener;
    private final Logger logger = LoggerFactory.getLogger(PullJob.class);
    private final int maxSize;
    private final URI sourceURI;

    /**
     * Constructor of PullJob for creating a single pull of a calendar.
     *
     * @param httpClient A HttpClient for getting the source
     * @param sourceURI The source as URI
     * @param username Optional username for basic auth. Must be set together with a password.
     * @param password Optional password for basic auth. Must be set together with an username.
     * @param destination The destination the downloaded calendar should be saved to.
     * @param maxSize The maximum size of the downloaded calendar in bytes.
     * @param listener The listener that should be fired when update succeed.
     */
    public PullJob(HttpClient httpClient, URI sourceURI, @Nullable String username, @Nullable String password,
            File destination, int maxSize, CalendarUpdateListener listener) {
        this.httpClient = httpClient;
        this.sourceURI = sourceURI;
        if (username != null && password != null) {
            authentication = new BasicAuthentication.BasicResult(this.sourceURI, username, password);
        } else {
            authentication = null;
        }
        this.destination = destination;
        this.listener = listener;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        final Request request = httpClient.newRequest(sourceURI).followRedirects(true).method(HttpMethod.GET)
                .timeout(HTTP_TIMEOUT_SECS, TimeUnit.SECONDS);
        final Authentication.Result currentAuthentication = authentication;
        if (currentAuthentication != null) {
            currentAuthentication.apply(request);
        }

        final InputStreamResponseListener asyncListener = new InputStreamResponseListener();
        request.send(asyncListener);

        Response response;
        try {
            response = asyncListener.get(HTTP_TIMEOUT_SECS, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            logger.warn("Download of calendar was interrupted: {}", e1.getMessage());
            request.abort(e1.getCause() != null ? e1.getCause() : e1);
            return;
        } catch (TimeoutException e1) {
            logger.warn("Download of calendar timed out (waited too long for headers): {}", e1.getMessage());
            request.abort(e1.getCause() != null ? e1.getCause() : e1);
            return;
        } catch (ExecutionException e1) {
            String msg = e1.getCause() != null ? e1.getCause().getMessage() : "";
            logger.warn("Download of calendar failed with ExecutionException: {}", msg);
            request.abort(e1.getCause() != null ? e1.getCause() : e1);
            return;
        }

        if (response.getStatus() != HttpStatus.OK_200) {
            logger.warn("Response status for getting \"{}\" was {} instead of 200. Ignoring it.", sourceURI,
                    response.getStatus());
            request.abort(new IllegalStateException(
                    "Got response status " + response.getStatus() + " while requesting " + sourceURI));
            return;
        }

        final String responseLength = response.getHeaders().get(HttpHeader.CONTENT_LENGTH);
        if (responseLength != null) {
            try {
                if (Integer.parseInt(responseLength) > maxSize) {
                    logger.warn(
                            "Calendar is too big ({} bytes > {} bytes), aborting request. You may change the maximum calendar size in configuration, if appropriate.",
                            responseLength, maxSize);
                    response.abort(new ResponseTooBigException());
                    return;
                }
            } catch (NumberFormatException e) {
                logger.debug(
                        "While requesting calendar Content-Length was set, but is malformed. Falling back to read-loop.",
                        e);
            }
        }

        File tmpTargetFile;
        try {
            tmpTargetFile = Files.createTempFile(TMP_FILE_PREFIX, null).toFile();
        } catch (IOException e) {
            logger.warn("Not able to create temporary file for downloading iCal. Error message is: {}", e.getMessage());
            return;
        }

        try (final FileOutputStream tmpOutStream = new FileOutputStream(tmpTargetFile);
                final InputStream httpInputStream = asyncListener.getInputStream()) {
            final byte[] buffer = new byte[1024];
            int readBytesTotal = 0;
            int currentReadBytes = -1;
            while ((currentReadBytes = httpInputStream.read(buffer)) > -1) {
                readBytesTotal += currentReadBytes;
                if (readBytesTotal > maxSize) {
                    logger.warn(
                            "Calendar is too big (> {} bytes). Stopping receiving calendar. You may change the maximum calendar size in configuration, if appropriate.",
                            maxSize);
                    response.abort(new ResponseTooBigException());
                    return;
                }
                tmpOutStream.write(buffer, 0, currentReadBytes);
            }
        } catch (IOException e) {
            logger.warn("Not able to write temporary file with downloaded iCal. Error Message is: {}", e.getMessage());
            return;
        }

        try (final FileInputStream tmpInput = new FileInputStream(tmpTargetFile)) {
            AbstractPresentableCalendar.create(tmpInput);
        } catch (IOException | CalendarException e) {
            logger.warn(
                    "Not able to read downloaded iCal. Validation failed or file not readable. Error message is: {}",
                    e.getMessage());
            return;
        }

        try {
            Files.move(tmpTargetFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warn("Failed to replace iCal file. Error message is: {}", e.getMessage());
            return;
        }

        try {
            listener.onCalendarUpdated();
        } catch (Exception e) {
            logger.debug("An Exception was thrown while calling back", e);
        }
    }

    /**
     * Interface for calling back when the update succeed.
     */
    public static interface CalendarUpdateListener {
        /**
         * Callback when update was successful and result was placed onto target file.
         */
        public void onCalendarUpdated();
    }

    /**
     * Exception for failure if size of the response is greater than allowed.
     */
    private static class ResponseTooBigException extends Exception {

        /**
         * The only local definition. Rest of implementation is taken from Exception or is default.
         */
        private static final long serialVersionUID = 7033851403473533793L;
    }
}
