/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.subhandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lcn.internal.LcnBindingConstants;
import org.openhab.binding.lcn.internal.LcnModuleHandler;
import org.openhab.binding.lcn.internal.common.DimmerOutputCommand;
import org.openhab.binding.lcn.internal.common.LcnChannelGroup;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.common.PckGenerator;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Commands and State changes of dimmer outputs of an LCN module.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class LcnModuleOutputSubHandler extends AbstractLcnModuleSubHandler {
    private final Logger logger = LoggerFactory.getLogger(LcnModuleOutputSubHandler.class);
    private static final int COLOR_RAMP_MS = 1000;
    private static final String OUTPUT_COLOR = "color";
    private static final Pattern PERCENT_PATTERN;
    private static final Pattern NATIVE_PATTERN;
    private volatile HSBType currentColor = new HSBType();
    private volatile PercentType output4 = new PercentType();

    public LcnModuleOutputSubHandler(LcnModuleHandler handler, ModInfo info) {
        super(handler, info);
    }

    static {
        PERCENT_PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX + "A(?<outputId>\\d)(?<percent>\\d{3})");
        NATIVE_PATTERN = Pattern.compile(LcnBindingConstants.ADDRESS_REGEX + "O(?<outputId>\\d)(?<value>\\d{3})");
    }

    @Override
    public Collection<Pattern> getPckStatusMessagePatterns() {
        return Arrays.asList(NATIVE_PATTERN, PERCENT_PATTERN);
    }

    @Override
    public void handleRefresh(LcnChannelGroup channelGroup, int number) {
        info.refreshOutput(number);
    }

    @Override
    public void handleRefresh(String groupId) {
        if (OUTPUT_COLOR.equals(groupId)) {
            info.refreshAllOutputs();
        }
    }

    @Override
    public void handleCommandOnOff(OnOffType command, LcnChannelGroup channelGroup, int number) throws LcnException {
        // don't use OnOffType.as() here, because it returns @Nullable
        handler.sendPck(PckGenerator.dimOutput(number, command == OnOffType.ON ? 100 : 0, 0));
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, int number)
            throws LcnException {
        handler.sendPck(PckGenerator.dimOutput(number, command.doubleValue(), 0));
    }

    @Override
    public void handleCommandPercent(PercentType command, LcnChannelGroup channelGroup, String idWithoutGroup)
            throws LcnException {
        if (!OUTPUT_COLOR.equals(idWithoutGroup)) {
            throw new LcnException("Unknown group ID: " + idWithoutGroup);
        }
        updateAndSendColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), command));
    }

    @Override
    public void handleCommandHsb(HSBType command, String groupId) throws LcnException {
        if (!OUTPUT_COLOR.equals(groupId)) {
            throw new LcnException("Unknown group ID: " + groupId);
        }
        updateAndSendColor(command);
    }

    private synchronized void updateAndSendColor(HSBType hsbType) throws LcnException {
        currentColor = hsbType;
        handler.updateChannel(LcnChannelGroup.OUTPUT, OUTPUT_COLOR, currentColor);

        PercentType[] rgb = ColorUtil.hsbToRgbPercent(currentColor);

        if (info.getFirmwareVersion().map(v -> v >= LcnBindingConstants.FIRMWARE_2014).orElse(true)) {
            handler.sendPck(PckGenerator.dimAllOutputs(rgb[0].doubleValue(), rgb[1].doubleValue(), rgb[2].doubleValue(),
                    output4.doubleValue(), COLOR_RAMP_MS));
        } else {
            handler.sendPck(PckGenerator.dimOutput(0, rgb[0].doubleValue(), COLOR_RAMP_MS));
            handler.sendPck(PckGenerator.dimOutput(1, rgb[1].doubleValue(), COLOR_RAMP_MS));
            handler.sendPck(PckGenerator.dimOutput(2, rgb[2].doubleValue(), COLOR_RAMP_MS));
        }
    }

    @Override
    public void handleCommandDimmerOutput(DimmerOutputCommand command, int number) throws LcnException {
        int rampMs = command.getRampMs();
        if (command.isControlAllOutputs()) { // control all dimmer outputs
            if (rampMs == LcnDefs.FIXED_RAMP_MS) {
                // compatibility command
                handler.sendPck(PckGenerator.controlAllOutputs(command.intValue()));
            } else {
                // command since firmware 180501
                handler.sendPck(PckGenerator.dimAllOutputs(command.doubleValue(), command.doubleValue(),
                        command.doubleValue(), command.doubleValue(), rampMs));
            }
        } else if (command.isControlOutputs12()) { // control dimmer outputs 1+2
            if (command.intValue() == 0 || command.intValue() == 100) {
                handler.sendPck(PckGenerator.controlOutputs12(command.intValue() > 0, rampMs >= LcnDefs.FIXED_RAMP_MS));
            } else {
                // ignore ramp when dimming
                handler.sendPck(PckGenerator.dimOutputs12(command.doubleValue()));
            }
        } else {
            handler.sendPck(PckGenerator.dimOutput(number, command.doubleValue(), rampMs));
        }
    }

    @Override
    public void handleCommandString(StringType command, int number) throws LcnException {
        int mode = 0;

        switch (command.toString()) {
            case "DISABLE":
                mode = 0;
                break;
            case "OUTPUT1":
                mode = 1;
                break;
            case "BOTH":
                mode = 2;
                break;
        }

        handler.sendPck(PckGenerator.setTunableWhiteMode(mode));
    }

    @Override
    public void handleStatusMessage(Matcher matcher) {
        int outputId = Integer.parseInt(matcher.group("outputId")) - 1;

        if (!LcnChannelGroup.OUTPUT.isValidId(outputId)) {
            logger.warn("outputId out of range: {}", outputId);
            return;
        }
        double percent;
        if (matcher.pattern() == PERCENT_PATTERN) {
            percent = Integer.parseInt(matcher.group("percent"));
        } else if (matcher.pattern() == NATIVE_PATTERN) {
            percent = (double) Integer.parseInt(matcher.group("value")) / 2;
        } else {
            logger.warn("Unexpected pattern: {}", matcher.pattern());
            return;
        }

        info.onOutputResponseReceived(outputId);

        percent = Math.min(100, Math.max(0, percent));

        PercentType percentType = new PercentType((int) Math.round(percent));
        fireUpdate(LcnChannelGroup.OUTPUT, outputId, percentType);

        if (outputId == 3) {
            output4 = percentType;
        }

        if (percent > 0) {
            if (outputId == 0) {
                fireUpdate(LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, UpDownType.UP);
            } else if (outputId == 1) {
                fireUpdate(LcnChannelGroup.ROLLERSHUTTEROUTPUT, 0, UpDownType.DOWN);
            }
        }
    }
}
