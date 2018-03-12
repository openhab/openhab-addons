/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.channel;

import static org.openhab.binding.knx.KNXBindingConstants.CHANNEL_GENERIC;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.channel.AbstractKNXChannelType;
import org.openhab.binding.knx.internal.channel.ChannelConfiguration;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;

import tuwien.auto.calimero.dptxlator.DPTXlatorUtf8;

/**
 * Generic channel type description
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
class TypeGeneric extends AbstractKNXChannelType {

    private static final Pattern PATTERN = Pattern.compile(
            "^((?<dpt>[0-9]{1,2}\\.[0-9]{3}):)?(?<ga>[0-9]{1,3}/[0-9]{1,3}/[0-9]{1,3})(?<flags>(\\+([R,W,T,U]))*)$");

    private static final Pattern PATTERN_FLAGS = Pattern.compile("\\+(?<flag>[R,W,T,U])");

    TypeGeneric(String... channelTypeIDs) {
        super(CHANNEL_GENERIC);
    }

    @Override
    @Nullable
    protected ChannelConfiguration parse(@Nullable String fancy) {
        if (fancy == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(fancy);

        if (matcher.matches()) {
            String input = matcher.group("flags");
            Matcher m2 = PATTERN_FLAGS.matcher(input);
            boolean readFlag = false;
            boolean writeFlag = false;
            boolean transmitFlag = false;
            boolean updateFlag = false;

            while (m2.find()) {
                switch (m2.group("flag")) {
                    case "R": {
                        readFlag = true;
                        break;
                    }
                    case "W": {
                        writeFlag = true;
                        break;
                    }
                    case "T": {
                        transmitFlag = true;
                        break;
                    }
                    case "U": {
                        updateFlag = true;
                        break;
                    }
                }
            }

            List<GroupAddressConfiguration> listenGAs = new LinkedList<>();
            if (readFlag || transmitFlag) {
                listenGAs.add(new GroupAddressConfiguration(matcher.group("ga"), readFlag));
            }

            GroupAddressConfiguration mainGA = null;
            if (writeFlag || updateFlag) {
                mainGA = new GroupAddressConfiguration(matcher.group("ga"), readFlag);
            }

            return new ChannelConfiguration(matcher.group("dpt"), mainGA, listenGAs);
        }
        return null;
    }

    @Override
    protected Set<String> getAllGAKeys() {
        return Collections.singleton("ga");
    }

    @Override
    protected String getDefaultDPT(@Nullable String gaConfigKey) {
        return DPTXlatorUtf8.DPT_UTF8.getID();
    }
}
