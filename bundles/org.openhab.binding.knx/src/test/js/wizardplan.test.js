// The wizard's plan: the tree, the tabs of the list, the checks of call 22 and the summary bar.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { KnxApiError } from '../web/js/api.js';
import { CHECK_WAIT, show } from '../web/js/views/wizard.js';
import { settle } from './fakes.js';
import { viewContext } from './fakeview.js';
import { BASE, BRIDGE, badPreview, bodyRows, cellOf, checked, click, createButton, editCell, editorOf, fakeApi, fakeAuth, fakeCore, goodPreview, item, lastPreview, linesPreview, only, open, part, pick, proposal, proposed, row, steps, tab, treeItems, treeLine, treeNode, type } from './wizardfakes.js';

test('wizard: the tree is the openHAB side: bridge, Things, channels, Items and the location groups nested as the spaces, each with its state', async () => {
    const offered = proposal();
    offered.things[0].channels[0].selected = false;
    offered.things[0].channels.push({ ...offered.things[0].channels[0], id: 'Old', label: 'Old light', item: item('Kitchen_Old', 'Switch'), alreadyConfigured: ['knx:device:made-up:Old:sw'] });
    const opened = await proposed({ api: fakeApi({ proposed: offered }) });
    const { root } = opened;
    assert.deepEqual(treeItems(root).map(treeLine), [
        [1, 'Line 7', 'knx:ip:made-up', 'already there, will be left alone', false],
        [2, '<i>x</i> kitchen', 'Kitchen', 'has a warning', false],
        [3, 'Light', 'switch', 'left out', true],
        [4, 'Kitchen_Light', 'Switch', 'left out', true],
        [3, 'Dimmer', 'dimmer', 'has a warning', false],
        [4, 'Kitchen_Dimmer', 'Dimmer', 'has a warning', false],
        [3, 'Old light', 'switch', 'already there, will be left alone', true],
        [4, 'Kitchen_Old', 'Switch', 'already there, will be left alone', true],
        [1, 'Locations', '', 'new', false],
        [2, 'House', 'gHouse', 'new', false],
        [3, 'Kitchen', 'gKitchen', 'new', false],
        [4, 'Kitchen_Dimmer', 'Dimmer', 'has a warning', false],
    ]);
    // each space has the drawing of its kind, with the kind's word
    assert.equal(only(treeItems(root)[9].querySelector('.node'), '.kind-icon').getAttribute('title'), 'Building');
    assert.ok(treeItems(root)[9].querySelector('.node').querySelector('.icon-building'));
    assert.equal(only(treeItems(root)[10].querySelector('.node'), '.kind-icon').getAttribute('title'), 'Room');
    assert.ok(treeItems(root)[10].querySelector('.node').querySelector('.icon-room'));
    assert.ok(treeItems(root)[1].querySelector('.icon-thing'));
    assert.ok(treeItems(root)[2].querySelector('.icon-channel'));
    assert.ok(treeItems(root)[3].querySelector('.icon-item'));

    // the room unticked: it stays in the tree, greyed, and its Item stands under the house
    await click(tab(root, 'Locations'));
    await click(only(bodyRows(root)[1], '.location-group'));
    await checked(opened);
    assert.deepEqual(
        treeItems(root).map(treeLine).slice(8),
        [
            [1, 'Locations', '', 'new', false],
            [2, 'House', 'gHouse', 'new', false],
            [3, 'Kitchen', 'gKitchen', 'left out', true],
            [3, 'Kitchen_Dimmer', 'Dimmer', 'has a warning', false],
        ],
    );
    await click(only(root, '.panel').querySelectorAll('.location-groups')[0]);
    await checked(opened);
    assert.deepEqual(
        treeItems(root).map(treeLine).slice(8),
        [
            [1, 'Locations', '', 'left out', true],
            [2, 'House', 'gHouse', 'left out', true],
            [3, 'Kitchen', 'gKitchen', 'left out', true],
        ],
    );
});

test('wizard: a group openHAB has already is "already there" in the tree and the list, and the sidebar says it is used as it is', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [{ ...goodPreview(), existingGroups: ['gHouse'] }] }) });
    const { root } = opened;
    assert.deepEqual(treeItems(root).map(treeLine).slice(7, 9), [
        [2, 'House', 'gHouse', 'already there, will be left alone', true],
        [3, 'Kitchen', 'gKitchen', 'new', false],
    ]);
    await click(tab(root, 'Locations'));
    assert.equal(only(bodyRows(root)[0], '.badge').getAttribute('title'), 'already there, will be left alone');
    await click(bodyRows(root)[0]);
    assert.equal(only(only(root, '.plan-edit'), '.location-exists').textContent, 'openHAB has a group of this name already: it is used as it is and not changed.');
    await click(bodyRows(root)[1]);
    assert.ok(only(root, '.plan-edit').querySelector('.location-exists') === null);
});

