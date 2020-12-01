/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.transports;

import java.io.Closeable;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.SonyBindingConstants;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

/**
 * This interface defines the contract for all sony transports and provides some default logic to each
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyTransport extends Closeable {
    /**
     * Executes the payload on the transport using the specified options (if any)
     *
     * @param payload a non-null payload to deliver
     * @param options any transport options to use
     * @return the non-null future result
     */
    public CompletableFuture<? extends TransportResult> execute(TransportPayload payload, TransportOption... options);

    /**
     * Sets a global option on the transport
     * 
     * @param option a non-null option
     */
    public void setOption(TransportOption option);

    /**
     * Removes a global option from the transport.
     * 
     * @param option a non-null option
     */
    public void removeOption(TransportOption option);

    /**
     * Returns a list of all global options used by the transport
     * 
     * @return a non-null, possibly empty list of global transport options
     */
    public List<TransportOption> getOptions();

    /**
     * Adds a listener to the transport
     * 
     * @param listener a non-null listener to add
     */
    public void addListener(SonyTransportListener listener);

    /**
     * Removes a listener from the transport
     * 
     * @param listener a non-null listener to remove
     * @return
     */
    public boolean removeListener(SonyTransportListener listener);

    /**
     * Get's the protocol type used by this transport (matches one of the protocol types on
     * {@link SonyTransportFactory})
     * 
     * @return a non-null, non-empty protocol type
     */
    public String getProtocolType();

    /**
     * Returns the base URI used by the transport
     * 
     * @return a non-null base URI
     */
    public URI getBaseUri();

    /**
     * Helper metohd to execute a {@link ScalarWebRequest} on the transport and return a {@link ScalarWebResult}
     * 
     * @param payload a non-null {@link ScalarWebRequest}
     * @param options any transport options to include
     * @return a non-null {@link ScalarWebRequest}
     */
    public default ScalarWebResult execute(final ScalarWebRequest payload, final TransportOption... options) {
        Objects.requireNonNull(payload, "payload cannot be null");
        try {
            final TransportResult result = execute(new TransportPayloadScalarWebRequest(payload), options)
                    .get(SonyBindingConstants.RSP_WAIT_TIMEOUTSECONDS, TimeUnit.SECONDS);
            if (result instanceof TransportResultScalarWebResult) {
                return ((TransportResultScalarWebResult) result).getResult();
            } else {
                return new ScalarWebResult(new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, "Execution of "
                        + payload + " didn't return a TransportResultScalarWebResult: " + result.getClass().getName()));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ScalarWebResult(new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR_500,
                    "Execution of " + payload + " threw an exception: " + e.getMessage()));
        }
    };

    @Override
    public void close();
}
