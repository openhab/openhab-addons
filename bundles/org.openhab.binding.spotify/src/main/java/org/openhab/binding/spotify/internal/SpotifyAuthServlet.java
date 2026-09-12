/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.spotify.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyAuthServlet} manages the authorization with the Spotify Web API. The servlet implements the
 * Authorization Code flow and saves the resulting refreshToken with the bridge.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Matthew Bowman - Initial contribution
 * @author Hilbrand Bouwkamp - Rewrite, moved service part to service class. Uses templates, simplified calls.
 */
@NonNullByDefault
public class SpotifyAuthServlet extends HttpServlet {

    private static final long serialVersionUID = -4719613645562518231L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    // Headers consulted, in order, to determine the scheme (http/https) used by the client when openHAB is
    // reachable through a reverse proxy. The first header found to be present wins.
    private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String HEADER_X_FORWARDED_SSL = "X-Forwarded-Ssl";
    private static final String HEADER_FRONT_END_HTTPS = "Front-End-Https";
    private static final String HEADER_FORWARDED = "Forwarded";
    private static final Pattern FORWARDED_PROTO_PATTERN = Pattern.compile("proto=\"?([a-zA-Z]+)\"?",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> VALID_SCHEMES = Set.of("http", "https");

    // Simple HTML templates for inserting messages.
    private static final String HTML_EMPTY_PLAYERS = "<p class='block'>Manually add a Spotify Player Bridge to authorize it here.<p>";
    private static final String HTML_USER_AUTHORIZED = "<p class='block authorized'>Bridge authorized for user %s.</p>";
    private static final String HTML_ERROR = "<p class='block error'>Call to Spotify failed with error: %s</p>";

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    // Keys present in the index.html
    private static final String KEY_PAGE_REFRESH = "pageRefresh";
    private static final String HTML_META_REFRESH_CONTENT = "<meta http-equiv='refresh' content='10; url=%s'>";
    private static final String KEY_AUTHORIZED_USER = "authorizedUser";
    private static final String KEY_ERROR = "error";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_REDIRECT_URI = "redirectUri";
    // Keys present in the player.html
    private static final String PLAYER_ID = "player.id";
    private static final String PLAYER_NAME = "player.name";
    private static final String PLAYER_SPOTIFY_USER_ID = "player.user";
    private static final String PLAYER_AUTHORIZED_CLASS = "player.authorized";
    private static final String PLAYER_AUTHORIZE = "player.authorize";

    private final Logger logger = LoggerFactory.getLogger(SpotifyAuthServlet.class);
    private final SpotifyAuthService spotifyAuthService;
    private final String indexTemplate;
    private final String playerTemplate;

    public SpotifyAuthServlet(SpotifyAuthService spotifyAuthService, String indexTemplate, String playerTemplate) {
        this.spotifyAuthService = spotifyAuthService;
        this.indexTemplate = indexTemplate;
        this.playerTemplate = playerTemplate;
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.debug("Spotify auth callback servlet received GET request {}.", req.getRequestURI());
        final String servletBaseURL = extractServletBaseURL(req);
        final Map<String, String> replaceMap = new HashMap<>();

        handleSpotifyRedirect(replaceMap, servletBaseURL, req.getQueryString());
        resp.setContentType(CONTENT_TYPE);
        replaceMap.put(KEY_REDIRECT_URI, servletBaseURL);
        replaceMap.put(KEY_PLAYERS, formatPlayers(playerTemplate, servletBaseURL));
        resp.getWriter().append(replaceKeysFromMap(indexTemplate, replaceMap));
        resp.getWriter().close();
    }

    /**
     * Extracts the servlet base url from the received http request, taking into account that openHAB may be running
     * behind a reverse proxy that terminates https, which means {@link HttpServletRequest#getRequestURL()} would
     * incorrectly report a http scheme instead of the https scheme used by the client.
     *
     * @param req the received http request
     * @return the servlet base url with the correct scheme
     */
    String extractServletBaseURL(HttpServletRequest req) {
        final StringBuffer requestURL = req.getRequestURL();
        final String scheme;

        if (spotifyAuthService.isForceHttps()) {
            logger.debug("Force HTTPS is enabled, using 'https' as the redirect URI scheme.");
            scheme = "https";
        } else {
            scheme = determineScheme(req);
        }
        return requestURL.replace(0, requestURL.indexOf(":"), scheme).toString();
    }

    /**
     * Determines the scheme (http/https) used by the client, cascading through a series of headers commonly set by
     * reverse proxies before falling back to the scheme reported by the servlet container itself, which is wrong
     * when a reverse proxy terminates https and connects to openHAB over plain http.
     *
     * @param req the received http request
     * @return the scheme to use for the redirect URI
     */
    String determineScheme(HttpServletRequest req) {
        String scheme = schemeFromForwardedProto(req);
        if (scheme == null) {
            scheme = schemeFromOnOffHeader(req, HEADER_X_FORWARDED_SSL);
        }
        if (scheme == null) {
            scheme = schemeFromOnOffHeader(req, HEADER_FRONT_END_HTTPS);
        }
        if (scheme == null) {
            scheme = schemeFromForwarded(req);
        }
        if (scheme == null) {
            scheme = req.getScheme();
            logger.debug("None of the recognized forwarded-proto headers had a valid value, falling back to the "
                    + "request scheme '{}'.", scheme);
        }
        return scheme;
    }

    private @Nullable String schemeFromForwardedProto(HttpServletRequest req) {
        final String value = logAndGetHeader(req, HEADER_X_FORWARDED_PROTO);
        if (value == null || value.isBlank()) {
            return null;
        }
        // The header may contain a comma-separated list when multiple proxies are chained; the first entry is
        // the scheme seen by the outermost proxy, i.e. the one the client actually used.
        final String candidate = value.split(",")[0].trim();
        return normalizeSchemeOrIgnore(HEADER_X_FORWARDED_PROTO, candidate);
    }

    /**
     * Parses a de-facto "on"/"off" style header (e.g. {@code X-Forwarded-Ssl}, {@code Front-End-Https}) into a
     * scheme. Any value other than the two documented ones is ignored rather than defaulting to http, so an
     * unrecognized value does not incorrectly shadow a lower-priority but valid header.
     *
     * @param req the received http request
     * @param headerName the name of the header to read
     * @return "https" for "on", "http" for "off", or {@code null} if the header is absent or has any other value
     */
    private @Nullable String schemeFromOnOffHeader(HttpServletRequest req, String headerName) {
        final String value = logAndGetHeader(req, headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        final String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("on".equals(normalized)) {
            return "https";
        } else if ("off".equals(normalized)) {
            return "http";
        } else {
            logger.debug("Header '{}' has unrecognized value '{}', expected 'on' or 'off'; ignoring it.", headerName,
                    value);
            return null;
        }
    }

    private @Nullable String schemeFromForwarded(HttpServletRequest req) {
        final String value = logAndGetHeader(req, HEADER_FORWARDED);
        if (value == null || value.isBlank()) {
            return null;
        }
        // RFC 7239, e.g. "for=1.2.3.4;proto=https;by=203.0.113.43"; only the first hop is relevant here.
        final Matcher matcher = FORWARDED_PROTO_PATTERN.matcher(value.split(",")[0]);
        if (!matcher.find()) {
            return null;
        }
        return normalizeSchemeOrIgnore(HEADER_FORWARDED, matcher.group(1));
    }

    /**
     * Normalizes a candidate scheme extracted from a header and validates it is either "http" or "https", logging
     * and returning {@code null} for anything else so the caller can fall through to the next, lower-priority
     * source instead of using an invalid scheme in the redirect URI.
     *
     * @param headerName the header the candidate was extracted from, used for logging only
     * @param candidate the candidate scheme value
     * @return the normalized scheme ("http" or "https"), or {@code null} if the candidate is not a valid scheme
     */
    private @Nullable String normalizeSchemeOrIgnore(String headerName, String candidate) {
        final String normalized = candidate.toLowerCase(Locale.ROOT);
        if (VALID_SCHEMES.contains(normalized)) {
            return normalized;
        }
        logger.debug("Header '{}' has unrecognized scheme value '{}', expected 'http' or 'https'; ignoring it.",
                headerName, candidate);
        return null;
    }

    /**
     * Reads the given header from the request and logs whether it was present and, if so, its value.
     *
     * @param req the received http request
     * @param headerName the name of the header to read
     * @return the header value, or {@code null} if not present
     */
    private @Nullable String logAndGetHeader(HttpServletRequest req, String headerName) {
        final String value = req.getHeader(headerName);
        if (value == null) {
            logger.debug("Header '{}' is not present on the request.", headerName);
        } else {
            logger.debug("Header '{}' is present with value '{}'.", headerName, value);
        }
        return value;
    }

    /**
     * Handles a possible call from Spotify to the redirect_uri. If that is the case Spotify will pass the authorization
     * codes via the url and these are processed. In case of an error this is shown to the user. If the user was
     * authorized this is passed on to the handler. Based on all these different outcomes the HTML is generated to
     * inform the user.
     *
     * @param replaceMap a map with key String values that will be mapped in the HTML templates.
     * @param servletBaseURL the servlet base, which should be used as the Spotify redirect_uri value
     * @param queryString the query part of the GET request this servlet is processing
     */
    private void handleSpotifyRedirect(Map<String, String> replaceMap, String servletBaseURL,
            @Nullable String queryString) {
        replaceMap.put(KEY_AUTHORIZED_USER, "");
        replaceMap.put(KEY_ERROR, "");
        replaceMap.put(KEY_PAGE_REFRESH, "");

        if (queryString != null) {
            final MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(queryString, params, StandardCharsets.UTF_8.name());
            final String reqCode = params.getString("code");
            final String reqState = params.getString("state");
            final String reqError = params.getString("error");

            replaceMap.put(KEY_PAGE_REFRESH,
                    params.isEmpty() ? "" : String.format(HTML_META_REFRESH_CONTENT, servletBaseURL));
            if (!StringUtil.isBlank(reqError)) {
                logger.debug("Spotify redirected with an error: {}", reqError);
                replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, reqError));
            } else if (!StringUtil.isBlank(reqState)) {
                try {
                    replaceMap.put(KEY_AUTHORIZED_USER, String.format(HTML_USER_AUTHORIZED,
                            spotifyAuthService.authorize(servletBaseURL, reqState, reqCode)));
                } catch (RuntimeException e) {
                    logger.debug("Exception during authorizaton: ", e);
                    replaceMap.put(KEY_ERROR, String.format(HTML_ERROR, e.getMessage()));
                }
            }
        }
    }

