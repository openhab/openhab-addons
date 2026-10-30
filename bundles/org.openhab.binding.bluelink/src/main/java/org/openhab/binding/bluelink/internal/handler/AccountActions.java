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
package org.openhab.binding.bluelink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actions of the Bluelink account bridge.
 *
 * @author Carlo Dischler - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AccountActions.class)
@ThingActionsScope(name = "bluelink")
@NonNullByDefault
public class AccountActions implements ThingActions {
    private @Nullable BluelinkAccountHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (BluelinkAccountHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.request-verification-code.label", description = "@text/action.request-verification-code.desc")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "@text/action.result.label", type = "java.lang.String") }) String requestVerificationCode() {
        final BluelinkAccountHandler hnd = handler;
        return hnd != null ? hnd.requestVerificationCode() : null;
    }

    @RuleAction(label = "@text/action.verify-code.label", description = "@text/action.verify-code.desc")
    public @Nullable @ActionOutputs({
            @ActionOutput(name = "result", label = "@text/action.result.label", type = "java.lang.String") }) String verifyCode(
                    @ActionInput(name = "code", label = "@text/action.verify-code.input.code.label", required = true) final String code) {
        final BluelinkAccountHandler hnd = handler;
        return hnd != null ? hnd.verifyCode(code) : null;
    }

    public static @Nullable String requestVerificationCode(final @Nullable ThingActions actions) {
        if (actions instanceof AccountActions aa) {
            return aa.requestVerificationCode();
        } else {
            throw new IllegalArgumentException("expected AccountActions");
        }
    }

    public static @Nullable String verifyCode(final @Nullable ThingActions actions, final String code) {
        if (actions instanceof AccountActions aa) {
            return aa.verifyCode(code);
        } else {
            throw new IllegalArgumentException("expected AccountActions");
        }
    }
}
