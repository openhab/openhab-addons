import { test } from 'node:test';
import assert from 'node:assert/strict';

import { createLayout } from '../web/js/layout.js';
import { createPanel } from '../web/js/panel.js';
import { PROPERTY_TABS, createProperties } from '../web/js/properties.js';
import { CONTAINERS, createSidebar } from '../web/js/sidebar.js';
import { KEY_STEP, createSplitter } from '../web/js/splitter.js';
import { createTree } from '../web/js/tree.js';
import { createSide, createWorkplace } from '../web/js/workplace.js';
import { createDocument, textNodes } from './fakedom.js';

function store(initial = null) {
    const kept = { text: initial, writes: 0 };
    return {
        kept,
        read: () => kept.text,
        write(text) {
            kept.writes++;
            kept.text = text;
        },
    };
}

function splitter(layout, more = {}) {
    const document = createDocument();
    const target = document.createElement('div');
    const made = createSplitter({ document, target, property: '--tree-width', name: 'tree', layout, initial: 280, min: 140, max: 640, label: 'Width of the tree', ...more });
    return { document, target, made };
}

test('splitter: a separator that can be focused, with the starting width on its target', () => {
    const { target, made } = splitter(createLayout(store()));
    assert.equal(made.node.getAttribute('role'), 'separator');
    assert.equal(made.node.getAttribute('tabindex'), '0');
    assert.equal(made.node.getAttribute('aria-orientation'), 'vertical');
    assert.equal(made.node.getAttribute('aria-label'), 'Width of the tree');
    assert.equal(made.node.getAttribute('aria-valuenow'), '280');
    assert.equal(target.style.getPropertyValue('--tree-width'), '280px');
});

test('splitter: dragging with the pointer sets the width within its limits and keeps it when the pointer lets go', () => {
    const kept = store();
    const { target, made } = splitter(createLayout(kept));
    made.node.fire('pointermove', { clientX: 900 });
    assert.equal(made.width(), 280);
    made.node.fire('pointerdown', { clientX: 300, pointerId: 1 });
    made.node.fire('pointermove', { clientX: 360 });
    assert.equal(target.style.getPropertyValue('--tree-width'), '340px');
    assert.equal(kept.kept.writes, 0);
    made.node.fire('pointermove', { clientX: 2000 });
    assert.equal(made.width(), 640);
    made.node.fire('pointermove', { clientX: 0 });
    assert.equal(made.width(), 140);
    made.node.fire('pointermove', { clientX: 320 });
    made.node.fire('pointerup', {});
    assert.deepEqual(JSON.parse(kept.kept.text), { tree: 300 });
    made.node.fire('pointermove', { clientX: 500 });
    assert.equal(made.width(), 300);
});

test('splitter: the arrow keys move the border, Home and End go to the limits; a pane on the right grows to the left', () => {
    const kept = store();
    const left = splitter(createLayout(kept));
    assert.equal(left.made.node.fire('keydown', { key: 'ArrowRight' }).defaultPrevented, true);
    assert.equal(left.made.width(), 280 + KEY_STEP);
    left.made.node.fire('keydown', { key: 'ArrowLeft' });
    left.made.node.fire('keydown', { key: 'ArrowLeft' });
    assert.equal(left.made.width(), 280 - KEY_STEP);
    assert.deepEqual(JSON.parse(kept.kept.text), { tree: 264 });
    left.made.node.fire('keydown', { key: 'Home' });
    assert.equal(left.made.width(), 140);
    left.made.node.fire('keydown', { key: 'End' });
    assert.equal(left.made.width(), 640);
    assert.equal(left.made.node.fire('keydown', { key: 'Enter' }).defaultPrevented, false);

    const right = splitter(createLayout(store()), { reverse: true });
    right.made.node.fire('keydown', { key: 'ArrowLeft' });
    assert.equal(right.made.width(), 280 + KEY_STEP);
    right.made.node.fire('pointerdown', { clientX: 500 });
    right.made.node.fire('pointermove', { clientX: 450 });
    assert.equal(right.made.width(), 280 + KEY_STEP + 50);
});

test('splitter: a kept width comes back, within the limits; a kept text that is no such thing is left aside', () => {
    assert.equal(splitter(createLayout(store('{"tree":400}'))).made.width(), 400);
    assert.equal(splitter(createLayout(store('{"tree":4000}'))).made.width(), 640);
    assert.equal(splitter(createLayout(store('{"tree":"wide"}'))).made.width(), 280);
    assert.equal(splitter(createLayout(store('not json'))).made.width(), 280);
    assert.equal(splitter(createLayout(store('[1]'))).made.width(), 280);
});

