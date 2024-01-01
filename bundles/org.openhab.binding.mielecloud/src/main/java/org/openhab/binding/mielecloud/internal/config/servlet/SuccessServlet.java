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
package org.openhab.binding.mielecloud.internal.config.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.config.ThingsTemplateGenerator;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet showing the success page.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class SuccessServlet extends AbstractShowPageServlet {
    private static final long serialVersionUID = 7013060161686096950L;

    public static final String BRIDGE_UID_PARAMETER_NAME = "bridgeUid";
    public static final String EMAIL_PARAMETER_NAME = "email";

    public static final String BRIDGE_CREATION_FAILED_PARAMETER_NAME = "bridgeCreationFailed";
    public static final String BRIDGE_RECONFIGURATION_FAILED_PARAMETER_NAME = "bridgeReconfigurationFailed";

    private static final String ERROR_MESSAGE_TEXT_PLACEHOLDER = "<!-- ERROR MESSAGE TEXT -->";
    private static final String BRIDGE_UID_PLACEHOLDER = "<!-- BRIDGE UID -->";
    private static final String EMAIL_PLACEHOLDER = "<!-- EMAIL -->";
    private static final String THINGS_TEMPLATE_CODE_PLACEHOLDER = "<!-- THINGS TEMPLATE CODE -->";

    private static final String LOCALE_OPTIONS_PLACEHOLDER = "<!-- LOCALE OPTIONS -->";

    private static final String DEFAULT_LANGUAGE = "en";
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("da", "nl", "en", "fr", "de", "it", "nb", "es");

    private final Logger logger = LoggerFactory.getLogger(SuccessServlet.class);

    private final LanguageProvider languageProvider;
    private final ThingsTemplateGenerator templateGenerator;

    /**
     * Creates a new {@link SuccessServlet}.
     *
     * @param resourceLoader Loader for resources.
     * @param languageProvider Provider for the language to use as default selection.
     */
    public SuccessServlet(ResourceLoader resourceLoader, LanguageProvider languageProvider) {
        super(resourceLoader);
        this.languageProvider = languageProvider;
        this.templateGenerator = new ThingsTemplateGenerator();
    }

    @Override
    protected String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws MieleHttpException, IOException {
        String bridgeUidString = request.getParameter(BRIDGE_UID_PARAMETER_NAME);
        if (bridgeUidString == null || bridgeUidString.isEmpty()) {
            logger.warn("Success page is missing bridge UID.");
            return getResourceLoader().loadResourceAsString("failure.html").replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                    "Missing bridge UID.");
        }

        String email = request.getParameter(EMAIL_PARAMETER_NAME);
        if (email == null || email.isEmpty()) {
            logger.warn("Success page is missing e-mail address.");
            return getResourceLoader().loadResourceAsString("failure.html").replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                    "Missing e-mail address.");
        }

        ThingUID bridgeUid = null;
        try {
            bridgeUid = new ThingUID(bridgeUidString);
        } catch (IllegalArgumentException e) {
            logger.warn("Success page received malformed bridge UID '{}'.", bridgeUidString);
            return getResourceLoader().loadResourceAsString("failure.html").replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                    "Malformed bridge UID.");
        }

        String skeleton = getResourceLoader().loadResourceAsString("success.html");
        skeleton = renderErrorMessage(request, skeleton);
        skeleton = renderBridgeUid(skeleton, bridgeUid);
        skeleton = renderEmail(skeleton, email);
        skeleton = renderLocaleSelection(skeleton);
        skeleton = renderBridgeConfigurationTemplate(skeleton, bridgeUid, email);
        return skeleton;
    }

    private String renderErrorMessage(HttpServletRequest request, String skeleton) {
        if (ServletUtil.isParameterEnabled(request, BRIDGE_CREATION_FAILED_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Could not auto configure the bridge. Failed to approve the bridge from the inbox. Please try the configuration flow again.</div>");
        } else if (ServletUtil.isParameterEnabled(request, BRIDGE_RECONFIGURATION_FAILED_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Could not auto reconfigure the bridge. Bridge thing or thing handler is not available. Please try the configuration flow again.</div>");
        } else {
            return skeleton.replace(ERROR_MESSAGE_TEXT_PLACEHOLDER, "");
        }
    }

    private String renderBridgeUid(String skeleton, ThingUID bridgeUid) {
        return skeleton.replace(BRIDGE_UID_PLACEHOLDER, bridgeUid.getAsString());
    }

    private String renderEmail(String skeleton, String email) {
        return skeleton.replace(EMAIL_PLACEHOLDER, email);
    }

    private String renderLocaleSelection(String skeleton) {
        String preSelectedLanguage = languageProvider.getLanguage().filter(SUPPORTED_LANGUAGES::contains)
                .orElse(DEFAULT_LANGUAGE);

        return skeleton.replace(LOCALE_OPTIONS_PLACEHOLDER,
                SUPPORTED_LANGUAGES.stream().map(Language::fromCode).filter(Optional::isPresent).map(Optional::get)
                        .sorted()
                        .map(language -> createOptionTag(language, preSelectedLanguage.equals(language.getCode())))
                        .collect(Collectors.joining("\n")));
    }

    private String createOptionTag(Language language, boolean selected) {
        String firstPart = "                                    <option value=\"" + language.getCode() + "\"";
        String secondPart = ">" + language.format() + "</option>";
        if (selected) {
            return firstPart + " selected=\"selected\"" + secondPart;
        } else {
            return firstPart + secondPart;
        }
    }

    private String renderBridgeConfigurationTemplate(String skeleton, ThingUID bridgeUid, String email) {
        String bridgeTemplate = templateGenerator.createBridgeConfigurationTemplate(bridgeUid.getId(), email,
                languageProvider.getLanguage().orElse("en"));
        return skeleton.replace(THINGS_TEMPLATE_CODE_PLACEHOLDER, bridgeTemplate);
    }

    /**
     * A language representation for user display.
     *
     * @author Björn Lange - Initial contribution
     */
    private static final class Language implements Comparable<Language> {
        private final String code;
        private final String name;

        private Language(String code, String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * Gets the 2-letter language code for accessing the Miele Cloud service.
         */
        public String getCode() {
            return code;
        }

        /**
         * Formats the language for displaying.
         */
        public String format() {
            return name + " - " + code;
        }

        @Override
        public int compareTo(Language other) {
            return name.toUpperCase().compareTo(other.name.toUpperCase());
        }

        /**
         * Constructs a {@link Language} from a 2-letter language code.
         *
         * @param code 2-letter language code.
         * @return An {@link Optional} wrapping the {@link Language} or an empty {@link Optional} if there is no
         *         representation for the given language code.
         */
        public static Optional<Language> fromCode(String code) {
            Locale locale = new Locale(code);
            String name = locale.getDisplayLanguage(locale);
            if (name.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(new Language(code, name));
            }
        }
    }
}
