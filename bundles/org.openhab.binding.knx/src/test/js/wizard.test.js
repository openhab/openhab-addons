// The wizard's first screen, the way through it, and what is kept between visits.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { allElements, textNodes } from './fakedom.js';
import { BRIDGE, DEVICES, HOUSE_GROUPS, LIGHT, THING, bodyRows, cellOf, checked, click, confirmed, createButton, editCell, editorOf, fakeApi, inKitchen, item, lastPreview, only, open, part, pick, proposal, proposed, row, steps, tab } from './wizardfakes.js';

test('wizard: a filter device has the label in the device list and the note stands once under the heading', async () => {
    const fillers = [
        { id: 'P-7_D-5', address: '7.7.5', name: 'Line filler', objects: 2, linkedObjects: 2, filterDevice: true },
        { id: 'P-7_D-6', address: '7.7.6', name: 'Second filler', objects: 1, linkedObjects: 1, filterDevice: true },
    ];
    const { root } = await open({ api: fakeApi({ devices: [...DEVICES, ...fillers] }) });
    const part = root.querySelectorAll('.devices').find((node) => node.tagName === 'DIV');
    assert.deepEqual(
        part.querySelectorAll('li').map((item) => textNodes(item).filter((words) => words.trim() !== '')),
        [
            ['7.7.1', 'Kitchen actuator'],
            ['7.7.2', 'Hall sensor'],
            ['7.7.5', 'Line filler', 'filter device'],
            ['7.7.6', 'Second filler', 'filter device'],
        ],
    );
    assert.equal(part.querySelectorAll('.filter-device').length, 2);
    assert.equal(
        only(root, '.filter-note').textContent,
        "A filter device only fills a line coupler's filter table. It is no hardware and never answers, so its objects give no read mark.",
    );
    assert.equal(part.firstChild, only(root, '.filter-note'));
    assert.equal(pick(root, 'device', 'Line filler').disabled, false);
});

test('wizard: a device list without a filter device has neither the label nor the note', async () => {
    const { root } = await open({ api: fakeApi({ devices: [{ ...DEVICES[0], filterDevice: false }, { ...DEVICES[1], filterDevice: 'true' }] }) });
    assert.equal(root.querySelectorAll('.filter-device').length, 0);
    assert.equal(root.querySelectorAll('.filter-note').length, 0);
    assert.ok(!textNodes(root).includes('filter device'));
});

test('wizard: choose, the plan, the sheet, create, and undo the run', async () => {
    const opened = await open();
    const { root, api, fetch, h } = opened;
    assert.equal(root.querySelector('h2').textContent, 'Add to openHAB');
    assert.deepEqual(api.calls.map((call) => call[0]).sort(), ['allDevices', 'allGroupAddresses', 'locations']);
    assert.equal(only(root, '.propose').disabled, true);
    assert.equal(only(root, '.propose').textContent, 'Show what will be made');
    assert.ok(root.querySelector('.create') === null);

    await click(pick(root, 'device', '7.7.1'));
    await click(pick(root, 'address', '31/7/3'));
    await click(pick(root, 'function', 'Ceiling light'));
    assert.equal(only(root, '.ticked').textContent, 'Ticked: 0 rooms, 1 functions, 1 devices, 1 group addresses');
    await click(pick(root, 'function', 'Ceiling light'));
    await click(only(root, '.propose'));
    assert.deepEqual(api.requests('proposal'), [{ groupAddresses: ['31/7/3'], functions: [], spaces: [], devices: ['P-7_D-1'], readAtStart: true }]);

    // the plan is checked at once, with the proposal as it came
    const [request] = api.requests('preview');
    assert.deepEqual(request.things, inKitchen(proposal().things));
    assert.deepEqual(request.roomGroups, HOUSE_GROUPS);
    assert.equal(request.readAtStart, true);
    assert.ok(root.querySelector('.wizard-choose') === null);
    assert.equal(bodyRows(root).filter((line) => !line.classList.contains('group-row')).length, 3);
    assert.ok(textNodes(root).includes("from the device's objects"));
    assert.ok(textNodes(root).includes('A made-up note of the mapper'));
    assert.ok(textNodes(root).includes('Already used by: knx:device:made-up:Old:dim'));
    assert.ok(textNodes(root).some((words) => words.startsWith('1 KNX Things of this bridge have no handler')));
    assert.ok(only(root, '.plan-checks').textContent.includes('A channel of this bridge already uses the address.'));
    assert.equal(createButton(root).disabled, false);
    assert.equal(fetch.calls.length, 0);

    await click(createButton(root));
    assert.equal(fetch.calls.length, 0);
    await click(only(root, '.confirm-create'));
    assert.equal(h.questions.length, 0);
    const paths = fetch.calls.map((call) => `${call.method} ${decodeURIComponent(call.url.pathname)}`);
    assert.deepEqual(paths.slice(0, 4), ['GET /oh/rest/items/gKitchen', `GET /oh/rest/things/${THING}`, 'GET /oh/rest/items/Kitchen_Light', `GET /oh/rest/links/Kitchen_Light/${LIGHT}`]);
    assert.ok(paths.includes('POST /oh/rest/things'));
    assert.equal(fetch.calls.length, 12);
    assert.deepEqual(fetch.strays, []);
    const states = root.querySelector('.run').querySelectorAll('.state').map((node) => node.textContent);
    assert.deepEqual(states.slice(0, 4), ['created', 'created', 'created', 'created']);
    assert.ok(textNodes(root).includes('Done: 4 created.'));
    assert.equal(only(root, '.progress').textContent, '4 of 4 steps made');
    const undoButton = only(root, '.undo');
    assert.equal(undoButton.disabled, false);

    await click(undoButton);
    assert.equal(h.questions[0], 'Delete from openHAB the 4 Things, Items, links and groups this run created?');
    const deletes = fetch.calls.slice(12).map((call) => `${call.method} ${decodeURIComponent(call.url.pathname)}`);
    assert.deepEqual(deletes, [`DELETE /oh/rest/links/Kitchen_Light/${LIGHT}`, 'DELETE /oh/rest/items/Kitchen_Light', `DELETE /oh/rest/things/${THING}`, 'DELETE /oh/rest/items/gKitchen']);
    assert.ok(textNodes(root).includes('Undone: 4 deleted.'));
    assert.ok(root.querySelector('.undo') === null);
    assert.deepEqual(fetch.strays, []);
    for (const call of fetch.calls) {
        assert.equal(call.headers['X-OPENHAB-TOKEN'], 'made-up-token');
    }
});