test('splitter: without a store, or with one that refuses, the page works and keeps the width for this visit', () => {
    for (const refused of [null, undefined, { read: () => { throw new Error('blocked'); }, write: () => { throw new Error('blocked'); } }]) {
        const layout = createLayout(refused);
        const { made } = splitter(layout);
        made.node.fire('keydown', { key: 'ArrowRight' });
        assert.equal(made.width(), 296);
        assert.equal(layout.get('tree'), 296);
        assert.equal(splitter(layout).made.width(), 296);
    }
    const none = splitter(undefined);
    none.made.node.fire('keydown', { key: 'ArrowRight' });
    assert.equal(none.made.width(), 296);
});

test('splitter: the store is read only when a width is first needed, and close() leaves no listener', () => {
    let reads = 0;
    const layout = createLayout({ read: () => (reads++, null), write() {} });
    assert.equal(reads, 0);
    const { document, made } = splitter(layout);
    assert.equal(reads, 1);
    layout.get('tree');
    assert.equal(reads, 1);
    made.close();
    assert.equal(document.listenerCount(), 0);
});

test('properties: three tabs, Settings, Comments and Information; rows under the tab of their section, empty rows and empty sections left out', () => {
    const document = createDocument();
    const properties = createProperties(document);
    assert.deepEqual([...PROPERTY_TABS], ['settings', 'comments', 'information']);
    assert.equal(properties.node.textContent, 'Nothing is selected.');
    const link = document.createElement('a');
    properties.show('31/0/1 Kitchen <b>', [
        { rows: [['Name', 'Kitchen <b>'], ['Description', ''], ['Length', 8], ['Security', null]] },
        { heading: 'Flags', ticks: [['Read', true], ['Write', false]] },
        { tab: 'comments', rows: [['Description', '']] },
        { tab: 'information', rows: [['Last Value', undefined]], more: [link, null] },
    ]);
    assert.equal(properties.node.querySelector('.subject').textContent, '31/0/1 Kitchen <b>');
    const tabs = properties.node.querySelector('.prop-tabs').querySelectorAll('button');
    assert.equal(properties.node.querySelector('.prop-tabs').getAttribute('role'), 'tablist');
    assert.deepEqual(tabs.map((tab) => [tab.textContent, tab.getAttribute('role'), tab.getAttribute('aria-selected')]), [
        ['Settings', 'tab', 'true'],
        ['Comments', 'tab', 'false'],
        ['Information', 'tab', 'false'],
    ]);
    const [settings, comments, information] = ['settings', 'comments', 'information'].map((name) => properties.node.querySelector(`.tab-${name}`));
    assert.deepEqual(textNodes(settings.querySelector('dl')), ['Name', 'Kitchen <b>', 'Length', '8']);
    assert.deepEqual(settings.querySelectorAll('h4').map((heading) => heading.textContent), ['Flags']);
    assert.deepEqual(settings.querySelectorAll('input').map((box) => [box.getAttribute('type'), box.disabled, box.checked]), [['checkbox', true, true], ['checkbox', true, false]]);
    assert.equal(comments.textContent, 'Nothing here for this selection.');
    assert.ok(information.contains(link));
    assert.deepEqual([settings, comments, information].map((panel) => panel.classList.contains('off')), [false, true, true]);
    properties.empty();
    assert.equal(properties.node.textContent, 'Nothing is selected.');
    properties.close();
    assert.equal(document.listenerCount(), 0);
});

