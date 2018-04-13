/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.spotify.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.spotify.handler.SpotifyHandler;
import org.openhab.binding.spotify.internal.SpotifySession.SpotifyWebAPIAuthResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * The {@link SpotifyAuthServlet} manages the authorization with the Spotify Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Matthew Bowman - Initial contribution
 */
@Component(service = SpotifyAuthService.class, immediate = true, configurationPid = "binding.spotify.authService", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SpotifyAuthServlet extends HttpServlet implements SpotifyAuthService {

    /**
     *
     */
    private static final long serialVersionUID = -4719613645562518231L;

    public static final String SERVLET_NAME = "/connectspotify";
    public static final String WEBAPP_ALIAS = SERVLET_NAME + "/web";
    public static final String CALLBACK_ALIAS = SERVLET_NAME + "/authorize";

    private final Logger logger = LoggerFactory.getLogger(SpotifyAuthServlet.class);

    // TODO: clean up required scopes, currently all which is unnecessary
    private String[] scopes = new String[] { "playlist-read-private", "playlist-read-collaborative",
            "playlist-modify-public", "playlist-modify-private streaming", "user-follow-modify", "user-follow-read",
            "user-library-read", "user-library-modify", "user-read-private", "user-read-birthdate", "user-read-email",
            "user-top-read", "user-read-playback-state", "user-read-recently-played", "user-modify-playback-state",
            "user-read-currently-playing" };

    private HttpService httpService;

    // keep track of handler that require authentication.
    final private List<SpotifyHandler> handlers = new ArrayList<SpotifyHandler>();

    // keep track of session cookies and related handler - the cookie state value is used in authentication flow
    final private Map<String, SpotifyHandler> cookieHandler = new HashMap<String, SpotifyHandler>();
    final String stateKey = "spotify_auth_state";

    /**
     * Sets the httpService from eclipse OSGI framework.
     *
     * @param httpService the shared http service
     */
    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;

        try {
            logger.debug("Starting up the spotify auth callback servlet at " + SERVLET_NAME);

            httpService.registerServlet(SERVLET_NAME, this, null, createHttpContext());
            httpService.registerResources(WEBAPP_ALIAS, "web", null);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    /**
     * Unset the httpService from eclipse OSGI framework.
     *
     * @param httpService the shared http service
     */
    protected void unsetHttpService(HttpService httpService) {
        httpService.unregister(WEBAPP_ALIAS);
        httpService.unregister(SERVLET_NAME);
        this.httpService = null;
    }

    /**
     * Creates an {@link HttpContext}.
     *
     * @return an {@link HttpContext} that grants anonymous access
     */
    protected HttpContext createHttpContext() {
        HttpContext httpContext = httpService.createDefaultHttpContext();
        return httpContext;
    }

    /*
     * This is method provides the Authorization Code flow
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Spotify auth callback servlet received GET request {}.", req.getRequestURI());

        final String scheme = req.getScheme();
        final String host = req.getServerName();
        final int port = req.getServerPort();
        final String servletBaseURL = String.format("%s://%s:%d", scheme, host, port);

        final Map<String, String> queryStrs = splitQuery(req.getQueryString());
        final String url = req.getRequestURI();

        if (url.startsWith(WEBAPP_ALIAS)) {
            // let base Servlet implementation manage file request
            super.doGet(req, resp);

        } else if (url.equals(SERVLET_NAME + "/")) {
            // help finding the index page as default
            String indexPage = WEBAPP_ALIAS + "/index.html";
            resp.sendRedirect(resp.encodeRedirectURL(indexPage));

        } else if (url.equals(CALLBACK_ALIAS)) {
            // this entry point is for step 3 of the authorization flow
            // See https://developer.spotify.com/web-api/authorization-guide/#authorization-code-flow

            logger.debug("Spotify auth callback servlet received GET /authorize request.");

            final String reqCode = queryStrs.get("code");
            final String reqState = queryStrs.get("state");
            final String reqError = queryStrs.get("error");

            Cookie state = null;
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals(stateKey)) {
                    state = cookie;
                    break;
                }
            }

            if (reqError != null) {
                logger.error("Spotify auth callback servlet received GET /authorize request with error: {}", reqError);
            } else if (state == null) {
                logger.error("Spotify auth callback servlet received GET /authorize request without state cookie.");
            } else if (!state.getValue().equals(reqState)) {
                logger.error(
                        "Spotify auth callback servlet received GET /authorize request without matching state {} != {}.",
                        reqState, state.getValue());
            } else {
                SpotifyHandler authHandler = cookieHandler.get(state.getValue());

                if (authHandler != null) {
                    SpotifyWebAPIAuthResult result = authHandler.getSpotifySession()
                            .authenticate(servletBaseURL + CALLBACK_ALIAS, reqCode);

                    String clientId = (String) authHandler.getThing().getConfiguration().get("clientId");
                    String clientSecret = (String) authHandler.getThing().getConfiguration().get("clientSecret");
                    resp.setContentType("text/html");
                    resp.setCharacterEncoding("UTF-8");
                    resp.setStatus(200);

                    // TODO: replace with prettier solution...
                    PrintWriter wrout = resp.getWriter();
                    wrout.println(
                            "<html><head><title>Authenticated: Eclipse Smarthome Spotify Connect Bridge</title></head><body>");
                    wrout.println("<h1>Authenticated with Spotify!</h1>");
                    wrout.println("<p>Client ID: <b>" + clientId + "</b></p>");
                    wrout.println("<p>Client Secret: <b>" + clientSecret + "</b></p>");
                    wrout.println("<p>Access Token: <b>" + result.getAccessToken() + "</b></p>");
                    wrout.println("<p>Refresh Token: <b>" + result.getRefreshToken() + "</b><p>");
                    wrout.println("<p>Token Type: <b>" + result.getTokenType() + "</b></p>");
                    wrout.println("<p>Validity: <b>" + result.getExpiresIn() + "</b> seconds</p>");
                    wrout.println("<p>Allowed scopes: <b>" + result.getScope() + "</b></p>");

                    wrout.println("<p><a href=\"" + servletBaseURL + SERVLET_NAME + "/\">Back to start page<a/></p>");
                    wrout.println("</body></html>");
                    wrout.flush();

                    authHandler.initializeSession(clientId, clientSecret, result.getRefreshToken());
                    handlers.remove(authHandler);

                } else {
                    RequestDispatcher dispatcher = getServletContext()
                            .getRequestDispatcher(WEBAPP_ALIAS + "/unkownHandler.html");
                    dispatcher.forward(req, resp);
                }

            }
        } else if (url.equals(SERVLET_NAME + "/redirect_uri")) {
            // this entry point is provided for index.html to present the expected Redirect URIs.
            logger.debug("Spotify auth callback servlet received GET /list request");
            String redirectUri = servletBaseURL + CALLBACK_ALIAS;
            PrintWriter wrout = resp.getWriter();
            wrout.println("{ \"redirect_uri\" : \"" + redirectUri + "\"}");

        } else if (url.equals(SERVLET_NAME + "/remove")) {
            // this entry point is provided for index.html to remove devices to authorize.
            logger.debug("Spotify auth callback servlet received GET /remove request");

            // expects the thing uid of a Spotify bridge
            final String spotifyHandlerId = req.getParameter("playerId");

            SpotifyHandler authHandler = getHandler(spotifyHandlerId);
            if (authHandler != null) {
                handlers.remove(authHandler);
            }

            String indexPage = WEBAPP_ALIAS + "/index.html";
            resp.sendRedirect(resp.encodeRedirectURL(indexPage));

        } else if (url.equals(SERVLET_NAME + "/list")) {
            // this entry point is provided for index.html to retrieve the bridge(s) to authorize.
            logger.debug("Spotify auth callback servlet received GET /list request");

            List<Player> players = new ArrayList<Player>();

            for (SpotifyHandler handler : handlers) {
                Player player = new Player();
                player.setId(handler.getThing().getUID().getAsString());
                player.setLabel(handler.getThing().getLabel());
                player.setClientId((String) handler.getThing().getConfiguration().get("clientId"));
                players.add(player);
            }

            Gson gson = new Gson();
            PrintWriter wrout = resp.getWriter();
            wrout.println(gson.toJson(players));

        } else if (url.equals(SERVLET_NAME + "/login")) {
            // this entry point is for step 1 of the authorization flow
            // See https://developer.spotify.com/web-api/authorization-guide/#authorization-code-flow

            // expects the thing uid of a Spotify bridge
            final String spotifyHandlerId = req.getParameter("playerId");

            logger.debug("Spotify auth callback servlet received GET /login request for {}.", spotifyHandlerId);

            SpotifyHandler authHandler = getHandler(spotifyHandlerId);

            if (authHandler != null) {

                final String stateValue = generateRandomStateString(16);
                Cookie state = new Cookie(stateKey, stateValue);
                resp.addCookie(state);

                cookieHandler.put(stateValue, authHandler);

                String reqScope = new String();
                for (String scope : scopes) {
                    reqScope += scope + "%20";
                }

                String clientId = (String) authHandler.getThing().getConfiguration().get("clientId");

                String queryString = String.format("client_id=%s&response_type=code&redirect_uri=%s&state=%s&scope=%s",
                        clientId, URLEncoder.encode(servletBaseURL + CALLBACK_ALIAS, "UTF-8"), stateValue, reqScope);

                resp.sendRedirect(String.format("https://accounts.spotify.com/authorize/?%s", queryString));
            } else {
                RequestDispatcher dispatcher = getServletContext()
                        .getRequestDispatcher(WEBAPP_ALIAS + "/unkownHandler.html");
                dispatcher.forward(req, resp);

            }
        }
    }

    /**
     * @param spotifyHandlerId
     * @param authHandler
     * @return
     */
    private SpotifyHandler getHandler(final String spotifyHandlerId) {

        for (SpotifyHandler handler : handlers) {
            if (handler.getThing().getUID().getAsString().equals(spotifyHandlerId)) {
                return handler;
            }
        }
        return null;
    }

    /*
     * Direct conversion from Spotify node.js example implementation. Smarter alternatives?
     */
    private static String generateRandomStateString(int length) {
        String state = new String();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            state += possible.charAt((int) Math.floor(Math.random() * possible.length()));
        }
        return state;
    }

    /*
     * Are there no standard utility functions for this?
     */
    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        final Map<String, String> keyVals = new HashMap<String, String>();
        if (query != null) {
            final String[] keyValPairs = query.split("&");
            for (String keyVal : keyValPairs) {
                final int idx = keyVal.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(keyVal.substring(0, idx), "UTF-8") : keyVal;
                final String value = idx > 0 && keyVal.length() > idx + 1
                        ? URLDecoder.decode(keyVal.substring(idx + 1), "UTF-8") : null;
                keyVals.put(key, value);
            }
        }
        return keyVals;
    }

    @Override
    public void authenticateSpotifyPlayer(SpotifyHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    /**
     * Inner class used to serialize data to JSON
     */
    public class Player {
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("clientId")
        @Expose
        private String clientId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }
}
