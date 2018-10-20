/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal.mail.adapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.gmailparadoxparser.internal.GmailParadoxParserHandlerFactory;
import org.openhab.binding.gmailparadoxparser.internal.model.ParadoxPartition;

/**
 * The {@link GmailParadoxParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class MailParser {

    private static MailParser instance;
    // private Logger logger = LoggerFactory.getLogger(ParadoxStatesCache.class);

    public static MailParser getInstance() {
        if (instance == null) {
            instance = new MailParser();
        }
        return instance;
    }

    public Set<ParadoxPartition> parseToParadoxPartitionStates(List<String> mailContents) {
        Set<ParadoxPartition> partitionsStates = new HashSet<>();
        for (String mail : mailContents) {
            String[] split = mail.split(System.getProperty("line.separator"));
            Map<String, String> mailResultMap = MailParser.getInstance().parseToMap(split);
            partitionsStates.add(new ParadoxPartition(mailResultMap.get("Message"), mailResultMap.get("Partition"),
                    mailResultMap.get("By"), mailResultMap.get("Time")));
        }

        return partitionsStates;
    }

    private Map<String, String> parseToMap(String[] lines) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String[] split = lines[i].split(": ");
            if (split.length > 1) {
                result.put(split[0], split[1]);
                // logger.debug("Key: " + split[0] + "\tValue: " + split[1]);
            } else {
                // logger.debug("Message cannot be split: " + lines[i]);
            }

        }

        return result;
    }
}