test('properties: a tab is chosen by click or arrow key, and the choice is kept for the next selection and the next visit', () => {
    const kept = store();
    const document = createDocument();
    const properties = createProperties(document, createLayout(kept));
    properties.show('a', [{ rows: [['Name', 'a']] }, { tab: 'information', rows: [['Product', 'p']] }]);
    const tabs = () => properties.node.querySelector('.prop-tabs').querySelectorAll('button');
    const shown = () => ['settings', 'comments', 'information'].filter((name) => !properties.node.querySelector(`.tab-${name}`).classList.contains('off'));
    assert.deepEqual(shown(), ['settings']);
    tabs()[2].click();
    assert.deepEqual(shown(), ['information']);
    assert.equal(properties.tab(), 'information');
    assert.deepEqual(tabs().map((tab) => tab.getAttribute('aria-selected')), ['false', 'false', 'true']);
    assert.equal(JSON.parse(kept.kept.text).propertiesTab, 'information');
    properties.show('b', [{ rows: [['Name', 'b']] }, { tab: 'information', rows: [['Product', 'q']] }]);
    assert.deepEqual(shown(), ['information'], 'the tab stays when the selection changes');
    // a selection with nothing under the chosen tab shows the first tab that holds something, and the choice stays
    properties.show('c', [{ rows: [['Name', 'c']] }]);
    assert.deepEqual(shown(), ['settings']);
    assert.deepEqual(tabs().map((tab) => tab.getAttribute('aria-selected')), ['true', 'false', 'false']);
    assert.equal(properties.tab(), 'information');
    properties.show('d', [{ tab: 'information', rows: [['Product', 'r']] }]);
    assert.deepEqual(shown(), ['information']);
    tabs()[2].focus();
    assert.equal(tabs()[2].fire('keydown', { key: 'ArrowLeft' }).defaultPrevented, true);
    assert.deepEqual(shown(), ['comments']);
    assert.ok(document.activeElement === tabs()[1]);
    tabs()[1].fire('keydown', { key: 'ArrowRight' });
    tabs()[2].fire('keydown', { key: 'ArrowRight' });
    assert.deepEqual(shown(), ['settings'], 'around the end');
    assert.equal(JSON.parse(kept.kept.text).propertiesTab, 'settings');
    tabs()[1].click();
    assert.equal(createProperties(createDocument(), createLayout(kept)).tab(), 'comments');
    assert.equal(createProperties(createDocument(), createLayout(store('{"propertiesTab":"other"}'))).tab(), 'settings');
});

function workplace(layout, more = {}) {
    const document = createDocument();
    const element = document.createElement('section');
    const made = createWorkplace({ document, element, layout, title: 'Topology', ...more });
    return { document, element, made };
}

test('workplace: a title, one panel (bar, tree, tabs, list) and the sidebar, with a border between them', () => {
    const { element, made } = workplace(createLayout(store()));
    assert.ok(element.classList.contains('work'));
    assert.equal(element.querySelector('h2').textContent, 'Topology');
    const box = element.querySelector('.workplace');
    assert.deepEqual(box.children.map((child) => child.className), ['panel', 'splitter properties-splitter', 'pane sidebar']);
    const panel = box.children[0];
    assert.deepEqual(panel.children.map((child) => child.className), ['tool-row panel-bar', 'panel-body']);
    assert.deepEqual(panel.children[1].children.map((child) => child.className), ['pane tree-pane', 'splitter', 'pane list-pane']);
    assert.deepEqual(made.list.parentNode.children.map((child) => child.className), ['list-tabs', 'list']);
    assert.equal(made.treePane.getAttribute('aria-label'), 'Topology');
    // each panel carries the width of its own tree; the workplace the width of the sidebar
    assert.equal(panel.style.getPropertyValue('--tree-width'), '280px');
    assert.equal(box.style.getPropertyValue('--properties-width'), '300px');
    assert.equal(made.tools.parentNode.className, 'tool-row panel-bar');
    assert.equal(made.find.parentNode.className, 'tool-row panel-bar');
    assert.equal(element.querySelector('.sidebar').getAttribute('aria-label'), 'Sidebar');
});

test('panel: the bar holds back, forward, the panel chooser, the path, the actions and find, in this order', () => {
    const document = createDocument();
    const moves = [];
    const chosen = [];
    const panels = ['group-addresses', 'topology', 'buildings', 'monitor', 'project'].map((view) => ({ view, label: view.toUpperCase() }));
    const panel = createPanel({ document, title: 'Topology', panels, current: 'topology', onPanel: (view) => chosen.push(view), history: { back: () => moves.push('back'), forward: () => moves.push('forward') } });
    assert.deepEqual(panel.bar.children.map((child) => child.className), ['tool nav-back', 'tool nav-forward', 'panel-chooser', 'tool tree-toggle', 'crumbs', 'tools', 'find-slot']);
    const [back, forward, chooser] = panel.bar.children;
    assert.deepEqual([back.getAttribute('aria-label'), forward.getAttribute('aria-label'), chooser.getAttribute('aria-label')], ['Back', 'Forward', 'Panel']);
    back.click();
    forward.click();
    // a click that lands on the drawing inside the button
    back.querySelector('.icon').fire('click');
    assert.deepEqual(moves, ['back', 'forward', 'back']);
    assert.equal(chooser.tagName, 'SELECT');
    assert.deepEqual(chooser.querySelectorAll('option').map((option) => [option.getAttribute('value'), option.textContent]), panels.map((entry) => [entry.view, entry.label]));
    assert.equal(chooser.value, 'topology', 'the chooser shows the name of this panel');
    chooser.value = 'buildings';
    chooser.fire('change');
    assert.deepEqual(chosen, ['buildings']);
    assert.equal(chooser.value, 'topology', 'until the other panel is shown');
    chooser.fire('change');
    assert.deepEqual(chosen, ['buildings'], 'the same panel again is no choice');
    panel.close();
    assert.equal(document.listenerCount(), 0);
});

