/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.HomeConnectSseClient;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.Log;
import org.openhab.binding.homeconnect.internal.logger.Request;
import org.openhab.binding.homeconnect.internal.logger.Response;
import org.openhab.binding.homeconnect.internal.logger.Type;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * HomeConnect log viewer servlet.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
@Component(service = LogViewerServlet.class, scope = ServiceScope.SINGLETON, immediate = true)
public class LogViewerServlet extends AbstractServlet {

    private static final long serialVersionUID = -8222881609153046802L;
    private static final String TEMPLATE = "log_viewer.html";
    private static final String TEMPLATE_LOG = "part_log_default.html";
    private static final String TEMPLATE_DETAILS = "part_log_detail.html";
    private static final String PLACEHOLDER_KEY_DATE = "datetime";
    private static final String PLACEHOLDER_KEY_CLASS = "class";
    private static final String PLACEHOLDER_KEY_LEVEL = "level";
    private static final String PLACEHOLDER_KEY_MESSAGE = "message";
    private static final String PLACEHOLDER_KEY_DETAILS = "details";
    private static final String PLACEHOLDER_KEY_EXPANDABLE = "expand";
    private static final String PLACEHOLDER_KEY_DETAIL = "detail";
    private static final String PLACEHOLDER_KEY_STYLING_TYPE = "type";
    private static final String PLACEHOLDER_KEY_HA_ID = "haId";
    private static final String PLACEHOLDER_KEY_LOGS = "logs";
    private static final String SERVLET_LOG_VIEWER_PATH = SERVLET_BASE_PATH + "logs";
    private static final Pattern HA_ID_PATTERN = Pattern.compile("[A-Z]{5,}-\\w{6,}-\\w{6,}");

    private final Logger logger = LoggerFactory.getLogger(LogViewerServlet.class);
    private final EmbeddedLoggingService loggingService;
    private final HttpService httpService;
    private final DateTimeFormatter dtf;

    @Activate
    public LogViewerServlet(@Reference HttpService httpService, @Reference EmbeddedLoggingService loggingService) {
        this.httpService = httpService;
        this.loggingService = loggingService;
        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z");

        try {
            logger.info("Initialize log viewer servlet ({})", SERVLET_LOG_VIEWER_PATH);
            httpService.registerServlet(SERVLET_LOG_VIEWER_PATH, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException e) {
            try {
                httpService.unregister(SERVLET_LOG_VIEWER_PATH);
                httpService.registerServlet(SERVLET_LOG_VIEWER_PATH, this, null,
                        httpService.createDefaultHttpContext());
            } catch (ServletException | NamespaceException ex) {
                logger.error("Could not register log viewer servlet! ({})", SERVLET_LOG_VIEWER_PATH, ex);
            }
        } catch (ServletException e) {
            logger.error("Could not register log viewer servlet! ({})", SERVLET_LOG_VIEWER_PATH, e);
        }
    }

    @Deactivate
    protected void dispose() {
        try {
            logger.info("Unregister log viewer servlet ({}).", SERVLET_LOG_VIEWER_PATH);
            httpService.unregister(SERVLET_LOG_VIEWER_PATH);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not unregister log viewer servlet. Failed wth {}", e.getMessage());
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("GET {}", SERVLET_LOG_VIEWER_PATH);

        if (request == null || response == null) {
            throw new ServletException("Illegal state - Could not handle request!");
        }

        // clear storage
        if (request.getQueryString() != null && request.getQueryString().contains("clear")) {
            loggingService.clear();
        }

        addNoCacheHeader(response);

        // index page
        List<Log> logs = loggingService.getLogEntries();
        final HashMap<String, String> replaceMap = new HashMap<>();
        if (logs.isEmpty()) {
            replaceMap.put(PLACEHOLDER_KEY_LOGS, "<tr><td>No logs available</td></tr>");
        } else {
            replaceMap.put(PLACEHOLDER_KEY_LOGS,
                    logs.stream().map(logEntry -> renderLogPart(logEntry)).collect(Collectors.joining()));
        }

        if (request.getQueryString() != null && request.getQueryString().contains("export")) {
            ServletOutputStream sos = response.getOutputStream();
            response.setContentType("application/zip");

            String filePrefix = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filePrefix + ".zip.txt\"");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry(filePrefix + ".html"));

            String content = replaceKeysFromMap(readHtmlTemplate(TEMPLATE), replaceMap);
            zos.write(pseudonymize(content).getBytes());
            zos.closeEntry();
            zos.flush();
            baos.flush();
            zos.close();
            baos.close();

            byte[] zipOutput = baos.toByteArray();

            sos.write(zipOutput);
            sos.flush();
        } else {
            response.setContentType(CONTENT_TYPE);
            response.getWriter().append(replaceKeysFromMap(readHtmlTemplate(TEMPLATE), replaceMap));
            response.getWriter().close();
        }
    }

