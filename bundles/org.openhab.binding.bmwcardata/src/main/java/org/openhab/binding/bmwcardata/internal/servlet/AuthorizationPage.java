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
package org.openhab.binding.bmwcardata.internal.servlet;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.DeviceAuthorization;

/**
 * Renders the authorization page: for every CarData account waiting for authorization, the link to the BMW portal,
 * the same link as QR code and the code to confirm. The page reloads itself while an account is not authorized, so it
 * shows a new code when the previous one expired, and the result once the user confirmed.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
final class AuthorizationPage {

    /**
     * An account as shown on the page.
     *
     * @param label label of the account thing
     * @param uid UID of the account thing
     * @param authorized whether the account is authorized
     * @param authorization the pending device authorization, if any
     * @param status the thing status, shown when the account is neither authorized nor waiting for the user
     */
    record Account(String label, String uid, boolean authorized, @Nullable DeviceAuthorization authorization,
            String status) {
    }

    /**
     * Supplies the translated texts of the page.
     */
    @FunctionalInterface
    interface Texts {
        /**
         * @param key translation key
         * @param arguments values for the placeholders of the text
         * @return the translated text, not yet HTML escaped
         */
        String get(String key, Object... arguments);
    }

    static final int REFRESH_SECONDS = 5;

    // translation keys, in the binding's translation files
    private static final String PREFIX = "authorization-page.";
    static final String TITLE = PREFIX + "title";
    static final String NO_ACCOUNT = PREFIX + "no-account";
    static final String AUTHORIZED = PREFIX + "authorized";
    static final String INSTRUCTIONS = PREFIX + "instructions";
    static final String OPEN_PORTAL = PREFIX + "open-portal";
    static final String SCAN = PREFIX + "scan";
    static final String QR_CODE = PREFIX + "qr-code";
    static final String VALID_UNTIL = PREFIX + "valid-until";
    static final String WAITING = PREFIX + "waiting";
    static final String REFRESHING = PREFIX + "refreshing";
    static final List<String> KEYS = List.of(TITLE, NO_ACCOUNT, AUTHORIZED, INSTRUCTIONS, OPEN_PORTAL, SCAN, QR_CODE,
            VALID_UNTIL, WAITING, REFRESHING);
    private static final String STYLE = """
            :root { color-scheme: light dark; --accent: #1c69d4; }
            body { font-family: system-ui, sans-serif; margin: 0 auto; max-width: 40rem; padding: 1rem;
              line-height: 1.5; }
            .account { border: 1px solid #8884; border-radius: 0.75rem; padding: 1rem 1.25rem; margin: 1rem 0; }
            .account h2 { margin: 0; font-size: 1.2rem; }
            .uid { opacity: 0.7; font-size: 0.85rem; margin: 0 0 0.75rem; }
            .button { display: inline-block; background: var(--accent); color: #fff; text-decoration: none;
              padding: 0.6rem 1.2rem; border-radius: 0.5rem; font-weight: 600; }
            .code { font-family: ui-monospace, monospace; font-size: 2rem; letter-spacing: 0.2rem; font-weight: 700; }
            .qr { width: 12rem; height: 12rem; }
            .ok { color: #2e7d32; font-weight: 600; }
            .note { opacity: 0.7; font-size: 0.9rem; }
            @media (prefers-color-scheme: dark) { .ok { color: #66bb6a; } }
            """;

    private AuthorizationPage() {
        // utility class
    }

    /**
     * @param accounts the accounts to show
     * @param zone time zone for the expiry of the codes
     * @param locale language of the page
     * @param texts the translated texts
     * @return the page
     */
    static String render(List<Account> accounts, ZoneId zone, Locale locale, Texts texts) {
        boolean waiting = accounts.stream().anyMatch(account -> !account.authorized());
        DateTimeFormatter time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale);
        String title = escape(texts.get(TITLE));
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"").append(escape(locale.toLanguageTag()))
                .append("\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        if (waiting) {
            html.append("<meta http-equiv=\"refresh\" content=\"").append(REFRESH_SECONDS).append("\">");
        }
        html.append("<title>").append(title).append("</title><style>").append(STYLE).append("</style></head><body>")
                .append("<h1>").append(title).append("</h1>");
        if (accounts.isEmpty()) {
            html.append("<p>").append(escape(texts.get(NO_ACCOUNT))).append("</p>");
        }
        for (Account account : accounts) {
            html.append("<section class=\"account\"><h2>").append(escape(account.label())).append("</h2>")
                    .append("<p class=\"uid\">").append(escape(account.uid())).append("</p>");
            @Nullable
            DeviceAuthorization authorization = account.authorization();
            if (account.authorized()) {
                html.append("<p class=\"ok\">").append(escape(texts.get(AUTHORIZED))).append("</p>");
            } else if (authorization != null) {
                String link = escape(authorization.userLink());
                // same window, which the Main UI opened for the status link: after confirming, the user can close it;
                // BMW does not return to openHAB, and a page cannot close a tab of another site
                html.append("<p>").append(escape(texts.get(INSTRUCTIONS))).append("</p>")
                        .append("<p><a class=\"button\" href=\"").append(link).append("\" rel=\"noreferrer\">")
                        .append(escape(texts.get(OPEN_PORTAL))).append("</a></p>").append("<p class=\"code\">")
                        .append(escape(authorization.userCode())).append("</p>").append("<p>")
                        .append(escape(texts.get(SCAN))).append("</p><div class=\"qr\">")
                        .append(QrSvg.toSvg(authorization.userLink(), escape(texts.get(QR_CODE)))).append("</div>")
                        .append("<p class=\"note\">")
                        .append(escape(texts.get(VALID_UNTIL, time.format(authorization.expiresAt().atZone(zone)))))
                        .append("</p>");
            } else {
                html.append("<p>").append(escape(texts.get(WAITING, account.status()))).append("</p>");
            }
            html.append("</section>");
        }
        if (waiting) {
            html.append("<p class=\"note\">").append(escape(texts.get(REFRESHING, REFRESH_SECONDS))).append("</p>");
        }
        return html.append("</body></html>").toString();
    }

    /**
     * @return the text with the characters that have a meaning in HTML replaced by entities
     */
    static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