test('panel: the path of the selection as crumbs; each one but the last tells its key when it is chosen', () => {
    const document = createDocument();
    const panel = createPanel({ document, title: 'Buildings' });
    const picked = [];
    panel.setCrumbs(
        [
            { key: '', label: 'Buildings' },
            { key: 'space:f1', label: 'Ground <floor>' },
            { key: 'space:r1', label: 'Kitchen' },
            { key: 'device:d1', label: '31.1.5 Switch actuator' },
        ],
        (key) => picked.push(key),
    );
    const crumbs = panel.bar.querySelector('.crumbs');
    assert.equal(crumbs.tagName, 'NAV');
    assert.equal(crumbs.getAttribute('aria-label'), 'Path of the selection');
    const buttons = crumbs.querySelectorAll('button');
    assert.deepEqual(buttons.map((button) => button.textContent), ['Buildings', 'Ground <floor>', 'Kitchen', '31.1.5 Switch actuator']);
    assert.equal(buttons[1].children.length, 0, 'a name is text');
    assert.deepEqual(buttons.map((button) => button.getAttribute('aria-current')), [null, null, null, 'true']);
    buttons[1].click();
    buttons[0].click();
    assert.deepEqual(picked, ['space:f1', '']);
    panel.setCrumbs([{ key: '', label: 'Buildings' }]);
    assert.equal(crumbs.querySelectorAll('button').length, 1);
    buttons[1].click();
    assert.deepEqual(picked, ['space:f1', ''], 'a crumb that left tells nothing');
    panel.close();
});

test('panel: the tabs above the list show their counts; a click or an arrow key chooses another one', () => {
    const document = createDocument();
    const panel = createPanel({ document, title: 'Topology' });
    const picked = [];
    const draw = (shown) =>
        panel.setTabs(
            [
                { key: 'devices', label: 'Devices', count: 12 },
                { key: 'objects', label: 'Group Objects', count: 0 },
                { key: 'functions', label: 'Functions' },
            ],
            shown,
            (key) => {
                picked.push(key);
                draw(key);
            },
        );
    draw('devices');
    const row = panel.list.parentNode.querySelector('.list-tabs');
    assert.equal(row.getAttribute('role'), 'tablist');
    const tabs = () => row.querySelectorAll('button');
    assert.deepEqual(tabs().map((tab) => [tab.textContent, tab.getAttribute('role'), tab.getAttribute('aria-selected'), tab.getAttribute('tabindex')]), [
        ['Devices (12)', 'tab', 'true', '0'],
        ['Group Objects (0)', 'tab', 'false', '-1'],
        ['Functions', 'tab', 'false', '-1'],
    ]);
    tabs()[1].click();
    assert.deepEqual(picked, ['objects']);
    assert.deepEqual(tabs().map((tab) => tab.classList.contains('current')), [false, true, false]);
    tabs()[1].focus();
    assert.equal(tabs()[1].fire('keydown', { key: 'ArrowRight' }).defaultPrevented, true);
    assert.deepEqual(picked, ['objects', 'functions']);
    assert.ok(document.activeElement === tabs()[2], 'the focus follows to the tab that is shown');
    tabs()[2].fire('keydown', { key: 'ArrowRight' });
    assert.deepEqual(picked, ['objects', 'functions', 'devices']);
    panel.setTabs([], '');
    assert.equal(row.children.length, 0);
    panel.close();
});

test('panel: two panels in one document keep their own tree width, tabs, path and tree state', () => {
    const document = createDocument();
    const first = createPanel({ document, title: 'Buildings', current: 'buildings' });
    const second = createPanel({ document, title: 'Group Addresses', current: 'group-addresses' });
    first.setTabs([{ key: 'devices', label: 'Devices', count: 1 }], 'devices');
    second.setTabs([{ key: 'associations', label: 'Associations', count: 4 }], 'associations');
    first.setCrumbs([{ key: '', label: 'Buildings' }]);
    second.setCrumbs([{ key: '', label: 'Group Addresses' }, { key: 'ga:31/0/1', label: '31/0/1' }]);
    first.node.querySelector('.tree-toggle').click();
    assert.equal(first.node.classList.contains('tree-open'), true);
    assert.equal(second.node.classList.contains('tree-open'), false);
    assert.deepEqual(first.node.querySelector('.list-tabs').querySelectorAll('button').map((tab) => tab.textContent), ['Devices (1)']);
    assert.deepEqual(second.node.querySelector('.list-tabs').querySelectorAll('button').map((tab) => tab.textContent), ['Associations (4)']);
    assert.equal(second.node.querySelector('.crumbs').querySelectorAll('button').length, 2);
    first.node.querySelector('.splitter').fire('keydown', { key: 'ArrowRight' });
    assert.equal(first.node.style.getPropertyValue('--tree-width'), '296px');
    assert.equal(second.node.style.getPropertyValue('--tree-width'), '280px');
    assert.ok(first.list !== second.list && first.tools !== second.tools);
    first.close();
    second.close();
    assert.equal(document.listenerCount(), 0);
});

