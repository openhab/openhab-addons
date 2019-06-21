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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Abstract servlet.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TEMPLATE_BASE_PATH = "/templates/";
    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");
    protected static final String SERVLET_BASE_PATH = "/homeconnect";
    protected static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(AbstractServlet.class);

    protected String readHtmlTemplate(String htmlTemplate) throws IOException {
        final URL templateUrl = FrameworkUtil.getBundle(getClass()).getEntry(TEMPLATE_BASE_PATH + htmlTemplate);

        if (templateUrl == null) {
            throw new FileNotFoundException("Cannot find template file '" + htmlTemplate + "'.");
        }

        try (InputStream inputStream = templateUrl.openStream()) {
            return IOUtils.toString(inputStream);
        }
    }

    protected String replaceKeysFromMap(String template, Map<String, String> map) {
        final Matcher m = MESSAGE_KEY_PATTERN.matcher(template);
        final StringBuffer sb = new StringBuffer();

        while (m.find()) {
            try {
                final String key = m.group(1);
                m.appendReplacement(sb, Matcher.quoteReplacement(map.getOrDefault(key, "${" + key + '}')));
            } catch (RuntimeException e) {
                logger.error("Error occurred during template filling, cause ", e);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    protected void addNoCacheHeader(HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");
    }

}