test('wizard: server text such as a label <i>x</i> shows as text, never as an element', async () => {
    const { root } = await proposed();
    assert.ok(textNodes(root).includes('<i>x</i> kitchen'));
    await click(tab(root, 'Text files'));
    assert.ok(root.querySelector('textarea').value.includes('<i>x</i>'));
    await click(tab(root, 'Things'));
    assert.equal(editorOf(root, 'Kitchen', 'thing-label').value, '<i>x</i> kitchen');
    assert.deepEqual(
        allElements(root).filter((node) => node.tagName === 'I'),
        [],
    );
});

test('wizard: a preselection from the route ticks its entry', async () => {
    for (const [query, kind, words, field, value] of [
        [{ device: '7.7.1' }, 'device', '7.7.1', 'devices', 'P-7_D-1'],
        [{ device: 'P-7_D-2' }, 'device', '7.7.2', 'devices', 'P-7_D-2'],
        [{ function: 'P-7_F-1' }, 'function', 'Ceiling light', 'functions', 'P-7_F-1'],
        [{ space: 'P-7_B-2' }, 'space', 'Kitchen', 'spaces', 'P-7_B-2'],
    ]) {
        const { root, api } = await open({ query });
        assert.equal(pick(root, kind, words).checked, true, words);
        assert.equal(root.querySelectorAll('input').filter((box) => box.checked).length, 1, words);
        assert.equal(only(root, '.propose').disabled, false);
        await click(only(root, '.propose'));
        assert.deepEqual(api.requests('proposal')[0][field], [value]);
    }
});

