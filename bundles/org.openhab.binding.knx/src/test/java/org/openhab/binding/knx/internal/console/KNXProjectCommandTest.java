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
package org.openhab.binding.knx.internal.console;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.binding.knx.internal.monitor.ProjectLookup;
import org.openhab.binding.knx.internal.project.KnxChannelMapper;
import org.openhab.binding.knx.internal.project.KnxChannelSuggestion;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectException;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.StoreFailedException;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

import io.calimero.GroupAddress;

/**
 * Checks the commands that work with the stored ETS project.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KNXProjectCommandTest {
    private static final ThingUID HOME = new ThingUID("knx", "ip", "home");
    private static final String DESCRIBE = "KNX bridge \"Home KNX\" (knx:ip:home)";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-14T12:34:56.789Z"), ZoneOffset.UTC);
    private static final String PROMPT = "Password of the ETS project: ";

    @TempDir
    @Nullable
    Path tempFolder;

    private final KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
    private final Console console = mock(Console.class);
    private final LocaleProvider localeProvider = mock(LocaleProvider.class);
    private @Nullable KnxProjectStore store;
    private @Nullable KNXCommandExtension extension;

    private Path temp() {
        return Objects.requireNonNull(tempFolder);
    }

    private Path misc() throws IOException {
        return Files.createDirectories(temp().resolve("conf").resolve("misc"));
    }

    private KnxProjectStore store() {
        KnxProjectStore store = this.store;
        if (store == null) {
            store = new KnxProjectStore(temp().resolve("userdata").resolve("knx"), CLOCK);
            this.store = store;
        }
        return store;
    }

    private KNXCommandExtension extension() throws IOException {
        KNXCommandExtension extension = this.extension;
        if (extension == null) {
            extension = extensionOn(store());
        }
        return extension;
    }

    private KNXCommandExtension extensionOn(KnxProjectStore projectStore) throws IOException {
        when(localeProvider.getLocale()).thenReturn(Locale.GERMANY);
        Path misc = misc();
        KNXCommandExtension extension = new KNXCommandExtension(factory, projectStore, localeProvider, CLOCK,
                ZoneOffset.UTC, () -> misc);
        this.extension = extension;
        return extension;
    }

    private static KNXBridgeBaseThingHandler bridge(String id, String label) {
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        Bridge thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "ip", id));
        when(thing.getLabel()).thenReturn(label);
        when(handler.getThing()).thenReturn(thing);
        return handler;
    }

    private void bridges(KNXBridgeBaseThingHandler... handlers) {
        when(factory.getBridges()).thenReturn(Set.of(handlers));
    }

    private List<String> run(String... args) throws IOException {
        clearInvocations(console);
        extension().execute(args, console);
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).println(lines.capture());
        List<String> printed = new ArrayList<>(lines.getAllValues());
        ArgumentCaptor<String> usages = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).printUsage(usages.capture());
        printed.addAll(usages.getAllValues());
        return printed;
    }

    private static String all(List<String> lines) {
        return String.join("\n", lines);
    }

    private void withHome() {
        bridges(bridge("home", "Home KNX"));
    }

    @Test
    void aPlainProjectIsImportedAndCounted() throws Exception {
        withHome();
        Path file = ProjectTestFiles.plain();

        List<String> out = run("project", "import", "home", file.toString());

        StoredProject stored = Objects.requireNonNull(store().get(HOME));
        assertEquals(
                List.of("Imported \"" + file.getFileName() + "\" for " + DESCRIBE + ": 6 group addresses, "
                        + "3 devices, " + stored.project().groupObjects().size() + " group objects.",
                        "1 more installations in the file were ignored.", "The file was read once and is not kept."),
                out);
        assertEquals(4, stored.project().groupObjects().size());
        assertEquals("de-DE", stored.language());
        verify(console, never()).readLine(anyString(), any());
    }

    @Test
    void theIgnoredInstallationsAreSaid() throws Exception {
        withHome();

        List<String> out = run("project", "import", "home", ProjectTestFiles.plain().toString());

        assertEquals("1 more installations in the file were ignored.", out.get(1), out.toString());
    }

    @Test
    void aProtectedFileAsksOnceForTheHiddenPasswordAndWipesIt() throws Exception {
        withHome();
        KnxProjectStore spy = spy(store());
        extensionOn(spy);
        doReturn(ProjectTestFiles.PASSWORD).when(console).readLine(anyString(), any());
        List<String> logs;
        List<String> out;
        try (LogCapture capture = new LogCapture()) {
            out = run("project", "import", "home", ProjectTestFiles.protectedProject().toString());
            logs = capture.all();
        }

        verify(console, times(1)).readLine(eq(PROMPT), any(Character.class));
        assertTrue(out.get(0).startsWith("Imported \""), out.toString());
        assertNotNull(store().get(HOME));
        assertFalse(all(out).contains(ProjectTestFiles.PASSWORD), out.toString());
        assertFalse(all(logs).contains(ProjectTestFiles.PASSWORD), logs.toString());
        ArgumentCaptor<char[]> passwords = ArgumentCaptor.forClass(char[].class);
        verify(spy, times(2)).importProject(eq(HOME), any(Path.class), passwords.capture(), anyString());
        List<char[]> given = passwords.getAllValues();
        assertTrue(Objects.isNull(given.get(0)), "the first call has no password");
        char[] second = given.get(1);
        assertEquals(ProjectTestFiles.PASSWORD.length(), second.length);
        for (char c : second) {
            assertEquals(0, c);
        }
    }

    @Test
    void aWrongPasswordImportsNothingAndTheEarlierProjectStays() throws Exception {
        withHome();
        run("project", "import", "home", ProjectTestFiles.plain().toString());
        StoredProject before = Objects.requireNonNull(store().get(HOME));
        doReturn("not the password").when(console).readLine(anyString(), any());

        List<String> out = run("project", "import", "home", ProjectTestFiles.protectedProject().toString());

        assertEquals(List.of("Wrong password. Nothing imported. The earlier project is still in use."), out);
        assertSame(before, store().get(HOME));
        assertFalse(all(out).contains("not the password"));
    }

    @Test
    void aConsoleThatCannotAskSaysSo() throws Exception {
        withHome();
        Path file = ProjectTestFiles.protectedProject();
        String cannot = "The project is password protected and this console cannot ask for the password. "
                + "Use the openHAB console over ssh. Nothing imported.";

        doThrow(new IOException("MADEUP-LEAK-IO")).when(console).readLine(anyString(), any());
        assertEquals(List.of(cannot), run("project", "import", "home", file.toString()));
        doThrow(new UnsupportedOperationException("MADEUP-LEAK-UOE")).when(console).readLine(anyString(), any());
        assertEquals(List.of(cannot), run("project", "import", "home", file.toString()));
        doReturn(null).when(console).readLine(anyString(), any());
        assertEquals(List.of(cannot), run("project", "import", "home", file.toString()));
        doReturn("").when(console).readLine(anyString(), any());
        assertEquals(List.of(cannot), run("project", "import", "home", file.toString()));
        doThrow(new IllegalStateException("MADEUP-LEAK-ISE")).when(console).readLine(anyString(), any());
        assertEquals(List.of(cannot), run("project", "import", "home", file.toString()));

        assertEquals(FileState.NONE, store().fileState(HOME));
    }

    @Test
    void anEarlierProjectIsMentionedOnlyWhenItIsInUse() throws Exception {
        withHome();
        Path file = ProjectTestFiles.protectedProject();
        doReturn(null).when(console).readLine(anyString(), any());

        assertEquals(1, run("project", "import", "home", file.toString()).size());
        run("project", "import", "home", ProjectTestFiles.plain().toString());
        List<String> out = run("project", "import", "home", file.toString());

        assertTrue(out.get(0).endsWith("Nothing imported. The earlier project is still in use."), out.toString());
    }

    @Test
    void everyFailureHasItsFixedText() throws Exception {
        withHome();
        KnxProjectStore failing = mock(KnxProjectStore.class);
        when(failing.fileState(any())).thenReturn(FileState.NONE);
        extensionOn(failing);
        Path file = ProjectTestFiles.plain();
        String[][] cases = { { "NOT_A_PROJECT", "This is not an ETS project file (.knxproj)." },
                { "WRONG_PASSWORD", "Wrong password." },
                { "UNSUPPORTED_PROTECTION", "This kind of project protection is not supported yet." },
                { "LIMIT_EXCEEDED", "The file is larger than the reader accepts." },
                { "MALFORMED", "The file is damaged." } };
        for (String[] each : cases) {
            doThrow(new KnxProjectException(Reason.valueOf(each[0]), "MADEUP-LEAK-READER")).when(failing)
                    .importProject(any(), any(), any(), any());
            assertEquals(List.of(each[1] + " Nothing imported."), run("project", "import", "home", file.toString()),
                    each[0]);
        }
        doThrow(new StoreFailedException()).when(failing).importProject(any(), any(), any(), any());
        assertEquals(List.of("The project was read, but the result could not be stored. Nothing imported."),
                run("project", "import", "home", file.toString()));
        doThrow(new IOException("MADEUP-LEAK-IO")).when(failing).importProject(any(), any(), any(), any());
        assertEquals(List.of("The file could not be read. Nothing imported."),
                run("project", "import", "home", file.toString()));
        doThrow(new IllegalStateException("MADEUP-LEAK")).when(failing).importProject(any(), any(), any(), any());
        assertEquals(List.of("The file could not be read. Nothing imported."),
                run("project", "import", "home", file.toString()));
    }

    @Test
    void aWrongNumberOfArgumentsPrintsTheUsageAndEchoesNothing() throws Exception {
        withHome();
        Path file = ProjectTestFiles.plain();

        List<String> three = run("project", "import", "home", file.toString(), "MADEUP-THIRD-ARGUMENT");
        List<String> one = run("project", "import", "home");

        assertEquals(1, three.size(), three.toString());
        assertTrue(three.get(0).contains("project import <bridge> <file>"), three.toString());
        assertFalse(all(three).contains("MADEUP-THIRD-ARGUMENT"));
        assertTrue(one.get(0).contains("project import <bridge> <file>"), one.toString());
        assertEquals(FileState.NONE, store().fileState(HOME));
    }

    @Test
    void anUnknownBridgeIsNamedAndNothingIsRead() throws Exception {
        withHome();

        List<String> out = run("project", "import", "other", ProjectTestFiles.plain().toString());

        assertEquals(List.of("No KNX bridge \"other\". Known bridges: knx:ip:home"), out);
    }

    @Test
    void aFileIsGivenAsAbsolutePathOrAsNameInConfMisc() throws Exception {
        withHome();
        Path plain = ProjectTestFiles.plain();
        Files.copy(plain, misc().resolve("house.knxproj"));

        List<String> out = run("project", "import", "home", "house.knxproj");

        assertTrue(out.get(0).startsWith("Imported \"house.knxproj\" for "), out.toString());
        assertEquals("house.knxproj", Objects.requireNonNull(store().get(HOME)).sourceName());
    }

    @Test
    void nothingOutsideConfMiscIsReadByName() throws Exception {
        withHome();
        Path plain = ProjectTestFiles.plain();
        Files.copy(plain, misc().getParent().resolve("outside.knxproj"));
        Files.copy(plain, misc().resolve("inside.knxproj"));
        Files.createDirectories(misc().resolve("folder"));
        Files.copy(plain, misc().resolve("folder").resolve("deeper.knxproj"));
        String refused = "Give an absolute path, or the name of a file in conf/misc. Nothing imported.";

        for (String argument : new String[] { "../outside.knxproj", "folder/deeper.knxproj", "..", ".",
                "./inside.knxproj", "folder\\deeper.knxproj", "relative/../inside.knxproj", "a\0b", "" }) {
            assertEquals(List.of(refused), run("project", "import", "home", argument), argument);
        }
        assertEquals(FileState.NONE, store().fileState(HOME));
    }

    @Test
    void aMissingFileOrAFolderIsNotFound() throws Exception {
        withHome();
        Files.createDirectories(misc().resolve("folder"));
        String missing = "File not found. Nothing imported.";

        assertEquals(List.of(missing), run("project", "import", "home", "nothing.knxproj"));
        assertEquals(List.of(missing), run("project", "import", "home", "folder"));
        assertEquals(List.of(missing), run("project", "import", "home", temp().resolve("nothing.knxproj").toString()));
        assertEquals(List.of(missing), run("project", "import", "home", temp().toString()));
    }

    private void importPlain() throws Exception {
        store().importProject(HOME, ProjectTestFiles.plain(), null, "de-DE", "house.knxproj");
    }

    @Test
    void infoShowsTheProjectOfEveryBridgeAndTheOnesOfBridgesNotRunning() throws Exception {
        bridges(bridge("home", "Home KNX"), bridge("spare", "Spare"));
        importPlain();
        store().importProject(new ThingUID("knx:ip:gone"), ProjectTestFiles.plain(), null, null, "x.knxproj");
        String lastModified = Objects.requireNonNull(store().get(HOME)).project().info().lastModified();

        List<String> out = run("project", "info");

        assertEquals(List.of(
                DESCRIBE + ": project \"" + ProjectTestFiles.PROJECT_NAME
                        + "\", imported 2026-11-14 12:34 from \"house.knxproj\", texts: de-DE",
                "  6 group addresses (1 secured), 3 devices, 4 group objects, 2 spaces, 1 functions",
                "  written by ETS6 6.4.7777.0, last change " + lastModified,
                "KNX bridge \"Spare\" (knx:ip:spare): no project. Import one with: openhab:knx project import "
                        + "<bridge> <file>",
                "Stored project of a bridge that is not running (disabled or removed): knx:ip:gone. If the bridge "
                        + "no longer exists, remove it with: openhab:knx project remove knx:ip:gone"),
                out);
    }

    @Test
    void infoOfOneBridgeAndOfALanguageLessProject() throws Exception {
        bridges(bridge("home", "Home KNX"), bridge("spare", "Spare"));
        store().importProject(HOME, ProjectTestFiles.plain(), null, null, "house.knxproj");

        List<String> out = run("project", "info", "Home KNX");

        assertEquals(3, out.size(), out.toString());
        assertTrue(out.get(0).endsWith("from \"house.knxproj\", texts: default"), out.toString());
        assertEquals(List.of("No KNX bridge \"nothing\". Known bridges: knx:ip:home, knx:ip:spare"),
                run("project", "info", "nothing"));
        assertTrue(run("project", "info", "home", "spare").get(0).contains("project info [<bridge>]"));
    }

    @Test
    void infoOfAnUnusableFileSaysSo() throws Exception {
        withHome();
        Path folder = Files.createDirectories(temp().resolve("userdata").resolve("knx"));
        Files.writeString(folder.resolve("project-knx_ip_home.json"), "MADEUP-NOT-JSON");

        List<String> out = run("project", "info");

        assertEquals(List.of(DESCRIBE + ": the stored project cannot be used. Import it again."), out);
    }

    @Test
    void infoWithoutAnyBridgeOrProjectSaysSo() throws Exception {
        bridges();

        assertEquals(List.of("No KNX bridge."), run("project", "info"));
    }

    @Test
    void aProjectNameWithALineBreakIsOneCleanLine() throws Exception {
        withHome();
        store().importProject(HOME, ProjectTestFiles.plain("Made-up&#10;second&#9;line"), null, null, "a.knxproj");

        List<String> out = run("project", "info");

        assertEquals(DESCRIBE + ": project \"Made-up second line\", imported 2026-11-14 12:34 from \"a.knxproj\", "
                + "texts: default", out.get(0));
        assertTrue(out.stream().noneMatch(line -> line.chars().anyMatch(c -> c < ' ')), out.toString());
    }

    private static final List<String> ADDRESS_ROWS = List.of("31/7/1  1.001  secure  \"Made-up light\"",
            "31/7/2  9.001  -  \"Made-up temperature\"", "31/7/3  15.000  -  \"Made-up access\"",
            "31/7/4  1.001  -  \"Made-up second line [31m\"", "31/7/5  1.008  -  \"Made-up blind\"",
            "31/7/6  16.000  -  \"Made-up display\"");

    @Test
    void addressesListEveryGroupAddressByRawAddress() throws Exception {
        withHome();
        importPlain();

        List<String> out = run("project", "addresses", "home");

        List<String> expected = new ArrayList<>(List.of(DESCRIBE + ": 6 of 6 group addresses"));
        expected.addAll(ADDRESS_ROWS);
        assertEquals(expected, out);
    }

    @Test
    void addressesTakeAnAddressPrefixOrANameAndALimit() throws Exception {
        withHome();
        importPlain();

        assertEquals(List.of(DESCRIBE + ": 1 of 6 group addresses", ADDRESS_ROWS.get(3)),
                run("project", "addresses", "home", "31/7/4"));
        assertEquals(List.of(DESCRIBE + ": 1 of 6 group addresses", ADDRESS_ROWS.get(4)),
                run("project", "addresses", "home", "BLIND"));
        assertEquals(List.of(DESCRIBE + ": 0 of 6 group addresses"),
                run("project", "addresses", "home", "MADEUP-FILTER"));
        assertEquals(List.of(DESCRIBE + ": 6 of 6 group addresses", ADDRESS_ROWS.get(0), ADDRESS_ROWS.get(1),
                "4 more. Narrow the filter or use limit=<n>."), run("project", "addresses", "home", "limit=2"));
        assertEquals(
                List.of(DESCRIBE + ": 6 of 6 group addresses", ADDRESS_ROWS.get(0), ADDRESS_ROWS.get(1),
                        "4 more. Narrow the filter or use limit=<n>."),
                run("project", "addresses", "home", "31/7", "LIMIT=2"));
        assertEquals(7, run("project", "addresses", "home", "limit=10000").size());
        assertEquals(List.of(DESCRIBE + ": 0 of 6 group addresses"), run("project", "addresses", "home", "7.7"));
    }

    @Test
    void aBadLimitAnExtraWordOrNoBridgeIsTheUsage() throws Exception {
        withHome();
        importPlain();

        for (String[] args : new String[][] { { "limit=0" }, { "limit=10001" }, { "limit=MADEUP" }, { "limit=" },
                { "x", "y" }, { "limit=2", "limit=3" } }) {
            List<String> command = new ArrayList<>(List.of("project", "addresses", "home"));
            command.addAll(List.of(args));
            List<String> out = run(command.toArray(String[]::new));
            assertEquals(1, out.size(), out.toString());
            assertTrue(out.get(0).contains("project addresses <bridge>"), out.toString());
            assertFalse(out.get(0).contains("MADEUP"), out.toString());
        }
        assertTrue(run("project", "addresses").get(0).contains("project addresses <bridge>"));
    }

    @Test
    void addressesAndDevicesNameTheBridgeOrTheMissingProject() throws Exception {
        bridges(bridge("home", "Home KNX"));

        assertEquals(List.of("No KNX bridge \"other\". Known bridges: knx:ip:home"),
                run("project", "addresses", "other"));
        assertEquals(List.of(DESCRIBE + ": no project. Import one with: openhab:knx project import <bridge> <file>"),
                run("project", "addresses", "home"));
        assertEquals(List.of(DESCRIBE + ": no project. Import one with: openhab:knx project import <bridge> <file>"),
                run("project", "devices", "home"));
    }

    @Test
    void anAddressNameWithALineBreakAndAnEscapeCharacterIsOneCleanLine() throws Exception {
        withHome();
        importPlain();

        List<String> out = run("project", "addresses", "home", "31/7/4");

        assertEquals(2, out.size(), out.toString());
        assertTrue(out.stream().noneMatch(line -> line.chars().anyMatch(c -> c < ' ')), out.toString());
    }

    @Test
    void devicesListEveryDeviceByIndividualAddressTheUnplacedLast() throws Exception {
        withHome();
        importPlain();

        assertEquals(List.of(DESCRIBE + ": 3 of 3 devices",
                "7.7.1  \"" + ProjectTestFiles.DEVICE_NAME + "\"  3 objects, 3 linked",
                "7.7.2  \"" + ProjectTestFiles.PRODUCT_TEXT + "\"  1 objects, 1 linked",
                "-  \"Made-up spare\"  0 objects, 0 linked"), run("project", "devices", "home"));
        assertEquals(
                List.of(DESCRIBE + ": 2 of 3 devices",
                        "7.7.1  \"" + ProjectTestFiles.DEVICE_NAME + "\"  3 objects, 3 linked",
                        "7.7.2  \"" + ProjectTestFiles.PRODUCT_TEXT + "\"  1 objects, 1 linked"),
                run("project", "devices", "home", "7.7"));
        assertEquals(List.of(DESCRIBE + ": 1 of 3 devices", "-  \"Made-up spare\"  0 objects, 0 linked"),
                run("project", "devices", "home", "SPARE"));
        assertEquals(List.of(DESCRIBE + ": 3 of 3 devices",
                "7.7.1  \"" + ProjectTestFiles.DEVICE_NAME + "\"  3 objects, 3 linked",
                "2 more. Narrow the filter or use limit=<n>."), run("project", "devices", "home", "limit=1"));
        assertTrue(run("project", "devices", "home", "limit=0").get(0).contains("project devices <bridge>"));
    }

    @Test
    void removeDeletesTheProjectOfABridgeOnce() throws Exception {
        withHome();
        importPlain();

        assertEquals(List.of("Removed the project of knx:ip:home."), run("project", "remove", "home"));
        assertEquals(FileState.NONE, store().fileState(HOME));
        assertEquals(List.of("knx:ip:home: no project."), run("project", "remove", "knx:ip:home"));
    }

    @Test
    void removeAlsoTakesTheProjectOfABridgeThatIsGone() throws Exception {
        withHome();
        ThingUID gone = new ThingUID("knx:serial:gone");
        store().importProject(gone, ProjectTestFiles.plain(), null, null, "x.knxproj");
        assertEquals(List.of(gone), store().stored());

        assertEquals(List.of("Removed the project of knx:serial:gone."), run("project", "remove", "KNX:serial:GONE"));

        assertEquals(List.of(), store().stored());
    }

    @Test
    void removeOfAnUnusableFileWorksToo() throws Exception {
        withHome();
        Path folder = Files.createDirectories(temp().resolve("userdata").resolve("knx"));
        Files.writeString(folder.resolve("project-knx_ip_home.json"), "MADEUP-NOT-JSON");

        assertEquals(List.of("Removed the project of knx:ip:home."), run("project", "remove", "home"));
        assertEquals(FileState.NONE, store().fileState(HOME));
    }

    @Test
    void removeNeedsOneKnownBridgeOrStoredProject() throws Exception {
        withHome();
        importPlain();

        assertEquals(List.of("No KNX bridge \"MADEUP-NAME\". Known bridges: knx:ip:home"),
                run("project", "remove", "MADEUP-NAME"));
        assertEquals(List.of("No KNX bridge \"knx:ip:other\". Known bridges: knx:ip:home"),
                run("project", "remove", "knx:ip:other"));
        assertTrue(run("project", "remove").get(0).contains("project remove <bridge>"));
        List<String> two = run("project", "remove", "home", "MADEUP-SECOND");
        assertTrue(two.get(0).contains("project remove <bridge>"), two.toString());
        assertFalse(two.get(0).contains("MADEUP-SECOND"));
        assertNotNull(store().get(HOME));
    }

    @Test
    void removeThatFailsSaysSoWithoutTheReason() throws Exception {
        withHome();
        KnxProjectStore failing = mock(KnxProjectStore.class);
        extensionOn(failing);
        doThrow(new IOException("MADEUP-LEAK-IO")).when(failing).remove(any());

        assertEquals(List.of("The project file could not be removed."), run("project", "remove", "home"));
    }

    private static List<String> channelLines(KnxProject project, KnxDevice device) {
        List<String> lines = new ArrayList<>();
        List<KnxChannelSuggestion> suggestions = new KnxChannelMapper(project).forDevice(device);
        lines.add("7.7.1 \"" + ProjectTestFiles.DEVICE_NAME + "\": " + suggestions.size() + " channel lines");
        for (KnxChannelSuggestion suggestion : suggestions) {
            String type = suggestion.channelType();
            lines.add("  " + (type == null ? "-" : type) + "  " + suggestion.text() + "  — \"" + suggestion.label()
                    + "\"");
            suggestion.notes().forEach(note -> lines.add("      note: " + note));
        }
        lines.add("Nothing was created. Copy the lines you want into a channel of a device Thing.");
        return lines;
    }

    @Test
    void textsOfAHandEditedStoredFilePrintOnOneLineWithoutControlCharacters() throws Exception {
        withHome();
        importPlain();
        List<String> before = run("channels", "7.7.1");
        Path file = temp().resolve("userdata").resolve("knx").resolve("project-knx_ip_home.json");
        String json = Files.readString(file);
        String edited = json.replace("\"language\":\"de-DE\"", "\"language\":\"de-\\nDE\\u001b\"")
                .replace("\"address\":\"31/7/1\"", "\"address\":\"31/7/1\\n\\u001b\"");
        assertTrue(edited.contains("\"language\":\"de-\\nDE\\u001b\"") && edited.contains("31/7/1\\n\\u001b"));
        Files.writeString(file, edited);
        extensionOn(new KnxProjectStore(temp().resolve("userdata").resolve("knx"), CLOCK));

        List<String> info = run("project", "info");
        List<String> channels = run("channels", "7.7.1");

        for (String line : info) {
            assertFalse(line.contains("\n") || line.contains("\u001b"), line);
        }
        assertTrue(info.get(0).endsWith("texts: de- DE"), info.get(0));
        assertTrue(before.stream().anyMatch(line -> line.contains("31/7/1")), before.toString());
        assertEquals(before, channels);
    }

    @Test
    void channelsPrintTheLinesOfTheMapperForADeviceAndCreateNothing() throws Exception {
        withHome();
        importPlain();
        KnxProject project = Objects.requireNonNull(store().get(HOME)).project();
        KnxDevice device = Objects.requireNonNull(project.device(new KnxIndividualAddress(7, 7, 1)));

        List<String> out = run("channels", "7.7.1");

        assertEquals(channelLines(project, device), out);
        assertEquals(3, out.stream().filter(line -> line.contains("=\"")).count(), out.toString());
        assertEquals(out, run("channels", "home", "7.7.1"));
    }

    @Test
    void channelsNeedADeviceInTheProject() throws Exception {
        withHome();
        importPlain();

        assertEquals(List.of("No device 7.7.9 in the project."), run("channels", "7.7.9"));
        for (String argument : new String[] { "MADEUP-DEVICE", "7.7", "7.7.1.1", "16.7.1", "7.16.1", "7.7.256", "" }) {
            assertEquals(List.of("Give the device as an individual address, for example 7.7.1."),
                    run("channels", argument), argument);
        }
        assertTrue(run("channels").get(0).contains("channels [<bridge>] <device>"));
        assertTrue(run("channels", "home", "7.7.1", "MADEUP-THIRD").get(0).contains("channels [<bridge>] <device>"));
        assertEquals(List.of("No KNX bridge \"other\". Known bridges: knx:ip:home"), run("channels", "other", "7.7.1"));
    }

    @Test
    void aLeftOutBridgeIsTheOnlyOneWithAProject() throws Exception {
        bridges(bridge("home", "Home KNX"), bridge("spare", "Spare"));
        assertEquals(List.of("Name the bridge."), run("channels", "7.7.1"));
        importPlain();
        assertEquals("7.7.1 \"" + ProjectTestFiles.DEVICE_NAME + "\": 3 channel lines",
                run("channels", "7.7.1").get(0));
        store().importProject(new ThingUID("knx", "ip", "spare"), ProjectTestFiles.plain(), null, null, "b.knxproj");

        assertEquals(List.of("Name the bridge.", "Bridges with a project: knx:ip:home, knx:ip:spare"),
                run("channels", "7.7.1"));
    }

    private static KnxConfiguredAddress configured(int sub, @Nullable String type, boolean read, String label) {
        return new KnxConfiguredAddress(65280 + sub, type, read, true, label);
    }

    private static Thing thing(String id, @Nullable ThingHandler handler) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("knx", "device", id));
        when(thing.getThingTypeUID()).thenReturn(KNXBindingConstants.THING_TYPE_DEVICE);
        when(thing.getHandler()).thenReturn(handler);
        return thing;
    }

    private static DeviceThingHandler deviceHandler(KnxConfiguredAddress... addresses) {
        DeviceThingHandler handler = mock(DeviceThingHandler.class);
        when(handler.configuredAddresses()).thenReturn(List.of(addresses));
        return handler;
    }

    private static void things(KNXBridgeBaseThingHandler handler, Thing... things) {
        when(((Bridge) handler.getThing()).getThings()).thenReturn(List.of(things));
    }

    @Test
    void checkProjectPrintsTheDifferencesAndTheNotes() throws Exception {
        KNXBridgeBaseThingHandler home = bridge("home", "Home KNX");
        bridges(home);
        importPlain();
        things(home,
                thing("a",
                        deviceHandler(configured(2, "5.001", false, "Made-up temperature channel"),
                                configured(99, "1.001", false, "Made-up missing channel"),
                                configured(1, "1.002", false, "Made-up light channel"),
                                configured(6, "16.000", false, "Made-up display channel"),
                                configured(5, "1.008", false, "Made-up blind channel"))),
                thing("b", null));

        List<String> out = run("check-project");

        assertEquals(List.of(DESCRIBE + ": 5 configured addresses checked, 3 differences, 1 notes",
                "  DATAPOINT_TYPE_DIFFERS  31/7/2  Made-up temperature channel  configured 5.001, project 9.001",
                "  ADDRESS_NOT_IN_PROJECT  31/7/99  Made-up missing channel",
                "  note: DATAPOINT_TYPE_DIFFERS  31/7/1  Made-up light channel  configured 1.002, project 1.001",
                "  ADDRESS_WITHOUT_OBJECT  31/7/6  Made-up display channel",
                "1 device Things are not initialised and were not checked."), out);
    }

    @Test
    void checkProjectPrintsALongChannelLabelInFull() throws Exception {
        KNXBridgeBaseThingHandler home = bridge("home", "Home KNX");
        bridges(home);
        importPlain();
        String label = "knx:device:knxbridge:ground_floor_actuator:living_room_ceiling ga";
        things(home, thing("a", deviceHandler(configured(99, "1.001", false, label))));

        List<String> out = run("check-project");

        assertEquals("  ADDRESS_NOT_IN_PROJECT  31/7/99  " + label, out.get(1));
    }

    @Test
    void checkProjectWithoutDifferencesSaysSo() throws Exception {
        KNXBridgeBaseThingHandler home = bridge("home", "Home KNX");
        bridges(home);
        importPlain();
        things(home, thing("a", deviceHandler(configured(5, "1.008", false, "Made-up blind channel"))));

        assertEquals(List.of(DESCRIBE + ": 1 configured addresses checked, 0 differences, 0 notes", "No differences."),
                run("check-project", "home"));
    }

    @Test
    void checkProjectNeedsAProjectAndOneBridge() throws Exception {
        bridges(bridge("home", "Home KNX"), bridge("spare", "Spare"));

        assertEquals(List.of("Name the bridge."), run("check-project"));
        assertEquals(List.of("KNX bridge \"Spare\" (knx:ip:spare): no project. Import one with: openhab:knx project "
                + "import <bridge> <file>"), run("check-project", "spare"));
        assertEquals(List.of("No KNX bridge \"other\". Known bridges: knx:ip:home, knx:ip:spare"),
                run("check-project", "other"));
        assertTrue(run("check-project", "home", "MADEUP-SECOND").get(0).contains("check-project [<bridge>]"));
        importPlain();
        assertEquals(DESCRIBE + ": 0 configured addresses checked, 0 differences, 0 notes",
                run("check-project").get(0));
    }

    private static final String UNKNOWN_HEADER = "KNX bridge \"Home KNX\": group address, type, number of bytes, and "
            + "number of occurrence since last reload of binding:";
    private static final List<String> UNKNOWN_KEYS = List.of("31/7/  1  GROUP_WRITE(01)", "31/7/  2  GROUP_READ(00)",
            "31/7/  1  secure: missing group key, cannot decrypt", "31/7/ 99  GROUP_WRITE(02)",
            "31/7/200  GROUP_RESPONSE(01)", "99/9/999  GROUP_WRITE(01)", "31/7/256  GROUP_WRITE(01)",
            " 0/0/  0  GROUP_WRITE(01)", "no address at all");

    private void unknown(KNXBridgeBaseThingHandler handler) {
        SortedMap<String, Long> counts = new TreeMap<>();
        for (String key : UNKNOWN_KEYS) {
            counts.put(key, (long) UNKNOWN_KEYS.indexOf(key) + 1);
        }
        when(handler.getCommandExtensionData()).thenReturn(new CommandExtensionData(counts));
        bridges(handler);
    }

    /** The lines of the command as they were before the project existed: the header, then key and count. */
    private static List<String> unknownLinesWithoutNames() {
        List<String> lines = new ArrayList<>(List.of(UNKNOWN_HEADER));
        for (String key : new TreeSet<>(UNKNOWN_KEYS)) {
            lines.add(key + "  " + (UNKNOWN_KEYS.indexOf(key) + 1));
        }
        return lines;
    }

    @Test
    void listUnknownGaIsAsBeforeWithoutAProject() throws Exception {
        unknown(bridge("home", "Home KNX"));

        List<String> out = run("list-unknown-ga");

        assertEquals(unknownLinesWithoutNames(), out);
        assertEquals(" 0/0/  0  GROUP_WRITE(01)  8", out.get(1));
    }

    @Test
    void listUnknownGaGivesTheNameAfterTheCountAndKeepsTheRestOfTheLine() throws Exception {
        unknown(bridge("home", "Home KNX"));
        importPlain();

        List<String> out = run("list-unknown-ga");

        List<String> plain = unknownLinesWithoutNames();
        assertEquals(plain.size(), out.size());
        int named = 0;
        for (int i = 0; i < plain.size(); i++) {
            String name = plain.get(i).startsWith("31/7/  1  ") ? "Made-up light"
                    : plain.get(i).startsWith("31/7/  2  ") ? "Made-up temperature" : "";
            named += name.isEmpty() ? 0 : 1;
            assertEquals(plain.get(i) + (name.isEmpty() ? "" : "  \"" + name + "\""), out.get(i));
        }
        assertEquals(3, named);
        assertTrue(out.contains("31/7/  1  secure: missing group key, cannot decrypt  3  \"Made-up light\""),
                out.toString());
        assertTrue(out.contains("31/7/  1  GROUP_WRITE(01)  1  \"Made-up light\""), out.toString());
    }

    @Test
    void listUnknownGaNamesWhatTheLookupKnowsAndLeavesTheRest() throws Exception {
        unknown(bridge("home", "Home KNX"));
        KnxProjectStore stub = mock(KnxProjectStore.class);
        ProjectLookup lookup = mock(ProjectLookup.class);
        when(lookup.groupAddressName(new GroupAddress(31, 7, 200))).thenReturn("Made-up far away\nname");
        when(lookup.groupAddressName(new GroupAddress(0, 0, 0))).thenReturn("Made-up zero");
        when(stub.lookup(any())).thenReturn(lookup);
        extensionOn(stub);

        List<String> out = run("list-unknown-ga");

        assertTrue(out.contains("31/7/200  GROUP_RESPONSE(01)  5  \"Made-up far away name\""), out.toString());
        assertTrue(out.contains(" 0/0/  0  GROUP_WRITE(01)  8  \"Made-up zero\""), out.toString());
        assertTrue(out.contains("99/9/999  GROUP_WRITE(01)  6"), out.toString());
        assertTrue(out.contains("31/7/256  GROUP_WRITE(01)  7"), out.toString());
        assertTrue(out.contains("no address at all  9"), out.toString());
        assertEquals(UNKNOWN_KEYS.size() + 1, out.size());
    }

    /** The secrets of the fixture that no command may print or log. */
    private static List<String> secrets() {
        List<String> secrets = new ArrayList<>(ProjectTestFiles.madeUpValues());
        secrets.add(ProjectTestFiles.PASSWORD);
        secrets.add(ProjectTestFiles.PARAMETER_VALUE);
        return secrets;
    }

    private static List<String> leaked(List<String> secrets, List<String> lines) {
        String text = String.join("\n", lines);
        return secrets.stream().filter(text::contains).toList();
    }

    @Test
    void theSecretCheckCanFail() {
        List<String> secrets = secrets();

        assertTrue(secrets.size() > 8, secrets.toString());
        assertEquals(List.of(secrets.get(0)), leaked(secrets, List.of("a line with " + secrets.get(0) + " in it")));
        assertEquals(List.of(), leaked(secrets, List.of("Imported \"x.knxproj\"")));
    }

    @Test
    void noCommandPrintsOrLogsAPasswordOrAKeyOfTheProject() throws Exception {
        KNXBridgeBaseThingHandler home = bridge("home", "Home KNX");
        unknown(home);
        things(home, thing("a", deviceHandler(configured(1, "1.001", true, "Made-up light channel"),
                configured(99, "5.001", false, "Made-up missing channel"))));
        Path protectedFile = ProjectTestFiles.protectedProject();
        List<String> lines = new ArrayList<>();
        List<String> logs;
        try (LogCapture capture = new LogCapture()) {
            doReturn("MADEUP-WRONG-PASSWORD-SEVEN").when(console).readLine(anyString(), any());
            lines.addAll(run("project", "import", "home", protectedFile.toString()));
            doReturn(ProjectTestFiles.PASSWORD).when(console).readLine(anyString(), any());
            lines.addAll(run("project", "import", "home", protectedFile.toString()));
            for (String[] command : new String[][] { { "project", "info" }, { "project", "addresses", "home" },
                    { "project", "devices", "home" }, { "channels", "7.7.1" }, { "channels", "7.7.2" },
                    { "check-project" }, { "list-unknown-ga" }, { "project", "addresses", "home", "MADEUP" },
                    { "project", "bogus" }, { "project", "remove", "home" } }) {
                lines.addAll(run(command));
            }
            logs = capture.all();
        }

        assertTrue(lines.size() > 30, lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("Imported \"")), lines.toString());
        assertEquals(List.of(), leaked(secrets(), lines), "printed");
        assertEquals(List.of(), leaked(secrets(), logs), "logged");
        assertEquals(List.of(), leaked(List.of("MADEUP-WRONG-PASSWORD-SEVEN"), lines), "printed");
        assertEquals(List.of(), leaked(List.of("MADEUP-WRONG-PASSWORD-SEVEN"), logs), "logged");
    }

    @Test
    void everyCommandHasItsUsage() throws Exception {
        List<String> usages = extension().getUsages();

        for (String syntax : List.of("list-unknown-ga", "keyring [<bridge>]", "monitor [<bridge>]",
                "project import <bridge> <file>", "project info [<bridge>]",
                "project addresses <bridge> [<filter>] [limit=<n>]", "project devices <bridge> [<filter>] [limit=<n>]",
                "project remove <bridge>", "channels [<bridge>] <device>", "check-project [<bridge>]")) {
            assertTrue(usages.stream().anyMatch(usage -> usage.contains(syntax)), syntax + " in " + usages);
        }
    }

    @Test
    void anUnknownProjectWordPrintsTheProjectUsagesAndEchoesNothing() throws Exception {
        withHome();

        List<String> bare = run("project");
        List<String> unknown = run("project", "MADEUP-WORD");

        assertEquals(bare, unknown);
        assertEquals(5, unknown.size(), unknown.toString());
        assertFalse(all(unknown).contains("MADEUP-WORD"));
        assertTrue(unknown.stream().allMatch(line -> line.contains("project ")), unknown.toString());
    }

    private List<String> complete(int index, String... args) throws Exception {
        List<String> candidates = new ArrayList<>();
        extension().complete(args, index, args[index].length(), candidates);
        return candidates.stream().sorted().toList();
    }

    @Test
    void completionKnowsTheCommandsTheProjectWordsAndTheBridges() throws Exception {
        bridges(bridge("home", "Home KNX"), bridge("spare", "Spare"));
        List<String> uids = List.of("knx:ip:home ", "knx:ip:spare ");

        assertEquals(List.of("project "), complete(0, "pro"));
        assertEquals(List.of("channels ", "check-project "), complete(0, "ch"));
        assertEquals(List.of("addresses ", "devices ", "import ", "info ", "remove "), complete(1, "project", ""));
        assertEquals(List.of("import "), complete(1, "project", "im"));
        for (String word : List.of("import", "info", "addresses", "devices", "remove")) {
            assertEquals(uids, complete(2, "project", word, ""), word);
        }
        assertEquals(uids, complete(1, "channels", ""));
        assertEquals(uids, complete(1, "check-project", ""));
        assertEquals(List.of(), complete(3, "project", "import", "home", ""));
        assertEquals(List.of(), complete(2, "project", "bogus", ""));
        assertEquals(List.of(), complete(2, "channels", "home", ""));
        assertEquals(uids, complete(1, "keyring", ""));
        assertEquals(List.of(), complete(1, "list-unknown-ga", ""));
    }
}