test('wizard: an error of call 22 marks its node and its row, and the plan has no "new" for it', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [badPreview()] }) });
    assert.deepEqual(treeItems(opened.root).map(treeLine).slice(1, 4), [
        [2, '<i>x</i> kitchen', 'Kitchen', 'has an error', false],
        [3, 'Light', 'switch', 'has an error', false],
        [4, 'Kitchen_Light', 'Switch', 'has an error', false],
    ]);
    assert.match(only(row(opened.root, 'Kitchen_Light'), '.badge').getAttribute('title'), /^has an error: /);
});

test('wizard: each tab has its rows and columns, with the count in the tab', async () => {
    const opened = await proposed();
    const { root } = opened;
    const headings = () => root.querySelector('thead').querySelectorAll('th').map((cell) => cell.textContent);
    assert.deepEqual(
        root.querySelectorAll('.list-tab').map((node) => node.textContent),
        ['Channels (2)', 'Items (2)', 'Things (1)', 'Locations (2)', 'Read at start (2)', 'Text files'],
    );
    assert.deepEqual(headings(), ['State', 'Problems', 'Add', 'Channel', 'Type', 'Address', 'Read', 'Status address', 'Read status', 'Item name', 'Item type', 'Paired', 'Notes']);
    // under the root the channels stand under a heading row per Thing; a channel of two parts has a row per part
    assert.equal(only(root, '.group-row').textContent, '<i>x</i> kitchen Kitchen3');
    const light = row(root, 'Kitchen_Light');
    assert.deepEqual(
        light.querySelectorAll('td').map((cell) => cell.textContent),
        ['', '', '', 'Light', 'switch', '31/7/1Name of 31/7/11.001', '', '31/7/2 Kitchen light status', '', 'Kitchen_Light', 'Switch', "from the device's objects", ''],
    );
    assert.equal(only(light, '.channel-pick').checked, true);
    assert.equal(only(light, '.part-read').checked, false);
    assert.equal(only(light, '.part-status-read').checked, true);
    const position = row(root, '31/7/6');
    assert.ok(position.classList.contains('part-row'));
    assert.equal(only(position, '.col-address').textContent, 'position31/7/6Name of 31/7/65.001');
    assert.equal(only(position, '.col-channel-label').textContent, '');

    await click(tab(root, 'Items'));
    assert.deepEqual(headings(), ['State', 'Problems', 'Add', 'Name', 'Label', 'Type', 'Category', 'Tags', 'Groups', 'Channel']);
    assert.deepEqual(
        row(root, 'Kitchen_Dimmer')
            .querySelectorAll('td')
            .map((cell) => cell.textContent),
        ['', 'A channel of this bridge already uses the address.31/7/5 is used by another channel of this bridge.', '', 'Kitchen_Dimmer', 'Kitchen_Dimmer', 'Dimmer', '', '', 'gKitchen', '<i>x</i> kitchen › Dimmer'],
    );

    await click(tab(root, 'Things'));
    assert.deepEqual(headings(), ['State', 'Problems', 'Label', 'Id', 'Channels', 'Room', 'Location group']);
    assert.deepEqual(
        row(root, 'Kitchen')
            .querySelectorAll('td')
            .map((cell) => cell.textContent),
        ['', 'A channel of this bridge already uses the address.31/7/5 is used by another channel of this bridge.', '<i>x</i> kitchen', 'Kitchen', '2 of 2', 'Kitchen', 'gKitchen'],
    );

    await click(tab(root, 'Locations'));
    assert.deepEqual(headings(), ['State', 'Add', 'Space', 'Group name', 'Label', 'Location tag', 'In group', 'Items']);
    assert.deepEqual(
        bodyRows(root).map((line) => line.querySelectorAll('td').map((cell) => cell.textContent)),
        [
            ['', '', ' House', 'gHouse', 'House', 'Building', '', '0'],
            ['', '', ' Kitchen', 'gKitchen', 'Kitchen', 'Room', 'gHouse', '2'],
        ],
    );
    assert.equal(only(bodyRows(root)[0], '.col-space').querySelector('.kind-icon').getAttribute('title'), 'Building');
    assert.deepEqual(
        bodyRows(root).map((line) => only(line, '.location-group').checked),
        [true, true],
    );

    await click(tab(root, 'Read at start'));
    assert.deepEqual(headings(), ['Address', 'Name', 'Marked by', 'Answered by']);
    assert.deepEqual(
        bodyRows(root).map((line) => line.querySelectorAll('td').map((cell) => cell.textContent)),
        [
            ['31/7/2', 'Kitchen light status', '<i>x</i> kitchen › Light', '–'],
            ['31/7/6', 'Name of 31/7/6', '<i>x</i> kitchen › Dimmer', '–'],
        ],
    );
    assert.equal(only(root, '.reads').textContent, 'openHAB will send 3 read telegrams, to: 31/7/2, 31/7/6');

    await click(tab(root, 'Text files'));
    assert.equal(root.querySelectorAll('textarea')[0].value, goodPreview().thingsText);
    assert.equal(root.querySelectorAll('textarea')[1].value, goodPreview().itemsText);
});

