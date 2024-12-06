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
package org.openhab.binding.unifi.internal.action;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.binding.unifi.internal.api.dto.UniFiVoucher;
import org.openhab.binding.unifi.internal.handler.UniFiSiteThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link UniFiSiteActions} class defines rule actions for creating guest hotspot vouchers
 *
 * @author Mark Herwege - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = UniFiSiteActions.class)
@ThingActionsScope(name = "unifi")
@NonNullByDefault
public class UniFiSiteActions implements ThingActions {

    private static final int DEFAULT_COUNT = 1;
    private static final int DEFAULT_EXPIRE_MIN = 1440;
    private static final int DEFAULT_USERS = 1;

    private static final Pattern NON_DIGITS_PATTERN = Pattern.compile("\\D+");

    private final Logger logger = LoggerFactory.getLogger(UniFiSiteActions.class);

    private @Nullable UniFiSiteThingHandler handler;
    private final Gson gson = new Gson();

    @RuleAction(label = "@text/action.unifi.generateVouchers.label", description = "@text/action.unifi.generateVouchers.description")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean generateVoucher(
    /* @formatter:off */
            @ActionInput(name = "expire",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherExpiration.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherExpiration.description") @Nullable Integer expire,
            @ActionInput(name = "users",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUsers.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUsers.description") @Nullable Integer users,
            @ActionInput(name = "upLimit",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUpLimit.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUpLimit.description") @Nullable Integer upLimit,
            @ActionInput(name = "downLimit",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDownLimit.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDownLimit.description") @Nullable Integer downLimit,
            @ActionInput(name = "dataQuota",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDataQuota.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDataQuota.description") @Nullable Integer dataQuota) {
    /* @formatter:on */
        return generateVouchers(1, expire, users, upLimit, downLimit, dataQuota);
    }

    @RuleAction(label = "@text/action.unifi.generateVouchers.label", description = "@text/action.unifi.generateVouchers.description")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean generateVouchers(
    /* @formatter:off */
            @ActionInput(name = "count",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherCount.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherCount.description") @Nullable Integer count,
            @ActionInput(name = "expire",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherExpiration.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherExpiration.description") @Nullable Integer expire,
            @ActionInput(name = "users",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUsers.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUsers.description") @Nullable Integer users,
            @ActionInput(name = "upLimit",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUpLimit.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherUpLimit.description") @Nullable Integer upLimit,
            @ActionInput(name = "downLimit",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDownLimit.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDownLimit.description") @Nullable Integer downLimit,
            @ActionInput(name = "dataQuota",
                label = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDataQuota.label",
                description = "@text/channel-type.config.unifi.guestVouchersGenerate.voucherDataQuota.description") @Nullable Integer dataQuota) {
    /* @formatter:on */
        UniFiSiteThingHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Could not create guest vouchers, site thing handler not set");
            return false;
        }
        final @Nullable UniFiSite entity = handler.getEntity();
        final UniFiController controller = handler.getController();
        if (entity == null || controller == null) {
            logger.debug("Could not create guest vouchers, site thing error");
            return false;
        }
        try {
            controller.generateVouchers(entity, ((count != null) && (count != 0)) ? count : DEFAULT_COUNT,
                    (expire != null) ? expire : DEFAULT_EXPIRE_MIN, (users != null) ? users : DEFAULT_USERS, upLimit,
                    downLimit, dataQuota);
        } catch (UniFiException e) {
            logger.debug("Could not create guest vouchers, uniFi exception", e);
            return false;
        }
        return true;
    }

    public static boolean generateVoucher(ThingActions actions) {
        return UniFiSiteActions.generateVoucher(actions, DEFAULT_EXPIRE_MIN);
    }

    public static boolean generateVoucher(ThingActions actions, @Nullable Integer expire) {
        return UniFiSiteActions.generateVoucher(actions, expire, DEFAULT_USERS);
    }

    public static boolean generateVoucher(ThingActions actions, @Nullable Integer expire, @Nullable Integer users) {
        return UniFiSiteActions.generateVoucher(actions, expire, users, null, null, null);
    }

    public static boolean generateVoucher(ThingActions actions, @Nullable Integer expire, @Nullable Integer users,
            @Nullable Integer upLimit, @Nullable Integer downLimit, @Nullable Integer dataQuota) {
        return ((UniFiSiteActions) actions).generateVoucher(expire, users, upLimit, downLimit, dataQuota);
    }

    public static boolean generateVouchers(ThingActions actions) {
        return UniFiSiteActions.generateVouchers(actions, DEFAULT_COUNT);
    }

    public static boolean generateVouchers(ThingActions actions, @Nullable Integer count) {
        return UniFiSiteActions.generateVouchers(actions, count, DEFAULT_EXPIRE_MIN);
    }

    public static boolean generateVouchers(ThingActions actions, @Nullable Integer count, @Nullable Integer expire) {
        return UniFiSiteActions.generateVouchers(actions, count, expire, DEFAULT_USERS);
    }

    public static boolean generateVouchers(ThingActions actions, @Nullable Integer count, @Nullable Integer expire,
            @Nullable Integer users) {
        return UniFiSiteActions.generateVouchers(actions, count, expire, users, null, null, null);
    }

    public static boolean generateVouchers(ThingActions actions, @Nullable Integer count, @Nullable Integer expire,
            @Nullable Integer users, @Nullable Integer upLimit, @Nullable Integer downLimit,
            @Nullable Integer dataQuota) {
        return ((UniFiSiteActions) actions).generateVouchers(count, expire, users, upLimit, downLimit, dataQuota);
    }

    @RuleAction(label = "@text/action.unifi.revokeVouchers.label", description = "@text/action.unifi.revokeVouchers.description")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean revokeVoucher(
    /* @formatter:off */
            @ActionInput(name = "voucherCodes", label = "@text/action.unifi.vouchersInputVoucherCodes.label",
                description = "@text/action.unifi.vouchersInputVoucherCodes.description") String voucherCode) {
    /* @formatter:on */
        return revokeVouchers(List.of(voucherCode));
    }

    @RuleAction(label = "@text/action.unifi.revokeVouchers.label", description = "@text/action.unifi.revokeVouchers.description")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean revokeVouchers(
    /* @formatter:off */
            @ActionInput(name = "voucherCodes", label = "@text/action.unifi.vouchersInputVoucherCodes.label",
                description = "@text/action.unifi.vouchersInputVoucherCodes.description",
                type = "List<String>") List<String> voucherCodes) {
    /* @formatter:on */
        UniFiSiteThingHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Could not revoke guest vouchers, site thing handler not set");
            return false;
        }
        final @Nullable UniFiSite entity = handler.getEntity();
        final UniFiController controller = handler.getController();
        if (entity == null || controller == null) {
            logger.debug("Could not revoke guest vouchers, site thing error");
            return false;
        }

        // Only keep digits in provided codes, so matching is done correctly. This makes blanks and dashes in the input
        // possible, as shown in the UniFi voucher UI.
        List<String> cleanCodes = voucherCodes.stream().map(c -> NON_DIGITS_PATTERN.matcher(c).replaceAll(""))
                .filter(c -> !c.isEmpty()).toList();
        Stream<UniFiVoucher> voucherStream = entity.getCache().getVoucherStreamForSite(entity);
        // If no codes provided, revoke all codes
        List<UniFiVoucher> vouchers = (voucherCodes.isEmpty() ? voucherStream
                : voucherStream.filter(v -> cleanCodes.contains(v.getCode()))).toList();
        try {
            controller.revokeVouchers(entity, vouchers);
        } catch (UniFiException e) {
            logger.debug("Could not revoke guest vouchers, uniFi exception", e);
            return false;
        }
        return true;
    }

    @RuleAction(label = "@text/action.unifi.revokeAllVouchers.label", description = "@text/action.unifi.revokeAllVouchers.description")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean revokeAllVouchers() {
        return revokeVouchers(List.of());
    }

    public static boolean revokeVoucher(ThingActions actions, String voucherCode) {
        return revokeVouchers(actions, List.of(voucherCode));
    }

    public static boolean revokeVouchers(ThingActions actions, List<String> voucherCodes) {
        return ((UniFiSiteActions) actions).revokeVouchers(voucherCodes);
    }

    public static boolean revokeAllVouchers(ThingActions actions) {
        return revokeVouchers(actions);
    }

    public static boolean revokeVouchers(ThingActions actions) {
        return revokeVouchers(actions, List.of());
    }

    @RuleAction(label = "@text/action.unifi.listVouchers.label", description = "@text/action.unifi.listVouchers.description")
    public @ActionOutput(label = "Vouchers", type = "java.lang.String") String listVouchers() {
        UniFiSiteThingHandler handler = this.handler;
        if (handler == null) {
            logger.debug("Could not list guest vouchers, site thing handler not set");
            return "";
        }
        final @Nullable UniFiSite entity = handler.getEntity();
        if (entity == null) {
            logger.debug("Could not list guest vouchers, site thing error");
            return "";
        }

        record Voucher(String code, String createTime, Integer duration, Integer quota, Integer used,
                Integer qosUsageQuota, Integer qosRateMaxUp, Integer qosRateMaxDown, Boolean qosOverwrite, String note,
                String status) {
        }

        return gson
                .toJson(entity.getCache().getVoucherStreamForSite(entity)
                        .collect(Collectors.mapping(
                                v -> new Voucher(v.getCode(), v.getCreateTime().toString(), v.getDuration(),
                                        v.getQuota(), v.getUsed(), v.getQosUsageQuota(), v.getQosRateMaxUp(),
                                        v.getQosRateMaxDown(), v.isQosOverwrite(), v.getNote(), v.getStatus()),
                                Collectors.toList())));
    }

    public static String listVouchers(ThingActions actions) {
        return ((UniFiSiteActions) actions).listVouchers();
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof UniFiSiteThingHandler siteThingHandler) {
            this.handler = siteThingHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