    /**
     * Formats the HTML of all available Spotify Bridge Players and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the players formatted with the player template
     */
    private String formatPlayers(String playerTemplate, String servletBaseURL) {
        final List<SpotifyAccountHandler> players = spotifyAuthService.getSpotifyAccountHandlers();

        return players.isEmpty() ? HTML_EMPTY_PLAYERS
                : players.stream().map(p -> formatPlayer(playerTemplate, p, servletBaseURL))
                        .collect(Collectors.joining());
    }

    /**
     * Formats the HTML of a Spotify Bridge Player and returns it as a String
     *
     * @param playerTemplate The player template to format the player values in
     * @param handler The handler for the player to format
     * @param servletBaseURL the redirect_uri to be used in the authorization url created on the authorization button.
     * @return A String with the player formatted with the player template
     */
    private String formatPlayer(String playerTemplate, SpotifyAccountHandler handler, String servletBaseURL) {
        final Map<String, String> map = new HashMap<>();

        map.put(PLAYER_ID, handler.getUID().getAsString());
        map.put(PLAYER_NAME, handler.getLabel());
        final String spotifyUser = handler.getUser();

        if (handler.isAuthorized()) {
            map.put(PLAYER_AUTHORIZED_CLASS, " authorized");
            map.put(PLAYER_SPOTIFY_USER_ID, String.format(" (Authorized user: %s)", spotifyUser));
        } else if (!StringUtil.isBlank(spotifyUser)) {
            map.put(PLAYER_AUTHORIZED_CLASS, " Unauthorized");
            map.put(PLAYER_SPOTIFY_USER_ID, String.format(" (Unauthorized user: %s)", spotifyUser));
        } else {
            map.put(PLAYER_AUTHORIZED_CLASS, "");
            map.put(PLAYER_SPOTIFY_USER_ID, "");
        }

        map.put(PLAYER_AUTHORIZE, handler.formatAuthorizationUrl(servletBaseURL));
        return replaceKeysFromMap(playerTemplate, map);
    }

    /**
     * Replaces all keys from the map found in the template with values from the map. If the key is not found the key
     * will be kept in the template.
     *
     * @param template template to replace keys with values
     * @param map map with key value pairs to replace in the template
     * @return a template with keys replaced
     */
    private String replaceKeysFromMap(String template, Map<String, String> map) {
        final Matcher m = MESSAGE_KEY_PATTERN.matcher(template);
        final StringBuffer sb = new StringBuffer();

        while (m.find()) {
            try {
                final String key = m.group(1);
                m.appendReplacement(sb, Matcher.quoteReplacement(map.getOrDefault(key, "${" + key + '}')));
            } catch (RuntimeException e) {
                logger.debug("Error occurred during template filling, cause ", e);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
