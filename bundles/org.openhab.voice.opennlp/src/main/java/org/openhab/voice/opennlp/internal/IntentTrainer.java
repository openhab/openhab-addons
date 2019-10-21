/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.voice.opennlp.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.UnsupportedLanguageException;
import org.openhab.voice.opennlp.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.ml.AbstractTrainer;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;

/**
 * This class is used both for training the OpenNLP document categorizer and token extractor, and for doing the actual
 * natural language understanding - it converts a natural query into an {@link Intent}.
 *
 * @author Yannick Schaus - Initial contribution
 */
public class IntentTrainer {

    private final Logger logger = LoggerFactory.getLogger(IntentTrainer.class);

    private DocumentCategorizerME categorizer;
    private NameFinderME nameFinder;
    private Tokenizer tokenizer;

    /**
     * Trains a new IntentTrainer instance with intents sourced from a collection of skills for the specified language
     *
     * @param language the ISO-639 language code
     * @param skills the collection of skills providing training data
     * @throws Exception
     */
    public IntentTrainer(String language, Collection<Skill> skills) throws Exception {
        this(language, skills, null, null);
    }

    /**
     * Trains a new IntentTrainer instance with intents sourced from a collection of skills for the specified language,
     * and additional name samples
     *
     * @param language the ISO-639 language code
     * @param skills the collection of skills providing training data
     * @param additionalNameSamples additional user-provided name samples to train the token name finder
     * @param tokenizerId tokenizer to use ("alphanumeric" or "whitespace")
     * @throws Exception
     */
    public IntentTrainer(String language, Collection<Skill> skills, InputStream additionalNameSamples,
            String tokenizerId) throws Exception {
        this.tokenizer = (tokenizerId == "alphanumeric") ? AlphaNumericTokenizer.INSTANCE
                : WhitespaceTokenizer.INSTANCE;

        /* Prepare the streams of document samples sourced from each skill's training data */
        List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<ObjectStream<DocumentSample>>();
        for (Skill skill : skills) {
            String intent = skill.getIntentId();

            try {
                InputStream trainingData = skill.getTrainingData(language);
                ObjectStream<String> lineStream = new LowerCasePlainTextByLineStream(trainingData);
                ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream(intent, lineStream);
                categoryStreams.add(documentSampleStream);
            } catch (UnsupportedLanguageException e) {
                logger.warn("Ignoring intent {} because no training data for language {}", skill.getIntentId(),
                        language);
            }
        }

        if (categoryStreams.isEmpty()) {
            throw new UnsupportedLanguageException(language);
        }

        /* concatenate the document samples object streams into one to feed to the trainer */
        ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils
                .concatenateObjectStream(categoryStreams);

        TrainingParameters trainingParams = TrainingParameters.defaultParams();
        trainingParams.put(AbstractTrainer.VERBOSE_PARAM, false);

        /* train the categorizer! */
        DoccatModel doccatModel = DocumentCategorizerME.train(language, combinedDocumentSampleStream, trainingParams,
                new DoccatFactory());
        combinedDocumentSampleStream.close();

        List<TokenNameFinderModel> tokenNameFinderModels = new ArrayList<TokenNameFinderModel>();

        /* Use the skill's training data again, to train the entity extractor (token name finder) this time */
        List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();
        for (Skill skill : skills) {
            try {
                InputStream trainingData = skill.getTrainingData(language);
                ObjectStream<String> lineStream = new LowerCasePlainTextByLineStream(trainingData);
                ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
                nameStreams.add(nameSampleStream);
            } catch (UnsupportedLanguageException e) {
                logger.warn("Ignoring intent {} because no training data for language {}", skill.getIntentId(),
                        language);
            }
        }

        /* Also use the additional training data for entity extraction (i.e. actual items' tags) if provided */
        if (additionalNameSamples != null) {
            ObjectStream<String> additionalLineStream = new LowerCasePlainTextByLineStream(additionalNameSamples);
            ObjectStream<NameSample> additionalNameSamplesStream = new NameSampleDataStream(additionalLineStream);
            nameStreams.add(additionalNameSamplesStream);
        }

        /* concatenate the name samples object streams into one to feed to the trainer */
        ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils.concatenateObjectStream(nameStreams);

        /* train the token name finder! */
        TokenNameFinderModel tokenNameFinderModel = NameFinderME.train(language, null, combinedNameSampleStream,
                trainingParams, new TokenNameFinderFactory());
        combinedNameSampleStream.close();
        tokenNameFinderModels.add(tokenNameFinderModel);

        categorizer = new DocumentCategorizerME(doccatModel);
        nameFinder = new NameFinderME(tokenNameFinderModel);
        logger.debug("IntentTrainer created");
    }

    /**
     * Tries to understand the natural language query into an {@link Intent}
     *
     * @param query the natural language query
     * @return the resulting @{link Intent}
     */
    public Intent interpret(String query) {
        String[] tokens = this.tokenizer.tokenize(query.toLowerCase());
        // remove eventual trailing punctuation
        tokens[tokens.length - 1] = tokens[tokens.length - 1].replaceAll("\\s*[!?.]+$", "");

        double[] outcome = categorizer.categorize(tokens);
        logger.debug("{}", categorizer.getAllResults(outcome));

        Intent intent = new Intent(categorizer.getBestCategory(outcome));

        Span[] spans = nameFinder.find(tokens);
        String[] names = Span.spansToStrings(spans, tokens);
        for (int i = 0; i < spans.length; i++) {
            logger.debug("Span {}: {} {} {} name {}", i, spans[i].getStart(), spans[i].getEnd(), spans[i].getType(),
                    names[i]);
            intent.getEntities().put(spans[i].getType(), names[i]);
        }

        logger.debug("IntentTrainer interpret => {}", intent.toString());

        return intent;
    }

    /**
     * Retrieves a sorted score map of the categorisation results for a query.
     * Mainly used for testing the categorizer.
     *
     * @param query the natural language query
     * @return the score map
     */
    public SortedMap<Double, Set<String>> getScoreMap(String query) {
        String[] tokens = this.tokenizer.tokenize(query.toLowerCase());
        return categorizer.sortedScoreMap(tokens);
    }

}
