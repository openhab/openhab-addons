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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.candy.internal.handler.BiancaHandler;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.ProgramOption;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.internal.module.handler.AnnotationActionHandler;
import org.openhab.core.automation.type.ActionType;
import org.openhab.core.automation.type.Input;
import org.openhab.core.automation.util.ActionInputsHelper;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonParser;

/**
 * Verifies localized action results, framework outputs and per-Thing service lifecycle.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class CandyActionsTest {
    private final BiancaHandler handler = mock(BiancaHandler.class);
    private final Bundle bundle = mock(Bundle.class);
    private final BundleContext context = mock(BundleContext.class);
    private final LocaleProvider locales = mock(LocaleProvider.class);
    private final TranslationProvider translations = mock(TranslationProvider.class);
    private final CandyActions actions;

    CandyActionsTest() {
        when(context.getBundle()).thenReturn(bundle);
        when(locales.getLocale()).thenReturn(Locale.UK);
        when(translations.getText(eq(bundle), anyString(), anyString(), any(Locale.class), any(Object[].class)))
                .thenAnswer(call -> {
                    String key = call.getArgument(1);
                    Locale locale = call.getArgument(3);
                    ResourceBundle messages = ResourceBundle.getBundle("OH-INF.i18n.candy", locale);
                    String pattern = messages.containsKey(key) ? messages.getString(key) : call.getArgument(2);
                    Object[] arguments = (Object[]) requireNonNull(call.getRawArguments()[4]);
                    return new MessageFormat(pattern, locale).format(arguments);
                });
        actions = new CandyActions(context, translations, locales);
    }

    @Test
    void returnsProgramDataAndLocalizedInformationForInstanceAndRulesDslCalls() {
        List<ProgramOption> programs = List
                .of(new ProgramOption("cottons", "Cottons", true, Map.of("default_temperature", "60")));
        when(handler.getPrograms()).thenReturn(programs);
        when(handler.getCatalogStatus()).thenReturn("READY");
        when(handler.getLastCommandResult()).thenReturn("CONFIRMED");
        actions.setThingHandler(handler);
        assertSame(handler, actions.getThingHandler());
        String result = actions.getPrograms();
        assertEquals(result, CandyActions.getPrograms(actions));
        assertTrue(result.contains("\n"));
        var parsed = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("Read cached program options. Count: 1.", parsed.get("message").getAsString());
        var program = parsed.getAsJsonArray("programs").get(0).getAsJsonObject();
        assertEquals(Set.of("id", "label", "startable", "parameters"), program.keySet());
        assertEquals("cottons", program.get("id").getAsString());
        assertEquals("Cottons", program.get("label").getAsString());
        assertTrue(program.get("startable").getAsBoolean());
        assertEquals("60", program.getAsJsonObject("parameters").get("default_temperature").getAsString());
        assertEquals(text("action.catalog.READY"), actions.getCatalogStatus());
        assertEquals(actions.getCatalogStatus(), CandyActions.getCatalogStatus(actions));
        assertEquals(text("action.command.CONFIRMED"), actions.getLastCommandResult());
        assertEquals(actions.getLastCommandResult(), CandyActions.getLastCommandResult(actions));
    }

    @Test
    void acknowledgesSubmissionWithoutClaimingDeviceAcceptance() {
        actions.setThingHandler(handler);
        when(handler.refreshPrograms()).thenReturn(true);
        when(handler.startProgram(anyString())).thenReturn(true);
        when(handler.stopProgram()).thenReturn(true);
        assertEquals(text("action.result.refreshQueued"), actions.refreshPrograms());
        assertEquals(actions.refreshPrograms(), CandyActions.refreshPrograms(actions));
        assertEquals(text("action.result.startQueued"), actions.startProgram("cottons"));
        assertEquals(text("action.result.startQueued"), CandyActions.startProgram(actions, "rinse"));
        assertEquals(text("action.result.stopQueued"), actions.stopProgram());
        assertEquals(actions.stopProgram(), CandyActions.stopProgram(actions));
        verify(handler, times(3)).refreshPrograms();
        verify(handler).startProgram("cottons");
        verify(handler).startProgram("rinse");
        verify(handler, times(3)).stopProgram();
    }

    @Test
    void reportsWhenAnAttachedHandlerCannotQueueWork() {
        actions.setThingHandler(handler);
        assertEquals(text("action.result.unavailable"), actions.refreshPrograms());
        assertEquals(text("action.result.unavailable"), actions.startProgram("cottons"));
        assertEquals(text("action.result.unavailable"), actions.stopProgram());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t" })
    void missingProgramInputIsRejectedWithoutQueuingWork(@Nullable String id) {
        actions.setThingHandler(handler);
        assertEquals(text("action.result.programRequired"), actions.startProgram(id));
        verifyNoInteractions(handler);
    }

    @ParameterizedTest
    @ValueSource(strings = { "unattached", "detached", "disposed", "deactivated", "wrong-handler" })
    void unavailableActionsCannotReachAHandler(String state) {
        if (!"unattached".equals(state)) {
            actions.setThingHandler(handler);
        }
        switch (state) {
            case "detached" -> actions.setThingHandler(null);
            case "disposed" -> actions.dispose();
            case "deactivated" -> actions.deactivate();
            case "wrong-handler" -> actions.setThingHandler(mock(ThingHandler.class));
        }
        if ("disposed".equals(state) || "deactivated".equals(state)) {
            assertSame(handler, actions.getThingHandler());
        } else {
            assertNull(actions.getThingHandler());
        }
        String unavailable = "deactivated".equals(state) ? "Washer action information is unavailable."
                : text("action.result.unavailable");
        var parsed = JsonParser.parseString(actions.getPrograms()).getAsJsonObject();
        assertEquals(unavailable, parsed.get("message").getAsString());
        assertTrue(parsed.getAsJsonArray("programs").isEmpty());
        assertEquals(unavailable, actions.getCatalogStatus());
        assertEquals(unavailable, actions.getLastCommandResult());
        assertEquals(unavailable, actions.refreshPrograms());
        assertEquals(unavailable, actions.startProgram("cottons"));
        assertEquals(unavailable, actions.stopProgram());
        verifyNoInteractions(handler);
    }

    @Test
    void deactivatedActionsDoNotUseReleasedTranslationServices() {
        actions.setThingHandler(handler);
        actions.deactivate();
        actions.setThingHandler(handler);
        assertSame(handler, actions.getThingHandler());
        clearInvocations(translations, locales);
        assertEquals("Washer action information is unavailable.", actions.stopProgram());
        assertEquals("Washer action information is unavailable.", actions.refreshPrograms());
        assertEquals("Washer action information is unavailable.", actions.startProgram("cottons"));
        assertEquals("Washer action information is unavailable.", actions.getCatalogStatus());
        assertEquals("Washer action information is unavailable.", actions.getLastCommandResult());
        assertEquals("Washer action information is unavailable.",
                JsonParser.parseString(actions.getPrograms()).getAsJsonObject().get("message").getAsString());
        verifyNoInteractions(handler, translations, locales);
    }

    @Test
    void preservesThingIdentityForFrameworkUnregistrationAfterCleanup() {
        Thing thing = mock(Thing.class);
        ThingUID uid = new ThingUID("candy:bianca:deployment");
        when(handler.getThing()).thenReturn(thing);
        when(thing.getUID()).thenReturn(uid);
        actions.setThingHandler(handler);
        actions.dispose();
        assertEquals(uid, requireNonNull(actions.getThingHandler()).getThing().getUID());
        actions.deactivate();
        assertEquals(uid, requireNonNull(actions.getThingHandler()).getThing().getUID());
    }

    @Test
    void separateThingsDoNotShareActionHandlers() {
        CandyActions other = new CandyActions(context, translations, locales);
        BiancaHandler otherHandler = mock(BiancaHandler.class);
        when(otherHandler.startProgram("rinse")).thenReturn(true);
        actions.setThingHandler(handler);
        other.setThingHandler(otherHandler);
        actions.dispose();
        assertEquals(text("action.result.startQueued"), other.startProgram("rinse"));
        verify(otherHandler).startProgram("rinse");
        verifyNoInteractions(handler);
    }

    @Test
    void usesTheCurrentOpenHabLocaleForFeedback() {
        actions.setThingHandler(handler);
        when(handler.stopProgram()).thenReturn(true);
        assertEquals(text("action.result.stopQueued"), actions.stopProgram());
        when(locales.getLocale()).thenReturn(Locale.FRENCH);
        when(translations.getText(eq(bundle), eq("action.result.stopQueued"), anyString(), eq(Locale.FRENCH),
                any(Object[].class))).thenReturn("La demande d’arrêt a été mise en file d’attente.");
        assertEquals("La demande d’arrêt a été mise en file d’attente.", actions.stopProgram());
    }

    @ParameterizedTest
    @ValueSource(strings = { "NOT_READY", "ACCOUNT_REQUIRED", "LOADING", "READY", "RATE_LIMITED", "FAILED" })
    void translatesEveryCatalogStatus(String status) {
        actions.setThingHandler(handler);
        when(handler.getCatalogStatus()).thenReturn(status);
        assertEquals(text("action.catalog." + status), actions.getCatalogStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = { "PENDING", "SENT", "CONFIRMED", "REJECTED", "DISABLED", "BUSY", "INVALID_PROGRAM",
            "NOT_READY", "REMOTE_DISABLED", "FAILED", "UNKNOWN" })
    void translatesEveryCommandResult(String status) {
        actions.setThingHandler(handler);
        when(handler.getLastCommandResult()).thenReturn(status);
        assertEquals(text("action.command." + status), actions.getLastCommandResult());
    }

    @Test
    void missingTranslationsStillProduceDisplayableFeedback() {
        actions.setThingHandler(handler);
        when(handler.getCatalogStatus()).thenReturn("FUTURE_STATUS");
        assertEquals("Washer action information is unavailable.", actions.getCatalogStatus());
        when(translations.getText(eq(bundle), anyString(), anyString(), any(Locale.class), any(Object[].class)))
                .thenReturn(null);
        assertEquals("Washer action information is unavailable.", actions.stopProgram());
    }

    @ParameterizedTest
    @ValueSource(strings = { "getPrograms", "refreshPrograms", "startProgram", "stopProgram", "getCatalogStatus",
            "getLastCommandResult" })
    void everyActionExposesAStringResultThroughTheAutomationExecutor(String name) throws Exception {
        actions.setThingHandler(handler);
        when(handler.getPrograms()).thenReturn(List.of());
        when(handler.refreshPrograms()).thenReturn(true);
        when(handler.startProgram(anyString())).thenReturn(true);
        when(handler.stopProgram()).thenReturn(true);
        when(handler.getCatalogStatus()).thenReturn("READY");
        when(handler.getLastCommandResult()).thenReturn("PENDING");
        boolean start = "startProgram".equals(name);
        var method = start ? CandyActions.class.getMethod(name, String.class) : CandyActions.class.getMethod(name);
        var annotation = method.getAnnotation(ActionOutput.class);
        assertNotNull(annotation);
        assertEquals("java.lang.String", annotation.type());
        assertEquals("result", annotation.name());
        assertEquals(String.class, method.getReturnType());
        ActionType type = mock(ActionType.class);
        Input input = mock(Input.class);
        when(input.getName()).thenReturn("programId");
        when(type.getInputs()).thenReturn(start ? List.of(input) : List.of());
        var execution = new AnnotationActionHandler(mock(Action.class), type, method, actions,
                mock(ActionInputsHelper.class));
        var result = execution.execute(start ? Map.of("programId", "cottons") : Map.of());
        assertNotNull(result);
        String feedback = assertInstanceOf(String.class, result.get("result"));
        assertFalse(feedback.isBlank());
        assertFalse(feedback.startsWith("@text/"));
        var scope = CandyActions.class.getAnnotation(ThingActionsScope.class);
        assertNotNull(scope);
        assertEquals("candy", scope.name());
    }

    private String text(String key) {
        return ResourceBundle.getBundle("OH-INF.i18n.candy", Locale.UK).getString(key);
    }
}
