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
const { readFileSync, readdirSync } = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

const templates = path.resolve(__dirname, '../../main/resources/templates');
const callback = 'https://openhab.example/connectlinky/callback?state=existing';
const values = ['12345678901234', '', 'code&state=attacker', 'code#fragment',
    '<img src=x onerror=alert(1)>', 'a + % é'];

for (const template of readdirSync(templates).filter(file => file.endsWith('.html'))) {
    const html = readFileSync(path.join(templates, template), 'utf8');
    const script = html.match(/<script\b[^>]*>([\s\S]*?)<\/script>/i)[1]
        .replaceAll('${retrieveToken.uri}', callback);

    for (const [index, value] of values.entries()) {
        test(`${template}: preserves authorization code ${index} as one query value`, () => {
            const document = { getElementById: () => ({ value }), location: '' };
            vm.runInNewContext(script + '\nretrieveToken();', { document });

            const target = new URL(document.location);
            assert.equal(target.origin, 'https://openhab.example');
            assert.equal(target.pathname, '/connectlinky/callback');
            assert.equal(target.searchParams.get('state'), 'existing');
            assert.equal(target.searchParams.get('code'), value);
            assert.equal([...target.searchParams].length, 2);
            assert.equal(target.hash, '');
        });
    }
}
