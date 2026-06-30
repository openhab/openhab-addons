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
package org.openhab.binding.tuya.internal.actions;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.BINDING_ID;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.handler.TuyaSmartLifeHandler;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actions for
 * {@link org.openhab.binding.tuya.internal.handler.TuyaSmartLifeHandler}.
 *
 * @author Mike Jagdis - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TuyaSmartLifeActions.class)
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class TuyaSmartLifeActions implements ThingActions {
    private final Bundle bundle;
    private final LocaleProvider localeProvider;
    private final TranslationProvider translationProvider;

    private @Nullable TuyaSmartLifeHandler handler;

    @Activate
    public TuyaSmartLifeActions( //
            final @Reference LocaleProvider localeProvider, //
            final @Reference TranslationProvider translationProvider //
    ) {
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
    }

    private String getText(String key) {
        var text = translationProvider.getText(bundle, key, key, localeProvider.getLocale());
        return (text != null ? text : key);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (TuyaSmartLifeHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.auth.rule.label", description = "@text/action.auth.rule.description")
    public @ActionOutputs({ //
            @ActionOutput(name = "result", //
                    label = "@text/action.auth.result.label", //
                    description = "@text/action.auth.result.description", //
                    type = "java.lang.String"), //
            @ActionOutput(name = "qrCode", //
                    label = "@text/action.auth.qrcode.label", //
                    description = "@text/action.auth.qrcode.description", //
                    type = "qrCode") //
    }) Map<String, Object> auth() {
        var handler = this.handler;
        if (handler != null) {
            if (handler.getThing().getStatus() != ThingStatus.UNINITIALIZED) {
                var qrText = handler.actionQrLogin();

                if (!qrText.isBlank()) {
                    return Map.of( //
                            "result", getText("action.auth.result.qr-shown"), //
                            "qrCode", qrText //
                    );
                } else {
                    return Map.of("result", getText("action.auth.result.qr-missing"));
                }
            } else {
                return Map.of("result", getText("action.status.not-initialized"));
            }
        }

        return Map.of("result", getText("action.status.no-action"));
    }
}
