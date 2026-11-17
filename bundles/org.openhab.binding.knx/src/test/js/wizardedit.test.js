// Changing the wizard's plan: in the cells of the list, by keyboard, and in the sidebar.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { HOUSE_GROUPS, LOCATION_TAGS, inKitchen, bodyRows, cellOf, checked, click, editCell, editorOf, fakeApi, item, lastPreview, only, part, pick, proposal, proposed, row, steps, tab, treeItems, treeNode, type } from './wizardfakes.js';

test('wizard: the location groups go out with the request, nested, and the Items get the group of their room; name, label and tag can be changed', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    assert.equal(only(root, '.panel').querySelectorAll('.location-groups')[0].checked, true, 'the switch is on when the project has spaces');
    assert.deepEqual(lastPreview(api).roomGroups, HOUSE_GROUPS);
    assert.deepEqual(
        lastPreview(api).things[0].channels.map((channel) => channel.item.groups),
        [['gKitchen'], ['gKitchen']],
    );
    await click(tab(root, 'Locations'));
    editCell(root, 'Kitchen', 'location-name', 'gCooking');
    await click(treeNode(root, 'gCooking'));
    type(only(only(root, '.plan-edit'), '.location-label'), 'Cooking');
    const tag = only(only(root, '.plan-edit'), '.location-tag');
    assert.deepEqual(
        tag.querySelectorAll('option').map((option) => [option.getAttribute('value'), option.textContent]),
        [...LOCATION_TAGS.map((name) => [name, name]), ['', 'no tag']],
    );
    assert.equal(only(only(root, '.plan-edit'), '.location-parent').textContent, 'in gHouse');
    type(tag, 'Corridor');
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, [HOUSE_GROUPS[0], { groupName: 'gCooking', label: 'Cooking', tag: 'Corridor', category: 'group', parent: 'gHouse' }]);
    assert.deepEqual(lastPreview(api).things[0].channels[0].item.groups, ['gCooking']);
    type(only(only(root, '.plan-edit'), '.location-tag'), '');
    await checked(opened);
    assert.equal(lastPreview(api).roomGroups[1].tag, '');
});

test('wizard: unticking a group puts what is in it into the next ticked group above, or into none', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    await click(tab(root, 'Locations'));
    // the room's group off: its Items go to the house
    await click(only(bodyRows(root)[1], '.location-group'));
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, [HOUSE_GROUPS[0]]);
    assert.deepEqual(lastPreview(api).things[0].channels[0].item.groups, ['gHouse']);
    await click(tab(root, 'Items'));
    assert.equal(cellOf(root, 'Kitchen_Light', 'groups').textContent, 'gHouse');
    await click(tab(root, 'Locations'));
    assert.equal(only(bodyRows(root)[0], '.col-count').textContent, '2', 'the house now holds the two Items');
    // the house off, the room on again: the room stands on top
    await click(only(bodyRows(root)[1], '.location-group'));
    await click(only(bodyRows(root)[0], '.location-group'));
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, [{ groupName: 'gKitchen', label: 'Kitchen', tag: 'Room', category: 'group' }]);
    assert.equal(only(bodyRows(root)[1], '.col-parent').textContent, '');
    // both off: no group, the Items in none
    await click(only(bodyRows(root)[1], '.location-group'));
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, []);
    assert.deepEqual(lastPreview(api).things[0].channels[0].item.groups, []);
});

test('wizard: the switch "Location groups" takes all of them out of the request and greys their ticks', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const flip = only(root, '.panel').querySelectorAll('.location-groups')[0];
    await click(flip);
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, []);
    assert.deepEqual(lastPreview(api).things[0].channels[0].item.groups, []);
    await click(tab(root, 'Locations'));
    assert.deepEqual(
        root.querySelector('tbody').querySelectorAll('.location-group').map((box) => box.disabled),
        [true, true],
    );
    await click(flip);
    await checked(opened);
    assert.deepEqual(lastPreview(api).roomGroups, HOUSE_GROUPS);
});

test('wizard: a project without spaces has the switch off and unusable, and no Locations in the tree', async () => {
    const offered = proposal();
    offered.rooms = [];
    const opened = await proposed({ api: fakeApi({ proposed: offered }) });
    const { root, api } = opened;
    const flip = only(root, '.panel').querySelectorAll('.location-groups')[0];
    assert.deepEqual([flip.checked, flip.disabled], [false, true]);
    assert.deepEqual(lastPreview(api).roomGroups, []);
    assert.equal(treeItems(root).some((node) => node.textContent.includes('Locations')), false);
});