test('sidebar: four containers under each other, Properties, openHAB, Live and Find, each opened and closed by its heading', () => {
    const kept = store();
    const { element } = workplace(createLayout(kept));
    const sidebar = element.querySelector('.sidebar');
    const containers = sidebar.querySelectorAll('section').filter((section) => section.classList.contains('container'));
    assert.deepEqual([...CONTAINERS].map(([name]) => name), ['properties', 'openhab', 'live', 'find']);
    assert.deepEqual(containers.map((box) => box.querySelector('h3').textContent), ['Properties', 'openHAB', 'Live', 'Find']);
    const heads = containers.map((box) => box.querySelector('.container-head'));
    assert.ok(heads.every((head) => head.tagName === 'BUTTON' && head.querySelector('.icon')));
    assert.deepEqual(heads.map((head) => head.getAttribute('aria-expanded')), ['true', 'true', 'false', 'false'], 'Properties and openHAB are open at first');
    assert.deepEqual(containers.map((box) => box.classList.contains('closed')), [false, false, true, true]);
    assert.ok(containers[0].querySelector('.container-body').querySelector('.properties'));
    assert.ok(containers[1].querySelector('.container-body').querySelector('.openhab'));
    assert.ok(containers[2].querySelector('.container-body').querySelector('.live'));

    heads[2].click();
    heads[0].querySelector('.label').fire('click');
    assert.deepEqual(heads.map((head) => head.getAttribute('aria-expanded')), ['false', 'true', 'true', 'false'], 'several can be open');
    assert.deepEqual(JSON.parse(kept.kept.text), { 'sidebar.live': true, 'sidebar.properties': false });

    const again = workplace(createLayout(kept)).element.querySelector('.sidebar');
    assert.deepEqual(again.querySelectorAll('.container-head').map((head) => head.getAttribute('aria-expanded')), ['false', 'true', 'true', 'false'], 'kept for the viewer');
    assert.deepEqual(workplace(undefined).element.querySelector('.sidebar').querySelectorAll('.container-head').map((head) => head.getAttribute('aria-expanded')), ['true', 'true', 'false', 'false']);
});

test('sidebar: it closes to a strip of the headings\' drawings and opens again, and the choice is kept; a drawing of the strip opens its container', () => {
    const kept = store();
    const { element } = workplace(createLayout(kept));
    const box = element.querySelector('.workplace');
    const sidebar = element.querySelector('.sidebar');
    const toggle = element.querySelector('.sidebar-toggle');
    assert.deepEqual([toggle.getAttribute('aria-expanded'), toggle.getAttribute('aria-label')], ['true', 'Close the sidebar']);
    toggle.click();
    assert.ok(sidebar.classList.contains('closed'));
    assert.ok(box.classList.contains('sidebar-closed'));
    assert.deepEqual([toggle.getAttribute('aria-expanded'), toggle.getAttribute('aria-label')], ['false', 'Open the sidebar']);
    assert.equal(JSON.parse(kept.kept.text).propertiesOpen, false);
    assert.equal(sidebar.querySelectorAll('.container-head').length, 4, 'the headings stay, the style sheet leaves their drawings');
    const next = workplace(createLayout(kept)).element;
    assert.ok(next.querySelector('.sidebar').classList.contains('closed'));
    assert.ok(next.querySelector('.workplace').classList.contains('sidebar-closed'));

    const live = sidebar.querySelectorAll('.container-head')[2];
    live.click();
    assert.equal(sidebar.classList.contains('closed'), false);
    assert.equal(box.classList.contains('sidebar-closed'), false);
    assert.equal(live.getAttribute('aria-expanded'), 'true');
    assert.equal(JSON.parse(kept.kept.text).propertiesOpen, true);
    toggle.click();
    // an open container stays open when its drawing opens the sidebar
    live.click();
    assert.equal(live.getAttribute('aria-expanded'), 'true');
});