test('wizard: "Read at start" names the device that answers when the project\'s objects are at hand', async () => {
    const rows = {
        deviceDetails: async () => ({
            devices: [
                {
                    address: '31.1.4',
                    name: 'Made-up actuator',
                    groupObjects: [
                        { sendingGroupAddress: '31/7/2', read: true, communication: true },
                        { sendingGroupAddress: '31/7/6', read: false, communication: true },
                    ],
                },
            ],
            problems: [],
        }),
    };
    const { root } = await proposed({ rows });
    await click(tab(root, 'Read at start'));
    assert.deepEqual(
        bodyRows(root).map((line) => only(line, '.col-devices').textContent),
        ['31.1.4 Made-up actuator', ''],
    );
});

test('wizard: a Thing selected in the tree shows its channels without a heading row; a channel selects its row', async () => {
    const { root } = await proposed();
    await click(treeNode(root, 'kitchen'));
    assert.equal(root.querySelectorAll('.group-row').length, 0);
    assert.equal(bodyRows(root).length, 3);
    assert.equal(only(root, '.crumbs').textContent, 'Line 7<i>x</i> kitchen');
    await click(treeNode(root, 'Dimmerdimmer'));
    assert.ok(row(root, '31/7/5').classList.contains('selected'));
    assert.equal(only(only(root, '.plan-edit'), '.channel-label').value, 'Dimmer');
});

test('wizard: choosing another status address changes the request of call 22, in the sidebar and in the cell', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    await click(row(root, 'Kitchen_Light'));
    const form = only(root, '.plan-edit');
    const status = () => only(form, '.part-status');
    assert.equal(status().value, '31/7/2');
    assert.deepEqual(
        status().children.map((option) => option.getAttribute('value')),
        ['31/7/2', '31/7/4', ''],
    );
    assert.equal(only(form, '.pairing').textContent, "from the device's objects");
    type(status(), '31/7/4');
    assert.equal(only(form, '.pairing').textContent, 'chosen here');
    assert.equal(cellOf(root, 'Kitchen_Light', 'from').textContent, 'chosen here');
    await checked(opened);
    type(status(), '');
    assert.equal(only(form, '.pairing').textContent, 'not paired');
    assert.equal(only(form, '.part-status-read').disabled, true);
    assert.equal(only(row(root, 'Kitchen_Light'), '.part-status-read').disabled, true);
    await checked(opened);
    const [, first, second] = api.requests('preview').map((request) => request.things[0].channels[0].parts[0]);
    assert.equal(first.statusAddress, '31/7/4');
    assert.equal(first.statusRead, true);
    assert.equal(second.statusAddress, null);
    assert.equal(second.statusRead, false);

    const editor = editorOf(root, 'Kitchen_Light', 'part-status');
    assert.deepEqual(
        editor.children.map((option) => option.getAttribute('value')),
        ['31/7/2', '31/7/4', ''],
    );
    editor.value = '31/7/4';
    editor.fire('change');
    editor.fire('keydown', { key: 'Enter' });
    await checked(opened);
    assert.equal(lastPreview(api).things[0].channels[0].parts[0].statusAddress, '31/7/4');
    assert.ok(root.querySelector('.cell-editor') === null);
});