test('wizard: every field changed in its cell goes out to call 22 as it is on the page; tags are split at commas', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    editCell(root, 'Kitchen_Light', 'item-name', 'Kitchen_Ceiling');
    editCell(root, '31/7/5', 'channel-label', 'Ceiling dimmer');
    only(row(root, '31/7/5'), '.channel-pick').click();
    only(row(root, 'Kitchen_Ceiling'), '.part-read').click();
    only(row(root, 'Kitchen_Ceiling'), '.part-status-read').click();
    await click(tab(root, 'Items'));
    editCell(root, 'Kitchen_Ceiling', 'item-tags', ' Light, Switch ,, ');
    editCell(root, 'Kitchen_Ceiling', 'item-label', 'Ceiling light');
    editCell(root, 'Kitchen_Ceiling', 'item-category', 'light');
    await click(tab(root, 'Things'));
    editCell(root, 'Kitchen', 'thing-id', 'Kitchen_2');
    editCell(root, 'Kitchen_2', 'thing-label', 'Kitchen two');
    await checked(opened);
    const request = lastPreview(api);
    const [light, dimmer] = request.things[0].channels;
    assert.deepEqual(light.item, { ...proposal().things[0].channels[0].item, groups: ['gKitchen'], name: 'Kitchen_Ceiling', label: 'Ceiling light', category: 'light', tags: ['Light', 'Switch'] });
    assert.equal(request.things[0].id, 'Kitchen_2');
    assert.equal(request.things[0].label, 'Kitchen two');
    assert.equal(dimmer.label, 'Ceiling dimmer');
    assert.equal(dimmer.selected, false);
    assert.equal(light.parts[0].read, true);
    assert.equal(light.parts[0].statusRead, false);
    // nothing else of the proposal changed on the way
    const expected = inKitchen(proposal().things);
    Object.assign(expected[0], { id: 'Kitchen_2', label: 'Kitchen two' });
    Object.assign(expected[0].channels[0].item, light.item);
    Object.assign(expected[0].channels[0].parts[0], { read: true, statusRead: false });
    Object.assign(expected[0].channels[1], { label: 'Ceiling dimmer', selected: false });
    assert.deepEqual(request.things, expected);
});

test('wizard: the same fields changed in the sidebar give the same request as in the cells', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const form = only(root, '.plan-edit');
    await click(row(root, '31/7/5'));
    type(only(form, '.channel-label'), 'Ceiling dimmer');
    only(form, '.channel-pick').click();
    await click(row(root, 'Kitchen_Light'));
    type(only(form, '.item-name'), 'Kitchen_Ceiling');
    only(form, '.part-read').click();
    only(form, '.part-status-read').click();
    type(only(form, '.item-tags'), ' Light, Switch ,, ');
    type(only(form, '.item-label'), 'Ceiling light');
    type(only(form, '.item-category'), 'light');
    assert.equal(only(form, '.item-type').disabled, true);
    await click(treeNode(root, 'kitchen'));
    type(only(form, '.thing-id'), 'Kitchen_2');
    type(only(form, '.thing-label'), 'Kitchen two');
    await checked(opened);
    const expected = inKitchen(proposal().things);
    Object.assign(expected[0], { id: 'Kitchen_2', label: 'Kitchen two' });
    Object.assign(expected[0].channels[0].item, { name: 'Kitchen_Ceiling', label: 'Ceiling light', category: 'light', tags: ['Light', 'Switch'] });
    Object.assign(expected[0].channels[0].parts[0], { read: true, statusRead: false });
    Object.assign(expected[0].channels[1], { label: 'Ceiling dimmer', selected: false });
    assert.deepEqual(lastPreview(api).things, expected);
    assert.ok(only(form, '.thing-id').parentNode.parentNode.textContent.includes('The last part of the Thing UID.'));
});

test('wizard: a cell that may not be changed has no editor, and one that may shows the pencil', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    for (const [words, column] of [
        ['Kitchen_Light', 'address'],
        ['Kitchen_Light', 'channel-type'],
        ['Kitchen_Light', 'item-type'],
        ['Kitchen_Light', 'from'],
        ['31/7/5', 'part-status'],
        ['31/7/6', 'channel-label'],
    ]) {
        const cell = cellOf(root, words, column);
        assert.ok(cell.querySelector('.can-edit') === null, column);
        assert.ok(cell.querySelector('.pencil') === null, column);
        cell.fire('dblclick');
        assert.ok(root.querySelector('.cell-editor') === null, column);
    }
    assert.ok(only(cellOf(root, 'Kitchen_Light', 'item-name'), '.pencil').querySelector('.icon-pencil'));
    assert.equal(only(row(root, '31/7/5'), '.part-status-read').disabled, true);
    await click(tab(root, 'Read at start'));
    bodyRows(root)[0].fire('dblclick');
    bodyRows(root)[0].fire('keydown', { key: 'Enter' });
    assert.ok(root.querySelector('.cell-editor') === null);
    await checked(opened);
    assert.equal(api.requests('preview').length, 1);
});

