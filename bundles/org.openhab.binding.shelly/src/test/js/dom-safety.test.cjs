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

const html = readFileSync(path.resolve(__dirname, '../../main/resources/sniplets/overview.html'), 'utf8');
const handler = html.match(/onchange="([^"]*)"/)[1].replaceAll('${uri}', '/shelly');
const values = ['*', 'online', 'inactive', 'attention', 'update', 'unprotected',
    'online&action=reset_stat', 'online#fragment', '<img src=x onerror=alert(1)>', 'a + % é'];

for (const [index, value] of values.entries()) {
    test(`preserves device filter ${index} as one query value`, () => {
        const context = { location: '', selection: { options: [{ value }], selectedIndex: 0 } };
        vm.runInNewContext(`(function () { ${handler} }).call(selection);`, context);

        const target = new URL(context.location, 'https://openhab.example');
        assert.equal(target.origin, 'https://openhab.example');
        assert.equal(target.pathname, '/shelly/overview');
        assert.equal(target.searchParams.get('filter'), value);
        assert.equal([...target.searchParams].length, 1);
        assert.equal(target.hash, '');
    });
}
