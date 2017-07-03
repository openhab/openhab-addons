/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.scalarweb.ScalarUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterId;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterOptions;
import org.openhab.binding.sony.internal.scalarweb.models.api.MethodTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebService.
 *
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebService {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebService.class);

    /** The Constant AccessControl. */
    public final static String AccessControl = "accessControl";

    /** The Constant AppControl. */
    public final static String AppControl = "appControl";

    /** The Constant Audio. */
    public final static String Audio = "audio";

    /** The Constant AvContent. */
    public final static String AvContent = "avContent";

    /** The Constant Browser. */
    public final static String Browser = "browser";

    /** The Constant Cec. */
    public final static String Cec = "cec";

    /** The Constant ContentShare. */
    public final static String ContentShare = "contentshare";

    /** The Constant Encryption. */
    public final static String Encryption = "encryption";

    /** The Constant Guide. */
    public final static String Guide = "guide";

    /** The Constant System. */
    public final static String System = "system";

    /** The Constant VideoScreen. */
    public final static String VideoScreen = "videoScreen";

    /** The service name. */
    private final String serviceName;

    /** The base uri. */
    private final URI baseUri;

    /** The base uri string. */
    private final String baseUriString;

    /** The version. */
    private final String version;

    /** The requestor. */
    private final HttpRequest requestor;

    /** The gson. */
    private final Gson gson;

    /** The methods. */
    private final Map<String, ScalarWebMethod> methods = new HashMap<String, ScalarWebMethod>();

    /** The curr id. */
    private int currId = 1;

    /**
     * Instantiates a new scalar web service.
     *
     * @param requestor the requestor
     * @param baseUrl the base url
     * @param serviceName the service name
     * @param version the version
     * @throws URISyntaxException the URI syntax exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ScalarWebService(HttpRequest requestor, URI baseUrl, String serviceName, String version)
            throws URISyntaxException, IOException {
        this.serviceName = serviceName;

        gson = new GsonBuilder().registerTypeAdapter(ScalarWebResult.class, new ScalarWebResultDeserializer()).create();

        final String base = baseUrl.toString();

        baseUriString = base + (base.endsWith("/") ? "" : "/")
                + (serviceName.startsWith("/") ? serviceName.substring(1) : serviceName);
        baseUri = new URI(baseUriString);

        this.version = version;

        this.requestor = requestor;

        final List<String> versionResult = execute(ScalarWebMethod.GetVersions).asArray(String.class);

        for (String apiVersion : versionResult) {

            final ScalarWebResult mResult = execute(ScalarWebMethod.GetMethodTypes, apiVersion);
            if (mResult.isError()) {
                throw mResult.getHttpResponse().createException();
            } else {
                final MethodTypes mtdResults = mResult.as(MethodTypes.class);
                methods.putAll(mtdResults.getMethods());
            }
        }
    }

    /**
     * Gets the methods.
     *
     * @return the methods
     */
    public Collection<ScalarWebMethod> getMethods() {
        return methods.values();
    }

    /**
     * Gets the method.
     *
     * @param methodName the method name
     * @return the method
     */
    public ScalarWebMethod getMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * Gets the base uri.
     *
     * @return the base uri
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Execute.
     *
     * @param mthd the mthd
     * @return the scalar web result
     */
    public ScalarWebResult execute(String mthd) {
        return execute(mthd, new Object[0]);
    }

    /**
     * Execute.
     *
     * @param mthd the mthd
     * @param parms the parms
     * @return the scalar web result
     */
    public ScalarWebResult execute(String mthd, Object... parms) {
        final String json = gson.toJson(new ScalarWebRequest(currId++, mthd, version, parms));
        return execute(json, new Header[0]);
    }

    /**
     * Execute json.
     *
     * @param jsonRequest the json request
     * @return the scalar web result
     */
    public ScalarWebResult executeJson(String jsonRequest) {
        return execute(jsonRequest, new Header[0]);
    }

    /**
     * Execute.
     *
     * @param jsonRequest the json request
     * @param headers the headers
     * @return the scalar web result
     */
    public ScalarWebResult execute(String jsonRequest, Header... headers) {
        final HttpResponse resp = requestor.sendPostJsonCommand(baseUriString, jsonRequest, headers);

        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            // logger.debug(">>> contents: {}", resp.getContent());
            return gson.fromJson(resp.getContent(), ScalarWebResult.class);
        } else {
            return ScalarUtilities.createErrorResult(resp.getHttpCode(), resp.getContent());
        }
    }

    /**
     * Execute xml.
     *
     * @param uri the uri
     * @param body the body
     * @param headers the headers
     * @return the scalar web result
     */
    public ScalarWebResult executeXml(String uri, String body, Header... headers) {
        final HttpResponse resp = requestor.sendPostXmlCommand(uri, body, headers);

        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            return gson.fromJson(resp.getContent(), ScalarWebResult.class);
        } else {
            return ScalarUtilities.createErrorResult(resp.getHttpCode(), resp.getContent());
        }
    }

    /**
     * Act register.
     *
     * @param accessCode the access code
     * @return the scalar web result
     */
    public ScalarWebResult actRegister(Integer accessCode) {
        final ScalarWebMethod actRegister = methods.get(ScalarWebMethod.ActRegister);
        if (actRegister == null) {
            return ScalarUtilities.createNotImplementedResult(ScalarWebMethod.ActRegister);
        }

        if (accessCode == null) {
            return execute(ScalarWebMethod.ActRegister, new ActRegisterId(), new Object[] { new ActRegisterOptions() });
        } else {
            final String json = gson.toJson(new ScalarWebRequest(currId++, ScalarWebMethod.ActRegister, version,
                    new ActRegisterId(), new Object[] { new ActRegisterOptions() }));
            return execute(json, NetUtilities.createAuthHeader(accessCode));
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        final String newLine = java.lang.System.lineSeparator();

        sb.append("Service: ");
        sb.append(baseUri);
        sb.append(newLine);

        final Set<String> sortedNames = new TreeSet<String>(methods.keySet());
        for (String mthName : sortedNames) {
            sb.append("   ");
            final ScalarWebMethod mthd = methods.get(mthName);
            if (mthd != null) {
                sb.append(mthd);
                sb.append(newLine);

            }
        }

        return sb.toString();
    }
}