test('wizard: Enter or F2 on a row opens the editor of its marked cell, without a mark of its first text; neither flips a tick, Space on a marked tick does; Escape drops the change', async () => {
    const opened = await proposed();
    const { root, api, h } = opened;
    const light = () => row(root, 'Kitchen_Light');
    await click(light());
    // a key without a mark changes nothing by Space
    light().fire('keydown', { key: ' ' });
    light().fire('keydown', { key: 'Enter' });
    assert.equal(only(light(), '.channel-pick').checked, true, 'the tick before the label is not what Enter takes');
    assert.equal(only(root, '.cell-editor').value, 'Light');
    only(root, '.cell-editor').fire('keydown', { key: 'Escape' });
    light().fire('keydown', { key: 'ArrowLeft' });
    assert.ok(only(light(), '.channel-pick').classList.contains('current'));
    for (const key of ['Enter', 'F2']) {
        light().fire('keydown', { key });
        assert.equal(only(light(), '.channel-pick').checked, true, key);
        assert.ok(root.querySelector('.cell-editor') === null, key);
    }
    assert.equal(api.requests('preview').length, 1, 'nothing changed so far');
    light().fire('keydown', { key: ' ' });
    assert.equal(only(light(), '.channel-pick').checked, false, 'Space flips the marked tick');
    assert.ok(only(light(), '.channel-pick').classList.contains('current'), 'and the mark stays');
    light().fire('keydown', { key: 'ArrowRight' });
    assert.ok(only(cellOf(root, 'Kitchen_Light', 'channel-label'), '.can-edit').classList.contains('current'));
    light().fire('keydown', { key: ' ' });
    assert.equal(cellOf(root, 'Kitchen_Light', 'channel-label').textContent, 'Light');
    light().fire('keydown', { key: 'F2' });
    const editor = only(root, '.cell-editor');
    assert.equal(h.document.activeElement, editor);
    assert.equal(editor.value, 'Light');
    editor.value = 'Dropped';
    // the keys in the editor are its own: the list does not move its selection
    editor.fire('keydown', { key: 'ArrowDown' });
    assert.ok(light().classList.contains('selected'));
    editor.fire('keydown', { key: 'Escape' });
    assert.ok(root.querySelector('.cell-editor') === null);
    assert.equal(cellOf(root, 'Kitchen_Light', 'channel-label').textContent, 'Light');
    assert.equal(h.document.activeElement, light());

    light().fire('keydown', { key: 'Enter' });
    const again = only(root, '.cell-editor');
    again.value = 'Kept';
    again.fire('focusout');
    assert.equal(cellOf(root, 'Kitchen_Light', 'channel-label').textContent, 'Kept');
    await checked(opened);
    assert.equal(lastPreview(api).things[0].channels[0].label, 'Kept');
    assert.equal(lastPreview(api).things[0].channels[0].selected, false);

    // a click on a cell puts the mark there; in another row the mark is gone
    cellOf(root, 'Kitchen_Light', 'item-name').querySelector('.value').click();
    assert.ok(only(cellOf(root, 'Kitchen_Light', 'item-name'), '.can-edit').classList.contains('current'));
    assert.equal(root.querySelector('tbody').querySelectorAll('.current').length, 1);
    light().fire('keydown', { key: 'Enter' });
    assert.equal(only(root, '.cell-editor').value, 'Kitchen_Light');
    only(root, '.cell-editor').fire('keydown', { key: 'Escape' });
    row(root, '31/7/5').click();
    assert.equal(root.querySelector('tbody').querySelectorAll('.current').length, 0);
});

