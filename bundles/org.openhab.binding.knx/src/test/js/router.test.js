import { test } from 'node:test';
import assert from 'node:assert/strict';

import { NAV_VIEWS, SPLIT_VIEWS, chooser, defaultRoute, firstOf, href, parse, resolve, secondOf, selection, viewsOf, withSecond } from '../web/js/router.js';

const UID = 'knx:ip:example';
const OTHER = 'knx:serial:other';
const present = { uid: UID, label: 'Line 7', present: true, projectState: 'NONE' };
const removed = { uid: OTHER, present: false, projectState: 'LOADED' };

test('router: every route there and back', () => {
    const routes = [
        chooser(),
        { view: 'group-addresses', bridge: UID, query: {} },
        { view: 'topology', bridge: UID, query: { sel: 'device:7.7.1' } },
        { view: 'buildings', bridge: UID, query: {} },
        { view: 'monitor', bridge: UID, query: {} },
        { view: 'project', bridge: UID, query: {} },
    ];
    for (const route of routes) {
        const link = href(route);
        assert.match(link, /^#\//);
        assert.deepEqual(parse(link), route, link);
    }
    assert.equal(href(chooser()), '#/');
    assert.equal(href(routes[4]), '#/b/knx%3Aip%3Aexample/monitor');
    assert.equal(href(routes[1]), '#/b/knx%3Aip%3Aexample/group-addresses');
    assert.equal(href(routes[2]), '#/b/knx%3Aip%3Aexample/topology?sel=device%3A7.7.1');
});

test('router: the addresses of the page before the panels open the matching panel', () => {
    assert.deepEqual(parse('#/b/x/addresses'), { view: 'group-addresses', bridge: 'x', query: {} });
    assert.deepEqual(parse('#/b/x/building'), { view: 'buildings', bridge: 'x', query: {} });
    assert.deepEqual(parse('#/b/x/setup'), { view: 'project', bridge: 'x', query: {} });
    assert.deepEqual(parse('#/b/x/monitor'), { view: 'monitor', bridge: 'x', query: {} });
    assert.deepEqual(parse('#/b/x/devices'), { view: 'topology', bridge: 'x', query: {} }, 'the list of the Devices panel is the one of Topology\'s first node');
    assert.deepEqual(parse('#/b/x/devices?row=device%3AP-31_D-1'), { view: 'topology', bridge: 'x', query: { row: 'device:P-31_D-1' } });
    assert.deepEqual(parse('#/b/x/devices?sel=device%3AP-31_D-1&row=object%3A2'), { view: 'topology', bridge: 'x', query: { sel: 'device:P-31_D-1', row: 'object:2' } });
    assert.deepEqual(parse('#/b/x/buildings?p2=devices&p2-row=device%3AP-31_D-1'), { view: 'buildings', bridge: 'x', query: { p2: 'topology', 'p2-row': 'device:P-31_D-1' } });
    assert.equal(href(parse('#/b/x/devices?row=device%3AP-31_D-1')), '#/b/x/topology?row=device%3AP-31_D-1');
    assert.throws(() => href({ view: 'devices', bridge: 'x', query: {} }), TypeError, 'the page makes no address of the Devices panel');
    assert.deepEqual(parse('#/b/x/wizard?device=7.7.1'), { view: 'wizard', bridge: 'x', query: { device: '7.7.1' } });
    assert.deepEqual(parse('#/b/x/devices/7.7.1'), { view: 'topology', bridge: 'x', query: { sel: 'device:7.7.1' } });
    assert.deepEqual(parse('#/b/x/devices/P-7_DI-1%2Fa'), { view: 'topology', bridge: 'x', query: { sel: 'device:P-7_DI-1/a' } });
    assert.equal(href(parse('#/b/x/setup')), '#/b/x/project');
    assert.equal(href(parse('#/b/x/devices/7.7.1')), '#/b/x/topology?sel=device%3A7.7.1');
    assert.deepEqual(parse('#/b/x/addresses?sel=ga%3A31%2F7%2F1'), { view: 'group-addresses', bridge: 'x', query: { sel: 'ga:31/7/1' } });
});

test('router: a selection is the panel\'s address with its key, and no key is the panel alone', () => {
    const route = { view: 'group-addresses', bridge: UID, query: { sel: 'ga:31/7/1' } };
    assert.deepEqual(selection(route, 'range:Lights/Ground%20floor'), { view: 'group-addresses', bridge: UID, query: { sel: 'range:Lights/Ground%20floor' } });
    assert.deepEqual(selection(route, ''), { view: 'group-addresses', bridge: UID, query: {} });
    for (const key of ['ga:31/7/1', 'range:a%2Fb/c', 'device:a/b?c#d&e=f', 'unassigned', 'space:P-7_B-2']) {
        const link = href(selection(route, key));
        assert.ok(link.startsWith('#/b/knx%3Aip%3Aexample/group-addresses?sel='), link);
        assert.deepEqual(parse(link).query, { sel: key });
    }
});

test('router: a bridge UID is URI-encoded and comes back unchanged', () => {
    for (const uid of ['knx:ip:a/b', 'knx:ip:with space', 'knx:ip:ü€', 'knx:ip:%41', 'knx:ip:a?b#c&d']) {
        const route = { view: 'group-addresses', bridge: uid, query: {} };
        const link = href(route);
        assert.equal(link.split('/').length, 4, link);
        assert.doesNotMatch(link.slice(1), /[?#&\s]/, link);
        assert.deepEqual(parse(link), route);
    }
    assert.deepEqual(parse('#/b/knx:ip:example/monitor'), { view: 'monitor', bridge: UID, query: {} });
});

test('router: an unknown route is the chooser', () => {
    for (const hash of [
        '',
        '#',
        '#/',
        'x',
        '#x',
        '#/b',
        '#/b/',
        `#/b/${UID}`,
        '#/b//monitor',
        '#/b/x/charts',
        '#/b/x/monitor/',
        '#/b/x/monitor/more',
        '#/b/x/devices/',
        '#/b/x/device/7.7.1',
        '#/b/x/topology/7.7.1',
        '#/b/x/setup/7.7.1',
        '#/b/x/devices/7.7.1/objects',
        '#/c/x/monitor',
        '#//b/x/monitor',
        '#/b/x/constructor',
        '#/b/x/__proto__',
    ]) {
        assert.deepEqual(parse(hash), chooser(), hash);
    }
    assert.deepEqual(parse(undefined), chooser());
});

test('router: a hash that does not decode is an unknown route', () => {
    for (const hash of ['#%E0%A4%A', '#/b/%E0%A4%A/monitor', '#/b/x/devices/%E0%A4%A', '#/b/x/monitor?a=%E0%A4%A', '#/b/x/monitor?%=1']) {
        assert.deepEqual(parse(hash), chooser(), hash);
    }
});

test('router: a query part there and back', () => {
    const route = { view: 'monitor', bridge: UID, query: { dst: '31/7/1', text: 'a&b=c d', empty: '' } };
    const link = href(route);
    assert.equal(link, '#/b/knx%3Aip%3Aexample/monitor?dst=31%2F7%2F1&text=a%26b%3Dc%20d&empty=');
    assert.deepEqual(parse(link), route);
    assert.equal(href({ view: 'project', bridge: UID }), '#/b/knx%3Aip%3Aexample/project');
    assert.equal(href({ view: 'project', bridge: UID, query: { a: undefined, b: null } }), '#/b/knx%3Aip%3Aexample/project');
    assert.deepEqual(parse('#/?x=1'), { view: 'chooser', query: { x: '1' } });
});

test('router: a query name that is no plain name is dropped, the first of two names wins', () => {
    const route = parse('#/b/x/monitor?__proto__=1&a=1&a=2&=3&1x=4&b&c=d=e&&');
    assert.deepEqual(route.query, { a: '1', b: '', c: 'd=e' });
    assert.equal(Object.getPrototypeOf(route.query), Object.prototype);
    assert.throws(() => href({ view: 'monitor', bridge: UID, query: { 'a b': '1' } }), TypeError);
});

test('router: odd device ids give an href that starts with #/ and come back unchanged', () => {
    for (const device of ['javascript:alert(1)', '../x', 'a/b', '..', '#/b/x', '?a=1', '%E0%A4%A', ' ', '7.7.1']) {
        const route = { view: 'topology', bridge: UID, query: { sel: `device:${device}` } };
        const link = href(route);
        assert.ok(link.startsWith('#/b/'), link);
        assert.doesNotMatch(link, /javascript:/i);
        assert.equal(link.split('/').length, 4, link);
        assert.deepEqual(parse(link), route, link);
    }
});

test('router: href refuses a route the page does not have', () => {
    assert.throws(() => href({ view: 'charts', bridge: UID }), TypeError);
    assert.throws(() => href({ view: 'monitor', bridge: '' }), TypeError);
    assert.throws(() => href({ view: 'monitor' }), TypeError);
    assert.throws(() => href({ view: 'device', bridge: UID, device: '7.7.1' }), TypeError);
    assert.throws(() => href({ view: 'setup', bridge: UID }), TypeError);
});

test('router: a bridge opens on Buildings when its project has a building structure, else on Group Addresses; without a project as before', () => {
    assert.deepEqual(defaultRoute(removed), { view: 'group-addresses', bridge: OTHER, query: {} });
    assert.deepEqual(defaultRoute(present), { view: 'monitor', bridge: UID, query: {} });
    for (const bridge of [present, removed]) {
        assert.equal(defaultRoute({ ...bridge, projectState: 'LOADED', projectSpaces: 2 }).view, 'buildings');
        assert.equal(defaultRoute({ ...bridge, projectState: 'LOADED', projectSpaces: 0 }).view, 'group-addresses');
        assert.equal(defaultRoute({ ...bridge, projectState: 'LOADED' }).view, 'group-addresses', 'a server that does not say');
        assert.equal(defaultRoute({ ...bridge, projectState: 'UNREADABLE', projectSpaces: 2 }).view, defaultRoute(bridge).view);
    }
    assert.equal(defaultRoute({ ...present, projectState: 'NONE' }).view, 'monitor');
});

test('router: the panels in the order of the tabs; a removed bridge has no Group Monitor and no wizard', () => {
    assert.deepEqual(viewsOf(removed), ['group-addresses', 'topology', 'buildings', 'project']);
    assert.deepEqual(viewsOf(present), ['group-addresses', 'topology', 'buildings', 'monitor', 'project', 'wizard']);
    assert.deepEqual(NAV_VIEWS, ['group-addresses', 'topology', 'buildings', 'monitor', 'project'], 'no Devices panel: Topology lists every device');
});

test('router: resolve goes on from the chooser with exactly one bridge and stays with none or two', () => {
    assert.deepEqual(resolve(chooser(), [present]), defaultRoute(present));
    assert.deepEqual(resolve(chooser(), [removed]), defaultRoute(removed));
    assert.deepEqual(resolve(chooser(), []), chooser());
    assert.deepEqual(resolve(chooser(), [present, removed]), chooser());
});

test('router: resolve sends an unknown bridge to the chooser and a view a removed bridge lacks to its default', () => {
    const unknown = { view: 'buildings', bridge: 'knx:ip:gone', query: {} };
    assert.deepEqual(resolve(unknown, [present, removed]), chooser());
    assert.deepEqual(resolve(unknown, [present]), defaultRoute(present));
    assert.deepEqual(resolve({ view: 'monitor', bridge: OTHER, query: {} }, [present, removed]), defaultRoute(removed));
    const device = { view: 'topology', bridge: OTHER, query: { sel: 'device:7.7.2' } };
    assert.deepEqual(resolve(device, [present, removed]), device);
    const withQuery = { view: 'monitor', bridge: UID, query: { dst: '31/7/1' } };
    assert.deepEqual(resolve(withQuery, [present, removed]), withQuery);
});

test('router: the wizard is a route of a present bridge, with its preselection in the query part', () => {
    for (const query of [{}, { device: '7.7.1' }, { function: 'P-7_F-1' }, { space: 'P-7_B-2' }, { device: 'a/b?c#d' }]) {
        const route = { view: 'wizard', bridge: UID, query };
        const link = href(route);
        assert.ok(link.startsWith('#/b/knx%3Aip%3Aexample/wizard'), link);
        assert.deepEqual(parse(link), route, link);
        assert.deepEqual(resolve(route, [present, removed]), route);
    }
    assert.equal(href({ view: 'wizard', bridge: UID, query: { device: '7.7.1' } }), '#/b/knx%3Aip%3Aexample/wizard?device=7.7.1');
    assert.deepEqual(parse('#/b/x/wizard/more'), chooser());
    assert.deepEqual(resolve({ view: 'wizard', bridge: OTHER, query: { device: '7.7.1' } }, [present, removed]), defaultRoute(removed));
    assert.ok(!NAV_VIEWS.includes('wizard'));
});

test('router: the selected row of a list is one more value of the panel\'s address', () => {
    const route = { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'old', other: 'x' } };
    assert.deepEqual(selection(route, 'line:31.1', 'device:d-1'), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:31.1', row: 'device:d-1' } });
    assert.deepEqual(selection(route, '', '4'), { view: 'topology', bridge: 'knx:ip:line7', query: { row: '4' } });
    assert.deepEqual(selection(route, 'line:31.1', ''), { view: 'topology', bridge: 'knx:ip:line7', query: { sel: 'line:31.1' } });
    const there = href(selection(route, 'range:Main@31', 'ga:31/1/1'));
    assert.equal(there, '#/b/knx%3Aip%3Aline7/topology?sel=range%3AMain%4031&row=ga%3A31%2F1%2F1');
    assert.deepEqual(parse(there).query, { sel: 'range:Main@31', row: 'ga:31/1/1' });
});

test('router: the tab of a list is one more value of the panel\'s address, left out when there is none', () => {
    const route = { view: 'buildings', bridge: 'knx:ip:line7', query: {} };
    assert.deepEqual(selection(route, 'space:r1', 'object:d1|2', 'objects').query, { sel: 'space:r1', tab: 'objects', row: 'object:d1|2' });
    assert.deepEqual(selection(route, 'space:r1', '', 'functions').query, { sel: 'space:r1', tab: 'functions' });
    assert.deepEqual(selection(route, 'space:r1', 'device:d1', '').query, { sel: 'space:r1', row: 'device:d1' });
    assert.deepEqual(selection(route, 'space:r1', 'device:d1').query, { sel: 'space:r1', row: 'device:d1' });
    const there = href(selection(route, 'space:r1', '', 'objects'));
    assert.equal(there, '#/b/knx%3Aip%3Aline7/buildings?sel=space%3Ar1&tab=objects');
    assert.deepEqual(parse(there).query, { sel: 'space:r1', tab: 'objects' });
});

test('router: a second panel is part of the address: its panel as "p2", its own values with "p2-" before their names', () => {
    const first = { view: 'buildings', bridge: UID, query: { sel: 'space:S-1', tab: 'objects' } };
    const second = { view: 'group-addresses', bridge: UID, query: { sel: 'ga:31/0/1', row: 'object:D-1|0' } };
    const both = withSecond(first, second);
    assert.deepEqual(both, { view: 'buildings', bridge: UID, query: { sel: 'space:S-1', tab: 'objects', p2: 'group-addresses', 'p2-sel': 'ga:31/0/1', 'p2-row': 'object:D-1|0' } });
    assert.equal(href(both), '#/b/knx%3Aip%3Aexample/buildings?sel=space%3AS-1&tab=objects&p2=group-addresses&p2-sel=ga%3A31%2F0%2F1&p2-row=object%3AD-1%7C0');
    const back = parse(href(both));
    assert.deepEqual(firstOf(back), first);
    assert.deepEqual(secondOf(back), second);
    assert.deepEqual(secondOf(first), null);
    assert.deepEqual(withSecond(both, null), first);
    // another first panel keeps the second; another second replaces the one before, values and all
    assert.deepEqual(withSecond({ view: 'topology', bridge: UID, query: {} }, secondOf(both)).query, { p2: 'group-addresses', 'p2-sel': 'ga:31/0/1', 'p2-row': 'object:D-1|0' });
    assert.deepEqual(withSecond(both, { view: 'monitor', bridge: UID, query: {} }).query, { sel: 'space:S-1', tab: 'objects', p2: 'monitor' });
    assert.deepEqual(SPLIT_VIEWS, ['group-addresses', 'topology', 'buildings', 'monitor']);
});

test('router: Project and the wizard have no second panel and are none; resolve leaves out a second panel that cannot be', () => {
    const second = { view: 'group-addresses', bridge: UID, query: {} };
    assert.deepEqual(withSecond({ view: 'project', bridge: UID, query: {} }, second), { view: 'project', bridge: UID, query: {} });
    assert.deepEqual(withSecond({ view: 'topology', bridge: UID, query: {} }, { view: 'wizard', bridge: UID, query: {} }).query, {});
    assert.equal(secondOf(parse('#/b/knx%3Aip%3Aexample/project?p2=topology')), null);
    assert.equal(secondOf(parse('#/b/knx%3Aip%3Aexample/topology?p2=project')), null);
    assert.equal(secondOf(parse('#/b/knx%3Aip%3Aexample/topology?p2-sel=x')), null);
    const bridges = [present, removed];
    assert.equal(href(resolve(parse('#/b/knx%3Aip%3Aexample/project?p2=topology&p2-sel=x'), bridges)), '#/b/knx%3Aip%3Aexample/project');
    assert.equal(href(resolve(parse('#/b/knx%3Aip%3Aexample/topology?sel=a&p2=nothing'), bridges)), '#/b/knx%3Aip%3Aexample/topology?sel=a');
    assert.equal(href(resolve(parse('#/b/knx%3Aserial%3Aother/topology?p2=monitor'), bridges)), '#/b/knx%3Aserial%3Aother/topology', 'a removed bridge has no monitor');
    const kept = parse('#/b/knx%3Aserial%3Aother/topology?p2=buildings&p2-sel=x');
    assert.equal(resolve(kept, bridges), kept);
});