test('wizard: the words of a problem stand at its row: with the state drawing and in a Problems column, which is there only while a row has a problem', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [badPreview(), { ...goodPreview(), problems: [] }] }) });
    const { root } = opened;
    const headings = () => root.querySelector('thead').querySelectorAll('th').map((cell) => cell.textContent);
    const NAME = 'The Item name must start with a letter or _, hold only letters, digits and _, and not be true or false.';
    const STATUS = 'This is a status address only; openHAB may still send to it.';
    assert.deepEqual(headings().slice(0, 3), ['State', 'Problems', 'Add']);
    const light = row(root, 'Kitchen_Light');
    assert.equal(only(light, '.badge').getAttribute('title'), `has an error: ${NAME} · ${STATUS}`);
    assert.equal(only(light, '.badge').getAttribute('aria-label'), `has an error: ${NAME} · ${STATUS}`);
    assert.deepEqual(
        only(light, '.col-problems').querySelectorAll('.problem').map((line) => [line.className, line.textContent]),
        [
            ['problem error', `${NAME}A made-up error.`],
            ['problem warning', `${STATUS}A made-up warning.`],
        ],
    );
    assert.equal(only(row(root, 'Kitchen_Dimmer'), '.col-problems').textContent, '');
    assert.equal(only(row(root, 'Kitchen_Dimmer'), '.badge').getAttribute('title'), 'new');
    // the find box finds a row by the words of its problem
    await click(tab(root, 'Items'));
    assert.equal(only(row(root, 'Kitchen_Light'), '.col-problems').querySelectorAll('.problem').length, 2);
    // a Thing's row says what is wrong with its channels and with its room group
    await click(tab(root, 'Things'));
    assert.deepEqual(
        only(row(root, 'Kitchen'), '.col-problems').querySelectorAll('.problem').map((line) => line.textContent),
        [`${NAME}A made-up error.`, `${STATUS}A made-up warning.`],
    );
    await click(only(root, '.recheck'));
    assert.deepEqual(headings().slice(0, 3), ['State', 'Label', 'Id'], 'no problem, no column');
    await click(tab(root, 'Channels'));
    assert.deepEqual(headings().slice(0, 3), ['State', 'Add', 'Channel']);
    assert.equal(only(row(root, 'Kitchen_Light'), '.badge').getAttribute('title'), 'new');
});

test('wizard: another channel type takes the first Item type of its list, so the check has no error', async () => {
    const offered = proposal();
    offered.things[0].channels.push({
        id: 'Mode',
        channelType: 'number',
        allowedChannelTypes: ['number', 'string'],
        label: 'Heating mode',
        selected: true,
        statusOnly: false,
        combined: false,
        parts: [part('ga', '20.102', '31/7/7')],
        item: item('Kitchen_Mode', 'Number', { allowedItemTypes: { number: ['Number'], string: ['String'] } }),
        alreadyConfigured: [],
        notes: [],
    });
    // as call 22 checks it: the Item type must be one of the channel type's
    const fits = { switch: ['Switch'], dimmer: ['Dimmer'], number: ['Number'], string: ['String'] };
    const api = fakeApi({ proposed: offered });
    api.preview = async (bridge, request) => {
        api.calls.push(['preview', bridge, JSON.parse(JSON.stringify(request))]);
        const bad = request.things[0].channels.findIndex((channel) => !fits[channel.channelType].includes(channel.item.type));
        if (bad < 0) {
            return goodPreview();
        }
        return {
            problems: [{ severity: 'ERROR', code: 'type-not-allowed', where: `things/0/channels/${bad}/item/type`, text: 'A made-up error.' }],
            readAddresses: [],
            readTelegrams: 0,
        };
    };
    const opened = await proposed({ api });
    const { root } = opened;
    assert.ok(cellOf(root, 'Kitchen_Mode', 'item-type').querySelector('.can-edit') === null);

    editCell(root, 'Kitchen_Mode', 'channel-type', 'string');
    assert.equal(cellOf(root, 'Kitchen_Mode', 'item-type').textContent, 'String');
    await checked(opened);
    const mode = lastPreview(api).things[0].channels[2];
    assert.equal(mode.channelType, 'string');
    assert.equal(mode.item.type, 'String');
    assert.equal(createButton(root).disabled, false);

    await click(row(root, 'Kitchen_Mode'));
    type(only(only(root, '.plan-edit'), '.channel-type'), 'number');
    assert.equal(cellOf(root, 'Kitchen_Mode', 'item-type').textContent, 'Number');
    await checked(opened);
    assert.equal(lastPreview(api).things[0].channels[2].item.type, 'Number');
    assert.equal(createButton(root).disabled, false);
});

