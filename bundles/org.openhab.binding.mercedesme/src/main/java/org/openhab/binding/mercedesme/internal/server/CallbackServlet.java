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
package org.openhab.binding.mercedesme.internal.server;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.dto.PINRequest;
import org.openhab.binding.mercedesme.internal.dto.TokenResponse;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CallbackServlet} class provides authentication callback endpoint
 *
 * @author Bernd Weymann - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class CallbackServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(CallbackServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CallbackServer myServer = CallbackServer.getServer(request.getLocalPort());
        HttpClient client = myServer.getHttpClient();
        String guid = request.getParameter(Constants.GUID);
        String pin = request.getParameter(Constants.PIN);
        if (guid == null && pin == null) {
            // request PIN

            String url = Utils.getAuthURL(myServer.getRegion());
            Request req = client.POST(url);
            req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
            req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
            req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(myServer.getRegion()));
            req.header("X-Locale", myServer.getLocale().getLanguage() + "-" + myServer.getLocale().getCountry()); // de-DE
            req.header("User-Agent", Utils.getApplication(myServer.getRegion()));
            req.header("X-Applicationname", Utils.getUserAgent(myServer.getRegion()));
            req.header("Ris-Application-Version", Utils.getRisApplicationVersion(myServer.getRegion()));
            req.header("X-Trackingid", UUID.randomUUID().toString());
            req.header("X-Sessionid", UUID.randomUUID().toString());
            req.header(HttpHeader.CONTENT_TYPE, "application/json");

            PINRequest pr = new PINRequest(myServer.getMail(), myServer.getLocale().getCountry());
            req.content(new StringContentProvider(Utils.GSON.toJson(pr), "utf-8"));

            try {
                ContentResponse cr = req.send();
                if (cr.getStatus() == 200) {
                    logger.debug("Success requesting PIN");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("<HTML>");
                    response.getWriter().println("<BODY>");
                    response.getWriter().println("PIN received?<BR>");
                    response.getWriter().println(
                            "<a href=\"" + request.getLocalAddr() + "?guid=" + pr.nonce + "\">Click here to enter</a>");
                    response.getWriter().println("</BODY>");
                    response.getWriter().println("</HTML>");

                } else {
                    logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error getting image resources {}", e.getMessage());
            }

        } else if (guid != null && pin == null) {
            // show instert PIN input field

            String url = "/mb-auth?guid=" + guid;
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<HTML>");
            response.getWriter().println("<BODY>");
            response.getWriter().println("<form action=\"" + request.getLocalAddr() + "?guid=" + guid + "\">");

            response.getWriter().println("<label for=\"PIN\">PIN</label>");
            response.getWriter().println("");
            response.getWriter().println("<input type=\"text\" id=\"pin\" name=\"PIN\" placeholder=\"Your PIN");
            response.getWriter().println("</form>");
            response.getWriter().println("</BODY>");
            response.getWriter().println("</HTML>");
        } else if (guid != null && pin != null) {
            // call getToken and show result

            String url = Utils.getTokenUrl(myServer.getRegion());
            logger.info("Get Token base URL {}", url);
            String clientid = URLEncoder.encode("client_id=" + Utils.getLoginAppId(myServer.getRegion()), "utf-8");
            String grant = "grant_type=password";
            String user = URLEncoder.encode("username=" + myServer.getMail(), "utf-8");
            String password = URLEncoder.encode("password=" + guid + ":" + pin, "utf-8");
            String scope = URLEncoder.encode("scope=" + Constants.SCOPE, "utf-8");
            url = url + "?" + clientid + "&" + grant + "&" + user + "&" + password + "&" + scope;
            logger.info("Get Token URL {}", url);
            Request req = client.POST(url);
            req.header("Ris-Os-Name", Constants.RIS_OS_NAME);
            req.header("Ris-Os-Version", Constants.RIS_OS_VERSION);
            req.header("Ris-Sdk-Version", Utils.getRisSDKVersion(myServer.getRegion()));
            req.header("X-Locale", myServer.getLocale().getLanguage() + "-" + myServer.getLocale().getCountry()); // de-DE
            req.header("User-Agent", Utils.getApplication(myServer.getRegion()));
            req.header("X-Applicationname", Utils.getUserAgent(myServer.getRegion()));
            req.header("Ris-Application-Version", Utils.getRisApplicationVersion(myServer.getRegion()));
            req.header("X-Device-Id", UUID.randomUUID().toString());
            req.header("X-Request-Id", UUID.randomUUID().toString());
            req.header("Stage", "prod");
            req.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
            try {
                ContentResponse cr = req.send();
                if (cr.getStatus() == 200) {
                    logger.info("Success getting token");
                    TokenResponse tr = Utils.GSON.fromJson(cr.getContentAsString(), TokenResponse.class);
                    AccessTokenResponse atr = new AccessTokenResponse();
                    atr.setAccessToken(tr.access_token);
                    atr.setCreatedOn(Instant.now());
                    atr.setExpiresIn(tr.expires_in);
                    atr.setRefreshToken(tr.refresh_token);
                    atr.setTokenType("Bearer");
                    atr.setScope(Constants.SCOPE);
                    logger.info("ATR {}", atr);
                } else {
                    logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.debug("Error getting image resources {}", e.getMessage());
            }
        }
        logger.debug("Call from {}:{} parameters {}", request.getLocalAddr(), request.getLocalPort(),
                request.getParameterMap());
    }
}
