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

import static org.openhab.voice.opennlp.internal.OpenNLPInterpreter.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.core.voice.text.InterpretationResult;
import org.eclipse.smarthome.core.voice.text.ItemNamedAttribute;
import org.eclipse.smarthome.core.voice.text.ItemNamedAttribute.AttributeType;
import org.eclipse.smarthome.core.voice.text.ItemResolver;
import org.eclipse.smarthome.core.voice.text.UnsupportedLanguageException;
import org.openhab.voice.opennlp.AnswerFormatter;
import org.openhab.voice.opennlp.Skill;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OpenNLP-based interpreter used by HABot.
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - adapated to the extended interfaces + null annotations added
 */
@NonNullByDefault
@Component(service = HumanLanguageInterpreter.class, immediate = true, configurationPid = SERVICE_PID, property = {
        Constants.SERVICE_PID + "=" + SERVICE_PID,
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=" + SERVICE_URI,
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=" + SERVICE_LABEL,
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=" + SERVICE_CATEGORY })
public class OpenNLPInterpreter implements HumanLanguageInterpreter {

    static final String SERVICE_LABEL = "OpenNLP Interpreter";
    static final String SERVICE_ID = "opennlp";
    static final String SERVICE_CATEGORY = "voice";
    static final String SERVICE_PID = "org.openhab." + SERVICE_ID;
    static final String SERVICE_URI = SERVICE_CATEGORY + ":" + SERVICE_ID;

    private final Logger logger = LoggerFactory.getLogger(OpenNLPInterpreter.class);

    public static final Set<Locale> SUPPORTED_LOCALES = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN)));

    private @Nullable IntentTrainer intentTrainer;
    private @Nullable Locale currentLocale;
    private @Nullable String tokenizerId;

    private @NonNullByDefault({}) ItemRegistry itemRegistry;
    private @NonNullByDefault({}) ItemResolver itemResolver;
    private @NonNullByDefault({}) EventPublisher eventPublisher;

    private HashMap<String, Skill> skills = new HashMap<String, Skill>();

    private RegistryChangeListener<Item> registryChangeListener = new RegistryChangeListener<Item>() {
        @Override
        public void added(Item element) {
            intentTrainer = null;
        }

        @Override
        public void removed(Item element) {
            intentTrainer = null;
        }

        @Override
        public void updated(Item oldElement, Item element) {
            intentTrainer = null;
        }
    };

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_LABEL;
    }

    /**
     * Get an {@link InputStream} of additional name samples to feed to
     * the {@link IntentTrainer} to improve the recognition.
     *
     * @return an OpenNLP compatible input stream with the tagged name samples on separate lines
     */
    protected InputStream getNameSamples() throws UnsupportedLanguageException {
        StringBuilder nameSamplesDoc = new StringBuilder();
        Map<Item, Set<ItemNamedAttribute>> itemAttributes = itemResolver.getAllItemNamedAttributes();

        Stream<ItemNamedAttribute> attributes = itemAttributes.values().stream().flatMap(a -> a.stream());

        attributes.forEach(attribute -> {
            if (attribute.getType() == AttributeType.LOCATION) {
                nameSamplesDoc.append(String.format("<START:location> %s <END>%n", attribute.getValue()));
            } else {
                nameSamplesDoc.append(String.format("<START:object> %s <END>%n", attribute.getValue()));
            }
        });

        return IOUtils.toInputStream(nameSamplesDoc.toString());
    }

    @Override
    public @Nullable String interpret(@Nullable Locale locale, String text) throws InterpretationException {
        throw new InterpretationException("Voice control not yet supported by the OpenNLP interpreter");
    }

    @Override
    public InterpretationResult interpretForChat(Locale locale, String text) throws InterpretationException {
        logger.debug("interpretForChat text {}", text);
        if (text.isEmpty()) {
            // Return a greeting message
            AnswerFormatter answerFormatter = new AnswerFormatter(locale);
            return new InterpretationResult(locale.getLanguage(), answerFormatter.getRandomAnswer("greeting"));
        }

        if (!locale.equals(currentLocale) || intentTrainer == null) {
            try {
                itemResolver.setLocale(locale);
                intentTrainer = new IntentTrainer(locale.getLanguage(),
                        skills.values().stream().sorted(new Comparator<Skill>() {

                            @Override
                            public int compare(Skill o1, Skill o2) {
                                if (o1.getIntentId().equals("get-status")) {
                                    return -1;
                                }
                                if (o2.getIntentId().equals("get-status")) {
                                    return 1;
                                }
                                return o1.getIntentId().compareTo(o2.getIntentId());
                            }

                        }).collect(Collectors.toList()), getNameSamples(), this.tokenizerId);
                currentLocale = locale;
            } catch (Exception e) {
                InterpretationException fe = new InterpretationException(
                        "Error during trainer initialization: " + e.getMessage());
                fe.initCause(e);
                throw fe;
            }
        }

        Intent intent;

        // Shortcut: if there are any items whose named attributes match the query (case insensitive), consider
        // it a "get-status" intent with this attribute as the corresponding entity.
        // This allows the user to query a named attribute quickly by simply stating it - and avoid a
        // misinterpretation by the categorizer.
        if (this.itemResolver.getMatchingItems(text, null).findAny().isPresent()) {
            intent = new Intent("get-status");
            intent.setEntities(new HashMap<String, String>());
            intent.getEntities().put("object", text.toLowerCase());
        } else if (this.itemResolver.getMatchingItems(null, text).findAny().isPresent()) {
            intent = new Intent("get-status");
            intent.setEntities(new HashMap<String, String>());
            intent.getEntities().put("location", text.toLowerCase());
        } else {
            // Else, run it through the IntentTrainer
            intent = intentTrainer.interpret(text);
        }

        Skill skill = skills.get(intent.getName());
        if (skill == null) {
            throw new InterpretationException("No skill available for the intent " + intent.getName());
        }
        return skill.interpretForChat(intent, locale.getLanguage());
    }

    @Override
    public @Nullable String getGrammar(Locale locale, String format) {
        return null;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public Set<String> getSupportedGrammarFormats() {
        return Collections.emptySet();
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        if (this.itemRegistry == null) {
            this.itemRegistry = itemRegistry;
            this.itemRegistry.addRegistryChangeListener(registryChangeListener);
        }
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        if (itemRegistry == this.itemRegistry) {
            this.itemRegistry.removeRegistryChangeListener(registryChangeListener);
            this.itemRegistry = null;
        }
    }

    @Reference
    protected void setItemResolver(ItemResolver itemResolver) {
        this.itemResolver = itemResolver;
    }

    protected void unsetItemResolver(ItemResolver itemResolver) {
        this.itemResolver = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        if (this.eventPublisher == null) {
            this.eventPublisher = eventPublisher;
        }
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        if (eventPublisher == this.eventPublisher) {
            this.eventPublisher = null;
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addSkill(Skill skill) {
        this.skills.put(skill.getIntentId(), skill);
        // reset the trainer
        this.intentTrainer = null;
    }

    protected void removeSkill(Skill skill) {
        this.skills.remove(skill.getIntentId());
        // reset the trainer
        this.intentTrainer = null;
    }

    @Activate
    protected void activate(Map<String, Object> configProps, BundleContext bundleContext) {
        if (configProps.containsKey("tokenizer")) {
            this.tokenizerId = configProps.get("tokenizer").toString();
        }

        this.intentTrainer = null;
    }

    @Modified
    protected void modified(Map<String, Object> configProps) {
        if (configProps.containsKey("tokenizer")) {
            this.tokenizerId = configProps.get("tokenizer").toString();
        }

        this.intentTrainer = null;
    }

    @Deactivate
    protected void deactivate() {
    }
}