test('wizard: the plan under work is there again after a visit to another panel, and is checked again', async () => {
    const keep = new Map();
    const first = await proposed({ keep });
    editCell(first.root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    await click(tab(first.root, 'Items'));
    first.view.close();

    const second = await open({ keep });
    assert.ok(second.root.querySelector('.wizard-choose') === null);
    assert.equal(tab(second.root, 'Items').getAttribute('aria-selected'), 'true');
    assert.equal(cellOf(second.root, 'Kitchen_Ceiling', 'item-name').textContent, 'Kitchen_Ceiling');
    assert.deepEqual(second.api.calls.filter((call) => call[0] === 'proposal'), []);
    assert.equal(lastPreview(second.api).things[0].channels[0].item.name, 'Kitchen_Ceiling');
    assert.equal(createButton(second.root).disabled, false);
    // the edits of before can still be taken back
    await click(only(second.root, '.undo-edit'));
    assert.equal(cellOf(second.root, 'Kitchen_Light', 'item-name').textContent, 'Kitchen_Light');
    await click(only(second.root, '.back-choose'));
    assert.equal(pick(second.root, 'address', '31/7/3').checked, true);
    second.view.close();

    // "Add to openHAB" pressed somewhere else starts with that choice, not with the kept plan
    const other = await open({ keep, query: { device: 'P-7_D-2' } });
    assert.ok(other.root.querySelector('.wizard-choose'));
    assert.equal(pick(other.root, 'address', '31/7/3').checked, false);
    other.view.close();

    // and a plan is kept per bridge
    const again = new Map();
    const kept = await proposed({ keep: again });
    kept.view.close();
    const elsewhere = await open({ keep: again, bridge: { ...BRIDGE, uid: 'knx:ip:made-up-too' } });
    assert.ok(elsewhere.root.querySelector('.wizard-choose'));
    assert.ok(elsewhere.root.querySelector('.to-plan') === null);
});

test('wizard: "Back to choose" keeps the plan; a new proposal over a changed plan asks first', async () => {
    const opened = await proposed();
    const { root, api, h } = opened;
    editCell(root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    await click(only(root, '.back-choose'));
    assert.ok(root.querySelector('.wizard-choose'));
    h.answer = false;
    await click(only(root, '.propose'));
    assert.deepEqual(h.questions, ['Drop the changes of the plan under work and make a new proposal?']);
    assert.equal(api.requests('proposal').length, 1);
    await click(only(root, '.to-plan'));
    assert.equal(cellOf(root, 'Kitchen_Ceiling', 'item-name').textContent, 'Kitchen_Ceiling');
    await click(only(root, '.back-choose'));
    h.answer = true;
    await click(only(root, '.propose'));
    assert.equal(api.requests('proposal').length, 2);
    assert.equal(api.requests('proposal')[1].readAtStart, true);
    assert.equal(cellOf(root, 'Kitchen_Light', 'item-name').textContent, 'Kitchen_Light');
});

test('wizard: the help text names the limits of the step runner, in the run', async () => {
    const opened = await proposed();
    await confirmed(opened);
    const help = only(opened.root, '.help').textContent;
    assert.match(help, /a link someone else makes between the look-up and the create is overwritten/);
    assert.match(help, /"Undo this run" would remove it/);
    assert.match(help, /already overwritten when openHAB answers, and the run cannot bring it back/);
});

test('wizard: a failing proposal shows its error; no project shows the way to Project', async () => {
    const { root } = await proposed({ api: fakeApi({ proposed: new KnxApiError(400, 'bad-input', 'selection: nothing is selected') }) });
    assert.ok(textNodes(root).includes('openHAB could not understand the request.'));
    assert.ok(root.querySelector('.wizard-choose'));
    assert.ok(root.querySelector('.create') === null);
    const api = fakeApi();
    api.locations = async () => {
        throw new KnxApiError(404, 'no-project', 'x');
    };
    const missing = await open({ api });
    assert.ok(textNodes(missing.root).includes('Go to Project'));
    assert.equal(missing.root.querySelectorAll('.pick-address').length, 0);
});

test('wizard: close() removes its listeners, also those of the plan', async () => {
    const { h, view } = await proposed();
    assert.ok(h.document.listenerCount() > 0);
    view.close();
    assert.equal(h.element.ownListenerCount(), 0);
    assert.equal(h.document.listenerCount(), 0);
});

test('wizard: opened for another selection while a plan with changes is kept, the page asks: "yes" goes on with the kept plan, "no" drops it; a plan without changes is dropped unasked', async () => {
    const keep = new Map();
    const first = await proposed({ keep });
    editCell(first.root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    first.view.close();

    const kept = await open({ keep, query: { device: 'P-7_D-2' } });
    assert.deepEqual(kept.h.questions, ['A plan with changes is kept from before, for another selection. OK: go on with that plan. Cancel: drop it and start with what is selected now.']);
    assert.ok(kept.root.querySelector('.wizard-choose') === null);
    assert.equal(cellOf(kept.root, 'Kitchen_Ceiling', 'item-name').textContent, 'Kitchen_Ceiling');
    assert.deepEqual(kept.api.calls.filter((call) => call[0] === 'proposal'), []);
    // the page's address names again what the kept plan was opened for, and the way back to the panel goes there
    assert.deepEqual(kept.h.replacements, [{ view: 'wizard', bridge: BRIDGE.uid, query: {} }]);
    await confirmed(kept);
    await click(only(kept.root, '.back-panel'));
    assert.deepEqual(kept.h.navigations.at(-1), { view: 'group-addresses', bridge: BRIDGE.uid, query: {} }, 'not to the device the wizard was opened for last');
    kept.view.close();

    // it is still the plan of the first selection
    const plain = await open({ keep });
    assert.deepEqual(plain.h.questions, []);
    assert.equal(cellOf(plain.root, 'Kitchen_Ceiling', 'item-name').textContent, 'Kitchen_Ceiling');
    plain.view.close();

    const dropped = await open({ keep, query: { device: 'P-7_D-2' }, answer: false });
    assert.equal(dropped.h.questions.length, 1);
    assert.ok(dropped.root.querySelector('.wizard-choose'));
    assert.ok(dropped.root.querySelector('.to-plan') === null);
    assert.equal(pick(dropped.root, 'device', '7.7.2').checked, true);
    assert.equal(pick(dropped.root, 'address', '31/7/3').checked, false);
    dropped.view.close();

    const after = await open({ keep });
    assert.deepEqual(after.h.questions, []);
    assert.ok(after.root.querySelector('.wizard-choose'));
    assert.ok(after.root.querySelector('.to-plan') === null);
});
