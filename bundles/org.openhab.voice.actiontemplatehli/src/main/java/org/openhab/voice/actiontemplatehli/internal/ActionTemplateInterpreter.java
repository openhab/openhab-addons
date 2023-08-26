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
package org.openhab.voice.actiontemplatehli.internal;

import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.DYNAMIC_PLACEHOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.DYNAMIC_PLACEHOLDER_SYMBOL;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.GROUP_LABEL_PLACEHOLDER_SYMBOL;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.ITEM_LABEL_PLACEHOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.ITEM_LABEL_PLACEHOLDER_SYMBOL;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.ITEM_OPTION_PLACEHOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.ITEM_OPTION_PLACEHOLDER_SYMBOL;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.NER_FOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.NLP_FOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.POS_FOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_CATEGORY;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_ID;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_NAME;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.SERVICE_PID;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.STATE_PLACEHOLDER;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.STATE_PLACEHOLDER_SYMBOL;
import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.TYPE_ACTION_CONFIGS_FOLDER;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.openhab.core.voice.text.HumanLanguageInterpreter;
import org.openhab.core.voice.text.InterpretationException;
import org.openhab.voice.actiontemplatehli.internal.configuration.ActionTemplateConfiguration;
import org.openhab.voice.actiontemplatehli.internal.configuration.ActionTemplateGroupTargets;
import org.openhab.voice.actiontemplatehli.internal.configuration.ActionTemplatePlaceholder;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;
import opennlp.tools.util.StringList;