test('sidebar: a panel fills its own face; the sidebar shows one face at a time', () => {
    const document = createDocument();
    const sidebar = createSidebar({ document });
    const first = sidebar.face();
    const second = sidebar.face();
    first.properties.show('first', [{ rows: [['Name', 'one']] }]);
    second.properties.show('second', [{ rows: [['Name', 'two']] }]);
    first.openhab.show([['Channel', 'in use']]);
    second.openhab.show([], [document.createElement('a')]);
    assert.equal(sidebar.node.querySelector('.subject').textContent, 'first');
    assert.deepEqual(textNodes(sidebar.node.querySelector('.openhab')), ['Channel', 'in use']);
    sidebar.show(second);
    assert.equal(sidebar.node.querySelector('.subject').textContent, 'second');
    assert.equal(sidebar.node.querySelectorAll('.properties').length, 1);
    assert.equal(sidebar.node.querySelector('.openhab').querySelector('a') !== null, true);
    assert.equal(first.properties.node.querySelector('.subject').textContent, 'first', 'the face that is not shown keeps what it holds');
    second.openhab.show([], []);
    assert.equal(sidebar.node.querySelector('.openhab').textContent, 'Nothing is selected.');
    sidebar.close();
    assert.equal(document.listenerCount(), 0);
});

test('workplace: the tree button slides the tree in and out; a panel without a tree has neither', () => {
    const { element, made } = workplace(undefined);
    const box = element.querySelector('.panel');
    const toggle = element.querySelector('.tree-toggle');
    assert.equal(toggle.getAttribute('aria-label'), 'Show the tree');
    assert.equal(toggle.getAttribute('aria-expanded'), 'false');
    toggle.click();
    assert.ok(box.classList.contains('tree-open'));
    assert.equal(toggle.getAttribute('aria-expanded'), 'true');
    assert.equal(toggle.getAttribute('aria-label'), 'Close the tree');
    assert.equal(made.closeTree(), true);
    assert.equal(box.classList.contains('tree-open'), false);
    assert.equal(toggle.getAttribute('aria-label'), 'Show the tree');
    assert.equal(made.closeTree(), false);

    const flat = workplace(undefined, { tree: false });
    assert.equal(flat.made.treePane, null);
    assert.equal(flat.element.querySelector('.tree-toggle'), null);
    assert.deepEqual(flat.element.querySelector('.panel-body').children.map((child) => child.className), ['pane list-pane']);
    flat.made.close();
    assert.equal(flat.document.listenerCount(), 0);
});

test('workplace: the tree that slides in takes the focus; Escape in it closes it and the focus is on its button', () => {
    const { document, element, made } = workplace(undefined);
    const tree = createTree({ document, label: 'Topology' });
    tree.setRoots([{ key: 'a', name: 'Area 31' }, { key: 'b', name: 'Area 30' }]);
    made.setTree(tree);
    assert.equal(tree.node.parentNode, made.treePane);
    const box = element.querySelector('.panel');
    const toggle = element.querySelector('.tree-toggle');
    toggle.click();
    assert.ok(document.activeElement === tree.node.children[0]);
    assert.equal(made.list.fire('keydown', { key: 'Escape' }).defaultPrevented, false);
    assert.ok(box.classList.contains('tree-open'));
    assert.equal(tree.node.children[0].fire('keydown', { key: 'Escape' }).defaultPrevented, true);
    assert.equal(box.classList.contains('tree-open'), false);
    assert.ok(document.activeElement === toggle);
    toggle.click();
    toggle.click();
    assert.ok(document.activeElement === toggle);
    made.close();
    tree.close();
    assert.equal(document.listenerCount(), 0);
});

test('workplace: the list can take the focus itself', () => {
    const { document, made } = workplace(undefined);
    assert.equal(made.list.getAttribute('tabindex'), '-1');
    made.focusList();
    assert.equal(document.activeElement, made.list);
});

test('properties: set() changes one named value and leaves every other node of the pane where it is', () => {
    const document = createDocument();
    const properties = createProperties(document);
    const button = document.createElement('button');
    properties.show('31/0/1', [
        { tab: 'information', rows: [['Last Value', 'on', 'last'], ['Time', '', 'time'], ['Name', 'Kitchen']] },
        { more: [button] },
    ]);
    button.focus();
    const subject = properties.node.querySelector('.subject');
    assert.equal(properties.set('last', 'off'), true);
    assert.deepEqual(textNodes(properties.node.querySelector('dl')), ['Last Value', 'off', 'Name', 'Kitchen']);
    assert.equal(document.activeElement, button);
    assert.equal(properties.node.querySelector('.subject'), subject);
    const value = document.createElement('span');
    assert.equal(properties.set('last', value), true);
    assert.equal(value.parentNode.tagName, 'DD');
    assert.equal(properties.set('time', '10:00'), false, 'a row that is not shown');
    assert.equal(properties.set('last', ''), false, 'a value that would take the row away');
    assert.equal(properties.set('nothing', 'x'), false);
    properties.empty();
    assert.equal(properties.set('last', 'on'), false);
});

