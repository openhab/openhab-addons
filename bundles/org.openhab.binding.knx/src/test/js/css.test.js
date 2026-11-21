// What can be told about the style sheet without a browser: that a rule says what the layout relies on, and the
// contrast of the colours it names. How it looks is not tested here.

import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const CSS = readFileSync(new URL('../web/app.css', import.meta.url), 'utf8').replace(/\/\*[^]*?\*\//g, '');
const NARROW_AT = CSS.indexOf('@media (max-width: 800px)');
const WIDE = CSS.slice(0, NARROW_AT);
const NARROW = CSS.slice(NARROW_AT);

/** {@return the declarations of the rule with exactly this selector, as a map} */
function rule(sheet, selector) {
    const wanted = selector.replace(/\s+/g, ' ');
    for (const match of sheet.matchAll(/([^{}]+)\{([^{}]*)\}/g)) {
        if (match[1].trim().replace(/\s+/g, ' ') === wanted) {
            return Object.fromEntries(match[2].split(';').map((part) => part.trim()).filter(Boolean).map((part) => [part.slice(0, part.indexOf(':')).trim(), part.slice(part.indexOf(':') + 1).trim()]));
        }
    }
    return null;
}

function luminance(hex) {
    const [r, g, b] = [1, 3, 5].map((at) => parseInt(hex.slice(at, at + 2), 16) / 255).map((c) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4));
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

/** {@return the WCAG contrast ratio of two colours} */
function contrast(a, b) {
    const [high, low] = [luminance(a), luminance(b)].sort((x, y) => y - x);
    return (high + 0.05) / (low + 0.05);
}

const THEME_SELECTORS = [':root, :root[data-theme="light"]', ':root:not([data-theme])', ':root[data-theme="dark"]'];
const LIGHT = rule(WIDE, THEME_SELECTORS[0]);
const SYSTEM_DARK = rule(WIDE.slice(WIDE.indexOf('@media (prefers-color-scheme: dark)')), THEME_SELECTORS[1]);
const DARK = rule(WIDE, THEME_SELECTORS[2]);

// transparent, currentColor and inherit name no colour of their own
const COLOUR = new RegExp(
    [
        '#[0-9a-f]{3,8}\\b',
        '\\b(rgba?|hsla?|hwb|lab|lch|oklab|oklch|color|color-mix)\\(',
        '\\b(black|white|red|green|blue|yellow|orange|gray|grey|silver|purple|navy|teal|maroon|olive|lime|aqua|fuchsia|pink|brown|gold|cyan|magenta|beige|ivory|tan|crimson|indigo|violet|khaki|coral|salmon)\\b',
    ].join('|'),
    'i',
);

const COLOUR_ONLY = /^(color|fill|stroke|caret-color|accent-color|(background|outline|text-decoration|column-rule|border(-(top|right|bottom|left))?)-color)$/;
const THEMED = /^(var\(--[a-z0-9-]+\)|transparent|currentcolor|inherit|none)( !important)?$/i;

/** {@return every rule of the sheet, also those inside a media block, as [selector, declarations]} */
function rules(sheet) {
    return [...sheet.matchAll(/([^{}]+)\{([^{}]*)\}/g)].map((match) => [match[1].trim().replace(/\s+/g, ' '), match[2]]);
}

/** {@return the declarations that give a colour value outside the theme blocks} */
function strayColours(sheet) {
    const found = [];
    for (const [selector, body] of rules(sheet)) {
        if (THEME_SELECTORS.includes(selector)) {
            continue;
        }
        for (const part of body.split(';').map((line) => line.trim()).filter(Boolean)) {
            const name = part.slice(0, part.indexOf(':')).trim();
            const value = part.slice(part.indexOf(':') + 1).trim();
            // a property that takes one colour may only name a theme colour: no list of names can be whole
            if (COLOUR.test(value) || (COLOUR_ONLY.test(name) && !THEMED.test(value))) {
                found.push(`${selector} { ${part} }`);
            }
        }
    }
    return found;
}

test('css: the colours are read, and the ratio is the WCAG one', () => {
    assert.equal(LIGHT['--back'], '#ffffff');
    assert.equal(DARK['--back'], '#15191d');
    assert.equal(SYSTEM_DARK['--back'], '#15191d');
    assert.equal(contrast('#000000', '#ffffff'), 21);
    assert.equal(contrast('#777777', '#ffffff').toFixed(2), '4.48');
});

test('css: the side panes give way before the list does, which keeps 320px', () => {
    assert.equal(rule(WIDE, '.tree-pane').width, 'min(var(--tree-width), 40%)');
    assert.equal(rule(WIDE, '.tree-pane').flex, '0 1 auto');
    assert.equal(rule(WIDE, '.sidebar').width, 'min(var(--properties-width), 35%)');
    assert.equal(rule(WIDE, '.sidebar').flex, '0 1 auto');
    assert.equal(rule(WIDE, '.list-pane').flex, '1 0 320px');
    assert.equal(rule(WIDE, '.pane')['min-width'], '0');
    // the panel takes what the sidebar leaves and can shrink with the window
    assert.equal(rule(WIDE, '.panel').flex, '1 1 0');
    assert.equal(rule(WIDE, '.panel')['min-width'], '0');
});

test('css: the selection text of the status bar shrinks with an ellipsis; count and bridge state do not', () => {
    const selection = rule(WIDE, '#status .selection');
    assert.equal(selection['min-width'], '0');
    assert.equal(selection.overflow, 'hidden');
    assert.equal(selection['text-overflow'], 'ellipsis');
    assert.equal(rule(WIDE, '#status .count, #status .bridge-state').flex, 'none');
});

test('css: the focused row does not grow, and the heading that stays does not cover it', () => {
    const focused = rule(WIDE, 'table.grid tbody tr:focus-visible td');
    assert.deepEqual(Object.keys(focused), ['box-shadow']);
    assert.match(focused['box-shadow'], /^inset /);
    assert.doesNotMatch(CSS.match(/[^{}]*tr:focus[^{}]*\{[^{}]*\}/g).join(''), /border/);
    assert.equal(rule(WIDE, '.list')['scroll-padding-top'], 'calc(var(--row) + 1px)');
    assert.equal(rule(WIDE, 'th').position, 'sticky');
});

test('css: the open containers of the sidebar share its height and scroll by themselves; a closed one is only its heading row', () => {
    const open = rule(WIDE, '.container');
    assert.equal(open.flex, '1 1 0');
    assert.equal(open.display, 'flex');
    assert.equal(open['flex-direction'], 'column');
    assert.deepEqual(rule(WIDE, '.container.closed, .sidebar.closed .container'), { flex: 'none', 'min-height': '0' });
    assert.equal(rule(WIDE, '.container > h3').flex, 'none');
    const body = rule(WIDE, '.container-body');
    assert.equal(body.flex, '1 1 0');
    assert.equal(body['min-height'], '0');
    assert.equal(body.overflow, 'auto');
    assert.equal(rule(WIDE, '.pane')['flex-direction'], 'column', 'the sidebar stacks them');
});

test('css: the closed sidebar is a narrow strip: its own splitter, the containers\' content and the headings\' words are hidden', () => {
    assert.deepEqual(rule(WIDE, '.sidebar-closed > .properties-splitter, .sidebar.closed .container-body, .sidebar.closed .container-head .label'), { display: 'none' });
    const strip = rule(WIDE, '.sidebar.closed');
    assert.equal(strip.width, '30px');
    assert.equal(strip.flex, 'none');
    assert.deepEqual(rule(WIDE, '.container.closed > .container-body'), { display: 'none' });
    assert.deepEqual(rule(WIDE, '.tab-panel.off'), { display: 'none' });
    assert.doesNotMatch(CSS, /last-of-type/);
});

test('css: a row of tabs or a path without anything in it takes no room, and a crumb is led by a mark of the style sheet', () => {
    assert.deepEqual(rule(WIDE, '.list-tabs:empty'), { display: 'none' });
    assert.equal(rule(WIDE, '.crumb + .crumb::before').content, '"\\203A"');
    assert.equal(rule(WIDE, '.crumbs')['min-width'], '0');
});

test('css: the rows of the Group Monitor are tinted by type from the theme, and hover and selection lie over the tint', () => {
    for (const type of ['write', 'read', 'response']) {
        assert.deepEqual(rule(WIDE, `table.grid tbody tr.type-${type} td`), { background: `var(--tint-${type})` });
    }
    const at = (selector) => CSS.indexOf(`${selector} {`);
    assert.ok(at('table.grid tbody tr.type-response td') > 0);
    assert.ok(at('table.grid tbody tr:hover td') > at('table.grid tbody tr.type-response td'), 'hover comes later, with the same weight');
    assert.ok(at('table.grid tbody tr.selected td') > at('table.grid tbody tr:hover td'));
    assert.equal(new Set([LIGHT, DARK].flatMap((theme) => ['--tint-write', '--tint-read', '--tint-response'].map((name) => theme[name]))).size, 6, 'three tints, different in each theme');
});

test('css: a column whose width was set keeps it in heading and cells, and a group heading spans the list', () => {
    const sized = rule(WIDE, '.sized');
    assert.deepEqual([sized.width, sized['min-width'], sized['max-width']], ['var(--col-width)', 'var(--col-width)', 'var(--col-width)']);
    assert.equal(sized.overflow, 'hidden');
    assert.equal(rule(WIDE, 'th .col-resize').cursor, 'col-resize');
    assert.equal(rule(WIDE, 'th .col-resize').position, 'absolute');
});

test('css: muted parts inside a selected row take the row\'s colour, which reads at 4.5:1 or better in both themes', () => {
    assert.deepEqual(rule(WIDE, 'table.grid tbody tr.selected td :is(.time, .clock, .name, .payload, .reason, .icon, .filter-device, .flag)'), { color: 'inherit' });
    assert.equal(rule(WIDE, 'table.grid tbody tr.selected td').color, 'var(--select-text)');
    for (const theme of [LIGHT, DARK]) {
        assert.ok(contrast(theme['--select-text'], theme['--select']) >= 4.5);
        assert.ok(contrast(theme['--muted'], theme['--select']) < 4.5, 'which the muted colour would not');
    }
    assert.equal(contrast(LIGHT['--select-text'], LIGHT['--select']).toFixed(2), '11.57');
    assert.equal(contrast(DARK['--select-text'], DARK['--select']).toFixed(2), '8.92');
});

test('css: the border of a field or button stands 3:1 or better against what it lies on, in both themes', () => {
    assert.equal(rule(WIDE, 'button, a.add').border, '1px solid var(--control-line)');
    assert.equal(rule(WIDE, 'input, select, textarea').border, '1px solid var(--control-line)');
    const ratios = [LIGHT, DARK].map((theme) => ['--back', '--panel', '--frame'].map((ground) => contrast(theme['--control-line'], theme[ground]).toFixed(2)));
    assert.deepEqual(ratios, [
        ['3.80', '3.51', '3.29'],
        ['4.27', '3.92', '3.55'],
    ]);
});

test('css: on a narrow screen the open tree lies over the list only, below the bar with its button, and the sidebar goes under the panel', () => {
    assert.equal(rule(NARROW, '.workplace')['flex-direction'], 'column');
    // the bar is no part of the place tree and list share, so the open tree cannot cover it
    const body = rule(NARROW, '.panel-body');
    assert.equal(body.display, 'grid');
    const tree = rule(NARROW, '.tree-pane');
    const list = rule(NARROW, '.list-pane');
    assert.deepEqual([tree['grid-row'], tree['grid-column'], list['grid-row'], list['grid-column']], ['1', '1', '1', '1']);
    assert.equal(tree.position, undefined);
    assert.equal(tree.display, 'none');
    assert.equal(tree['z-index'], '3');
    assert.equal(rule(NARROW, '.tree-open .tree-pane').display, 'flex');
    assert.equal(rule(NARROW, '.sidebar').width, 'auto');
    assert.equal(rule(NARROW, '.tree-toggle').display, 'inline-flex');
    assert.equal(rule(NARROW, '.splitter').display, 'none');
});

test('css: both themes are given completely: for the root with data-theme and, without it, by the system setting', () => {
    assert.deepEqual(SYSTEM_DARK, DARK, 'the dark theme of the system and the chosen one are the same');
    assert.deepEqual(Object.keys(LIGHT).sort(), Object.keys(DARK).sort(), 'every colour has a value in both themes');
    assert.ok(Object.keys(LIGHT).length >= 20);
    assert.ok(Object.keys(LIGHT).every((name) => name.startsWith('--')));
    assert.ok(Object.values(LIGHT).concat(Object.values(DARK)).every((value) => /^#[0-9a-f]{6}([0-9a-f]{2})?$/.test(value)));
    assert.match(WIDE, /@media \(prefers-color-scheme: dark\) \{\s*:root:not\(\[data-theme\]\)/);
    assert.equal(rules(CSS).filter(([selector]) => selector === THEME_SELECTORS[2]).length, 2, 'the colours and the colour-scheme');
});

test('css: no colour value stands outside the theme blocks', () => {
    assert.deepEqual(strayColours(CSS), []);
    assert.ok(rules(CSS).length > 150);
    // the rule sees what it must see
    assert.deepEqual(strayColours('a { color: #123456 }'), ['a { color: #123456 }']);
    assert.deepEqual(strayColours('@media (max-width: 1px) { a { box-shadow: 0 0 2px rgba(0, 0, 0, 0.2); border: 1px solid red } }').length, 2);
    assert.deepEqual(strayColours(':root { --x: #123456 }').length, 1, 'only the theme blocks may name colours');
    assert.deepEqual(strayColours('a { color: var(--text); background: none; border-color: transparent; white-space: nowrap; fill: currentColor }'), []);
    assert.deepEqual(strayColours(':root[data-theme="dark"] { --x: #123456 }'), []);
    assert.deepEqual(strayColours('a { color: darkgreen; border-left-color: GrayText; fill: rebeccapurple; background-color: var(--back) }').length, 3);
});

test('css: the drawings take the colour of the text around them', () => {
    const icon = rule(WIDE, '.icon');
    assert.equal(icon.stroke, 'currentColor');
    assert.equal(icon.fill, 'none');
    const dom = readFileSync(new URL('../web/js/dom.js', import.meta.url), 'utf8');
    assert.doesNotMatch(dom, /setAttribute\('(fill|stroke|color)'/);
});

/** The pairs of the page: [what, colour, ground, least ratio]. */
const PAIRS = [
    ['text', '--text', '--back', 4.5],
    ['text on the panel', '--text', '--panel', 4.5],
    ['muted text', '--muted', '--back', 4.5],
    ['muted text on the panel', '--muted', '--panel', 4.5],
    ['muted text on the frame', '--muted', '--frame', 4.5],
    ['muted text on a hovered row', '--muted', '--hover', 4.5],
    ['a link', '--accent', '--back', 4.5],
    ['a filled button', '--on-accent', '--accent', 4.5],
    ['a selected row', '--select-text', '--select', 4.5],
    ['a warning', '--warn-text', '--warn-back', 4.5],
    ['the strip', '--strip-text', '--strip', 4.5],
    ['the strip, hovered', '--strip-text', '--strip-hover', 4.5],
    ['muted text on the strip', '--strip-muted', '--strip', 4.5],
    ['text on a write row', '--text', '--tint-write', 4.5],
    ['text on a read row', '--text', '--tint-read', 4.5],
    ['text on a response row', '--text', '--tint-response', 4.5],
    ['muted text on a write row', '--muted', '--tint-write', 4.5],
    ['muted text on a read row', '--muted', '--tint-read', 4.5],
    ['muted text on a response row', '--muted', '--tint-response', 4.5],
    ['the line of a control', '--control-line', '--back', 3],
    ['the line of a control on the panel', '--control-line', '--panel', 3],
    ['the line of a control on the frame', '--control-line', '--frame', 3],
    ['the line of a button on the strip', '--strip-line', '--strip', 3],
    ['the focus mark', '--accent', '--back', 3],
    ['the focus mark on the panel', '--accent', '--panel', 3],
    ['the focus mark on a selected row', '--accent', '--select', 3],
    ['the focus mark on the strip', '--strip-text', '--strip', 3],
];

test('css: text reads at 4.5:1, the lines of controls and the focus mark at 3:1, in both themes', () => {
    for (const [name, theme] of [['light', LIGHT], ['dark', DARK]]) {
        for (const [what, colour, ground, least] of PAIRS) {
            const ratio = contrast(theme[colour], theme[ground]);
            assert.ok(ratio >= least, `${name}: ${what} ${ratio.toFixed(2)} < ${least}`);
        }
    }
    assert.equal(rule(WIDE, 'header a:focus-visible, header button:focus-visible')['outline-color'], 'var(--strip-text)');
    assert.equal(rule(WIDE, 'header').background, 'var(--strip)');
});