test('wizard: a change in the sidebar goes to every selected row where it makes sense', async () => {
    const offered = proposal();
    offered.things[0].channels[1].item.allowedTypes = ['Dimmer', 'Switch'];
    offered.things[0].channels[0].item.allowedTypes = ['Switch', 'Number'];
    const opened = await proposed({ api: fakeApi({ proposed: offered }) });
    const { root, api } = opened;
    await click(row(root, 'Kitchen_Light'));
    row(root, '31/7/6').fire('click', { shiftKey: true });
    assert.equal(bodyRows(root).filter((line) => line.classList.contains('selected')).length, 3);
    const form = only(root, '.plan-edit');
    assert.ok(form.textContent.includes('3 rows selected'));
    assert.equal(only(form, '.channel-pick').indeterminate, false);
    assert.ok(form.querySelector('.channel-label') === null);
    assert.ok(form.querySelector('.part-status') === null);
    // the Item type offers what every row allows
    assert.deepEqual(
        only(form, '.item-type').children.map((option) => option.getAttribute('value')),
        ['', 'Switch'],
    );
    type(only(form, '.item-type'), 'Switch');
    type(only(form, '.item-tags'), 'Light, Kitchen');
    type(only(form, '.item-category'), 'light');
    only(form, '.part-read').click();
    only(form, '.channel-pick').click();
    await checked(opened);
    const [light, dimmer] = lastPreview(api).things[0].channels;
    for (const channel of [light, dimmer]) {
        assert.equal(channel.selected, false);
        assert.deepEqual([channel.item.type, channel.item.tags, channel.item.category], ['Switch', ['Light', 'Kitchen'], 'light']);
        assert.ok(channel.parts.every((entry) => entry.read === true));
    }
    assert.equal(api.requests('preview').length, 2);

    // Ctrl and a click takes one row out again; Shift and an arrow key takes the next
    row(root, '31/7/5').fire('click', { ctrlKey: true });
    assert.equal(bodyRows(root).filter((line) => line.classList.contains('selected')).length, 2);
    await click(row(root, 'Kitchen_Light'));
    row(root, 'Kitchen_Light').fire('keydown', { key: 'ArrowDown', shiftKey: true });
    assert.deepEqual(
        bodyRows(root).map((line) => line.classList.contains('selected')),
        [false, true, true, false],
    );
    // one step of "Undo change" takes the change of all rows back
    for (let steps = 0; steps < 5; steps++) {
        await click(only(root, '.undo-edit'));
    }
    await checked(opened);
    assert.deepEqual(lastPreview(api).things, inKitchen(offered.things));
});

test('wizard: "Undo change", "Redo" and "Reset to the proposal" walk the edits of the plan', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const undoEdit = only(root, '.undo-edit');
    const redoEdit = only(root, '.redo-edit');
    const reset = only(root, '.reset-plan');
    assert.deepEqual([undoEdit.disabled, redoEdit.disabled, reset.disabled], [true, true, true]);
    editCell(root, 'Kitchen_Light', 'item-name', 'Kitchen_A');
    editCell(root, 'Kitchen_A', 'channel-label', 'Ceiling');
    await click(only(root, '.split'));
    assert.deepEqual([undoEdit.disabled, redoEdit.disabled, reset.disabled], [false, true, false]);
    await click(undoEdit);
    assert.equal(root.querySelector('tbody').querySelectorAll('.split').length, 1);
    await click(undoEdit);
    assert.equal(cellOf(root, 'Kitchen_A', 'channel-label').textContent, 'Light');
    assert.equal(redoEdit.disabled, false);
    await click(redoEdit);
    assert.equal(cellOf(root, 'Kitchen_A', 'channel-label').textContent, 'Ceiling');
    await checked(opened);
    assert.deepEqual([lastPreview(api).things[0].channels[0].label, lastPreview(api).things[0].channels[0].item.name, lastPreview(api).things[0].channels.length], ['Ceiling', 'Kitchen_A', 2]);
    // a new change ends what could be made again
    await click(redoEdit);
    await click(undoEdit);
    editCell(root, 'Kitchen_A', 'item-name', 'Kitchen_B');
    assert.equal(redoEdit.disabled, true);
    await click(reset);
    assert.equal(reset.disabled, true);
    await checked(opened);
    assert.deepEqual(lastPreview(api).things, inKitchen(proposal().things));
    assert.deepEqual(lastPreview(api).roomGroups, HOUSE_GROUPS);
    await click(undoEdit);
    assert.equal(cellOf(root, 'Kitchen_B', 'channel-label').textContent, 'Ceiling');
});

test('wizard: the controls inside the cells of the list are no tab stops', async () => {
    const { root } = await proposed();
    const inCells = bodyRows(root)
        .filter((line) => !line.classList.contains('group-row'))
        .flatMap((line) => [...line.querySelectorAll('input'), ...line.querySelectorAll('button')]);
    assert.ok(inCells.length > 3);
    assert.deepEqual([...new Set(inCells.map((node) => node.getAttribute('tabindex')))], ['-1']);
});

