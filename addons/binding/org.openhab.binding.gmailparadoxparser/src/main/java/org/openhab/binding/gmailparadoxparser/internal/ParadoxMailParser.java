/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.gmailparadoxparser.internal.model.ParadoxPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxMailParser {

    private static Logger logger = LoggerFactory.getLogger(ParadoxMailParser.class);

    public static Set<ParadoxPartition> parseToParadoxPartitions(List<String> mailContents) {
        Set<ParadoxPartition> partitionsStates = new HashSet<>();
        for (String mail : mailContents) {
            String[] split = mail.split(System.getProperty("line.separator"));
            Map<String, String> mailResultMap = parseToMap(split);
            logger.debug(mailResultMap.toString());
            partitionsStates.add(new ParadoxPartition(cleanCarriageReturn(mailResultMap.get("Message")),
                    cleanCarriageReturn(mailResultMap.get("Partition")), cleanCarriageReturn(mailResultMap.get("By")),
                    cleanCarriageReturn(mailResultMap.get("Time"))));
        }

        return partitionsStates;
    }

    private static String cleanCarriageReturn(String input) {
        if (input == null || input.length() == 0) {
            return input;
        }

        return input.substring(0, input.length());
    }

    private static Map<String, String> parseToMap(String[] lines) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String[] split = lines[i].split(": ");
            if (split.length > 1) {
                result.put(split[0].replaceAll("(\\r|\\n)", ""), split[1].replaceAll("(\\r|\\n)", ""));
                // logger.debug("Key: " + split[0] + "\tValue: " + split[1]);
            } else {
                logger.debug("Message cannot be split: " + lines[i]);
            }

        }

        return result;
    }
}
