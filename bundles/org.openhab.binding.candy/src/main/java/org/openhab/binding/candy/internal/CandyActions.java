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
package org.openhab.binding.candy.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.candy.internal.CandyBindingConstants.BINDING_ID;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.candy.internal.handler.BiancaHandler;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.ProgramOption;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Per-washer actions for cached program queries and asynchronous washing commands.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
// noinspection StaticMethodOnlyUsedInOneClass
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = CandyActions.class)
@ThingActionsScope(name = BINDING_ID)
public class CandyActions implements ThingActions {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FALLBACK_MESSAGE = "Washer action information is unavailable.";

    private final Bundle bundle;
    private volatile @Nullable TranslationProvider translations;
    private volatile @Nullable LocaleProvider localeProvider;
    private volatile @Nullable BiancaHandler handler;
    // openHAB needs the original Thing identity in unregistration callbacks after dispose or DS deactivation.
    private volatile @Nullable BiancaHandler handlerContext;

    @Activate
    public CandyActions(BundleContext context, @Reference TranslationProvider translations,
            @Reference LocaleProvider localeProvider) {
        bundle = context.getBundle();
        this.translations = translations;
        this.localeProvider = localeProvider;
    }

    @Override
    public synchronized void setThingHandler(@Nullable ThingHandler handler) {
        if (translations != null) {
            this.handler = handler instanceof BiancaHandler bianca ? bianca : null;
            handlerContext = this.handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handlerContext;
    }

    @Override
    public void dispose() {
        handler = null;
    }

    @Override
    @Deactivate
    public synchronized void deactivate() {
        handler = null;
        translations = null;
        localeProvider = null;
    }

    @RuleAction(label = "@text/action.getPrograms.label", description = "@text/action.getPrograms.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.getPrograms.outputLabel", description = "@text/action.getPrograms.outputDescription")
    public String getPrograms() {
        BiancaHandler current = handler;
        List<ProgramOption> programs = current == null ? List.of() : current.getPrograms();
        String summary = current == null ? message("action.result.unavailable")
                : message("action.result.programs", programs.size());
        JsonObject result = new JsonObject();
        result.addProperty("message", summary);
        result.add("programs", GSON.toJsonTree(programs));
        return GSON.toJson(result);
    }

    public static String getPrograms(@Nullable ThingActions actions) {
        return ((CandyActions) requireNonNull(actions)).getPrograms();
    }

    @RuleAction(label = "@text/action.refreshPrograms.label", description = "@text/action.refreshPrograms.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.result.outputLabel", description = "@text/action.result.outputDescription")
    public String refreshPrograms() {
        BiancaHandler current = handler;
        return message(current != null && current.refreshPrograms() ? "action.result.refreshQueued"
                : "action.result.unavailable");
    }

    public static String refreshPrograms(@Nullable ThingActions actions) {
        return ((CandyActions) requireNonNull(actions)).refreshPrograms();
    }

    @RuleAction(label = "@text/action.startProgram.label", description = "@text/action.startProgram.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.result.outputLabel", description = "@text/action.result.outputDescription")
    public String startProgram(
            @ActionInput(name = "programId", label = "@text/action.startProgram.programIdLabel", description = "@text/action.startProgram.programIdDescription") @Nullable String programId) {
        BiancaHandler current = handler;
        if (current == null) {
            return message("action.result.unavailable");
        }
        if (programId == null || programId.isBlank()) {
            return message("action.result.programRequired");
        }
        return message(current.startProgram(programId) ? "action.result.startQueued" : "action.result.unavailable");
    }

    public static String startProgram(@Nullable ThingActions actions, @Nullable String programId) {
        return ((CandyActions) requireNonNull(actions)).startProgram(programId);
    }

    @RuleAction(label = "@text/action.stopProgram.label", description = "@text/action.stopProgram.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.result.outputLabel", description = "@text/action.result.outputDescription")
    public String stopProgram() {
        BiancaHandler current = handler;
        return message(
                current != null && current.stopProgram() ? "action.result.stopQueued" : "action.result.unavailable");
    }

    public static String stopProgram(@Nullable ThingActions actions) {
        return ((CandyActions) requireNonNull(actions)).stopProgram();
    }

    @RuleAction(label = "@text/action.getCatalogStatus.label", description = "@text/action.getCatalogStatus.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.getCatalogStatus.outputLabel")
    public String getCatalogStatus() {
        BiancaHandler current = handler;
        return message(current == null ? "action.result.unavailable" : "action.catalog." + current.getCatalogStatus());
    }

    public static String getCatalogStatus(@Nullable ThingActions actions) {
        return ((CandyActions) requireNonNull(actions)).getCatalogStatus();
    }

    @RuleAction(label = "@text/action.getLastCommandResult.label", description = "@text/action.getLastCommandResult.description")
    @ActionOutput(type = "java.lang.String", label = "@text/action.getLastCommandResult.outputLabel")
    public String getLastCommandResult() {
        BiancaHandler current = handler;
        return message(
                current == null ? "action.result.unavailable" : "action.command." + current.getLastCommandResult());
    }

    public static String getLastCommandResult(@Nullable ThingActions actions) {
        return ((CandyActions) requireNonNull(actions)).getLastCommandResult();
    }

    private String message(String key, Object... arguments) {
        TranslationProvider currentTranslations = translations;
        LocaleProvider currentLocaleProvider = localeProvider;
        if (currentTranslations == null || currentLocaleProvider == null) {
            // Rule scripts may retain an old action instance after its translation services have been released.
            return FALLBACK_MESSAGE;
        }
        return Objects.requireNonNullElse(currentTranslations.getText(bundle, key, FALLBACK_MESSAGE,
                currentLocaleProvider.getLocale(), arguments), FALLBACK_MESSAGE);
    }
}
