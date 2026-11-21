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
package org.openhab.binding.knx.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * What is kept of one application program file while it is read: only the entries whose ids are wanted, at
 * most {@link ReadLimits#maxProgramEntries()} of them. An entry read can make further ids wanted, for example a
 * {@code ComObjectRef} the {@code ComObject} it refers to; when such an entry sits before the one that names
 * it, the file takes another pass.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProgramFile {

    /** One kind of entry, kept by id. */
    static final class Wanted<T> {

        private final ProgramFile file;
        private final Set<String> ids = new HashSet<>();
        private final Map<String, T> found = new LinkedHashMap<>();
        private final Set<String> wantedDuringPass = new HashSet<>();
        private int seenInPass;

        private Wanted(ProgramFile file) {
            this.file = file;
        }

        /**
         * An id wanted during a pass before any entry of this kind was met lies ahead in the same pass, so only
         * a later want can need another pass.
         */
        void want(String id) {
            if (ids.add(id) && file.reading && seenInPass > 0) {
                wantedDuringPass.add(id);
            }
        }

        /** Called for every entry of this kind the pass meets, wanted or not. */
        void seen() {
            seenInPass++;
        }

        /** Whether the id is wanted and not found yet. */
        boolean wants(String id) {
            return ids.contains(id) && !found.containsKey(id);
        }

        void keep(String id, T entry) throws KnxProjectException {
            file.hold();
            found.put(id, entry);
        }

        @Nullable
        T get(String id) {
            return found.get(id);
        }

        /** The entries found, in file order. */
        Collection<Map.Entry<String, T>> entries() {
            return Collections.unmodifiableCollection(found.entrySet());
        }

        private boolean missesAnEntryWantedDuringThePass() {
            for (String id : wantedDuringPass) {
                if (!found.containsKey(id)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** A {@code ComObjectRef}: the object it refers to and what it overrides. */
    record ComObjectRefEntry(String refId, @Nullable String text, @Nullable String functionText,
            @Nullable String objectSize, List<KnxDatapointType> datapointTypes, @Nullable String textParameterRefId,
            KnxObjectFlags flags) {
    }

    /**
     * A {@code ComObject} of the program or of a module definition; {@code baseNumber} is the
     * {@code BaseNumber} attribute as written, the id of a module argument.
     */
    record ComObjectEntry(@Nullable Integer number, @Nullable String baseNumber, @Nullable String text,
            @Nullable String functionText, @Nullable String objectSize, List<KnxDatapointType> datapointTypes,
            KnxObjectFlags flags) {
    }

    /** A {@code ParameterRef}: the parameter it refers to and its own value, if any. */
    record ParameterRefEntry(String refId, @Nullable String value) {
    }

    /** A {@code Parameter}: its value, if any. */
    record ParameterEntry(@Nullable String value) {
    }

    /** An {@code Allocator}: its start, {@code null} when it is not a whole number. */
    record AllocatorEntry(@Nullable Long start) {
    }

    /** An argument of a module definition: its name and the {@code Allocates} step as written. */
    record ArgumentEntry(String name, @Nullable String allocates) {
    }

    static final String TEXT = "Text";
    static final String FUNCTION_TEXT = "FunctionText";

    private final String id;
    private final String entryName;
    private final ReadBudget budget;
    private final List<Wanted<?>> kinds = new ArrayList<>();
    private int held;
    private boolean reading;
    private boolean programFound;
    private boolean passBoundReached;
    private @Nullable String defaultLanguage;

    private final @Nullable String language;
    private final Set<String> argumentRefIds = new HashSet<>();
    private boolean exactLanguage;
    private boolean partLanguage;
    private boolean exactTaken;
    private boolean partTaken;
    private @Nullable Wanted<String> languageSlot;
    private int languageDepth = -1;
    private @Nullable String translationElement;
    private int translationElementDepth = -1;

    final Wanted<ComObjectRefEntry> comObjectRefs = wanted();
    final Wanted<ComObjectEntry> comObjects = wanted();
    final Wanted<ParameterRefEntry> parameterRefs = wanted();
    final Wanted<ParameterEntry> parameters = wanted();
    final Wanted<AllocatorEntry> allocators = wanted();
    final Wanted<ArgumentEntry> arguments = wanted();
    /** Translations of the language equal to the tag, keyed by {@link #translationKey}. */
    final Wanted<String> exactTranslations = wanted();
    /** Translations of the first language with the tag's language part. */
    final Wanted<String> partTranslations = wanted();

    /**
     * @param id the program id, {@code ApplicationProgram@Id}
     * @param entryName the archive entry, for messages
     * @param language the language tag the texts are wanted in, {@code null} for the default texts; an
     *            underscore in it is read as a hyphen, so {@code de_DE} is {@code de-DE}
     */
    ProgramFile(String id, String entryName, ReadBudget budget, @Nullable String language) {
        this.id = id;
        this.entryName = entryName;
        this.budget = budget;
        this.language = language == null || language.isBlank() ? null : language.trim().replace('_', '-');
    }

    private <T> Wanted<T> wanted() {
        Wanted<T> kind = new Wanted<>(this);
        kinds.add(kind);
        return kind;
    }

    String id() {
        return id;
    }

    /** Whether the file holds the {@code ApplicationProgram} with the wanted id. */
    boolean programFound() {
        return programFound;
    }

    @Nullable
    String defaultLanguage() {
        return defaultLanguage;
    }

    void foundProgram(@Nullable String defaultLanguage) {
        if (!programFound) {
            programFound = true;
            this.defaultLanguage = defaultLanguage;
        }
    }

    void startPass() {
        reading = true;
        for (Wanted<?> kind : kinds) {
            kind.wantedDuringPass.clear();
            kind.seenInPass = 0;
        }
        exactTaken = false;
        partTaken = false;
        languageSlot = null;
        languageDepth = -1;
        translationElement = null;
        translationElementDepth = -1;
    }

    /** Wants the translated texts of a program element, when a language is wanted. */
    void wantTranslations(String elementId) {
        if (language != null) {
            for (String attribute : List.of(TEXT, FUNCTION_TEXT)) {
                exactTranslations.want(translationKey(attribute, elementId));
                partTranslations.want(translationKey(attribute, elementId));
            }
        }
    }

    static String translationKey(String attribute, String elementId) {
        return attribute + " " + elementId;
    }

    /**
     * A {@code Language} starts at this depth. Of the languages of the file, the first equal to the tag
     * (ignoring case) and the first with the same language part are read.
     */
    void startLanguage(String identifier, int depth) {
        languageSlot = null;
        languageDepth = depth;
        String tag = language;
        if (tag == null) {
            return;
        }
        if (tag.equalsIgnoreCase(identifier)) {
            if (!exactTaken) {
                exactTaken = true;
                exactLanguage = true;
                languageSlot = exactTranslations;
            }
        } else if (!partTaken && languagePart(tag).equalsIgnoreCase(languagePart(identifier))) {
            partTaken = true;
            partLanguage = true;
            languageSlot = partTranslations;
        }
    }

    private static String languagePart(String tag) {
        int end = tag.indexOf('-');
        return end < 0 ? tag : tag.substring(0, end);
    }

    void startTranslationElement(String refId, int depth) {
        translationElement = refId;
        translationElementDepth = depth;
    }

    /** A {@code Translation} at this depth, inside the last translation element and language started. */
    void translation(@Nullable String attribute, @Nullable String text, int depth) throws KnxProjectException {
        Wanted<String> slot = languageSlot;
        String element = translationElement;
        if (slot == null || element == null || languageDepth != depth - 3 || translationElementDepth != depth - 1) {
            return;
        }
        slot.seen();
        if (attribute == null || text == null || text.isEmpty()) {
            return;
        }
        String key = translationKey(attribute, element);
        if (slot.wants(key)) {
            slot.keep(key, text);
        }
    }

    /**
     * The translation in the chosen language: equal to the tag, else the same language part, else none. A tag
     * equal to the program's default language (ignoring case) has none: the default texts are that language.
     */
    @Nullable
    String translation(String attribute, String elementId) {
        String tag = language;
        if (tag != null && tag.equalsIgnoreCase(defaultLanguage)) {
            return null;
        }
        Wanted<String> chosen = exactLanguage ? exactTranslations : partLanguage ? partTranslations : null;
        return chosen == null ? null : chosen.get(translationKey(attribute, elementId));
    }

    /** An argument reference of a module instance in the project, short as stored there. */
    void wantArgument(String refId) {
        if (!refId.isEmpty()) {
            argumentRefIds.add(refId);
        }
    }

    /** Whether a module definition's argument ends in {@code _} and an argument reference of the project. */
    boolean wantsArgument(String id) {
        if (arguments.get(id) != null) {
            return false;
        }
        for (int i = id.indexOf('_'); i >= 0; i = id.indexOf('_', i + 1)) {
            if (argumentRefIds.contains(id.substring(i + 1))) {
                return true;
            }
        }
        return false;
    }

    /** Whether the last allowed pass still left wanted entries missing. */
    boolean passBoundReached() {
        return passBoundReached;
    }

    void reachedPassBound() {
        passBoundReached = true;
    }

    /** Ends a pass; whether another pass can find more. */
    boolean endPass() {
        reading = false;
        if (!programFound) {
            return false;
        }
        for (Wanted<?> kind : kinds) {
            if (kind.missesAnEntryWantedDuringThePass()) {
                return true;
            }
        }
        return false;
    }

    ReadBudget budget() {
        return budget;
    }

    private void hold() throws KnxProjectException {
        held++;
        int maxEntries = budget.limits().maxProgramEntries();
        if (held > maxEntries) {
            throw new KnxProjectException(Reason.LIMIT_EXCEEDED,
                    entryName + " needs more than " + maxEntries + " entries kept while it is read");
        }
        budget.countRecord();
    }
}