    private String renderLogPart(Log entry) {
        HashMap<String, String> replaceMap = new HashMap<>();

        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(entry.getCreated()), ZoneId.systemDefault());
        replaceMap.put(PLACEHOLDER_KEY_DATE, zdt.format(dtf));
        replaceMap.put(PLACEHOLDER_KEY_CLASS, entry.getClassName().replace("HomeConnect", ""));
        replaceMap.put(PLACEHOLDER_KEY_LEVEL, entry.getLevel().toString());
        String haId = entry.getHaId();
        replaceMap.put(PLACEHOLDER_KEY_HA_ID, haId == null ? "" : haId);

        Request request = entry.getRequest();
        Response response = entry.getResponse();
        if (request != null && (entry.getType() == Type.API_CALL || entry.getType() == Type.API_ERROR)) {
            StringBuilder sb = new StringBuilder();
            sb.append(request.getMethod()).append(" ");
            if (response != null) {
                sb.append(response.getCode()).append(" ");
            }
            sb.append(request.getUrl());
            replaceMap.put(PLACEHOLDER_KEY_MESSAGE, sb.toString());
        } else {
            if (entry.getLabel() != null) {
                replaceMap.put(PLACEHOLDER_KEY_MESSAGE,
                        (entry.getMessage() == null ? "" : entry.getMessage()) + " (" + entry.getLabel() + ")");
            } else {
                String message = entry.getMessage();
                replaceMap.put(PLACEHOLDER_KEY_MESSAGE, message == null ? "" : message);
            }
        }

        // CSS highlighting
        String stylingType = "default";
        if (entry.getType() == Type.API_CALL || entry.getType() == Type.API_ERROR) {
            stylingType = "api";
        } else if (HomeConnectSseClient.class.getSimpleName().equals(entry.getClassName())) {
            stylingType = "sse";
        }
        replaceMap.put(PLACEHOLDER_KEY_STYLING_TYPE, stylingType);

        // details
        String details = renderDetails(entry);
        replaceMap.put(PLACEHOLDER_KEY_DETAILS, details);
        replaceMap.put(PLACEHOLDER_KEY_EXPANDABLE, StringUtils.isEmpty(details) ? "" : "class=\"pointer\"");

        try {
            return replaceKeysFromMap(readHtmlTemplate(TEMPLATE_LOG), replaceMap);
        } catch (IOException e) {
            logger.error("Could not render template {}!", TEMPLATE_LOG, e);
            return "";
        }
    }

    private String renderDetails(Log entry) {
        StringBuffer sb = new StringBuffer();
        HashMap<String, String> replaceMap = new HashMap<>();

        Request request = entry.getRequest();
        Response response = entry.getResponse();
        if (request != null && (entry.getType() == Type.API_CALL || entry.getType() == Type.API_ERROR)) {
            request.getHeader().forEach((key, value) -> {
                if (value.startsWith("[Bearer ")) {
                    sb.append("> ").append(key).append(": ").append("[Bearer ***]").append("\n");
                } else {
                    sb.append("> ").append(key).append(": ").append(value).append("\n");
                }
            });

            if (entry.getRequest() != null && request.getBody() != null) {
                sb.append(request.getBody()).append("\n");
            }

            if (response != null) {
                sb.append("\n");
                response.getHeader()
                        .forEach((key, value) -> sb.append("< ").append(key).append(": ").append(value).append("\n"));
            }
            if (response != null && response.getBody() != null) {
                sb.append(response.getBody()).append("\n");
            }
        }

        List<String> details = entry.getDetails();
        if (details != null) {
            details.forEach(detail -> sb.append("\n").append(detail));
        }

        String content = sb.toString();
        replaceMap.put(PLACEHOLDER_KEY_DETAIL, content);

        try {
            if (StringUtils.isEmpty(content)) {
                return "";
            }
            return replaceKeysFromMap(readHtmlTemplate(TEMPLATE_DETAILS), replaceMap);
        } catch (IOException e) {
            logger.error("Could not render template {}!", TEMPLATE_DETAILS, e);
            return "";
        }
    }

    private String pseudonymize(String content) {
        StringBuffer sb = new StringBuffer(content.length());
        Matcher haIdMatcher = HA_ID_PATTERN.matcher(content);

        while (haIdMatcher.find()) {
            String haId = haIdMatcher.group();
            String haIdPart[] = haId.split("-");
            if (haIdPart.length == 3) {
                String id2 = sha1(haIdPart[2]);
                String pseudonymizedHaId = haIdPart[0] + "-" + haIdPart[1] + "-"
                        + (id2 == null ? "XXXXXXXXXXXX" : "XXXXX" + id2.substring(5, 12));
                haIdMatcher.appendReplacement(sb, Matcher.quoteReplacement(pseudonymizedHaId));
            }
        }
        haIdMatcher.appendTail(sb);

        return sb.toString();
    }

    private @Nullable String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            logger.error("Could not create SHA-1 hash.", e);
        }
        return sha1;
    }
}