test('wizard: "Split" puts the split channels in the place of the combined one', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    assert.ok(row(root, 'Kitchen_Light').querySelector('.split') === null);
    await click(only(root, '.split'));
    assert.equal(bodyRows(root).filter((line) => !line.classList.contains('group-row')).length, 3);
    assert.ok(root.querySelector('.split') === null);
    assert.deepEqual(
        bodyRows(root)
            .filter((line) => !line.classList.contains('group-row'))
            .map((line) => only(line, '.col-item-name').textContent),
        ['Kitchen_Light', 'Kitchen_Dimmer_switch', 'Kitchen_Dimmer_position'],
    );
    await checked(opened);
    const request = lastPreview(api);
    assert.deepEqual(
        request.things[0].channels.map((channel) => channel.id),
        ['Light', 'Dimmer_switch', 'Dimmer_position'],
    );
    assert.deepEqual(
        request.things[0].channels.slice(1),
        proposal().things[0].channels[1].split.map((channel) => ({ ...channel, item: { ...channel.item, groups: ['gKitchen'] } })),
    );
});

test('wizard: "Read values at start" off sends readAtStart false and holds the read ticks', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const readSwitch = only(root, '.read-at-start');
    assert.equal(readSwitch.checked, true);
    assert.ok(root.querySelectorAll('.part-read').every((box) => !box.disabled));
    await click(readSwitch);
    assert.ok(root.querySelectorAll('.part-read').every((box) => box.disabled));
    await checked(opened);
    assert.equal(lastPreview(api).readAtStart, false);
});

test('wizard: every change disables "Create" until call 22 has answered for it; quick changes make one call', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    assert.equal(createButton(root).disabled, false);
    editCell(root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    assert.equal(createButton(root).disabled, true);
    assert.equal(only(root, '.check-state').textContent, 'Checking…');
    await settle();
    assert.equal(api.requests('preview').length, 1);
    editCell(root, 'Kitchen_Ceiling', 'channel-label', 'Ceiling');
    await opened.h.clock.advance(CHECK_WAIT - 1);
    assert.equal(api.requests('preview').length, 1);
    await checked(opened);
    assert.equal(api.requests('preview').length, 2);
    assert.equal(createButton(root).disabled, false);
    assert.equal(only(root, '.check-state').textContent, '');
    assert.equal(lastPreview(api).things[0].channels[0].item.name, 'Kitchen_Ceiling');
    assert.equal(lastPreview(api).things[0].channels[0].label, 'Ceiling');
    await click(row(root, 'Kitchen_Ceiling'));
    const form = only(root, '.plan-edit');
    for (const edit of [
        () => type(only(form, '.channel-label'), 'Kitchen 2'),
        () => only(form, '.channel-pick').click(),
        () => only(form, '.part-read').click(),
        () => type(only(form, '.part-status'), '31/7/4'),
        () => type(only(form, '.item-tags'), 'Light'),
        () => only(row(root, 'Kitchen_Ceiling'), '.part-read').click(),
        () => only(root, '.read-at-start').click(),
        () => only(root, '.undo-edit').click(),
        () => only(root, '.reset-plan').click(),
    ]) {
        await checked(opened);
        assert.equal(createButton(root).disabled, false, edit.toString());
        edit();
        assert.equal(createButton(root).disabled, true, edit.toString());
    }
});

test('wizard: an answer of call 22 that comes after a later change does not enable "Create"', async () => {
    const api = fakeApi();
    const waiting = [];
    api.preview = (bridge, request) => {
        api.calls.push(['preview', bridge, JSON.parse(JSON.stringify(request))]);
        return new Promise((resolve) => waiting.push(() => resolve(goodPreview())));
    };
    const opened = await proposed({ api });
    const { root } = opened;
    assert.equal(createButton(root).disabled, true);
    editCell(root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    waiting.shift()();
    await settle();
    assert.equal(createButton(root).disabled, true);
    assert.equal(only(root, '.check-state').textContent, 'Checking…');
    await checked(opened);
    waiting.shift()();
    await settle();
    assert.equal(createButton(root).disabled, false);
});

test('wizard: an ERROR disables "Create"; Checks lists errors first, and a click on one selects its row', async () => {
    // call 22 leaves the steps out with an ERROR; the page must not rely on that
    const withSteps = { ...badPreview(), steps: steps(), counts: goodPreview().counts };
    const opened = await proposed({ api: fakeApi({ previews: [withSteps] }) });
    const { root, fetch } = opened;
    assert.equal(createButton(root).disabled, true);
    createButton(root).fire('click');
    await settle();
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.equal(fetch.calls.length, 0);
    const list = only(only(root, '.plan-checks'), '.problems');
    assert.deepEqual(
        list.children.map((entry) => entry.className),
        ['error', 'error', 'warning'],
    );
    assert.ok(list.children[0].textContent.startsWith('Error The Item name must start'));
    assert.equal(only(root, '.problem-counts').textContent, '2 errors · 1 warnings');
    assert.match(only(root, '.check-state').textContent, /Fix the errors/);
    await click(tab(root, 'Items'));
    await click(only(root, '.plan-checks').querySelectorAll('.check')[0]);
    assert.equal(tab(root, 'Channels').getAttribute('aria-selected'), 'true');
    assert.ok(row(root, 'Kitchen_Light').classList.contains('selected'));
    assert.equal(root.querySelectorAll('.group-row').length, 0);
});

test('wizard: a location group error stands at its space in the tree and in the Locations list, and its line in Checks selects it', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [badPreview()] }) });
    const { root } = opened;
    assert.deepEqual(
        treeItems(root)
            .map(treeLine)
            .filter((line) => line[2] === 'gKitchen' || line[2] === 'gHouse'),
        [
            [2, 'House', 'gHouse', 'new', false],
            [3, 'Kitchen', 'gKitchen', 'has an error', false],
        ],
    );
    const line = only(root, '.plan-checks')
        .querySelectorAll('.check')
        .find((node) => node.textContent.includes('openHAB has an Item of this name already.'));
    await click(line);
    assert.equal(tab(root, 'Locations').getAttribute('aria-selected'), 'true');
    assert.ok(bodyRows(root)[0].classList.contains('selected'), 'the list shows the space the problem stands at, selected');
    assert.equal(only(bodyRows(root)[0], '.col-problems').textContent, 'openHAB has an Item of this name already.gKitchen exists.');
    assert.equal(only(only(root, '.plan-edit'), '.location-name').value, 'gKitchen');
});