test('splitter: between panes that stand one above the other it moves up and down and sets the height of the lower one', () => {
    const kept = store();
    const { target, made } = splitter(createLayout(kept), { property: '--second-height', name: 'second', initial: 320, min: 120, max: 1600, reverse: true, rows: true, label: 'Height of the second panel' });
    assert.equal(made.node.getAttribute('aria-orientation'), 'horizontal');
    assert.equal(target.style.getPropertyValue('--second-height'), '320px');
    made.node.fire('pointerdown', { clientX: 10, clientY: 500, pointerId: 1 });
    made.node.fire('pointermove', { clientX: 900, clientY: 440 });
    assert.equal(made.width(), 380, 'up makes the lower pane higher; moving sideways does nothing');
    made.node.fire('pointerup', {});
    assert.deepEqual(JSON.parse(kept.kept.text), { second: 380 });
    made.node.fire('keydown', { key: 'ArrowLeft' });
    assert.equal(made.width(), 380);
    made.node.fire('keydown', { key: 'ArrowUp' });
    assert.equal(made.width(), 380 + KEY_STEP);
    made.node.fire('keydown', { key: 'ArrowDown' });
    made.node.fire('keydown', { key: 'ArrowDown' });
    assert.equal(made.width(), 380 - KEY_STEP);
    made.node.fire('keydown', { key: 'Home' });
    assert.equal(made.width(), 120);
});

test('splitter: a pane that the layout gives less than was set moves from the size it really has, so the border has no dead stretch', () => {
    let real = 400;
    const { made } = splitter(createLayout(store('{"second":1500}')), { property: '--second-height', name: 'second', initial: 320, min: 120, max: 1600, reverse: true, rows: true, label: 'Height of the second panel', actual: () => real });
    assert.equal(made.width(), 1500);
    made.node.fire('pointerdown', { clientY: 500, pointerId: 1 });
    made.node.fire('pointermove', { clientY: 510 });
    assert.equal(made.width(), 390, 'ten down from the 400 it has, not from the 1500 that were kept');
    made.node.fire('pointerup', {});
    made.node.fire('keydown', { key: 'End' });
    assert.equal(made.width(), 1600);
    made.node.fire('keydown', { key: 'ArrowDown' });
    assert.equal(made.width(), 400 - KEY_STEP);
    // a pane that has what was set, or a size the browser does not tell, moves from the set one
    real = 0;
    made.node.fire('keydown', { key: 'ArrowUp' });
    assert.equal(made.width(), 400);
    real = 900;
    made.node.fire('keydown', { key: 'ArrowUp' });
    assert.equal(made.width(), 400 + KEY_STEP);
});

test('panel: the Panels menu stands after the actions and before the find box and offers to open or close the second panel', () => {
    const document = createDocument();
    const calls = [];
    const closed = createPanel({ document, title: 'Buildings', second: { open: false, toggle: () => calls.push('toggle') } });
    assert.deepEqual(closed.bar.children.map((child) => child.className), ['tool nav-back', 'tool nav-forward', 'panel-chooser', 'tool tree-toggle', 'crumbs', 'tools', 'menu-box', 'find-slot']);
    assert.ok(closed.tools === closed.bar.children[5] && closed.find === closed.bar.children[7]);
    const box = closed.bar.querySelector('.menu-box');
    const button = box.querySelector('.panels-menu');
    assert.equal(button.textContent, 'Panels');
    assert.equal(button.getAttribute('aria-expanded'), 'false');
    assert.equal(box.classList.contains('open'), false);
    button.click();
    assert.equal(box.classList.contains('open'), true);
    assert.equal(button.getAttribute('aria-expanded'), 'true');
    const item = box.querySelector('.menu').querySelector('button');
    assert.equal(item.getAttribute('role'), 'menuitem');
    assert.equal(item.textContent, 'Open second panel');
    assert.equal(document.activeElement, item);
    item.fire('keydown', { key: 'Escape' });
    assert.equal(box.classList.contains('open'), false);
    assert.equal(document.activeElement, button);
    assert.deepEqual(calls, []);
    button.click();
    // a click anywhere else in the panel closes the menu
    closed.list.fire('click');
    assert.equal(box.classList.contains('open'), false);
    button.click();
    item.click();
    assert.deepEqual(calls, ['toggle']);
    assert.equal(box.classList.contains('open'), false);

    // also a click outside the panel: in the sidebar, the other half, the top strip
    const page = document.createElement('main');
    const beside = document.createElement('aside');
    document.appendChild(page);
    page.appendChild(closed.node);
    page.appendChild(beside);
    const quiet = document.ownListenerCount();
    button.click();
    assert.equal(box.classList.contains('open'), true);
    beside.fire('click');
    assert.equal(box.classList.contains('open'), false);
    assert.equal(document.ownListenerCount(), quiet, 'the panel listens on the page only while its menu is open');
    button.click();
    closed.close();
    assert.equal(document.ownListenerCount(), quiet);

    const open = createPanel({ document, title: 'Buildings', second: { open: true, toggle() {} } });
    assert.equal(open.bar.querySelector('.menu').textContent, 'Close second panel');
    assert.equal(createPanel({ document, title: 'Buildings' }).bar.querySelector('.menu-box'), null, 'no menu where there can be no second panel');
    closed.close();
    open.close();
});

