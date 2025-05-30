/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.http;

import static org.openhab.binding.linktap.internal.LinkTapBindingConstants.*;
import static org.openhab.binding.linktap.protocol.http.NotTapLinkGatewayException.NotTapLinkGatewapExecptionDefinitions.*;
import static org.openhab.binding.linktap.protocol.http.TransientCommunicationIssueException.TransientExecptionDefinitions.*;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.linktap.internal.Firmware;
import org.openhab.binding.linktap.internal.Utils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebServerApi} defines interactions with the web server interface.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public final class WebServerApi {

    public static final String URI_SCHEME = "http";
    public static final String URI_HOST_PREFIX = URI_SCHEME + "://";

    /**
     * Headers
     */
    public static final String HEADER_SERVER = "Server";
    public static final String HEADER_GW_SERVER_NAME = "LinkTap Gateway";

    /**
     * HTML title field mappings to use cases
     */
    private static final String TITLE_API_RESPONSE = "api";
    private static final String TITLE_API_CONFIG_PAGE = "LinkTap Gateway";
    private static final String TITLE_API_LOGIN_PAGE = "LinkTap Gateway Login";

    /**
     * Field names for form submission API's
     */
    private static final String FIELD_ADMIN_USER = "admin";
    private static final String FIELD_ADMIN_USER_PWD = "adminpwd";

    private static final WebServerApi INSTANCE = new WebServerApi();
    private static final String REQ_HDR_APPLICATION_JSON = new MediaType("application", "json", "UTF-8").toString();
    private final Logger logger = LoggerFactory.getLogger(WebServerApi.class);
    private final JettyTraceListener jettyTraceListener = new JettyTraceListener(logger);

    private @NonNullByDefault({}) HttpClient httpClient;
    private @Nullable TranslationProvider translationProvider;
    private @Nullable LocaleProvider localeProvider;
    private @Nullable Bundle bundle;

    private WebServerApi() {
    }

    public static WebServerApi getInstance() {
        return INSTANCE;
    }

    public void setTranslationProviderInfo(TranslationProvider translationProvider, LocaleProvider localeProvider,
            Bundle bundle) {
        this.bundle = bundle;
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        TranslationProvider translationProv = translationProvider;
        LocaleProvider localeProv = localeProvider;
        if (translationProv == null || localeProv == null) {
            return key;
        }
        String result = translationProv.getText(bundle, key, key, localeProv.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    /**
     * Sets the httpClient object to be used for API calls to LinkTap.
     *
     * @param httpClient the client to be used.
     */
    public void setHttpClient(@Nullable HttpClient httpClient) {
        if (httpClient != null) {
            this.httpClient = httpClient;
        }
    }

    public Map<String, String> getBridgeProperities(final String hostname, final int timeoutSeconds)
            throws LinkTapException, NotTapLinkGatewayException, TransientCommunicationIssueException {
        try {
            final Request request = httpClient.newRequest(URI_HOST_PREFIX + hostname).method(HttpMethod.GET);
            final ContentResponse cr = addTraceListener(request).timeout(timeoutSeconds, TimeUnit.SECONDS).send();
            if (HttpURLConnection.HTTP_OK != cr.getStatus()) {
                throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
            }
            validateHeaders(cr.getHeaders());
            final String responseData = cr.getContentAsString();
            final Document doc = Jsoup.parse(responseData);

            switch (doc.title()) {
                case TITLE_API_CONFIG_PAGE:
                    break;
                case TITLE_API_LOGIN_PAGE:
                    return Map.of();
                default:
                    throw new NotTapLinkGatewayException(MISSING_SERVER_TITLE);
            }
            final Map<String, String> deviceProps = getMetadataProperties(doc);
            Firmware firmware = new Firmware(deviceProps.get(BRIDGE_PROP_GW_VER));
            if (firmware.supportsMDNS()) {
                getMdnsEnableArgs(doc);
            } else {
                logger.debug("Firmware revision does not include mDNS support");
            }
            return deviceProps;

        } catch (InterruptedException e) {
            return Map.of();
        } catch (TimeoutException e) {
            throw new TransientCommunicationIssueException(COMMUNICATIONS_LOST);
        } catch (ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof UnknownHostException || t instanceof SocketTimeoutException
                    || t instanceof SocketException) {
                throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
            } else if (t instanceof SSLHandshakeException) {
                throw new NotTapLinkGatewayException(UNEXPECTED_HTTPS);
            } else {
                logger.warn("{}", getLocalizedText("ExecutionException -> {}", Utils.getMessage(e)));
            }
            throw new LinkTapException(getLocalizedText("exception.unexpected-failure", Utils.getMessage(e)));
        }
    }

    /**
     * Extract the common properties for all devices, from the given meta-data of a device.
     *
     * @param doc the html document returns from the potential Gateway device
     * @return Map of common props
     */
    private Map<String, String> getMetadataProperties(final Document doc) {
        final Map<String, String> newProps = new HashMap<>(7);

        /*
         * Extract elements based on td location using the text markers
         */
        String firmwareVer = "?";
        String hwModel = "?";
        String id = "?";
        String macAddr = "?";

        final org.jsoup.select.Elements tdEntries = doc.getElementsByTag("td");
        for (int i = 0; i < tdEntries.size(); ++i) {
            if (tdEntries.get(i).hasText()) {
                switch (tdEntries.get(i).text()) {
                    case "Firmware version":
                        firmwareVer = tdEntries.get(i + 1).text();
                        i++;
                        break;
                    case "Model":
                        hwModel = tdEntries.get(i + 1).text();
                        i++;
                        break;
                    case "ID":
                        id = tdEntries.get(i + 1).text();
                        i++;
                        break;
                    case "MAC address":
                        macAddr = tdEntries.get(i + 1).text();
                        i++;
                        break;
                }
            }
        }

        newProps.put(BRIDGE_PROP_GW_ID, id.split("[-]")[0]);
        newProps.put(BRIDGE_PROP_GW_VER, firmwareVer.split("[_]")[0]);
        newProps.put(BRIDGE_PROP_MAC_ADDR, macAddr);
        newProps.put(BRIDGE_PROP_HW_MODEL, hwModel);

        /*
         * Extract elements based on name markers and attributes
         */
        final boolean httpApiEnabled = doc.getElementsByAttributeValue("name", "htapi").hasAttr("checked");
        final String httpApiEndpoint = doc.getElementsByAttributeValue("name", "URL").attr("value");

        newProps.put(BRIDGE_PROP_HTTP_API_ENABLED, String.valueOf(httpApiEnabled));
        newProps.put(BRIDGE_PROP_HTTP_API_EP, httpApiEndpoint);

        Optional<Element> vunitSelections = doc.getElementsByAttributeValue("name", "vunit").stream()
                .filter(x -> x.hasAttr("checked")).findFirst();
        if (vunitSelections.isPresent()) {
            switch (vunitSelections.get().attr("value")) {
                case "0":
                    newProps.put(BRIDGE_PROP_VOL_UNIT, "L");
                    break;
                case "1":
                    newProps.put(BRIDGE_PROP_VOL_UNIT, "gal");
                    break;
            }
        }

        return newProps;
    }

    public Optional<Element> getSection(final Document doc, final String title) {
        final Elements thead = doc.getElementsByTag("thead");
        Optional<Element> element = thead.stream()
                .filter(x -> x.hasText() && x.text().toLowerCase().contains(title.toLowerCase())).findFirst();
        if (element.isPresent()) {
            return Optional.of(element.get().parent());
        }
        return Optional.empty();
    }

    public String getUriInputArg(final Element el) {
        final StringBuilder sb = new StringBuilder();
        switch (el.attr("type")) {
            case "checkbox":
                sb.append(el.attr("name"));
                sb.append("=");
                if (el.hasAttr("checked")) {
                    sb.append(el.attr("value"));
                } else {
                    sb.append("0");
                }
                break;
            case "radio":
                if (el.hasAttr("checked")) {
                    sb.append(el.attr("name"));
                    sb.append("=");
                    sb.append(el.attr("value"));
                }
                break;
            case "text":
                sb.append(el.attr("name"));
                sb.append("=");
                sb.append(URLDecoder.decode(el.attr("value"), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    public String getMdnsEnableArgs(final Document doc) {
        final Optional<Element> miscSection = getSection(doc, "Misc settings");
        StringBuilder sb = new StringBuilder();

        if (!miscSection.isPresent()) {
            return sb.toString();
        }
        final Elements inputs = miscSection.get().getElementsByTag("input");
        for (int i = 0; i < inputs.size(); ++i) {
            final String val = getUriInputArg(inputs.get(i));
            if (!val.isEmpty() && !sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(val);
        }
        // Change the mdns flag to true
        {
            final int mdnsIdx = sb.indexOf("mdns=0");
            if (mdnsIdx != -1) {
                sb.replace(mdnsIdx, mdnsIdx + 6, "mdns=1");
                return sb.toString();
            }
        }

        return "";
    }

    public String getLocalHttpApiArgs(final Document doc, final Optional<String> targetServerOpt,
            final Optional<Boolean> wrapHtmlDisable) {
        final Optional<Element> localHttpApiSection = getSection(doc, "Local HTTP API settings");
        StringBuilder sb = new StringBuilder();

        if (!localHttpApiSection.isPresent()) {
            return sb.toString();
        }
        final Elements inputs = localHttpApiSection.get().getElementsByTag("input");
        for (int i = 0; i < inputs.size(); ++i) {
            final String val = getUriInputArg(inputs.get(i));
            if (!val.isEmpty() && !sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(val);
        }

        boolean updatedUri = false;
        int enableApiIdx = -1;

        if (targetServerOpt.isPresent()) {
            String targetServer = targetServerOpt.get();
            // Change the enable Local HTTP API flag to true
            enableApiIdx = sb.indexOf("htapi=0");
            if (enableApiIdx != -1) {
                sb.replace(enableApiIdx, enableApiIdx + 7, "htapi=1");
            }

            final int urlApiMarker = sb.indexOf("URL=");
            if (urlApiMarker != -1) {
                final int nextArg = sb.indexOf("&", urlApiMarker);
                String urlArg = (nextArg == -1) ? sb.substring(urlApiMarker + 4)
                        : sb.substring(urlApiMarker + 4, nextArg);
                logger.trace("Found existing HTTP URL Server : {}", urlArg);
                if (!urlArg.equals(targetServer)) {
                    updatedUri = true;
                    sb.replace(urlApiMarker, urlApiMarker + urlArg.length() + 4,
                            "URL=" + URLEncoder.encode(targetServer, StandardCharsets.UTF_8));
                }
            }
        }

        int wgrhIdx = -1;
        if (wrapHtmlDisable.isPresent() && wrapHtmlDisable.get()) {
            // Change the wgrhIdx flag to true
            {
                wgrhIdx = sb.indexOf("wgrh=1");
                if (wgrhIdx != -1) {
                    sb.replace(wgrhIdx, wgrhIdx + 6, "wgrh=0");
                    return sb.toString();
                }
            }
        }

        if (wgrhIdx != -1 || enableApiIdx != -1 || updatedUri) {
            return sb.toString();
        }

        return "";
    }

    public boolean configureBridge(final @Nullable String hostname, final int timeoutSeconds,
            final Optional<Boolean> mdnsEnable, final Optional<Boolean> nonHtmlEnable,
            final Optional<String> localServer)
            throws InterruptedException, NotTapLinkGatewayException, TransientCommunicationIssueException {
        try {
            if (hostname == null) {
                throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
            }
            final String targetHost = URI_HOST_PREFIX + hostname;
            final Request request = httpClient.newRequest(targetHost).method(HttpMethod.GET);
            final ContentResponse cr = addTraceListener(request).timeout(timeoutSeconds, TimeUnit.SECONDS).send();
            if (HttpURLConnection.HTTP_OK != cr.getStatus()) {
                throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
            }
            logger.trace("Validating response from Gateway web UI");
            validateHeaders(cr.getHeaders());
            final String responseData = cr.getContentAsString();
            final Document doc = Jsoup.parse(responseData);

            switch (doc.title()) {
                case TITLE_API_CONFIG_PAGE:
                    break;
                case TITLE_API_LOGIN_PAGE:
                    return false;
                default:
                    throw new NotTapLinkGatewayException(MISSING_SERVER_TITLE);
            }
            // Send the GET request to configure mdns if it's not enabled
            boolean rebootReq = false;
            if (mdnsEnable.isPresent() && mdnsEnable.get()) {
                logger.trace("Enabling mdns server on gateway");
                String mdnsEnableReqStr = getMdnsEnableArgs(doc);
                if (!mdnsEnableReqStr.isEmpty()) {
                    logger.debug("Updating mdns server settings on gateway");
                    final Request mdnsRequest = httpClient
                            .newRequest(targetHost + "/index.shtml?flag=4&" + mdnsEnableReqStr).method(HttpMethod.GET);
                    final ContentResponse mdnsCr = addTraceListener(mdnsRequest)
                            .timeout(timeoutSeconds, TimeUnit.SECONDS).send();
                    if (HttpURLConnection.HTTP_OK != mdnsCr.getStatus()) {
                        throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
                    }
                    rebootReq = true;
                }
            }

            if (localServer.isPresent() && !localServer.get().isBlank()
                    || nonHtmlEnable.isPresent() && nonHtmlEnable.get()) {
                if (localServer.isPresent() && !localServer.get().isBlank()) {
                    logger.trace("Setting Local HTTP Api on gateway");
                }
                if (nonHtmlEnable.isPresent() && nonHtmlEnable.get()) {
                    logger.trace("Enabling efficient non HTML communications on gateway");
                }

                String localHttpApiReqStr = this.getLocalHttpApiArgs(doc, localServer, nonHtmlEnable);
                if (!localHttpApiReqStr.isEmpty()) {
                    logger.debug("Updating Local HTTP API server settings on gateway");
                    final Request lhttpApiRequest = httpClient
                            .newRequest(targetHost + "/index.shtml?flag=5&" + localHttpApiReqStr)
                            .method(HttpMethod.GET);
                    final ContentResponse mdnsCr = addTraceListener(lhttpApiRequest)
                            .timeout(timeoutSeconds, TimeUnit.SECONDS).send();
                    if (HttpURLConnection.HTTP_OK != mdnsCr.getStatus()) {
                        throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
                    }
                    rebootReq = true;
                }
            }

            if (rebootReq) {
                logger.debug("Rebooting gateway to apply new settings");
                final Request restartReq = httpClient.newRequest(targetHost + "/index.shtml?flag=0")
                        .method(HttpMethod.GET);
                final ContentResponse mdnsCr = addTraceListener(restartReq).timeout(timeoutSeconds, TimeUnit.SECONDS)
                        .send();
                if (HttpURLConnection.HTTP_OK != mdnsCr.getStatus()) {
                    throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
                }
            }

            return rebootReq;

        } catch (TimeoutException e) {
            throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
        } catch (ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof UnknownHostException) {
                throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
            } else if (t instanceof SocketTimeoutException || t instanceof SocketException) {
                throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
            } else if (t instanceof SSLHandshakeException) {
                throw new NotTapLinkGatewayException(UNEXPECTED_HTTPS);
            } else {
                logger.warn("{}", getLocalizedText("ExecutionException -> {}", Utils.getMessage(e)));
            }
            throw new NotTapLinkGatewayException(getLocalizedText("exception.unexpected-failure", Utils.getMessage(t)));
        }
    }

    public boolean unlockWebInterface(final String hostname, final int timeoutSeconds, final String username,
            final String password)
            throws LinkTapException, NotTapLinkGatewayException, TransientCommunicationIssueException {
        try {
            org.eclipse.jetty.util.Fields fields = new org.eclipse.jetty.util.Fields();
            fields.put(FIELD_ADMIN_USER, username);
            fields.put(FIELD_ADMIN_USER_PWD, password);
            final Request request = httpClient.newRequest(URI_HOST_PREFIX + hostname + "/login.shtml")
                    .method(HttpMethod.POST).content(new FormContentProvider(fields));

            final ContentResponse cr = addTraceListener(request).timeout(timeoutSeconds, TimeUnit.SECONDS).send();
            if (HttpURLConnection.HTTP_OK != cr.getStatus()) {
                throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
            }
            validateHeaders(cr.getHeaders());
            return !getBridgeProperities(hostname, timeoutSeconds).isEmpty();
        } catch (InterruptedException e) {
            return false;
        } catch (TimeoutException e) {
            throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
        } catch (ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof UnknownHostException) {
                throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
            } else if (t instanceof SocketTimeoutException || t instanceof SocketException) {
                throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
            } else if (t instanceof SSLHandshakeException) {
                throw new NotTapLinkGatewayException(UNEXPECTED_HTTPS);
            } else {
                logger.warn("{}", getLocalizedText("ExecutionException -> {}", Utils.getMessage(e)));
            }
            throw new NotTapLinkGatewayException(getLocalizedText("exception.unexpected-failure", Utils.getMessage(e)));
        }
    }

    /**
     * Returns whether a response from the HTTP endpoint reached, appears to have the correct
     * header markers for a Link Tap Gateway device.
     *
     * @param headers the http headers from the response to be checked
     * @throws NotTapLinkGatewayException if the response does not appear to be from a Link Tap Gateway
     */
    private void validateHeaders(final HttpFields headers) throws NotTapLinkGatewayException {
        if (!headers.contains(HEADER_SERVER, HEADER_GW_SERVER_NAME)) {
            throw new NotTapLinkGatewayException(HEADERS_MISSING);
        }
    }

    public String sendRequest(final String hostname, final int timeoutSeconds, final String requestBody)
            throws NotTapLinkGatewayException, TransientCommunicationIssueException {
        try {
            final InetAddress address = InetAddress.getByName(hostname);
            logger.trace("API Endpoint: {}", URI_HOST_PREFIX + address.getHostAddress() + "/api.shtml");
            final Request request = httpClient.POST(URI_HOST_PREFIX + address.getHostAddress() + "/api.shtml")
                    .content(new StringContentProvider(requestBody), REQ_HDR_APPLICATION_JSON);

            final ContentResponse cr = addTraceListener(request).timeout(timeoutSeconds, TimeUnit.SECONDS).send();
            if (HttpURLConnection.HTTP_OK != cr.getStatus()) {
                throw new NotTapLinkGatewayException(UNEXPECTED_STATUS_CODE);
            }

            final HttpFields headers = cr.getHeaders();
            validateHeaders(headers);

            String responseData = cr.getContentAsString();
            final String contentType = headers.get(HttpHeader.CONTENT_TYPE);

            // If content type is test/html its wrapped in HTML (Old standard)
            // If content type is application/json it's a raw compact response (More efficient new standard)
            switch (contentType) {
                case "text/html":
                    final Document doc = Jsoup.parse(responseData);
                    final String docTitle = doc.title();
                    if (!docTitle.equals(TITLE_API_RESPONSE)) {
                        throw new NotTapLinkGatewayException(MISSING_API_TITLE);
                    }
                    responseData = doc.body().text();
                    break;
                case "application/json":
                    // Do nothing - the raw content is the response
                    break;
                default:
                    responseData = "";
            }

            return responseData;
        } catch (InterruptedException e) {
            return "";
        } catch (TimeoutException e) {
            throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
        } catch (UnknownHostException e) {
            throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
        } catch (ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof UnknownHostException || t instanceof SocketException) {
                throw new TransientCommunicationIssueException(HOST_NOT_RESOLVED);
            } else if (t instanceof SSLHandshakeException) {
                throw new NotTapLinkGatewayException(UNEXPECTED_HTTPS);
            } else {
                throw new TransientCommunicationIssueException(HOST_UNREACHABLE);
            }
        }
    }

    private org.eclipse.jetty.client.api.Request addTraceListener(final Request request) {
        if (logger.isTraceEnabled()) {
            return request.onRequestQueued(jettyTraceListener).onRequestBegin(jettyTraceListener)
                    .onRequestSuccess(jettyTraceListener).onRequestFailure(jettyTraceListener);
        }
        return request;
    }
}
