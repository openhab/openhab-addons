/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Universal logging filter.
 *
 * Can be used on client or server side. Has the highest priority.
 *
 * @author Tim Roberts - Initial contribution
 */
@PreMatching
public class Slf4jLoggingFilter implements ContainerRequestFilter, ClientRequestFilter, ContainerResponseFilter,
        ClientResponseFilter, WriterInterceptor {

    /** The Constant NOTIFICATION_PREFIX. */
    private static final String NOTIFICATION_PREFIX = "* ";

    /** The Constant REQUEST_PREFIX. */
    private static final String REQUEST_PREFIX = "> ";

    /** The Constant RESPONSE_PREFIX. */
    private static final String RESPONSE_PREFIX = "< ";

    /** The Constant ENTITY_LOGGER_PROPERTY. */
    private static final String ENTITY_LOGGER_PROPERTY = Slf4jLoggingFilter.class.getName() + ".entityLogger";

    /** The Constant COMPARATOR. */
    private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR = new Comparator<Map.Entry<String, List<String>>>() {

        @Override
        public int compare(final Map.Entry<String, List<String>> o1, final Map.Entry<String, List<String>> o2) {
            final String s1 = o1.getKey();
            final String s2 = o2.getKey();
            if (s1 == s2) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            return s1.compareToIgnoreCase(s2);
        }
    };

    /** The Constant DEFAULT_MAX_ENTITY_SIZE. */
    private static final int DEFAULT_MAX_ENTITY_SIZE = 32 * 1024;

    /** The logger. */
    private final Logger logger;

    /** The id. */
    private final AtomicLong _id = new AtomicLong(0);

    /** The print entity. */
    private final boolean printEntity;

    /** The max entity size. */
    private final int maxEntitySize;

    /**
     * Create a logging filter logging the request and response to a default JDK
     * logger, named as the fully qualified class name of this class. Entity
     * logging is turned off by default.
     */
    public Slf4jLoggingFilter() {
        this(LoggerFactory.getLogger(Slf4jLoggingFilter.class), false);
    }

    /**
     * Create a logging filter with custom logger and custom settings of entity
     * logging.
     *
     * @param logger the logger to log requests and responses.
     * @param printEntity if true, entity will be logged as well up to the default maxEntitySize, which is 8KB
     */
    public Slf4jLoggingFilter(final Logger logger, final boolean printEntity) {
        this.logger = logger;
        this.printEntity = printEntity;
        this.maxEntitySize = DEFAULT_MAX_ENTITY_SIZE;
    }

    /**
     * Creates a logging filter with custom logger and entity logging turned on, but potentially limiting the size
     * of entity to be buffered and logged.
     *
     * @param logger the logger to log requests and responses.
     * @param maxEntitySize maximum number of entity bytes to be logged (and buffered) - if the entity is larger,
     *            logging filter will print (and buffer in memory) only the specified number of bytes
     *            and print "...more..." string at the end.
     */
    public Slf4jLoggingFilter(final Logger logger, final int maxEntitySize) {
        this.logger = logger;
        this.printEntity = true;
        this.maxEntitySize = maxEntitySize;
    }

    /**
     * Log.
     *
     * @param b the b
     */
    private void log(final StringBuilder b) {
        if (logger != null) {
            logger.debug("{}", b);
        }
    }

    /**
     * Prefix id.
     *
     * @param b the b
     * @param id the id
     * @return the string builder
     */
    private StringBuilder prefixId(final StringBuilder b, final long id) {
        b.append(Long.toString(id)).append(" ");
        return b;
    }

    /**
     * Prints the request line.
     *
     * @param b the b
     * @param note the note
     * @param id the id
     * @param method the method
     * @param uri the uri
     */
    private void printRequestLine(final StringBuilder b, final String note, final long id, final String method,
            final URI uri) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append(note).append(" on thread ")
                .append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id).append(REQUEST_PREFIX).append(method).append(" ").append(uri.toASCIIString()).append("\n");
    }

    /**
     * Prints the response line.
     *
     * @param b the b
     * @param note the note
     * @param id the id
     * @param status the status
     */
    private void printResponseLine(final StringBuilder b, final String note, final long id, final int status) {
        prefixId(b, id).append(NOTIFICATION_PREFIX).append(note).append(" on thread ")
                .append(Thread.currentThread().getName()).append("\n");
        prefixId(b, id).append(RESPONSE_PREFIX).append(Integer.toString(status)).append("\n");
    }

    /**
     * Prints the prefixed headers.
     *
     * @param b the b
     * @param id the id
     * @param prefix the prefix
     * @param headers the headers
     */
    private void printPrefixedHeaders(final StringBuilder b, final long id, final String prefix,
            final MultivaluedMap<String, String> headers) {
        for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
            final List<?> val = headerEntry.getValue();
            final String header = headerEntry.getKey();

            if (val.size() == 1) {
                prefixId(b, id).append(prefix).append(header).append(": ").append(val.get(0)).append("\n");
            } else {
                final StringBuilder sb = new StringBuilder();
                boolean add = false;
                for (final Object s : val) {
                    if (add) {
                        sb.append(',');
                    }
                    add = true;
                    sb.append(s);
                }
                prefixId(b, id).append(prefix).append(header).append(": ").append(sb.toString()).append("\n");
            }
        }
    }

    /**
     * Gets the sorted headers.
     *
     * @param headers the headers
     * @return the sorted headers
     */
    private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
        final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<Map.Entry<String, List<String>>>(
                COMPARATOR);
        sortedHeaders.addAll(headers);
        return sortedHeaders;
    }

    /**
     * Log inbound entity.
     *
     * @param b the b
     * @param stream the stream
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private InputStream logInboundEntity(final StringBuilder b, InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = stream.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize)));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        b.append('\n');
        stream.reset();
        return stream;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.client.ClientRequestFilter#filter(javax.ws.rs.client.ClientRequestContext)
     */
    @Override
    public void filter(final ClientRequestContext context) throws IOException {
        final long id = this._id.incrementAndGet();
        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Sending client request", id, context.getMethod(), context.getUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getStringHeaders());

        if (printEntity && context.hasEntity()) {
            final OutputStream stream = new LoggingStream(b, context.getEntityStream());
            context.setEntityStream(stream);
            context.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.client.ClientResponseFilter#filter(javax.ws.rs.client.ClientRequestContext,
     * javax.ws.rs.client.ClientResponseContext)
     */
    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext)
            throws IOException {
        final long id = this._id.incrementAndGet();
        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Client response received", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getHeaders());

        if (printEntity && responseContext.hasEntity()) {
            responseContext.setEntityStream(logInboundEntity(b, responseContext.getEntityStream()));
        }

        log(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
     */
    @Override
    public void filter(final ContainerRequestContext context) throws IOException {
        final long id = this._id.incrementAndGet();
        final StringBuilder b = new StringBuilder();

        printRequestLine(b, "Server has received a request", id, context.getMethod(),
                context.getUriInfo().getRequestUri());
        printPrefixedHeaders(b, id, REQUEST_PREFIX, context.getHeaders());

        if (printEntity && context.hasEntity()) {
            context.setEntityStream(logInboundEntity(b, context.getEntityStream()));
        }

        log(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext,
     * javax.ws.rs.container.ContainerResponseContext)
     */
    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        final long id = this._id.incrementAndGet();
        final StringBuilder b = new StringBuilder();

        printResponseLine(b, "Server responded with a response", id, responseContext.getStatus());
        printPrefixedHeaders(b, id, RESPONSE_PREFIX, responseContext.getStringHeaders());

        if (printEntity && responseContext.hasEntity()) {
            final OutputStream stream = new LoggingStream(b, responseContext.getEntityStream());
            responseContext.setEntityStream(stream);
            requestContext.setProperty(ENTITY_LOGGER_PROPERTY, stream);
            // not calling log(b) here - it will be called by the interceptor
        } else {
            log(b);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.ext.WriterInterceptor#aroundWriteTo(javax.ws.rs.ext.WriterInterceptorContext)
     */
    @Override
    public void aroundWriteTo(final WriterInterceptorContext writerInterceptorContext)
            throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) writerInterceptorContext.getProperty(ENTITY_LOGGER_PROPERTY);
        writerInterceptorContext.proceed();
        if (stream != null) {
            log(stream.getStringBuilder());
        }
    }

    /**
     * The Class LoggingStream.
     */
    private class LoggingStream extends OutputStream {

        /** The b. */
        private final StringBuilder b;

        /** The inner. */
        private final OutputStream inner;

        /** The baos. */
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /**
         * Instantiates a new logging stream.
         *
         * @param b the b
         * @param inner the inner
         */
        LoggingStream(final StringBuilder b, final OutputStream inner) {
            this.b = b;
            this.inner = inner;
        }

        /**
         * Gets the string builder.
         *
         * @return the string builder
         */
        StringBuilder getStringBuilder() {
            // write entity to the builder
            final byte[] entity = baos.toByteArray();

            b.append(new String(entity, 0, Math.min(entity.length, maxEntitySize)));
            if (entity.length > maxEntitySize) {
                b.append("...more...");
            }
            b.append('\n');

            return b;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(final int i) throws IOException {
            if (baos.size() <= maxEntitySize) {
                baos.write(i);
            }
            inner.write(i);
        }
    }
}