test('wizard: the plan says under its notes that central addresses are not added', async () => {
    const { root } = await proposed();
    const notes = only(root, '.plan-notes');
    assert.equal(notes.children[0], only(root, '.no-central'));
    assert.equal(
        notes.children[0].textContent,
        "A channel listens on its own address and on its status address. Central addresses are not added: where a device has no status address, add them by hand in the channel's configuration.",
    );
});

test('wizard: the left-out line gives both counts, and is absent for none or a missing field', async () => {
    const withLeftOut = (leftOut) => {
        const offered = proposal();
        if (leftOut === undefined) {
            delete offered.leftOut;
        } else {
            offered.leftOut = leftOut;
        }
        return proposed({ api: fakeApi({ proposed: offered }) });
    };
    const line = (root) => root.querySelectorAll('.left-out').filter((node) => node.tagName === 'P').map((node) => node.textContent);
    const fn = (n) => ({ source: 'FUNCTION', sourceId: `P-7_F-${n}` });
    const dev = (n) => ({ source: 'DEVICE', sourceId: `P-7_DI-${n}` });
    assert.deepEqual(line((await withLeftOut([])).root), []);
    assert.deepEqual(line((await withLeftOut(undefined)).root), []);
    assert.deepEqual(line((await proposed()).root), ['Got no Thing, no address of their own left: functions 0, devices 1']);
    assert.deepEqual(line((await withLeftOut([dev(1), dev(2)])).root), ['Got no Thing, no address of their own left: functions 0, devices 2']);
    assert.deepEqual(line((await withLeftOut([fn(1)])).root), ['Got no Thing, no address of their own left: functions 1, devices 0']);
    assert.deepEqual(line((await withLeftOut([fn(1), fn(2), fn(3), dev(1), dev(2)])).root), [
        'Got no Thing, no address of their own left: functions 3, devices 2',
    ]);
});

test('wizard: a left-out entry of another source is counted nowhere', async () => {
    const withLeftOut = (leftOut) => proposed({ api: fakeApi({ proposed: { ...proposal(), leftOut } }) });
    const line = (root) => root.querySelectorAll('.left-out').filter((node) => node.tagName === 'P').map((node) => node.textContent);
    const other = { source: 'ADDRESS', sourceId: 'P-7_GA-3' };
    assert.deepEqual(line((await withLeftOut([other, { sourceId: 'P-7_GA-4' }, null])).root), []);
    assert.deepEqual(line((await withLeftOut([other, { source: 'FUNCTION', sourceId: 'P-7_F-1' }, other])).root), [
        'Got no Thing, no address of their own left: functions 1, devices 0',
    ]);
});