test('workplace: a panel of two shares the sidebar it is handed: no sidebar of its own, its face given back when it closes', () => {
    const document = createDocument();
    const outer = document.createElement('section');
    const side = createSide({ document, target: outer });
    const released = [];
    const split = (half) => ({ half, open: true, toggle() {}, sidebar: { face: () => side.sidebar.face(), release: (face) => (released.push(half), side.sidebar.release(face)) } });
    const bridge = { uid: 'knx:ip:line7', present: true };
    const route = { view: 'buildings', bridge: bridge.uid, query: {} };
    const elements = [document.createElement('section'), document.createElement('section')];
    const [first, second] = ['first', 'second'].map((half, at) => createWorkplace({ document, element: elements[at], title: 'Buildings', context: { bridge, route, split: split(half), navigate() {} } }));
    for (const element of elements) {
        assert.deepEqual(element.querySelector('.workplace').children.map((child) => child.className), ['panel']);
        assert.equal(element.querySelector('.menu').textContent, 'Close second panel');
    }
    assert.equal(first.sidebar, null);
    // the lower panel's chooser has no Project: it cannot stand in a half
    const choices = (element) => element.querySelector('.panel-chooser').children.map((option) => option.textContent);
    assert.deepEqual(choices(elements[0]), ['Group Addresses', 'Topology', 'Buildings', 'Group Monitor', 'Project']);
    assert.deepEqual(choices(elements[1]), ['Group Addresses', 'Topology', 'Buildings', 'Group Monitor']);
    first.properties.show('upper', [{ rows: [['Name', 'one']] }]);
    second.properties.show('lower', [{ rows: [['Name', 'two']] }]);
    assert.equal(side.sidebar.node.querySelector('.subject').textContent, 'upper');
    assert.equal(side.sidebar.node.querySelectorAll('.properties').length, 1);
    second.close();
    first.close();
    assert.deepEqual(released, ['second', 'first']);
    // a single panel has no menu without a toggle
    const alone = document.createElement('section');
    const single = createWorkplace({ document, element: alone, title: 'Buildings', context: { bridge, route, split: { half: 'first', open: false, toggle: null, sidebar: null }, navigate() {} } });
    assert.equal(alone.querySelector('.menu-box'), null);
    assert.deepEqual(alone.querySelector('.workplace').children.map((child) => child.className), ['panel', 'splitter properties-splitter', 'pane sidebar']);
    single.close();
    side.close();
    assert.equal(document.listenerCount(), 0);
});

test('sidebar: other containers than the usual four can be given; each opens, closes and is kept under its own name', () => {
    const document = createDocument();
    const kept = store();
    const layout = createLayout(kept);
    const sidebar = createSidebar({
        document,
        layout,
        containers: [
            ['edit', 'pencil', 'wizard.edit', true],
            ['checks', 'check', 'wizard.checks', false],
        ],
    });
    assert.deepEqual(
        sidebar.node.querySelectorAll('.container').map((box) => [box.querySelector('.label').textContent, box.classList.contains('closed')]),
        [
            ['Edit', false],
            ['Checks', true],
        ],
    );
    assert.equal(sidebar.node.querySelector('.container-properties'), null);
    sidebar.body('checks').appendChild(document.createElement('p'));
    sidebar.node.querySelectorAll('.container-head')[1].click();
    assert.equal(sidebar.node.querySelector('.container-checks').classList.contains('closed'), false);
    assert.equal(JSON.parse(kept.kept.text)['sidebar.checks'], true);
    sidebar.close();
});
