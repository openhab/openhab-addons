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
package org.openhab.binding.ecovacs.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.handler.EcovacsApiHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Danny Baumann - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EcovacsApiActions.class)
@ThingActionsScope(name = "ecovacs")
@NonNullByDefault
public class EcovacsApiActions implements ThingActions {
    private @Nullable EcovacsApiHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (EcovacsApiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/requestVerificationCodeLabel", description = "@text/requestVerificationCodeDesc")
    public @ActionOutput(label = "@text/actionOutputSuccessLabel", type = "java.lang.Boolean") boolean requestVerificationCode() {
        EcovacsApiHandler handler = this.handler;
        if (handler != null) {
            return handler.requestVerificationCode();
        }
        return false;
    }

    @RuleAction(label = "@text/enterVerificationCodeLabel", description = "@text/enterVerificationCodeDesc")
    public @ActionOutput(label = "@text/actionOutputSuccessLabel", type = "java.lang.Boolean") boolean enterVerificationCode(
            @ActionInput(name = "verificationCode", label = "@text/actionInputVerificationCodeLabel", description = "@text/actionInputVerificationCodeDesc", required = true) String verificationCode) {
        EcovacsApiHandler handler = this.handler;
        if (handler != null) {
            return handler.enterVerificationCode(verificationCode);
        }
        return false;
    }
}