test('wizard: the summary\'s numbers are the answer\'s once it is there, and the plan\'s own while a change waits', async () => {
    const offered = proposal();
    offered.things[0].channels.push({ ...offered.things[0].channels[0], id: 'Old', label: 'Old light', selected: false, item: item('Kitchen_Old', 'Switch'), alreadyConfigured: ['knx:device:made-up:Old:sw'] });
    const opened = await proposed({ api: fakeApi({ proposed: offered }) });
    const { root } = opened;
    assert.equal(only(root, '.will').textContent, 'Will make: 1 Things · 2 channels · 2 Items · 1 location groups · 2 links. Leaves alone: 1 already there. Reads at start: 2 group addresses (3 read telegrams).');
    assert.equal(only(root, '.problem-counts').textContent, '0 errors · 1 warnings');
    only(row(root, '31/7/5'), '.channel-pick').click();
    assert.equal(only(root, '.will').textContent, 'Will make: 1 Things · 1 channels · 1 Items · 2 location groups · 1 links. Leaves alone: 2 already there. Reads at start: 2 group addresses (3 read telegrams).');
    only(row(root, 'Kitchen_Light'), '.channel-pick').click();
    assert.ok(only(root, '.will').textContent.startsWith('Will make: 0 Things · 0 channels · 0 Items · 2 location groups · 0 links.'));
    const buttons = only(root, '.plan-summary')
        .querySelectorAll('button')
        .map((node) => node.textContent);
    assert.deepEqual(buttons, ['Back to choose', 'Undo change', 'Redo', 'Reset to the proposal', 'Check again', 'Create in openHAB…']);
});

test('wizard: a check that fails says so, keeps "Create" disabled, and "Check again" asks once more', async () => {
    const api = fakeApi();
    let fail = true;
    const answer = api.preview;
    api.preview = async (bridge, request) => {
        if (fail) {
            api.calls.push(['preview', bridge, request]);
            throw new KnxApiError(500, 'internal', 'x');
        }
        return answer(bridge, request);
    };
    const { root } = await proposed({ api });
    assert.equal(createButton(root).disabled, true);
    assert.equal(only(root, '.check-state').textContent, 'Something went wrong inside the KNX add-on.');
    fail = false;
    await click(only(root, '.recheck'));
    assert.equal(createButton(root).disabled, false);
});

test('wizard: "Copy" copies the .things and the .items text, also after the plan was changed', async () => {
    const api = fakeApi();
    const h = viewContext({ bridge: BRIDGE, api, route: { view: 'wizard', bridge: BRIDGE.uid, query: {} } });
    const copied = [];
    Object.assign(h.context, { fetch: fakeCore(), base: BASE, auth: fakeAuth(), clipboard: { writeText: async (words) => copied.push(words) } });
    show(h.context);
    await settle();
    await click(pick(h.element, 'address', '31/7/3'));
    await click(only(h.element, '.propose'));
    await click(only(h.element, '.split'));
    await click(tab(h.element, 'Text files'));
    const [things, items] = h.element.querySelectorAll('.copy');
    await click(things);
    await click(items);
    assert.deepEqual(copied, [goodPreview().thingsText, goodPreview().itemsText]);
    assert.equal(h.element.querySelector('.copied').textContent, 'Copied');
});

test('wizard: "Check again" without a change disables "Create" until the answer is back', async () => {
    const api = fakeApi();
    const waiting = [];
    api.preview = (bridge, request) => {
        api.calls.push(['preview', bridge, JSON.parse(JSON.stringify(request))]);
        return new Promise((resolve) => waiting.push(() => resolve(goodPreview())));
    };
    const opened = await proposed({ api });
    const { root } = opened;
    waiting.shift()();
    await settle();
    assert.equal(createButton(root).disabled, false);
    await click(only(root, '.recheck'));
    assert.equal(createButton(root).disabled, true);
    createButton(root).click();
    assert.ok(root.querySelector('.confirm-sheet') === null);
    waiting.shift()();
    await settle();
    assert.equal(createButton(root).disabled, false);
});

const LINES_NOTE =
    'openHAB cannot change a Thing that comes from a file, so these lines are for pasting: put the channel lines before the closing brace of the Thing in your .things file. "Create in openHAB…" does not look at this choice and always makes new Things.';

function modeButtons(root) {
    return only(root, '.text-mode').querySelectorAll('button');
}

