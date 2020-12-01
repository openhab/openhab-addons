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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;

/**
 * This class provides an abstract base to all sony transports and provides the following:
 * <ol>
 * <li>The base URI to be used</li>
 * <li>Management of listeners</li>
 * <li>Global transport options</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSonyTransport implements SonyTransport {

    /** The base URI used by the transport */
    private final URI baseUri;

    /** The listeners of the transport */
    private final List<SonyTransportListener> listeners = new CopyOnWriteArrayList<>();

    /** Global transport options */
    private final List<TransportOption> options = new CopyOnWriteArrayList<>();

    /**
     * Constructs the transport using the base URI
     * 
     * @param baseUri a non-null base URI
     */
    protected AbstractSonyTransport(final URI baseUri) {
        Objects.requireNonNull(baseUri, "baseUri cannot be null");
        this.baseUri = baseUri;
    }

    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    @Override
    public void addListener(final SonyTransportListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        listeners.add(listener);
    }

    @Override
    public boolean removeListener(final SonyTransportListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return listeners.remove(listener);
    }

    /**
     * Fires an onError message to all listeners
     * 
     * @param error a non-null thrown error
     */
    protected void fireOnError(final Throwable error) {
        Objects.requireNonNull(error, "error cannot be null");
        for (final SonyTransportListener listener : listeners) {
            listener.onError(error);
        }
    }

    /**
     * First a onEvent message to all listeners
     * 
     * @param event a non-null event
     */
    protected void fireEvent(final ScalarWebEvent event) {
        Objects.requireNonNull(event, "event cannot be null");
        for (final SonyTransportListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    @Override
    public void setOption(final TransportOption option) {
        Objects.requireNonNull(option, "option cannot be null");

        if (option instanceof TransportOptionHeader) {
            final String headerName = ((TransportOptionHeader) option).getHeader().getName();
            options.removeIf(e -> e instanceof TransportOptionHeader
                    && StringUtils.equalsIgnoreCase(headerName, ((TransportOptionHeader) e).getHeader().getName()));
        } else {
            final Class<?> optionClass = option.getClass();
            options.removeIf(e -> optionClass.equals(e.getClass()));
        }
        options.add(option);
    }

    @Override
    public void removeOption(final TransportOption option) {
        Objects.requireNonNull(option, "option cannot be null");
        options.remove(option);
    }

    @Override
    public List<TransportOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /**
     * Helper method to get all options that are assignable to the passed option class - regardless if it's a global
     * option or passed options
     * 
     * @param clazz the non-null class to use
     * @param options a possibly empty list of local options to query
     * @return a non-null, possibly empty list of matching options
     */
    @SuppressWarnings("unchecked")
    protected <O extends TransportOption> List<O> getOptions(final Class<O> clazz, final TransportOption... options) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        return Stream.concat(Arrays.stream(options), this.options.stream())
                .filter(obj -> obj.getClass().isAssignableFrom(clazz)).map(obj -> (O) obj).collect(Collectors.toList());
    }

    /**
     * Helper method to determine if a specific option is part of either the global options or the passed options
     * 
     * @param clazz the non-null class of the option
     * @param options the local options to also check
     * @return true if found, false otherwise
     */
    protected <O extends TransportOption> boolean hasOption(final Class<O> clazz, final TransportOption... options) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        return !getOptions(clazz, options).isEmpty();
    }

    /**
     * Helper method to get all the headers from both the global options and the passed options
     * 
     * @param options the local options to also check
     * @return a non-null, but possibly empty array of headers
     */
    protected Header[] getHeaders(final TransportOption... options) {
        return getOptions(TransportOptionHeader.class, options).stream().map(h -> h.getHeader())
                .collect(Collectors.toList()).toArray(new Header[0]);
    }
}
