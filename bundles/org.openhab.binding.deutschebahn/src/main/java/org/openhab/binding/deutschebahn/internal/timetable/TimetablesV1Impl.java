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
package org.openhab.binding.deutschebahn.internal.timetable;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.deutschebahn.internal.timetable.dto.Timetable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Default Implementation of {@link TimetablesV1Api}.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public final class TimetablesV1Impl implements TimetablesV1Api {

    /**
     * Interface for stubbing HTTP-Calls in jUnit tests.
     */
    public interface HttpCallable {

        /**
         * Executes the given <code>url</code> with the given <code>httpMethod</code>.
         * Furthermore, the <code>http.proxyXXX</code> System variables are read and
         * set into the {@link org.eclipse.jetty.client.HttpClient}.
         *
         * @param httpMethod the HTTP method to use
         * @param url the url to execute
         * @param httpHeaders optional http request headers which has to be sent within request
         * @param content the content to be sent to the given <code>url</code> or <code>null</code> if no content should
         *            be sent.
         * @param contentType the content type of the given <code>content</code>
         * @param timeout the socket timeout in milliseconds to wait for data
         * @return the response body or <code>NULL</code> when the request went wrong
         * @throws IOException when the request execution failed, timed out, or it was interrupted
         */
        public abstract String executeUrl(String httpMethod, String url, Properties httpHeaders,
                @Nullable InputStream content, @Nullable String contentType, int timeout) throws IOException;
    }

    private static final String BASE_URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1";
    private static final String PLAN_URL = BASE_URL + "/plan/%evaNo%/%date%/%hour%";
    private static final String FCHG_URL = BASE_URL + "/fchg/%evaNo%";
    private static final String RCHG_URL = BASE_URL + "/rchg/%evaNo%";

    private static final String DB_CLIENT_ID_HEADER_NAME = "DB-Client-Id";
    private static final String DB_CLIENT_SECRET_HEADER_NAME = "DB-Api-Key";

    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd");
    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH");

    private final String clientId;
    private final String clientSecret;
    private final HttpCallable httpCallable;

    private final Logger logger = LoggerFactory.getLogger(TimetablesV1Impl.class);
    private final JAXBContext jaxbContext;
    // private Schema schema;

    /**
     * Creates a new {@link TimetablesV1Impl}.
     * 
     * @param clientSecret The client secret for application with linked timetable api on developers.deutschebahn.com.
     */
    public TimetablesV1Impl( //
            final String clientId, //
            final String clientSecret, //
            final HttpCallable httpCallable) throws JAXBException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpCallable = httpCallable;

        // The results from webservice does not conform to the schema provided. The triplabel-Element (tl) is expected
        // to occour as
        // last Element within a timetableStop (s) element. But it is the first element when requesting the plan.
        // When requesting the changes it is the last element, so the schema can't just be corrected.
        // If written to developer support, but got no response yet, so schema validation is disabled at the moment.

        // final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // final URL schemaURL = getClass().getResource("/xsd/Timetables_REST.xsd");
        // assert schemaURL != null;
        // this.schema = schemaFactory.newSchema(schemaURL);
        this.jaxbContext = JAXBContext.newInstance(Timetable.class.getPackageName(), Timetable.class.getClassLoader());
    }

    @Override
    public Timetable getPlan(final String evaNo, final Date time) throws IOException {
        return this.performHttpApiRequest(buildPlanRequestURL(evaNo, time));
    }

    @Override
    public Timetable getFullChanges(final String evaNo) throws IOException {
        return this.performHttpApiRequest(buildFchgRequestURL(evaNo));
    }

    @Override
    public Timetable getRecentChanges(final String evaNo) throws IOException {
        return this.performHttpApiRequest(buildRchgRequestURL(evaNo));
    }

    private Timetable performHttpApiRequest(final String url) throws IOException {
        this.logger.debug("Performing http request to timetable api with url {}", url);

        String response;
        try {
            response = this.httpCallable.executeUrl( //
                    "GET", //
                    url, //
                    this.createHeaders(), //
                    null, //
                    null, //
                    REQUEST_TIMEOUT_MS);
            return this.mapResponseToTimetable(response);
        } catch (IOException e) {
            logger.debug("Error getting data from webservice.", e);
            throw e;
        }
    }

    /**
     * Parses and creates the {@link Timetable} from the response or
     * returns an empty {@link Timetable} if response was empty.
     */
    private Timetable mapResponseToTimetable(final String response) throws IOException {
        if (response.isEmpty()) {
            return new Timetable();
        }

        try {
            return unmarshal(response);
        } catch (JAXBException | SAXException e) {
            this.logger.error("Error parsing response from timetable api.", e);
            throw new IOException(e);
        }
    }

    /**
     * Creates the HTTP-Headers required for http requests.
     */
    private Properties createHeaders() {
        final Properties headers = new Properties();
        headers.put(HttpHeader.ACCEPT.asString(), "application/xml");
        headers.put(DB_CLIENT_ID_HEADER_NAME, this.clientId);
        headers.put(DB_CLIENT_SECRET_HEADER_NAME, this.clientSecret);
        return headers;
    }

    private <T> T unmarshal(final String xmlContent) throws JAXBException, SAXException {
        return unmarshal( //
                jaxbContext, //
                null, // Provide no schema, due webservice results are not schema-valid.
                xmlContent);
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(final JAXBContext jaxbContext, @Nullable final Schema schema,
            final String xmlContent) throws JAXBException {
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        final JAXBElement<T> resultObject = (JAXBElement<T>) unmarshaller.unmarshal(new StringReader(xmlContent));
        return resultObject.getValue();
    }

    /**
     * Build rest endpoint URL for request the planned timetable.
     */
    @SuppressWarnings("PMD.UnsynchronizedStaticFormatter")
    private String buildPlanRequestURL(final String evaNr, final Date date) {
        synchronized (this) {
            final String dateParam = DATE_FORMAT.format(date);
            final String hourParam = HOUR_FORMAT.format(date);

            return PLAN_URL //
                    .replace("%evaNo%", evaNr) //
                    .replace("%date%", dateParam) //
                    .replace("%hour%", hourParam);
        }
    }

    /**
     * Build rest endpoint URL for request all known changes in the timetable.
     */
    private static String buildFchgRequestURL(final String evaNr) {
        return FCHG_URL.replace("%evaNo%", evaNr);
    }

    /**
     * Build rest endpoint URL for request all known changes in the timetable.
     */
    private static String buildRchgRequestURL(final String evaNr) {
        return RCHG_URL.replace("%evaNo%", evaNr);
    }
}
