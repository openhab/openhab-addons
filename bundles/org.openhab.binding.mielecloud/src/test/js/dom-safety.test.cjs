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
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

const resources = path.resolve(__dirname, '../../main/resources');
const html = readFileSync(path.join(resources, 'org/openhab/binding/mielecloud/internal/config/success.html'), 'utf8');
const script = html.match(/<script\b[^>]*>([\s\S]*?)<\/script>/i)[1];
const template = 'Bridge mielecloud:account:home [ email="a&b<lab>@example.invalid", locale="en" ]';

function fixture() {
    const textarea = { value: template };
    Object.defineProperty(textarea, 'innerHTML', {
        get() { throw new Error('The configuration must remain text'); },
        set() { throw new Error('The configuration must remain text'); }
    });
    const elements = { 'things-template': textarea, locale: { value: 'de' } };
    const context = { window: {}, document: { getElementById: id => elements[id] } };
    vm.createContext(context);
    vm.runInContext(script, context);
    return { textarea, context };
}

test('updates the initial and subsequently selected locale without changing configuration text', () => {
    const { textarea, context } = fixture();
    context.window.onload();
    assert.equal(textarea.value, template.replace('locale="en"', 'locale="de"'));

    context.onLocaleChanged({ target: { value: 'fr' } });
    assert.equal(textarea.value, template.replace('locale="en"', 'locale="fr"'));
});

test('keeps markup in a locale value as literal textarea content', () => {
    const { textarea, context } = fixture();
    const locale = 'de"><img src=x onerror=alert(1)>';
    context.updateLocale(locale);

    assert.equal(textarea.value, template.replace('locale="en"', `locale="${locale}"`));
});

test('the generated JavaScript bundle matches its embedded source files', () => {
    const assets = path.join(resources, '3rdparty/assets/js');
    const bundle = readFileSync(path.join(assets, 'main.js'), 'utf8');
    const sourceMap = JSON.parse(readFileSync(path.join(assets, 'main.js.map'), 'utf8'));

    assert.equal(bundle, sourceMap.sourcesContent.join('\n') + '\n//# sourceMappingURL=main.js.map\n');
    new vm.Script(bundle);
});