/**
 * The {@link ActionTemplateInterpreter} is a configurable interpreter powered by OpenNLP
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME, description_uri = SERVICE_CATEGORY + ":"
        + SERVICE_ID)
public class ActionTemplateInterpreter implements HumanLanguageInterpreter {
    static {
        Logger logger = LoggerFactory.getLogger(ActionTemplateInterpreter.class);
        createFolder(logger, NLP_FOLDER);
        createFolder(logger, NER_FOLDER);
        createFolder(logger, POS_FOLDER);
        createFolder(logger, TYPE_ACTION_CONFIGS_FOLDER);
    }
    private static final Pattern COLOR_HEX_PATTERN = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");
    private final Logger logger = LoggerFactory.getLogger(ActionTemplateInterpreter.class);
    private final ItemRegistry itemRegistry;
    private final MetadataRegistry metadataRegistry;
    private final EventPublisher eventPublisher;
    private ActionTemplateInterpreterConfiguration config = new ActionTemplateInterpreterConfiguration();
    private Tokenizer tokenizer = WhitespaceTokenizer.INSTANCE;
    private List<String> optionalLanguageTags = List.of();
    @Nullable
    private NLPItemMaps nlpItemMaps;

    private final RegistryChangeListener<Item> registryChangeListener = new RegistryChangeListener<>() {
        @Override
        public void added(Item element) {
            invalidate();
        }

        @Override
        public void removed(Item element) {
            invalidate();
        }

        @Override
        public void updated(Item oldElement, Item element) {
            invalidate();
        }
    };

    @Activate
    public ActionTemplateInterpreter(@Reference ItemRegistry itemRegistry, @Reference MetadataRegistry metadataRegistry,
            @Reference EventPublisher eventPublisher) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.eventPublisher = eventPublisher;
        itemRegistry.addRegistryChangeListener(registryChangeListener);
    }

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        this.config = new Configuration(config).as(ActionTemplateInterpreterConfiguration.class);
        reloadConfigs();
    }

    @Deactivate
    protected void deactivate() {
        itemRegistry.removeRegistryChangeListener(registryChangeListener);
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public @Nullable String getGrammar(@Nullable Locale locale, @Nullable String s) {
        return null;
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        // all are supported
        return Set.of();
    }

    @Override
    public Set<String> getSupportedGrammarFormats() {
        return Set.of();
    }

    @Override
    public String interpret(Locale locale, String words) throws InterpretationException {
        if (words.isEmpty()) {
            throw new InterpretationException(config.unhandledMessage);
        }
        try {
            var finalWords = config.lowerText ? words.toLowerCase(locale) : words;
            var info = getNLPInfo(finalWords);
            if (info.tokens.length == 0) {
                logger.debug("no tokens produced; aborting");
                throw new InterpretationException(config.failureMessage);
            }
            String response = processAction(finalWords,
                    checkActionConfigs(finalWords, info.tokens, info.tags, info.lemmas));
            if (response == null) {
                logger.debug("silent mode; no response");
                return "";
            }
            logger.debug("response: {}", response);
            return response;
        } catch (IOException e) {
            logger.debug("IOException while interpreting: {}", e.getMessage(), e);
            var message = e.getMessage();
            throw new InterpretationException(message != null ? message : "Unknown error");
        } catch (RuntimeException e) {
            var message = e.getMessage();
            logger.debug("RuntimeException while interpreting: {}", e.getMessage(), e);
            throw new InterpretationException(message != null ? message : "Unknown error");
        }
    }

    private @Nullable String processAction(String words, @Nullable NLPInterpretationResult result)
            throws InterpretationException, IOException {
        if (result != null) {
            if (!result.actionConfig.read) {
                return sendItemCommand(result.targetItem, words, result.actionConfig, result.placeholderValues);
            } else {
                return readItemState(result.targetItem, result.actionConfig);
            }
        } else {
            throw new InterpretationException(config.unhandledMessage);
        }
    }

    private NLPInfo getNLPInfo(String text) throws IOException {
        logger.debug("Processing: '{}'", text);
        var tokens = tokenizeText(text);
        var tags = languagePOSTagging(tokens);
        var lemmas = languageLemmatize(tokens, tags);
        logger.debug("tokens: {}", List.of(tokens));
        logger.debug("tags: {}", List.of(tags));
        logger.debug("lemmas: {}", List.of(lemmas));
        return new NLPInfo(tokens, lemmas, tags);
    }

    private @Nullable NLPInterpretationResult checkActionConfigs(String text, String[] tokens, String[] tags,
            String[] lemmas) throws IOException {
        // item defined actions have priority over type defined actions
        var result = checkItemActions(text, tokens, tags, lemmas);
        if (result != null) {
            return result;
        }
        return checkTypeActionsConfigs(text, tokens, tags, lemmas);
    }

    private @Nullable NLPInterpretationResult checkItemActions(String text, String[] tokens, String[] tags,
            String[] lemmas) throws IOException {
        // Check item with action config
        var itemsWithActions = getItemsWithActionConfigs();
        Item targetItem = null;
        ActionTemplateConfiguration targetActionConfig = null;
        // store data to restore placeholder values
        List<NLPPlaceholderData> placeholderValues = null;
        // store span of dynamic placeholder, to invalidate others
        Span dynamicSpan = null;
        int matchScore = 0;
        for (var entry : itemsWithActions.entrySet()) {
            var actionConfigs = entry.getValue();
            for (var actionConfig : actionConfigs) {
                var templates = Arrays.stream(actionConfig.template.split(";")).map(String::trim)
                        .collect(Collectors.toList());
                for (var template : templates) {
                    List<NLPPlaceholderData> currentPlaceholderValues = new ArrayList<>();
                    var currentItem = entry.getKey();
                    var scoreResult = getScoreWithPlaceholders(text, currentItem, actionConfig.memberTargets,
                            actionConfig.read, tokens, tags, lemmas, actionConfig, template, currentPlaceholderValues);
                    if (scoreResult.score != 0 && scoreResult.score == matchScore) {
                        if (targetItem == currentItem) {
                            logger.warn(
                                    "multiple alternative templates for item '{}' has the same score, '{}' can be removed",
                                    targetItem.getName(), template);
                        } else {
                            logger.warn(
                                    "multiple templates with same score for items '{}' and '{}', the action with template '{}' can be removed",
                                    targetItem.getName(), currentItem.getName(), template);
                        }
                    }
                    if (scoreResult.score > matchScore) {
                        targetItem = currentItem;
                        targetActionConfig = actionConfig;
                        placeholderValues = currentPlaceholderValues;
                        matchScore = scoreResult.score;
                        dynamicSpan = scoreResult.dynamicSpan;
                    }
                }
            }
        }
        if (targetItem != null && targetActionConfig != null && placeholderValues != null) {
            if (dynamicSpan != null) {
                placeholderValues = updatePlaceholderValues(text, tokens, placeholderValues, dynamicSpan);
            }
            return new NLPInterpretationResult(targetItem, targetActionConfig, placeholderValues.stream()
                    .collect(Collectors.toMap(i -> i.placeholderName, i -> i.placeholderValue)));
        }
        return null;
    }

    private @Nullable NLPInterpretationResult checkTypeActionsConfigs(String text, String[] tokens, String[] tags,
            String[] lemmas) throws IOException {
        // Check item command
        var itemLabelSpans = nerItemLabels(tokens);
        logger.debug("itemLabelSpans: {}", List.of(itemLabelSpans));
        if (itemLabelSpans.length == 0) {
            logger.debug("No item labels found!");
            return null;
        }
        Item finalTargetItem = null;
        ActionTemplateConfiguration targetActionConfig = null;
        // store data to restore placeholder values
        List<NLPPlaceholderData> placeholderValues = null;
        // store span of dynamic placeholder, to invalidate others
        Span dynamicSpan = null;
        int matchScore = 0;
        // iterate itemLabelSpan to score the templates with each of them
        for (var itemLabelSpan : itemLabelSpans) {
            var labelTokens = getTargetItemTokens(tokens, itemLabelSpan);
            var targetItem = getTargetItemByLabelTokens(labelTokens);
            if (targetItem == null) {
                return null;
            }
            var tokensWithGenericLabel = replacePlaceholder(text, tokens, itemLabelSpan, ITEM_LABEL_PLACEHOLDER, null,
                    null);
            var lemmasWithGenericLabel = lemmas.length > 0
                    ? replacePlaceholder(text, lemmas, itemLabelSpan, ITEM_LABEL_PLACEHOLDER, null, null)
                    : new String[] {};
            var tagsWithGenericLabel = tags.length > 0
                    ? replacePlaceholder(text, tags, itemLabelSpan, ITEM_LABEL_PLACEHOLDER, null, null)
                    : new String[] {};
            logger.debug("Target item {}", targetItem.getName());
            // load templates defined for this item type
            var typeActionConfigs = getTypeActionConfigs(targetItem.getType());
            for (var actionConfig : typeActionConfigs) {
                // check required item tags
                if (actionConfig.requiredItemTags.length != 0) {
                    var itemLabels = targetItem.getTags();
                    if (!Arrays.stream(actionConfig.requiredItemTags).allMatch(itemLabels::contains)) {
                        logger.debug("action '{}' skipped, tags constrain '{}'", actionConfig.template,
                                List.of(actionConfig.requiredItemTags));
                        continue;
                    }
                }
                var templates = Arrays.stream(actionConfig.template.split(";")).map(String::trim)
                        .collect(Collectors.toList());
                for (var template : templates) {
                    var replacedValues = new ArrayList<NLPPlaceholderData>();
                    var scoreResult = getScoreWithPlaceholders(text, targetItem, actionConfig.memberTargets,
                            actionConfig.read, tokensWithGenericLabel, tagsWithGenericLabel, lemmasWithGenericLabel,
                            actionConfig, template, replacedValues);
                    if (scoreResult.score != 0 && scoreResult.score == matchScore
                            && actionConfig.requiredItemTags.length == targetActionConfig.requiredItemTags.length) {
                        if (targetActionConfig == actionConfig) {
                            logger.warn(
                                    "multiple alternative templates with same score, you can remove the alternative '{}'",
                                    template);
                        } else {
                            logger.warn(
                                    "multiple templates with same score, the action with template '{}' can be removed",
                                    template);
                        }
                    }
                    // for rules with same score the one with more restrictions have prevalence
                    if (scoreResult.score > matchScore || (scoreResult.score == matchScore && targetActionConfig != null
                            && actionConfig.requiredItemTags.length > targetActionConfig.requiredItemTags.length)) {
                        finalTargetItem = targetItem;
                        placeholderValues = replacedValues;
                        targetActionConfig = actionConfig;
                        matchScore = scoreResult.score;
                        dynamicSpan = scoreResult.dynamicSpan;
                    }
                }
            }
        }
        if (finalTargetItem != null && targetActionConfig != null && placeholderValues != null) {
            if (dynamicSpan != null) {
                placeholderValues = updatePlaceholderValues(text, tokens, placeholderValues, dynamicSpan);
            }
            return NLPInterpretationResult.from(finalTargetItem, targetActionConfig, placeholderValues);
        }
        return null;
    }

    private List<NLPPlaceholderData> updatePlaceholderValues(String text, String[] tokens,
            List<NLPPlaceholderData> placeholderValues, Span dynamicSpan) {
        // we should clean up placeholder values detected inside the dynamic template
        var validPlaceholderValues = placeholderValues.stream().filter(i -> !dynamicSpan.contains(i.placeholderSpan))
                .collect(Collectors.toList());
        // add dynamic content to values
        validPlaceholderValues.add(new NLPPlaceholderData(DYNAMIC_PLACEHOLDER,
                detokenize(Arrays.copyOfRange(tokens, dynamicSpan.getStart(), dynamicSpan.getEnd()), text),
                dynamicSpan));
        return validPlaceholderValues;
    }

    private Set<Item> getMembersByTypeRecursive(GroupItem group, String itemType, String[] requiredMemberTags) {
        Stream<Item> targetMembersStream = getMembersByType(group, itemType, requiredMemberTags).stream();
        var childGroups = getMembersByType(group, "Group", new String[] {});
        for (var childGroup : childGroups) {
            targetMembersStream = Stream.concat(targetMembersStream,
                    getMembersByTypeRecursive((GroupItem) childGroup, itemType, requiredMemberTags).stream());
        }
        return targetMembersStream.collect(Collectors.toUnmodifiableSet());
    }

    private State mergeSwitchMembersState(GroupItem group, String[] requiredMemberTags, boolean recursive) {
        var result = OnOffType.OFF;
        var targetMembers = recursive ? getMembersByTypeRecursive(group, "Switch", requiredMemberTags)
                : getMembersByType(group, "Switch", requiredMemberTags);
        for (var member : targetMembers) {
            if (UnDefType.UNDEF.equals(member.getState())) {
                return UnDefType.UNDEF;
            }
            if (UnDefType.NULL.equals(member.getState())) {
                return UnDefType.NULL;
            }
            if (OnOffType.ON.equals(member.getState())) {
                result = OnOffType.ON;
            }
        }
        return result;
    }

    private State mergeContactMembersState(GroupItem group, String[] requiredMemberTags, boolean recursive) {
        var result = OpenClosedType.CLOSED;
        var targetMembers = recursive ? getMembersByTypeRecursive(group, "Contact", requiredMemberTags)
                : getMembersByType(group, "Contact", requiredMemberTags);
        for (var member : targetMembers) {
            if (UnDefType.UNDEF.equals(member.getState())) {
                return UnDefType.UNDEF;
            }
            if (UnDefType.NULL.equals(member.getState())) {
                return UnDefType.NULL;
            }
            if (OpenClosedType.OPEN.equals(member.getState())) {
                result = OpenClosedType.OPEN;
            }
        }
        return result;
    }

    private String readItemState(Item targetItem, ActionTemplateConfiguration actionConfigMatch)
            throws IOException, InterpretationException {
        var memberTargets = actionConfigMatch.memberTargets;
        String state = null;
        String itemLabel = targetItem.getLabel();
        String groupLabel = null;
        Item finalTargetItem = targetItem;
        if ("Group".equals(finalTargetItem.getType()) && memberTargets != null) {
            if (memberTargets.mergeState && memberTargets.itemName.isEmpty() && !memberTargets.itemType.isEmpty()) {
                // handle states that can be merged
                switch (memberTargets.itemType) {
                    case "Switch":
                        state = mergeSwitchMembersState((GroupItem) finalTargetItem, memberTargets.requiredItemTags,
                                memberTargets.recursive).toFullString();
                        break;
                    case "Contact":
                        state = mergeContactMembersState((GroupItem) finalTargetItem, memberTargets.requiredItemTags,
                                memberTargets.recursive).toFullString();
                        break;
                    default:
                        logger.warn("state merge is not available for members of type {}", memberTargets.itemType);
                        throw new InterpretationException(config.failureMessage);
                }
            }
            if (state == null) {
                Set<Item> targetMembers = getTargetMembers((GroupItem) finalTargetItem, memberTargets);
                if (!targetMembers.isEmpty()) {
                    if (targetMembers.size() > 1) {
                        logger.warn("read action matches {} item members inside a group, using the first one",
                                targetMembers.size());
                    }
                    var targetMember = targetMembers.iterator().next();
                    // only one target in the group, adding groupLabel placeholder value
                    groupLabel = itemLabel;
                    itemLabel = targetMember.getLabel();
                    state = targetMember.getState().toFullString();
                    finalTargetItem = targetMember;
                } else {
                    logger.warn("configured targetMembers were not found in group '{}'", finalTargetItem.getName());
                    throw new InterpretationException(config.failureMessage);
                }
            }
        }
        if (state == null) {
            state = finalTargetItem.getState().toFullString();
        }
        var statePlaceholder = actionConfigMatch.placeholders.stream().filter(p -> p.label.equals(STATE_PLACEHOLDER))
                .findFirst();
        var itemState = state;
        if (statePlaceholder.isPresent()) {
            state = applyPOSTransformation(state, statePlaceholder.get());
        }
        var template = actionConfigMatch.value;
        if (!actionConfigMatch.emptyValue.isEmpty() && (state.isEmpty()
                || (UnDefType.UNDEF.toFullString().equals(state) || UnDefType.NULL.toFullString().equals(state)))) {
            // use alternative template for empty values
            template = actionConfigMatch.emptyValue;
        }
        if (template instanceof String) {
            String templateText = (String) template;
            if (templateText.contains(ITEM_OPTION_PLACEHOLDER)) {
                var itemOptionPlaceholder = getItemOptionPlaceholder(finalTargetItem, true, null);
                if (itemOptionPlaceholder != null) {
                    itemState = applyPOSTransformation(itemState, itemOptionPlaceholder);
                }
            }
            return templateText.replace(STATE_PLACEHOLDER_SYMBOL, state)
                    .replace(ITEM_OPTION_PLACEHOLDER_SYMBOL, itemState)
                    .replace(ITEM_LABEL_PLACEHOLDER_SYMBOL, itemLabel != null ? itemLabel : "")
                    .replace(GROUP_LABEL_PLACEHOLDER_SYMBOL, groupLabel != null ? groupLabel : "");
        }
        return state;
    }

    private NLPTokenComparisonResult getScoreWithPlaceholders(String text, Item targetItem,
            @Nullable ActionTemplateGroupTargets targetMembers, boolean isRead, String[] tokens, String[] tags,
            String[] lemmas, ActionTemplateConfiguration actionConfiguration, String template,
            List<NLPPlaceholderData> placeholderValues) throws IOException {
        var placeholders = new ArrayList<>(actionConfiguration.placeholders);
        var finalTokens = tokens;
        var finalLemmas = lemmas;
        var finalTags = tags;
        if (template.contains(ITEM_OPTION_PLACEHOLDER_SYMBOL)) {
            var itemOptionPlaceholder = getItemOptionPlaceholder(targetItem, isRead, targetMembers);
            if (itemOptionPlaceholder == null) {
                return NLPTokenComparisonResult.ZERO;
            }
            placeholders.add(itemOptionPlaceholder);
        }
        for (var placeholder : placeholders) {
            if (actionConfiguration.read && placeholder.label.equals(STATE_PLACEHOLDER)) {
                // This placeholder is reserved on read mode should not be replaced now
                continue;
            }
            if (placeholder.label.equals(DYNAMIC_PLACEHOLDER)) {
                logger.warn("the name {} is reserved for the dynamic placeholder", DYNAMIC_PLACEHOLDER);
                continue;
            }
            var nerStaticValues = placeholder.nerStaticValues;
            var nerFile = placeholder.nerFile;
            Span[] nerSpans;
            Map<String[], String> possibleValuesByTokensMap = null;
            if (nerStaticValues != null) {
                possibleValuesByTokensMap = getStringsByTokensMap(nerStaticValues);
                nerSpans = nerValues(finalTokens, possibleValuesByTokensMap.keySet().toArray(String[][]::new),
                        placeholder.label);
            } else if (nerFile != null) {
                nerSpans = nerWithFile(finalTokens, nerFile);
            } else {
                logger.warn("Placeholder {} could not be applied due to missing ner config", placeholder.label);
                continue;
            }
            for (Span nerSpan : nerSpans) {
                var placeholderName = placeholder.label;
                finalTokens = replacePlaceholder(text, finalTokens, nerSpan, placeholderName, placeholderValues,
                        possibleValuesByTokensMap);
                if (finalLemmas.length > 0) {
                    finalLemmas = replacePlaceholder(text, finalLemmas, nerSpan, placeholderName, null, null);
                }
                if (finalTags.length > 0) {
                    finalTags = replacePlaceholder(text, finalTags, nerSpan, placeholderName, null, null);
                }
            }
        }
        return getScore(finalTokens, finalTags, finalLemmas, actionConfiguration, template);
    }

    private NLPTokenComparisonResult getScore(String[] tokens, String[] tags, String[] lemmas,
            ActionTemplateConfiguration actionConfiguration, String template) {
        switch (actionConfiguration.type) {
            case "tokens":
                String[] tokensTemplate = splitString(template, "\\s");
                var scoreByTokens = compareTokens(tokens, tags, tokensTemplate);
                logger.debug("tokens '{}' score: {}", List.of(tokensTemplate), scoreByTokens.score);
                return scoreByTokens;
            case "lemmas":
                String[] lemmasTemplate = splitString(template, "\\s");
                var scoreByLemmas = compareTokens(lemmas, tags, lemmasTemplate);
                logger.debug("lemmas '{}' score: {}", List.of(lemmasTemplate), scoreByLemmas.score);
                return scoreByLemmas;
            default:
                logger.warn("Unsupported template type '{}'", actionConfiguration.type);
                return NLPTokenComparisonResult.ZERO;
        }
    }

    private @Nullable String sendItemCommand(Item item, String text, ActionTemplateConfiguration actionConfiguration,
            Map<String, String> placeholderValues) throws IOException, InterpretationException {
        Object valueTemplate = actionConfiguration.value;
        boolean silent = actionConfiguration.silent;
        String replacedValue = null;
        Command command = null;
        // Special type handling
        switch (item.getType()) {
            case "Color":
                if (valueTemplate instanceof String) {
                    replacedValue = templatePlaceholders((String) valueTemplate, item, placeholderValues,
                            actionConfiguration.placeholders);
                    if (COLOR_HEX_PATTERN.matcher(replacedValue).matches()) {
                        Color rgb = Color.decode(replacedValue);
                        try {
                            command = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
                        } catch (NumberFormatException e) {
                            logger.warn("Unable to parse value '{}' as color", replacedValue);
                            throw new InterpretationException(config.failureMessage);
                        }
                    }
                }
                break;
            case "Group":
                var groupItem = (GroupItem) item;
                var memberTargetsConfig = actionConfiguration.memberTargets;
                if (memberTargetsConfig != null) {
                    Set<Item> targetMembers = getTargetMembers(groupItem, memberTargetsConfig);
                    logger.debug("{} target members were found in group {}", targetMembers.size(), groupItem.getName());
                    if (!targetMembers.isEmpty()) {
                        // swap the command target by the matched members
                        boolean ok = true;
                        boolean groupsilent = true;
                        for (var targetMember : targetMembers) {
                            var response = sendItemCommand(targetMember, text, actionConfiguration, placeholderValues);
                            if (config.failureMessage.equals(response)) {
                                ok = false;
                            }
                            if (response != null) {
                                groupsilent = false;
                            }
                        }
                        return ok ? (groupsilent ? null : config.commandSentMessage) : config.failureMessage;
                    } else {
                        logger.warn("configured targetMembers were not found in group '{}'", groupItem.getName());
                        throw new InterpretationException(config.failureMessage);
                    }
                }
                break;
        }
        if (command == null) {
            // Common behavior
            var objectValue = actionConfiguration.value;
            if (objectValue != null) {
                if (replacedValue == null) {
                    var stringValue = String.valueOf(objectValue);
                    replacedValue = templatePlaceholders(stringValue, item, placeholderValues,
                            actionConfiguration.placeholders);
                }
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), replacedValue);
            } else if ("String".equals(item.getType())) {
                // We interpret processing will continue in a rule
                silent = true;
                command = new StringType(text);
            }
        }
        if (command == null) {
            logger.warn("Command '{}' is not valid for item '{}'.", actionConfiguration.value, item.getName());
            throw new InterpretationException(config.failureMessage);
        }
        eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), command));
        if (silent) {
            // when silent mode give no result
            return null;
        } else {
            return config.commandSentMessage;
        }
    }

    private @Nullable ActionTemplatePlaceholder getItemOptionPlaceholder(Item targetItem, boolean isRead,
            @Nullable ActionTemplateGroupTargets memberTargets) {
        if ("Group".equals(targetItem.getType()) && memberTargets != null) {
            var targetMembers = getTargetMembers((GroupItem) targetItem, memberTargets);
            logger.debug("{} target members were found in group {}", targetMembers.size(), targetItem.getName());
            if (!targetMembers.isEmpty()) {
                return targetMembers.stream().map(member -> getItemOptionPlaceholder(member, isRead, null))
                        .reduce(ActionTemplatePlaceholder.withLabel(ITEM_OPTION_PLACEHOLDER), (a, b) -> {
                            a.nerStaticValues = a.nerStaticValues != null
                                    ? Stream.concat(Arrays.stream(a.nerStaticValues), Arrays.stream(b.nerStaticValues))
                                            .distinct().toArray(String[]::new)
                                    : b.nerStaticValues;
                            return a;
                        });
            }
        }
        var cmdDescription = targetItem.getCommandDescription();
        var stateDescription = targetItem.getStateDescription();
        var itemOptionPlaceholder = ActionTemplatePlaceholder.withLabel(ITEM_OPTION_PLACEHOLDER);
        if (!isRead && cmdDescription != null) {
            itemOptionPlaceholder.nerStaticValues = cmdDescription.getCommandOptions().stream()
                    .map(option -> option.getLabel() != null ? option.getLabel() : option.getCommand())
                    .filter(Objects::nonNull).toArray(String[]::new);
            itemOptionPlaceholder.posStaticValues = cmdDescription.getCommandOptions().stream()
                    .collect(Collectors.toMap(
                            option -> option.getLabel() != null ? option.getLabel().replace(" ", "__")
                                    : option.getCommand().replace(" ", "__"),
                            option -> option.getCommand().replace(" ", "__")));
            return itemOptionPlaceholder;
        } else if (stateDescription != null) {
            itemOptionPlaceholder.nerStaticValues = stateDescription.getOptions().stream()
                    .map(option -> option.getLabel() != null ? option.getLabel() : option.getValue())
                    .filter(Objects::nonNull).toArray(String[]::new);
            if (isRead) {
                itemOptionPlaceholder.posStaticValues = stateDescription.getOptions().stream()
                        .collect(Collectors.toMap(option -> option.getValue().replace(" ", "__"),
                                option -> option.getLabel() != null ? option.getLabel().replace(" ", "__")
                                        : option.getValue().replace(" ", "__")));
            } else {
                itemOptionPlaceholder.posStaticValues = stateDescription.getOptions().stream()
                        .collect(Collectors.toMap(
                                option -> option.getLabel() != null ? option.getLabel().replace(" ", "__")
                                        : option.getValue().replace(" ", "__"),
                                option -> option.getValue().replace(" ", "__")));
            }
            return itemOptionPlaceholder;
        }
        logger.warn(
                "'{}' is the target item for an action that uses the '{}' placeholder but hasn't got any state/command description",
                targetItem.getName(), ITEM_OPTION_PLACEHOLDER_SYMBOL);
        return null;
    }

    private Set<Item> getTargetMembers(GroupItem groupItem, ActionTemplateGroupTargets memberTargets) {
        var childName = memberTargets.itemName;
        if (!childName.isEmpty()) {
            return groupItem.getMembers(i -> i.getName().equals(childName));
        }
        var itemType = memberTargets.itemType;
        var requiredItemTags = memberTargets.requiredItemTags;
        if (!itemType.isEmpty()) {
            return memberTargets.recursive ? getMembersByTypeRecursive(groupItem, itemType, requiredItemTags)
                    : getMembersByType(groupItem, itemType, requiredItemTags);
        }
        return Set.of();
    }

    private Set<Item> getMembersByType(GroupItem groupItem, String itemType, String[] requiredItemTags) {
        return groupItem.getMembers(i -> i.getType().equals(itemType)
                && (requiredItemTags.length == 0 || Arrays.stream(requiredItemTags).allMatch(i.getTags()::contains)));
    }

    private String templatePlaceholders(String text, Item targetItem, Map<String, String> placeholderValues,
            List<ActionTemplatePlaceholder> placeholders) throws IOException {
        var placeholdersCopy = new ArrayList<>(placeholders);
        if (placeholderValues.containsKey(ITEM_OPTION_PLACEHOLDER)) {
            var itemOptionPlaceholder = getItemOptionPlaceholder(targetItem, false, null);
            if (itemOptionPlaceholder != null) {
                placeholdersCopy.add(itemOptionPlaceholder);
            }
        }
        String finalText = text;
        // replace placeholder symbols
        for (var placeholder : placeholdersCopy) {
            var placeholderValue = placeholderValues.getOrDefault(placeholder.label, "");
            if (!placeholderValue.isBlank()) {
                placeholderValue = applyPOSTransformation(placeholderValue, placeholder);
            }
            finalText = finalText.replace(getPlaceholderSymbol(placeholder.label), placeholderValue);
        }
        // replace dynamic placeholder symbol
        var dynamicValue = placeholderValues.getOrDefault(DYNAMIC_PLACEHOLDER, "");
        if (!dynamicValue.isBlank()) {
            finalText = finalText.replace(DYNAMIC_PLACEHOLDER_SYMBOL, dynamicValue);
        }
        return finalText;
    }

    protected ActionTemplateConfiguration[] getTypeActionConfigs(String itemType) {
        File actionConfigsFile = Path.of(TYPE_ACTION_CONFIGS_FOLDER, itemType + ".json").toFile();
        logger.debug("loading action templates configuration file {}", actionConfigsFile);
        if (actionConfigsFile.exists() && !actionConfigsFile.isDirectory()) {
            try {
                return ActionTemplateConfiguration.fromJSON(actionConfigsFile);
            } catch (IOException e) {
                logger.warn("unable to parse action templates configuration for type {}: {}", itemType, e.getMessage());
            }
        }
        logger.debug("action templates configuration for type {} not available", itemType);
        return new ActionTemplateConfiguration[] {};
    }

    private @Nullable Item getTargetItemByLabelTokens(String[] tokens) {
        var label = getItemsByLabelTokensMap().entrySet().stream()
                .filter(entry -> Arrays.equals(tokens, entry.getKey())).findFirst();
        if (label.isEmpty()) {
            return null;
        }
        return label.get().getValue();
    }

    private String[] getTargetItemTokens(String[] tokens, Span itemLabelSpan) {
        return Arrays.copyOfRange(tokens, itemLabelSpan.getStart(), itemLabelSpan.getEnd());
    }

    private String[] replacePlaceholder(String text, String[] tokens, Span span, String placeholderName,
            @Nullable List<NLPPlaceholderData> replacements, @Nullable Map<String[], String> valueByTokensMap) {
        if (replacements != null) {
            var spanTokens = Arrays.copyOfRange(tokens, span.getStart(), span.getEnd());
            String value;
            if (valueByTokensMap != null) {
                var match = valueByTokensMap.entrySet().stream()
                        .filter(entry -> Arrays.equals(spanTokens, entry.getKey())).findFirst();
                if (match.isPresent()) {
                    value = match.get().getValue();
                } else {
                    value = getSpanTokens(tokens, span, text);
                }
            } else {
                value = getSpanTokens(tokens, span, text);
            }
            replacements.add(new NLPPlaceholderData(placeholderName, value, span));
        }
        var spanStart = span.getStart();
        try (Stream<String> dataStream = Stream.concat(
                spanStart != 0 ? Arrays.stream(Arrays.copyOfRange(tokens, 0, spanStart)) : Stream.of(),
                Arrays.stream(new String[] { getPlaceholderSymbol(placeholderName) }))) {
            var spanEnd = span.getEnd();
            if (spanEnd != tokens.length) {
                return Stream.concat(dataStream, Arrays.stream(Arrays.copyOfRange(tokens, spanEnd, tokens.length)))
                        .toArray(String[]::new);
            }
            return dataStream.toArray(String[]::new);
        }
    }

    private String getSpanTokens(String[] tokens, Span span, String original) {
        return detokenize(Arrays.copyOfRange(tokens, span.getStart(), span.getEnd()), original);
    }

    private String detokenize(String[] tokens, String text) {
        if (tokens.length == 1) {
            return tokens[0];
        }
        if (config.detokenizeOptimization) {
            // this is a dynamic regex to de-tokenize a part of the text based on the original text,
            // this way we don't miss special characters between tokens.
            var detokenizeRegex = String.join("[^a-bA-B0-9]?", tokens);
            var match = Pattern.compile(detokenizeRegex).matcher(text);
            if (match.find()) {
                return match.group();
            }
            logger.warn("Unable to detokenize using build-in optimization, consider reporting this case");
        }
        // Detokenize should be improved in the future, Detokenizer API seems to be a work in progress in OpenNLP
        return String.join(" ", tokens);
    }

    private Tokenizer getTokenizer() {
        try {
            Tokenizer tokenizer;
            var tokenModelFile = Path.of(NLP_FOLDER, "token.bin").toFile();
            if (tokenModelFile.exists()) {
                logger.debug("Tokenizing with model {}", tokenModelFile);
                InputStream inputStream = new FileInputStream(tokenModelFile);
                TokenizerModel model = new TokenizerModel(inputStream);
                tokenizer = new TokenizerME(model);
            } else {
                if (config.useSimpleTokenizer) {
                    logger.debug("Using simple tokenizer");
                    tokenizer = SimpleTokenizer.INSTANCE;
                } else {
                    logger.debug("Using white space tokenizer");
                    tokenizer = WhitespaceTokenizer.INSTANCE;
                }
            }
            return tokenizer;
        } catch (IOException e) {
            logger.warn("IOException while loading tokenizer: {}", e.getMessage());
            if (config.useSimpleTokenizer) {
                logger.warn("Fallback to simple tokenizer");
                return SimpleTokenizer.INSTANCE;
            } else {
                logger.warn("Fallback to white space tokenizer");
                return WhitespaceTokenizer.INSTANCE;
            }
        }
    }

    private String[] tokenizeText(String text) {
        return tokenizer.tokenize(text);
    }

    private Span[] nerWithFile(String[] tokens, String placeholderFileName) throws IOException {
        File nerModelFile = Path.of(NER_FOLDER, placeholderFileName + ".bin").toFile();
        File nerDictionaryFile = Path.of(NER_FOLDER, placeholderFileName + ".xml").toFile();
        if (nerModelFile.exists()) {
            return nerWithModel(tokens, nerModelFile);
        } else if (nerDictionaryFile.exists()) {
            return nerWithDictionary(tokens, nerDictionaryFile, getPlaceholderSymbol(placeholderFileName));
        } else {
            logger.debug("No model or dictionary found for '{}'", placeholderFileName);
            throw new IOException("Unable to find model or dictionary with name: " + placeholderFileName);
        }
    }

    private Span[] nerItemLabels(String[] tokens) {
        return nerValues(tokens, getItemsByLabelTokensMap().keySet().toArray(String[][]::new), ITEM_LABEL_PLACEHOLDER,
                false);
    }

    private Span[] nerWithModel(String[] tokens, File nerModelFile) throws IOException {
        logger.debug("applying NER with model {}", nerModelFile.getAbsolutePath());
        TokenNameFinderModel model = new TokenNameFinderModel(nerModelFile);
        var nameFinder = new NameFinderME(model);
        return nameFinder.find(tokens);
    }

    private Span[] nerWithDictionary(String[] tokens, File nerDictFile, String type) throws IOException {
        logger.debug("applying NER with dictionary {}", nerDictFile);
        var dictionary = new opennlp.tools.dictionary.Dictionary(new FileInputStream(nerDictFile));
        return nerWithDictionary(tokens, dictionary, type);
    }

    private Span[] nerValues(String[] tokens, String[][] valueTokens, String type) {
        return nerValues(tokens, valueTokens, type, config.caseSensitive);
    }

    private Span[] nerValues(String[] tokens, String[][] valueTokens, String type, boolean caseSensitive) {
        var runtimeDictionary = new Dictionary(caseSensitive);
        Arrays.stream(valueTokens).map(StringList::new).forEach(runtimeDictionary::put);
        return nerWithDictionary(tokens, runtimeDictionary, type);
    }

    private Span[] nerWithDictionary(String[] tokens, Dictionary dictionary, String type) {
        var nameFinder = new DictionaryNameFinder(dictionary, type);
        return nameFinder.find(tokens);
    }

    private String[] languagePOSTagging(String[] tokens) throws IOException {
        var posTaggingModelFile = Path.of(NLP_FOLDER, "pos.bin").toFile();
        if (posTaggingModelFile.exists()) {
            logger.debug("applying POSTagging with model {}", posTaggingModelFile);
            POSModel posModel = new POSModel(posTaggingModelFile);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            return posTagger.tag(tokens);
        } else {
            logger.debug("POSTagging model not found {}, disabled", posTaggingModelFile);
            return new String[] {};
        }
    }

    private String[] languageLemmatize(String[] tokens, String[] tags) throws IOException {
        if (tags.length == 0) {
            logger.debug("Tags are required for lemmatization, disabled");
            return new String[] {};
        }
        var lemmatizeModelFile = Path.of(NLP_FOLDER, "lemma.bin").toFile();
        var lemmatizeDictionaryFile = Path.of(NLP_FOLDER, "lemma.txt").toFile();
        Lemmatizer lemmatizer;
        if (lemmatizeModelFile.exists()) {
            logger.debug("applying lemmatize with model {}", lemmatizeModelFile);
            LemmatizerModel model = new LemmatizerModel(lemmatizeModelFile);
            lemmatizer = new LemmatizerME(model);
        } else if (lemmatizeDictionaryFile.exists()) {
            logger.debug("applying lemmatize with dictionary {}", lemmatizeDictionaryFile);
            lemmatizer = new DictionaryLemmatizer(lemmatizeDictionaryFile);
        } else {
            logger.debug("Unable to find lemmatize dictionary or model, disabled");
            return new String[] {};
        }
        return lemmatizer.lemmatize(tokens, tags);
    }

    private Map<String[], String> getStringsByTokensMap(String[] values) {
        var map = new HashMap<String[], String>();
        for (String value : values) {
            map.put(tokenizeText(value), value);
        }
        return map;
    }

    private String applyPOSTransformation(String text, ActionTemplatePlaceholder placeholderConfig) throws IOException {
        var singleWorldText = text.replaceAll("\\s", "__");
        String tag = null;
        if (placeholderConfig.posFile != null) {
            File posTaggingDictionary = Path.of(POS_FOLDER, placeholderConfig.posFile + ".xml").toFile();
            File posTaggingModel = Path.of(POS_FOLDER, placeholderConfig.posFile + ".bin").toFile();
            if (posTaggingModel.exists()) {
                POSModel posModel = new POSModel(posTaggingModel);
                var tags = new POSTaggerME(posModel).tag(new String[] { singleWorldText });
                if (tags.length > 0 && !"O".equals(tags[0])) {
                    tag = tags[0];
                }
            } else if (posTaggingDictionary.exists()) {
                POSDictionary posDictionary = POSDictionary.create(new FileInputStream(posTaggingDictionary));
                var tags = posDictionary.getTags(singleWorldText);
                if (tags != null && tags.length > 0 && !"O".equals(tags[0])) {
                    tag = tags[0];
                }
            } else {
                logger.warn("configured pos transformation file not found {}", placeholderConfig.posFile);
            }
        } else if (placeholderConfig.posStaticValues != null) {
            var dictionary = new POSDictionary(config.caseSensitive);
            for (var entry : placeholderConfig.posStaticValues.entrySet()) {
                dictionary.put(entry.getKey(), entry.getValue());
            }
            var tokenTags = dictionary.getTags(singleWorldText);
            if (tokenTags != null && tokenTags.length > 0) {
                tag = tokenTags[0];
            }
        } else {
            // no transformation configured
            return text;
        }
        if (tag == null) {
            return "";
        }
        return tag.replace("__", " ");
    }

    private Map<String[], Item> getItemsByLabelTokensMap() {
        return getItemsMaps().itemLabelByTokens;
    }

    private Map<Item, ActionTemplateConfiguration[]> getItemsWithActionConfigs() {
        return getItemsMaps().itemsWithActionConfigs;
    }

    private NLPItemMaps getItemsMaps() {
        var itemMaps = this.nlpItemMaps;
        if (itemMaps == null) {
            var itemByLabelTokens = new HashMap<String[], Item>();
            var itemsWithActionConfigs = new HashMap<Item, ActionTemplateConfiguration[]>();
            var labelList = new ArrayList<String>();
            for (Item item : itemRegistry.getAll()) {
                var alternativeNames = new ArrayList<String>();
                var label = item.getLabel();
                if (label != null) {
                    alternativeNames.add(label);
                }
                MetadataKey key = new MetadataKey("synonyms", item.getName());
                Metadata synonymsMetadata = metadataRegistry.get(key);
                if (synonymsMetadata != null) {
                    String[] synonyms = synonymsMetadata.getValue().split(",");
                    if (synonyms.length > 0) {
                        alternativeNames.addAll(List.of(synonyms));
                    }
                }
                if (!alternativeNames.isEmpty()) {
                    for (var alternative : alternativeNames) {
                        var lowerLabel = alternative.toLowerCase();
                        if (labelList.contains(lowerLabel)) {
                            logger.debug("Multiple items with label '{}', this is not supported, ignoring '{}'",
                                    lowerLabel, item.getName());
                            continue;
                        }
                        labelList.add(lowerLabel);
                        itemByLabelTokens.put(tokenizeText(lowerLabel), item);
                    }
                }
                var metadata = metadataRegistry.get(new MetadataKey(SERVICE_ID, item.getName()));
                if (metadata != null) {
                    try {
                        itemsWithActionConfigs.put(item, ActionTemplateConfiguration.fromMetadata(metadata));
                    } catch (IOException e) {
                        logger.warn("Unable to parse template action configs for item '{}': {}", item.getName(),
                                e.getMessage());
                    }
                }
            }
            itemMaps = new NLPItemMaps(itemByLabelTokens, itemsWithActionConfigs);
            this.nlpItemMaps = itemMaps;
        }
        return itemMaps;
    }

    private NLPTokenComparisonResult compareTokens(String[] tokens, String[] tokenTags, String[] tokensTemplate) {
        if (tokens.length == 0 || tokensTemplate.length == 0) {
            return NLPTokenComparisonResult.ZERO;
        }
        int score = 0;
        int processedIndex = 0;
        // avoid use tags if not available for all tokens
        var tagsEnabled = tokenTags.length == tokens.length;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            // Tag is used here to allow optional matching by language POS tag
            String tag = tagsEnabled ? tokenTags[i] : null;
            if (processedIndex == tokensTemplate.length) {
                return NLPTokenComparisonResult.ZERO;
            }
            String tokenTemplate = tokensTemplate[processedIndex];
            var tokenAlternatives = splitString(tokenTemplate, "\\|");
            boolean isMatch = false;
            for (var tokenAlternative : tokenAlternatives) {
                if (DYNAMIC_PLACEHOLDER_SYMBOL.equals(tokenAlternative)) {
                    if (tokenAlternatives.length > 1) {
                        logger.warn("Providing the dynamic placeholder as an optional token is not allowed");
                        return NLPTokenComparisonResult.ZERO;
                    }
                    if (tokensTemplate.length == 1) {
                        logger.warn("Providing the dynamic placeholder alone is not allowed");
                        return NLPTokenComparisonResult.ZERO;
                    }
                    if (processedIndex + 1 == tokensTemplate.length) {
                        // the dynamic placeholder is the last value in the template token array, returning score
                        // note that the dynamic placeholder does not count for score
                        return new NLPTokenComparisonResult(score, new Span(i, tokensTemplate.length));
                    }
                    // here we cut and reverse the arrays to run score backwards until the dynamic placeholder
                    var unprocessedTokens = Arrays.copyOfRange(tokens, i, tokens.length);
                    var unprocessedTags = tagsEnabled ? Arrays.copyOfRange(tokenTags, i, tokenTags.length)
                            : new String[] {};
                    var unprocessedTokensTemplate = Arrays.copyOfRange(tokensTemplate, processedIndex,
                            tokensTemplate.length);
                    Collections.reverse(Arrays.asList(unprocessedTokens));
                    Collections.reverse(Arrays.asList(unprocessedTags));
                    Collections.reverse(Arrays.asList(unprocessedTokensTemplate));
                    if (DYNAMIC_PLACEHOLDER_SYMBOL.equals(unprocessedTokens[0])) {
                        // here dynamic placeholder should be at the end, but if it's also at the beginning we should
                        // abort
                        logger.warn("Using multiple dynamic placeholders is not supported");
                        return NLPTokenComparisonResult.ZERO;
                    }
                    var partialScoreResult = compareTokens(unprocessedTokens, unprocessedTags,
                            unprocessedTokensTemplate);
                    if (NLPTokenComparisonResult.ZERO.equals(partialScoreResult)) {
                        return NLPTokenComparisonResult.ZERO;
                    } else {
                        var dynamicSpan = partialScoreResult.dynamicSpan;
                        if (dynamicSpan == null) {
                            logger.error(
                                    "dynamic span missed, this should never happen, please open an issue; aborting");
                            return NLPTokenComparisonResult.ZERO;
                        }
                        return new NLPTokenComparisonResult(score + partialScoreResult.score,
                                new Span(i, tokens.length - (dynamicSpan.getStart())));
                    }
                }
                if (tokenAlternative.equals(token)) {
                    isMatch = true;
                    break;
                }
            }
            if (isMatch) {
                processedIndex++;
                score++;
            } else if (tag != null && optionalLanguageTags.contains(tag)) {
                logger.debug("part '{}' tagged as '{}' skipped", token, tag);
            } else {
                return NLPTokenComparisonResult.ZERO;
            }
        }
        return new NLPTokenComparisonResult(score, null);
    }

    private String[] splitString(String template, String regex) {
        return Arrays.stream(template.split(regex)).map(String::trim).toArray(String[]::new);
    }

    private void invalidate() {
        logger.debug("Invalidate cached item data");
        nlpItemMaps = null;
    }

    private void reloadConfigs() {
        optionalLanguageTags = Arrays.stream(this.config.optionalLanguageTags.split(",")).filter(i -> !i.isEmpty())
                .collect(Collectors.toList());
        tokenizer = getTokenizer();
    }

    private static class NLPInfo {
        public final String[] tokens;
        public final String[] lemmas;
        public final String[] tags;

        public NLPInfo(String[] tokens, String[] lemmas, String[] tags) {
            this.tokens = tokens;
            this.lemmas = lemmas;
            this.tags = tags;
        }
    }

    private static void createFolder(Logger logger, String nlpFolder) {
        File directory = new File(nlpFolder);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.debug("dir created {}", nlpFolder);
            }
        }
    }

    public static String getPlaceholderSymbol(String name) {
        return "$" + name.replaceAll("\\s", "_");
    }

    private static class NLPInterpretationResult {
        public final Item targetItem;
        public final ActionTemplateConfiguration actionConfig;
        public final Map<String, String> placeholderValues;

        public NLPInterpretationResult(Item targetItem, ActionTemplateConfiguration actionConfig,
                Map<String, String> placeholderValues) {
            this.targetItem = targetItem;
            this.actionConfig = actionConfig;
            this.placeholderValues = placeholderValues;
        }

        public static NLPInterpretationResult from(Item item, ActionTemplateConfiguration actionConfig,
                List<NLPPlaceholderData> placeholderValues) {
            return new NLPInterpretationResult(item, actionConfig, placeholderValues.stream()
                    .collect(Collectors.toMap(i -> i.placeholderName, i -> i.placeholderValue)));
        }
    }

    public static class NLPPlaceholderData {
        private final String placeholderName;
        private final String placeholderValue;
        private final Span placeholderSpan;

        private NLPPlaceholderData(String placeholderName, String placeholderValue, Span placeholderSpan) {
            this.placeholderName = placeholderName;
            this.placeholderValue = placeholderValue;
            this.placeholderSpan = placeholderSpan;
        }
    }

    private static class NLPTokenComparisonResult {
        public static final NLPTokenComparisonResult ZERO = new NLPTokenComparisonResult(0, null);
        public final int score;
        public final @Nullable Span dynamicSpan;

        private NLPTokenComparisonResult(int score, @Nullable Span dynamicSpan) {
            this.score = score;
            this.dynamicSpan = dynamicSpan;
        }
    }

    private static class NLPItemMaps {
        private final Map<String[], Item> itemLabelByTokens;
        private final Map<Item, ActionTemplateConfiguration[]> itemsWithActionConfigs;

        private NLPItemMaps(Map<String[], Item> itemLabelByTokens,
                Map<Item, ActionTemplateConfiguration[]> itemsWithActionConfigs) {
            this.itemLabelByTokens = itemLabelByTokens;
            this.itemsWithActionConfigs = itemsWithActionConfigs;
        }
    }
}