test('wizard: a tick of the sidebar over rows that differ is neither ticked nor unticked', async () => {
    const opened = await proposed();
    const { root } = opened;
    only(row(root, 'Kitchen_Light'), '.channel-pick').click();
    await click(row(root, 'Kitchen_Light'));
    row(root, '31/7/6').fire('click', { shiftKey: true });
    const pickBox = only(only(root, '.plan-edit'), '.channel-pick');
    assert.deepEqual([pickBox.indeterminate, pickBox.checked], [true, false]);
});

test('wizard: a choice list in a cell is kept on Enter or on leaving the cell, not with each arrow key; Escape drops it', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const status = () => lastPreview(api).things[0].channels[0].parts[0].statusAddress;
    let editor = editorOf(root, 'Kitchen_Light', 'part-status');
    // a closed list says "change" with every arrow key
    for (const value of ['31/7/4', '', '31/7/4']) {
        editor.value = value;
        editor.fire('change');
    }
    assert.ok(only(root, '.cell-editor') === editor, 'the editor stays open on the way');
    await checked(opened);
    assert.equal(api.requests('preview').length, 1, 'and nothing is asked on the way');
    editor.fire('keydown', { key: 'Enter' });
    assert.ok(root.querySelector('.cell-editor') === null);
    await checked(opened);
    assert.equal(api.requests('preview').length, 2);
    assert.equal(status(), '31/7/4');

    editor = editorOf(root, 'Kitchen_Light', 'part-status');
    editor.value = '';
    editor.fire('change');
    editor.fire('focusout');
    assert.ok(root.querySelector('.cell-editor') === null);
    await checked(opened);
    assert.equal(api.requests('preview').length, 3);
    assert.equal(status(), null);

    editor = editorOf(root, 'Kitchen_Light', 'part-status');
    editor.value = '31/7/2';
    editor.fire('change');
    editor.fire('keydown', { key: 'Escape' });
    assert.ok(root.querySelector('.cell-editor') === null);
    await checked(opened);
    assert.equal(api.requests('preview').length, 3, 'Escape asks nothing');
    assert.equal(cellOf(root, 'Kitchen_Light', 'part-status').textContent, 'none');
});

test('wizard: "Split" is reached by keyboard from the list: the right arrow key puts the mark on it, Enter or Space splits', async () => {
    for (const key of ['Enter', ' ', 'F2']) {
        const opened = await proposed();
        const { root, api, h } = opened;
        const dimmer = () => row(root, '31/7/5');
        await click(dimmer());
        // the mark walks the cells that can be changed; the button stands last
        for (let presses = 0; presses < 12 && !only(dimmer(), '.split').classList.contains('current'); presses++) {
            dimmer().fire('keydown', { key: 'ArrowRight' });
        }
        assert.ok(only(dimmer(), '.split').classList.contains('current'), 'the mark reaches the button');
        assert.equal(root.querySelector('tbody').querySelectorAll('.current').length, 1);
        dimmer().fire('keydown', { key: 'ArrowRight' });
        assert.ok(only(dimmer(), '.split').classList.contains('current'), 'and stays on it at the end of the row');
        dimmer().fire('keydown', { key });
        assert.ok(root.querySelector('.split') === null, `${key} splits`);
        assert.ok(root.querySelector('.cell-editor') === null, 'no editor opens for it');
        assert.equal(h.document.activeElement.tagName, 'TR', 'the keyboard stays in the list');
        await checked(opened);
        assert.deepEqual(
            lastPreview(api).things[0].channels.map((channel) => channel.id),
            ['Light', 'Dimmer_switch', 'Dimmer_position'],
        );
    }
});

test('wizard: a row that cannot be split has no place for the mark beyond its fields, and Enter there never splits another row', async () => {
    const opened = await proposed();
    const { root, api } = opened;
    const light = () => row(root, 'Kitchen_Light');
    await click(light());
    for (let presses = 0; presses < 12; presses++) {
        light().fire('keydown', { key: 'ArrowRight' });
    }
    light().fire('keydown', { key: ' ' });
    light().fire('keydown', { key: 'Enter' });
    root.querySelector('.cell-editor')?.fire('keydown', { key: 'Escape' });
    assert.equal(root.querySelectorAll('.split').length, 1, 'the other row still has its button');
    await checked(opened);
    assert.equal(lastPreview(api).things[0].channels.length, 2);
});
