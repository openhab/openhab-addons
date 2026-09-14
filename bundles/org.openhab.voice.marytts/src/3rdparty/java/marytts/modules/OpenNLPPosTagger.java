/**
 * Copyright 2007 DFKI GmbH.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package marytts.modules;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

import marytts.datatypes.MaryData;
import marytts.datatypes.MaryDataType;
import marytts.server.MaryProperties;
import marytts.util.MaryUtils;
import marytts.util.dom.MaryDomUtils;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagFormat;
import opennlp.tools.postag.POSTaggerME;

/**
 * OpenNLP POS tagger compatible with the model and tag set expected by MaryTTS 5.2.1.
 *
 * <p>
 * MaryTTS 5.2.1 uses OpenNLP's removed {@code tag(List)} API and the legacy one-argument constructor. This replacement
 * uses the current array API and explicitly retains Penn Treebank output, on which MaryTTS's English prosody rules
 * depend.
 *
 * @author Marc Schröder - Initial contribution
 * @author Leo Siepel - Refactored for use with openHAB's MaryTTS runtime
 */
@NonNullByDefault
public class OpenNLPPosTagger extends InternalModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNLPPosTagger.class);

    private final String propertyPrefix;

    private @Nullable POSTaggerME tagger;
    private @Nullable Map<String, String> posMapper;

    public OpenNLPPosTagger(String locale, String propertyPrefix) throws Exception {
        super("OpenNLPPosTagger", MaryDataType.WORDS, MaryDataType.PARTSOFSPEECH, MaryUtils.string2locale(locale));
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    @Override
    public void startup() throws Exception {
        super.startup();

        try (InputStream modelStream = MaryProperties.needStream(propertyPrefix + "model")) {
            tagger = new POSTaggerME(new POSModel(modelStream), POSTagFormat.PENN);
        }

        InputStream posMapStream = MaryProperties.getStream(propertyPrefix + "posMap");
        if (posMapStream != null) {
            posMapper = readPosMapper(posMapStream);
        }
    }

    private Map<String, String> readPosMapper(InputStream posMapStream) throws Exception {
        Map<String, String> mapper = new HashMap<>();
        try (posMapStream; BufferedReader reader = new BufferedReader(new InputStreamReader(posMapStream, UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isBlank()) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    if (tokenizer.countTokens() >= 2) {
                        mapper.put(tokenizer.nextToken(), tokenizer.nextToken());
                    } else {
                        LOGGER.warn("Invalid POS map line (expected 2 tokens): '{}'", line);
                    }
                }
            }
        }
        return mapper;
    }

    @Override
    public MaryData process(@NonNullByDefault({}) MaryData input) throws Exception {
        Document document = input.getDocument();
        NodeIterator sentenceIterator = MaryDomUtils.createNodeIterator(document, document, "s");

        Element sentence;
        while ((sentence = (Element) sentenceIterator.nextNode()) != null) {
            tagSentence(sentence);
        }

        MaryData output = new MaryData(getOutputType(), input.getLocale());
        output.setDocument(document);
        return output;
    }

    private void tagSentence(Element sentence) {
        TreeWalker tokenWalker = MaryDomUtils.createTreeWalker(sentence, "t");
        List<Element> tokens = new ArrayList<>();
        List<String> words = new ArrayList<>();

        Element token;
        while ((token = (Element) tokenWalker.nextNode()) != null) {
            tokens.add(token);
            words.add(MaryDomUtils.tokenText(token));
        }

        // OpenNLP cannot determine useful context from a one-token sentence.
        if (words.size() == 1) {
            words.add(".");
        }

        String[] tags;
        synchronized (this) {
            POSTaggerME tagger = this.tagger;
            if (tagger != null) {
                tags = tagger.tag(words.toArray(String[]::new));
            } else {
                tags = new String[words.size()];
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Element currentToken = tokens.get(i);
            if (!currentToken.hasAttribute("pos")) {
                currentToken.setAttribute("pos", mapPosTag(tags[i]));
            }
        }
    }

    private String mapPosTag(String posTag) {
        Map<String, String> posMapper = this.posMapper;
        if (posMapper == null) {
            return posTag;
        }

        String mappedTag = posMapper.get(posTag);
        if (mappedTag == null) {
            LOGGER.warn("POS map file incomplete: do not know how to map '{}'", posTag);
            return posTag;
        }
        return mappedTag;
    }
}
