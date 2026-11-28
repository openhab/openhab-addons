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
package org.openhab.binding.knx.internal.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Reads every file of the page and fails on what the page must never hold: a way to turn text into HTML or code, an
 * inline script or style, an address of another host, a storage key that is not the page's own, a write to local
 * storage other than the pane layout, logging, and
 * {@code credentials}. Each rule is shown to catch a small bad sample.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class WebSourceRulesTest {
    static final int MAX_FILE_BYTES = 200 * 1024;
    static final String MAIN = "js/main.js";
    static final String AUTH = "js/auth.js";
    static final String CORE = "js/core.js";
    static final String MAINUI_KEY = "openhab.ui:refreshToken";
    /** The one address-like text a standalone SVG file needs: its namespace name, which is never fetched. */
    static final String SVG_NAMESPACE = "xmlns=\"http://www.w3.org/2000/svg\"";
    static final String DOM = "js/dom.js";
    /** The same name as a string, in the one module that makes the page's icons. */
    static final String SVG_NAMESPACE_IN_DOM = "'http://www.w3.org/2000/svg'";
    /** The one write to local storage: the widths of the panes, under the page's own key. */
    static final String LAYOUT_WRITE = "localStorage.setItem('knx.layout', ";

    /** Rule name → what it finds, anywhere in a file, comments included. */
    private static final Map<String, Pattern> FORBIDDEN = Map.ofEntries( //
            Map.entry("innerHTML", Pattern.compile("innerHTML", Pattern.CASE_INSENSITIVE)),
            Map.entry("outerHTML", Pattern.compile("outerHTML", Pattern.CASE_INSENSITIVE)),
            Map.entry("insertAdjacentHTML", Pattern.compile("insertAdjacentHTML", Pattern.CASE_INSENSITIVE)),
            Map.entry("document.write", Pattern.compile("document\\s*\\.\\s*write")),
            Map.entry("eval", Pattern.compile("\\beval\\b")),
            Map.entry("Function", Pattern.compile("\\bnew\\s+Function\\b|\\bFunction\\s*\\(")),
            Map.entry("DOMParser", Pattern.compile("DOMParser", Pattern.CASE_INSENSITIVE)),
            Map.entry("createContextualFragment",
                    Pattern.compile("createContextualFragment", Pattern.CASE_INSENSITIVE)),
            Map.entry("srcdoc", Pattern.compile("srcdoc", Pattern.CASE_INSENSITIVE)),
            Map.entry("dynamic import", Pattern.compile("\\bimport\\s*\\(")),
            Map.entry("node module", Pattern.compile("['\"`]node:")),
            Map.entry("timer with a string", Pattern.compile("\\bset(Timeout|Interval)\\s*\\(\\s*['\"`]")),
            Map.entry("javascript address", Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE)),
            Map.entry("credentials", Pattern.compile("credentials", Pattern.CASE_INSENSITIVE)),
            Map.entry("console", Pattern.compile("\\bconsole\\s*\\.")));

    /** Rule name → what it finds in an HTML or SVG file. */
    private static final Map<String, Pattern> FORBIDDEN_IN_MARKUP = Map.of( //
            "style attribute", Pattern.compile("\\bstyle\\s*=", Pattern.CASE_INSENSITIVE), //
            "style element", Pattern.compile("<style\\b", Pattern.CASE_INSENSITIVE), //
            "event attribute", Pattern.compile("\\son[a-z]+\\s*=", Pattern.CASE_INSENSITIVE));

    private static final Pattern SCRIPT = Pattern.compile("<script\\b([^>]*)>(.*?)(</script\\s*>|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SRC = Pattern.compile("\\bsrc\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern OTHER_HOST = Pattern.compile("https?://|//[\\w\\[%-]", Pattern.CASE_INSENSITIVE);
    private static final Pattern STORAGE_CALL = Pattern
            .compile("\\.\\s*(getItem|setItem|removeItem)\\s*\\(\\s*([^,)]*)");
    private static final Pattern STRING = Pattern.compile("^(['\"])([^'\"\\\\]*)\\1$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z_$][\\w$]*$");
    /** The files that may name the REST root; main.js builds it once, as {@link #REST_BASE}. */
    static final Set<String> REST_FILES = Set.of("js/api.js", AUTH, CORE);
    static final String REST = "rest/";
    static final String REST_BASE = "new URL('../rest/', page)";
    private static final Pattern LITERAL = Pattern.compile("(['\"`])((?:\\\\.|(?!\\1)[^\\\\])*)\\1");
    private static final Pattern CORE_PATH = Pattern.compile("^(things|items|links)(/|$)");
    private static final Set<String> MEDIA_TYPES = Set.of("application/json");

    /**
     * @param path the file's path below {@code web/}
     * @return one line per broken rule, each starting with the rule's name
     */
    static List<String> problems(String path, byte[] bytes) {
        List<String> problems = new ArrayList<>();
        if (bytes.length > MAX_FILE_BYTES) {
            problems.add("size: " + bytes.length + " bytes, more than " + MAX_FILE_BYTES);
        }
        String text = new String(bytes, StandardCharsets.UTF_8);
        FORBIDDEN.forEach((rule, pattern) -> {
            if (pattern.matcher(text).find()) {
                problems.add(rule);
            }
        });
        boolean markup = path.endsWith(".html") || path.endsWith(".svg");
        if (markup) {
            FORBIDDEN_IN_MARKUP.forEach((rule, pattern) -> {
                if (pattern.matcher(withoutComments(path, text)).find()) {
                    problems.add(rule);
                }
            });
        }
        Matcher script = SCRIPT.matcher(text);
        while (script.find()) {
            if (!SRC.matcher(script.group(1)).find() || !script.group(2).isBlank()) {
                problems.add("inline script");
            }
        }
        String code = withoutComments(path, text);
        if (path.endsWith(".svg")) {
            code = code.replace(SVG_NAMESPACE, "");
        }
        if (DOM.equals(path)) {
            code = code.replace(SVG_NAMESPACE_IN_DOM, "");
        }
        if (OTHER_HOST.matcher(code).find()) {
            problems.add("address of a host");
        }
        int named = count(text, "localStorage");
        if (named > 0
                && (!MAIN.equals(path) || named != count(text, "localStorage.getItem(") + count(text, LAYOUT_WRITE))) {
            problems.add("localStorage");
        }
        if (AUTH.equals(path)) {
            problems.addAll(storageKeyProblems(text));
        }
        int rest = count(text, REST);
        if (rest > 0 && !REST_FILES.contains(path) && !(MAIN.equals(path) && rest == 1 && text.contains(REST_BASE))) {
            problems.add("rest address");
        }
        if (CORE.equals(path)) {
            problems.addAll(corePathProblems(code));
        }
        return problems;
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
            count++;
        }
        return count;
    }

    /** Every string or template literal of the code that holds a {@code /} is a path below things, items or links. */
    private static List<String> corePathProblems(String code) {
        List<String> problems = new ArrayList<>();
        Matcher literal = LITERAL.matcher(code);
        while (literal.find()) {
            String value = literal.group(2);
            if (value.contains("/") && !MEDIA_TYPES.contains(value)
                    && (!CORE_PATH.matcher(value).find() || value.contains(".."))) {
                problems.add("core path: " + value);
            }
        }
        return problems;
    }

    /** Every key handed to a storage is a string, or a constant of the file, that starts with knx. or is MainUI's. */
    private static List<String> storageKeyProblems(String text) {
        List<String> problems = new ArrayList<>();
        Matcher call = STORAGE_CALL.matcher(text);
        while (call.find()) {
            String argument = call.group(2).trim();
            String key = null;
            Matcher string = STRING.matcher(argument);
            if (string.matches()) {
                key = string.group(2);
            } else if (NAME.matcher(argument).matches()) {
                Matcher constant = Pattern
                        .compile("\\bconst\\s+" + Pattern.quote(argument) + "\\s*=\\s*(['\"])([^'\"\\\\]*)\\1")
                        .matcher(text);
                if (constant.find()) {
                    key = constant.group(2);
                }
            }
            if (key == null) {
                problems.add("storage key: " + argument + " is no string and no constant of the file");
            } else if (!key.startsWith("knx.") && !MAINUI_KEY.equals(key)) {
                problems.add("storage key: " + key);
            }
        }
        return problems;
    }

    /**
     * Drops the comments of a file: {@code <!-- -->} in HTML and SVG, block comments in CSS, block and line comments
     * in JavaScript; strings are kept. A regular expression literal is read as code, which can only hide a part of
     * its line from the address rule.
     */
    static String withoutComments(String path, String text) {
        if (path.endsWith(".html") || path.endsWith(".svg")) {
            return text.replaceAll("(?s)<!--.*?-->", " ");
        }
        boolean lineComments = path.endsWith(".js");
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            if (c == '/' && next == '*') {
                int end = text.indexOf("*/", i + 2);
                i = end < 0 ? text.length() : end + 2;
                out.append(' ');
            } else if (lineComments && c == '/' && next == '/') {
                int end = text.indexOf('\n', i);
                i = end < 0 ? text.length() : end;
            } else if (c == '\'' || c == '"' || (lineComments && c == '`')) {
                int end = i + 1;
                while (end < text.length() && text.charAt(end) != c) {
                    end += text.charAt(end) == '\\' ? 2 : 1;
                }
                end = Math.min(end + 1, text.length());
                out.append(text, i, end);
                i = end;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private static List<String> problems(String path, String text) {
        return problems(path, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertCaught(String rule, String path, String sample) {
        List<String> problems = problems(path, sample);
        assertTrue(problems.stream().anyMatch(problem -> problem.startsWith(rule)),
                path + " " + sample + " → " + problems);
    }

    private static void assertClean(String path, String sample) {
        assertEquals(List.of(), problems(path, sample), path + " " + sample);
    }

    @Test
    void everyFileOfThePageFollowsEveryRule() throws IOException {
        List<String> files = List.copyOf(KnxWebFilesTest.folderFiles());
        assertTrue(files.contains("index.html"), files.toString());
        for (String file : files) {
            assertEquals(List.of(), problems(file, Files.readAllBytes(KnxWebFilesTest.WEB.resolve(file))), file);
        }
    }

    @Test
    void theForbiddenWaysToMakeHtmlOrCodeAreCaught() {
        assertCaught("innerHTML", "js/a.js", "row.innerHTML = name;");
        assertCaught("innerHTML", "js/a.js", "row['innerHtml'] = name;");
        assertCaught("outerHTML", "js/a.js", "row.outerHTML = name;");
        assertCaught("insertAdjacentHTML", "js/a.js", "row.insertAdjacentHTML('beforeend', name);");
        assertCaught("document.write", "js/a.js", "document.write(name);");
        assertCaught("document.write", "js/a.js", "document.writeln(name);");
        assertCaught("eval", "js/a.js", "const x = eval(text);");
        assertCaught("Function", "js/a.js", "const f = new Function('a', text);");
        assertCaught("Function", "js/a.js", "const f = Function(text);");
        assertCaught("DOMParser", "js/a.js", "new DOMParser().parseFromString(text, 'text/html');");
        assertCaught("createContextualFragment", "js/a.js", "range.createContextualFragment(text);");
        assertCaught("srcdoc", "js/a.js", "frame.srcdoc = text;");
        assertCaught("srcdoc", "index.html", "<iframe srcdoc=\"x\"></iframe>");
        assertCaught("dynamic import", "js/a.js", "const m = await import('./x.js');");
        assertCaught("node module", "js/a.js", "import { createHash } from 'node:crypto';");
        assertCaught("node module", "js/a.js", "import fs from \"node:fs\";");
        assertCaught("timer with a string", "js/a.js", "setTimeout('run()', 10);");
        assertCaught("timer with a string", "js/a.js", "setInterval( \"run()\", 10);");
        assertCaught("javascript address", "js/a.js", "link.href = 'javascript:run()';");
        assertCaught("javascript address", "index.html", "<a href=\"JavaScript:run()\">x</a>");
    }

    @Test
    void theAllowedLookAlikesAreNotCaught() {
        assertClean("js/a.js", "import { el } from './dom.js';\nexport const retrieval = 1;\n");
        assertClean("js/a.js", "const evaluated = 2; const fn = (x) => x; setTimeout(run, 10);\n");
        assertClean("js/a.js", "// see ./x.js\nconst path = '../knx/';\n/* a block\n comment */\n");
        assertClean("app.css", "/* see https://example.invalid */ main { color: black; }\n");
        assertClean("index.html",
                "<!-- see http://example.invalid --><script type=\"module\" src=\"js/main.js\"></script>");
        assertClean("icon.svg", "<svg " + SVG_NAMESPACE + " viewBox=\"0 0 1 1\"></svg>");
        assertClean(DOM, "const SVG = " + SVG_NAMESPACE_IN_DOM + ";\n");
    }

    @Test
    void anInlineScriptIsCaught() {
        assertCaught("inline script", "index.html", "<script>run()</script>");
        assertCaught("inline script", "index.html", "<SCRIPT type=\"module\">run()</SCRIPT>");
        assertCaught("inline script", "index.html", "<script src=\"js/main.js\">run()</script>");
        assertCaught("inline script", "js/a.js", "const s = '<script>run()</script>';");
    }

    @Test
    void inlineStylesAndEventAttributesInMarkupAreCaught() {
        assertCaught("style attribute", "index.html", "<p style=\"color:red\">x</p>");
        assertCaught("style element", "index.html", "<style>p { color: red; }</style>");
        assertCaught("event attribute", "index.html", "<img src=\"x.svg\" onerror=\"run()\">");
        assertCaught("event attribute", "index.html", "<body\nONLOAD=\"run()\">");
        assertCaught("event attribute", "icon.svg", "<svg onload=\"run()\"></svg>");
    }

    @Test
    void anAddressOfAHostOutsideCommentsIsCaught() {
        assertCaught("address of a host", "js/a.js", "const base = 'http://192.0.2.7:8080/rest/';");
        assertCaught("address of a host", "js/a.js", "fetch(\"https://example.invalid/x\");");
        assertCaught("address of a host", "js/a.js", "const base = '//example.invalid/rest/';");
        assertCaught("address of a host", "js/a.js", "const t = `see //example.invalid`; // fine here");
        assertCaught("address of a host", "app.css", "main { background: url(//example.invalid/x.svg); }");
        assertCaught("address of a host", "app.css", "@import url(\"https://example.invalid/x.css\");");
        assertCaught("address of a host", "index.html", "<link rel=\"stylesheet\" href=\"//example.invalid/x.css\">");
        assertCaught("address of a host", "index.html", "<!-- x --><a href=\"http://example.invalid\">x</a>");
        assertCaught("address of a host", "icon.svg", "<svg xmlns:x=\"http://example.invalid/ns\"></svg>");
        assertCaught("address of a host", "js/a.js", "const SVG = " + SVG_NAMESPACE_IN_DOM + ";");
        assertCaught("address of a host", DOM, "const other = 'http://www.w3.org/1999/xlink';");
        assertCaught("address of a host", "js/a.js", "/* a comment */ const a = 'http://x'; /* another */");
    }

    @Test
    void commentsAreDroppedButStringsKept() {
        assertEquals("const a = '// kept'; \n", withoutComments("js/a.js", "const a = '// kept'; // dropped\n"));
        assertEquals("const a = \"/* kept */\";   b;",
                withoutComments("js/a.js", "const a = \"/* kept */\"; /* dropped */ b;"));
        assertEquals("const a = 'it\\'s // kept';",
                withoutComments("js/a.js", "const a = 'it\\'s // kept';// dropped"));
        assertEquals("a {   }", withoutComments("app.css", "a { /* dropped */ }"));
        assertEquals("<p> </p>", withoutComments("index.html", "<p><!-- dropped --></p>"));
    }

    @Test
    void localStorageIsReadAndTakesOnlyTheLayoutAndOnlyInMain() {
        assertClean(MAIN, "const local = { getItem: (k) => window.localStorage.getItem(k) };\n");
        assertClean(MAIN, "const write = (value) => window.localStorage.setItem('knx.layout', value);\n");
        assertCaught("localStorage", MAIN, "window.localStorage.setItem(key, value);");
        assertCaught("localStorage", "js/layout.js", "window.localStorage.setItem('knx.layout', value);");
        assertCaught("localStorage", MAIN, "window.localStorage.setItem('knx.x', '1');");
        assertCaught("localStorage", MAIN, "const store = window.localStorage;");
        assertCaught("localStorage", MAIN, "localStorage.removeItem('knx.x');");
        assertCaught("localStorage", AUTH, "const t = localStorage.getItem('knx.x');");
        assertCaught("localStorage", "js/a.js", "// localStorage is never used here");
    }

    @Test
    void everyStorageKeyOfAuthIsThePagesOwnOrMainUisAndAConstant() {
        assertClean(AUTH, """
                const KEY_STATE = 'knx.loginState';
                const MAINUI_REFRESH = 'openhab.ui:refreshToken';
                export function step(session, local) {
                    session.setItem(KEY_STATE, 'x');
                    session.removeItem( KEY_STATE );
                    session.getItem('knx.refreshToken');
                    return local.getItem(MAINUI_REFRESH);
                }
                """);
        assertCaught("storage key", AUTH, "session.setItem('loginState', 'x');");
        assertCaught("storage key", AUTH, "const KEY = 'state';\nsession.getItem(KEY);");
        assertCaught("storage key", AUTH, "session.removeItem(keyFromServer);");
        assertCaught("storage key", AUTH, "session.setItem('knx.' + name, 'x');");
        assertCaught("storage key", AUTH, "session.setItem(`knx.${name}`, 'x');");
        assertCaught("storage key", AUTH, "local.getItem('openhab.ui:token');");
        assertClean("js/other.js", "map.getItem('anything');");
    }

    @Test
    void credentialsAndConsoleAreCaughtEverywhere() {
        assertCaught("credentials", "js/a.js", "fetch(url, { credentials: 'omit' });");
        assertCaught("credentials", "js/a.js", "// credentials stay as they are");
        assertCaught("console", "js/a.js", "console.log(token);");
        assertCaught("console", "js/a.js", "window.console .error(x);");
    }

    @Test
    void onlyTheApiClientsAndTheStartHoldTheRestAddress() {
        assertClean("js/api.js",
                "// the REST root, new URL('../rest/', <page address>)\nconst root = new URL('knx/', base);\n");
        assertClean("js/auth.js", "// new URL('../rest/', <page address>)\n");
        assertClean("js/core.js", "// new URL('../rest/', <page address>)\nconst a = 'things';\n");
        assertClean(MAIN, "const base = new URL('../rest/', page);\n");
        assertCaught("rest address", "js/views/wizard.js", "fetch('../rest/things');");
        assertCaught("rest address", "js/views/wizard.js", "// calls ../rest/items");
        assertCaught("rest address", "js/plan.js", "const path = `rest/${kind}`;");
        assertCaught("rest address", "index.html", "<a href=\"../rest/\">x</a>");
        assertCaught("rest address", MAIN,
                "const base = new URL('../rest/', page);\nconst more = new URL('../rest/items', page);\n");
        assertCaught("rest address", MAIN, "const base = new URL('/rest/', page);\n");
    }

    @Test
    void everyPathLiteralOfTheCoreClientIsAThingAnItemOrALink() {
        assertClean(CORE, """
                // a comment may say ../knx/ or rules/
                const kinds = ['GROUP', 'THING'];
                const headers = { Accept: 'application/json', 'Content-Type': 'application/json' };
                function corePath(uid, name, channel, create) {
                    if (create) {
                        return 'things';
                    }
                    return name ? `items/${name}` : uid ? "things/" + uid : `links/${name}/${channel}`;
                }
                """);
        assertCaught("core path", CORE, "const path = 'rules/' + uid;");
        assertCaught("core path", CORE, "const path = `knx/bridges/${b}`;");
        assertCaught("core path", CORE, "const path = '/rest/things';");
        assertCaught("core path", CORE, "const path = `${root}/things`;");
        assertCaught("core path", CORE, "const path = `things/../knx/${b}`;");
        assertCaught("core path", CORE, "const path = 'thingsx/' + uid;");
        assertClean("js/other.js", "const path = 'knx/bridges/' + b;");
    }

    @Test
    void aFileLargerThan200KilobytesIsCaught() {
        byte[] large = new byte[MAX_FILE_BYTES + 1];
        java.util.Arrays.fill(large, (byte) ' ');
        assertTrue(problems("js/a.js", large).stream().anyMatch(problem -> problem.startsWith("size")));
        assertEquals(List.of(), problems("js/a.js", new byte[MAX_FILE_BYTES]));
    }
}
