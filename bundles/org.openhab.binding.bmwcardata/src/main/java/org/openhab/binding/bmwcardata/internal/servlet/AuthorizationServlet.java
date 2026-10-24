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

import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.BINDING_ID;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.BMWCarDataHandlerFactory;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataAccountHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

/**
 * Serves the authorization page at {@code /bmwcardata}: the link to the BMW portal, a QR code of it and the code to
 * confirm, for every CarData account waiting for authorization. The account status links here, as the Main UI makes
 * links to openHAB itself clickable.
 * <p>
 * The page shows no secrets: the user code only lets someone confirm the authorization with their own BMW account.
 * It is in the language the browser asks for, like the Main UI's texts, or else in openHAB's language.
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(service = Servlet.class, immediate = true)
@HttpWhiteboardServletName(AuthorizationServlet.SERVLET_PATH)
@HttpWhiteboardServletPattern(AuthorizationServlet.SERVLET_PATH)
@NonNullByDefault
public class AuthorizationServlet extends HttpServlet {

    public static final String SERVLET_PATH = "/" + BINDING_ID;

    private static final long serialVersionUID = 5287430421563104713L;

    // the page embeds its style and the QR code as SVG, so it needs nothing else
    private static final String CONTENT_SECURITY_POLICY = "default-src 'none'; style-src 'unsafe-inline'";

    private final transient BMWCarDataHandlerFactory handlerFactory;
    private final transient TimeZoneProvider timeZoneProvider;
    private final transient TranslationProvider i18nProvider;
    private final transient LocaleProvider localeProvider;
    private final transient Bundle bundle;

    @Activate
    public AuthorizationServlet(@Reference BMWCarDataHandlerFactory handlerFactory,
            @Reference TimeZoneProvider timeZoneProvider, @Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        this.handlerFactory = handlerFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(AuthorizationServlet.class);
    }

    @Override
    protected void doGet(@NonNullByDefault({}) HttpServletRequest request,
            @NonNullByDefault({}) HttpServletResponse response) throws IOException {
        List<AuthorizationPage.Account> accounts = handlerFactory.getAccountHandlers().stream()
                .map(AuthorizationServlet::toAccount)
                .sorted(Comparator.comparing(AuthorizationPage.Account::label, String.CASE_INSENSITIVE_ORDER)).toList();
        response.setContentType("text/html");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Content-Type-Options", "nosniff");
        Locale locale = request.getHeader("Accept-Language") != null ? request.getLocale() : localeProvider.getLocale();
        response.getWriter().write(AuthorizationPage.render(accounts, timeZoneProvider.getTimeZone(), locale,
                (key, arguments) -> translate(key, locale, arguments)));
    }

    private String translate(String key, Locale locale, Object... arguments) {
        @Nullable
        String text = i18nProvider.getText(bundle, key, null, locale, arguments);
        return text != null ? text : key;
    }

    private static AuthorizationPage.Account toAccount(BMWCarDataAccountHandler handler) {
        Thing thing = handler.getThing();
        String uid = thing.getUID().getAsString();
        @Nullable
        String label = thing.getLabel();
        ThingStatusInfo status = thing.getStatusInfo();
        String statusText = status.getStatusDetail() == ThingStatusDetail.NONE ? status.getStatus().toString()
                : status.getStatus() + " (" + status.getStatusDetail() + ")";
        return new AuthorizationPage.Account(label != null && !label.isBlank() ? label : uid, uid,
                handler.isAuthorized(), handler.getPendingAuthorization(), statusText);
    }
}