test('wizard: "Text files" opens on the text for new Things; the choice "Lines for an existing Thing" lists the bridge\'s Things per Thing of the plan and says the lines are for pasting', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [goodPreview(), linesPreview()] }) });
    const { root, api } = opened;
    await click(tab(root, 'Text files'));
    assert.deepEqual(
        modeButtons(root).map((choice) => [choice.textContent, choice.getAttribute('aria-pressed')]),
        [
            ['New Things', 'true'],
            ['Lines for an existing Thing', 'false'],
        ],
    );
    assert.ok(root.querySelector('.lines-targets') === null);
    await click(modeButtons(root)[1]);
    assert.equal(modeButtons(root)[1].getAttribute('aria-pressed'), 'true');
    assert.equal(only(root, '.lines-note').textContent, LINES_NOTE);
    const select = only(root, '.target-thing');
    assert.equal(select.getAttribute('aria-label'), 'Existing Thing for <i>x</i> kitchen');
    assert.deepEqual(
        select.querySelectorAll('option').map((option) => [option.getAttribute('value'), option.textContent]),
        [
            ['', 'no Thing chosen'],
            ['knx:device:made-up:Old', 'Old <b>actuator</b> knx:device:made-up:Old'],
            ['knx:device:made-up:Wall', 'knx:device:made-up:Wall'],
            ['*', 'another Thing id…'],
        ],
    );
    // nothing chosen yet: no text of new Things stands in for the lines
    assert.equal(only(root, '.lines-none').textContent, 'Choose an existing Thing above to get its lines.');
    assert.equal(root.querySelectorAll('textarea').length, 0);
    assert.equal(api.requests('preview').length, 1, 'the choice itself asks nothing');
    assert.equal(Object.hasOwn(lastPreview(api).things[0], 'target'), false);

    type(select, 'knx:device:made-up:Old');
    await checked(opened);
    assert.equal(lastPreview(api).things[0].target, 'knx:device:made-up:Old');
    assert.equal(only(root, '.target-thing').value, 'knx:device:made-up:Old');
    // the text is the answer's, line for line: no Thing line, no Bridge line, no address
    const [things, items] = root.querySelectorAll('textarea');
    assert.equal(things.value, linesPreview().lines.thingsText);
    assert.equal(items.value, linesPreview().lines.itemsText);
    assert.equal(only(root, '.lines-existing').textContent, '2 channel ids exist already in knx:device:made-up:Old: rename them before pasting');
    assert.ok(root.querySelector('.lines-none') === null);
    // back on "New Things" the text is the one for new Things again, and the create steps never changed
    await click(modeButtons(root)[0]);
    assert.equal(root.querySelectorAll('textarea')[0].value, goodPreview().thingsText);
    assert.ok(root.querySelector('.lines-targets') === null);
});

test('wizard: a Thing that is not listed is typed by its id; the typed text goes out as the target and can be taken back', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [goodPreview()] }) });
    const { root, api } = opened;
    await click(tab(root, 'Text files'));
    await click(modeButtons(root)[1]);
    assert.ok(root.querySelector('.target-id') === null);
    type(only(root, '.target-thing'), '*');
    const field = only(root, '.target-id');
    assert.equal(field.getAttribute('aria-label'), 'Thing id or UID');
    type(field, ' cellar_actuator ');
    await checked(opened);
    assert.equal(lastPreview(api).things[0].target, 'cellar_actuator');
    assert.equal(only(root, '.target-thing').value, '*');
    assert.equal(only(root, '.target-id').value, 'cellar_actuator');
    type(only(root, '.target-thing'), '');
    await checked(opened);
    assert.equal(Object.hasOwn(lastPreview(api).things[0], 'target'), false);
    assert.ok(root.querySelector('.target-id') === null);
});

test('wizard: the lines choice changes nothing of what "Create in openHAB…" makes: the same steps run with and without a target', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [goodPreview(), linesPreview()] }) });
    const { root, fetch } = opened;
    await click(tab(root, 'Text files'));
    await click(modeButtons(root)[1]);
    type(only(root, '.target-thing'), 'knx:device:made-up:Old');
    await checked(opened);
    await click(createButton(root));
    await click(only(root, '.confirm-create'));
    const made = fetch.calls.filter((call) => call.method !== 'GET').map((call) => `${call.method} ${decodeURIComponent(call.url.pathname)}`);
    assert.deepEqual(made, ['PUT /oh/rest/items/gKitchen', 'POST /oh/rest/things', 'PUT /oh/rest/items/Kitchen_Light', `PUT /oh/rest/links/Kitchen_Light/${steps()[3].channelUID}`]);
    assert.equal(
        fetch.calls.some((call) => decodeURIComponent(call.url.pathname).includes('made-up:Old')),
        false,
        'the existing Thing is never looked at or written',
    );
});

test('wizard: the text choice is kept while the viewer is in another panel', async () => {
    const keep = new Map();
    const first = await proposed({ keep });
    await click(tab(first.root, 'Text files'));
    await click(modeButtons(first.root)[1]);
    first.view.close();
    const again = await open({ keep });
    assert.equal(modeButtons(again.root)[1].getAttribute('aria-pressed'), 'true');
});
