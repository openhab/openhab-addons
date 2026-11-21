// The wizard's sheet that asks, the run, and the undo of a run.

import { test } from 'node:test';
import assert from 'node:assert/strict';

import { textNodes } from './fakedom.js';
import { empty, settle } from './fakes.js';
import { BRIDGE, THING, bodyRows, checked, click, confirmed, createButton, fakeApi, fakeCore, goodPreview, morePreview, only, open, part, proposal, proposed, row, steps, tab, tabStore } from './wizardfakes.js';

const STALE = 'Nothing was created: the plan changed while the question was open. Look at it and press "Create in openHAB…" again.';

test('wizard: the sheet names the counts, the read telegrams, every read address and the warnings; nothing is made before "Create" in it', async () => {
    const opened = await proposed();
    const { root, fetch, api } = opened;
    await click(createButton(root));
    const sheet = only(root, '.confirm-sheet');
    assert.equal(
        only(sheet, '.question').textContent,
        'Create 1 Things, 2 Items, 2 links and 1 location groups in openHAB? openHAB will send 3 read telegrams now, and again at each start of openHAB and each time the bridge reconnects. A read that gets no answer is repeated by the bridge up to its retry limit, so on the bus there can be more telegrams than this count.',
    );
    assert.equal(only(sheet, '.counts').textContent, 'Will make: 1 Things · 2 channels · 2 Items · 1 location groups · 2 links.');
    assert.deepEqual(
        only(sheet, '.read-list').children.map((entry) => only(entry, '.address').textContent),
        goodPreview().readAddresses,
    );
    await click(only(sheet, '.confirm-cancel'));
    await click(tab(root, 'Read at start'));
    const named = bodyRows(root).map((line) => [only(line, '.col-address').textContent, only(line, '.col-name').textContent]);
    await click(createButton(root));
    assert.deepEqual(
        only(root, '.read-list').children.map((entry) => [only(entry, '.address').textContent, entry.querySelector('.name')?.textContent ?? '']),
        named,
        'each address with the name the Read at start tab gives it',
    );
    assert.ok(named.some(([, name]) => name !== ''));
    assert.ok(textNodes(sheet).includes('Read at start: 2 group addresses'));
    assert.ok(only(sheet, '.problems').textContent.includes('A channel of this bridge already uses the address.'));
    assert.match(only(sheet, '.limits').textContent, /a link someone else makes between the look-up and the create is overwritten/);
    assert.equal(fetch.calls.length, 0);
    assert.deepEqual(api.calls.map((call) => call[0]).filter((name) => !['locations', 'allDevices', 'allGroupAddresses', 'proposal', 'preview'].includes(name)), []);
});

test('wizard: "Cancel" in the sheet, or Escape, makes nothing and leaves the plan as it was', async () => {
    const opened = await proposed();
    const { root, fetch } = opened;
    await click(createButton(root));
    assert.equal(opened.h.document.activeElement, only(root, '.confirm-cancel'));
    await click(only(root, '.confirm-cancel'));
    assert.equal(opened.h.document.activeElement, createButton(root));
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.equal(fetch.calls.length, 0);
    assert.equal(createButton(root).disabled, false);
    assert.ok(root.querySelector('.run') === null);
    await click(createButton(root));
    only(root, '.confirm-sheet').fire('keydown', { key: 'Escape' });
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.equal(fetch.calls.length, 0);
});

test('wizard: Tab stays on the two buttons of the sheet and does not reach the plan behind it', async () => {
    const opened = await proposed();
    const { root } = opened;
    const focused = () => opened.h.document.activeElement;
    await click(createButton(root));
    const cancel = only(root, '.confirm-cancel');
    const create = root.querySelector('.confirm-sheet').querySelectorAll('button').find((button) => button !== cancel);
    assert.equal(focused(), cancel);
    assert.equal(cancel.fire('keydown', { key: 'Tab' }).defaultPrevented, true);
    assert.equal(focused(), create);
    assert.equal(create.fire('keydown', { key: 'Tab' }).defaultPrevented, false, 'the browser goes on to Cancel itself');
    assert.equal(create.fire('keydown', { key: 'Tab', shiftKey: true }).defaultPrevented, true);
    assert.equal(focused(), cancel);
    assert.equal(cancel.fire('keydown', { key: 'Tab', shiftKey: true }).defaultPrevented, false);
});

test('wizard: a second press on "Create" during a run calls nothing more; "Undo" waits for the run', async () => {
    let release = null;
    const fetch = fakeCore((call) => {
        if (release === null) {
            return new Promise((resolve) => {
                release = () => resolve(empty(404));
            });
        }
        return call.method === 'GET' ? empty(404) : empty(call.url.pathname.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    const opened = await proposed({ fetch });
    const { root } = opened;
    await click(createButton(root));
    const confirm = only(root, '.confirm-create');
    await click(confirm);
    assert.equal(fetch.calls.length, 1);
    confirm.fire('click');
    await settle();
    assert.equal(fetch.calls.length, 1);
    assert.equal(only(root, '.back-plan').disabled, true);
    release();
    await settle();
    assert.equal(fetch.calls.length, 12);
    await click(only(root, '.back-plan'));
    assert.equal(createButton(root).disabled, true);
    assert.match(only(root, '.check-state').textContent, /used for a run/);
});

test('wizard: a second run makes nothing twice: after a run the plan must be checked again, and what exists stops the next run', async () => {
    const made = new Set();
    const fetch = fakeCore((call) => {
        const path = call.url.pathname;
        if (call.method === 'GET') {
            return empty(made.has(path) ? 200 : 404);
        }
        made.add(path === '/oh/rest/things' ? `/oh/rest/things/${encodeURIComponent(THING)}` : path);
        return empty(path.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    const opened = await proposed({ fetch });
    const { root } = opened;
    await confirmed(opened);
    const creates = () => fetch.calls.filter((call) => call.method !== 'GET').length;
    assert.equal(creates(), 4);
    await click(only(root, '.back-plan'));
    assert.equal(createButton(root).disabled, true);
    createButton(root).fire('click');
    await settle();
    assert.ok(root.querySelector('.confirm-sheet') === null);
    await click(only(root, '.recheck'));
    assert.equal(createButton(root).disabled, false);
    await confirmed(opened);
    assert.equal(creates(), 4);
    assert.ok(textNodes(root).includes('Nothing was created: 4 of these exist in openHAB already.'));
    assert.ok(root.querySelector('.undo') === null);
});

test('wizard: undo removes what the run made and nothing else, and is disabled while it runs', async () => {
    let hold = null;
    const fetch = fakeCore((call) => {
        if (call.method === 'DELETE' && hold === null) {
            return new Promise((resolve) => {
                hold = () => resolve(empty(200));
            });
        }
        if (call.method === 'GET') {
            // the group exists already: the run stops before anything is made
            return empty(404);
        }
        if (call.method === 'DELETE') {
            return empty(call.url.pathname.startsWith('/oh/rest/things/') ? 202 : 200);
        }
        return empty(call.url.pathname.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    const opened = await proposed({ fetch });
    const { root, h } = opened;
    await confirmed(opened);
    const undoButton = only(root, '.undo');
    await click(undoButton);
    assert.equal(undoButton.disabled, true);
    undoButton.fire('click');
    await settle();
    assert.equal(h.questions.length, 1);
    assert.equal(fetch.calls.filter((call) => call.method === 'DELETE').length, 1);
    hold();
    await settle();
    const created = fetch.calls.filter((call) => call.method === 'POST' || call.method === 'PUT').map((call) => (call.method === 'POST' ? `/oh/rest/things/${encodeURIComponent(THING)}` : call.url.pathname));
    assert.deepEqual(
        fetch.calls
            .filter((call) => call.method === 'DELETE')
            .map((call) => call.url.pathname)
            .sort(),
        [...created].sort(),
    );
});

test('wizard: a step that would set the device Thing\'s address is refused: the run does not start', async () => {
    const withAddress = goodPreview();
    withAddress.steps[1].thing.configuration = { address: '31.1.9' };
    const opened = await proposed({ api: fakeApi({ previews: [withAddress] }) });
    await confirmed(opened);
    assert.equal(opened.fetch.calls.length, 0);
    assert.ok(textNodes(opened.root).includes('The run did not start: a Thing setting, which the wizard never makes.'));
    for (const request of opened.api.requests('preview')) {
        assert.ok(request.things.every((thing) => !Object.hasOwn(thing, 'address') && !Object.hasOwn(thing, 'configuration')));
    }
});

test('wizard: a run that finds what exists creates nothing and says so', async () => {
    const fetch = fakeCore((call) => (call.method === 'GET' && call.url.pathname.startsWith('/oh/rest/items/Kitchen_Light') ? empty(200) : empty(404)));
    const opened = await proposed({ fetch });
    await confirmed(opened);
    assert.ok(fetch.calls.every((call) => call.method === 'GET'));
    assert.ok(textNodes(opened.root).includes('Nothing was created: 1 of these exist in openHAB already.'));
    assert.ok(opened.root.querySelector('.undo') === null);
});

test('wizard: a lost answer that cannot be looked up says the object may exist', async () => {
    let creates = 0;
    const fetch = fakeCore((call) => {
        if (call.method === 'GET') {
            return creates > 0 ? Promise.reject(new TypeError('Failed to fetch')) : empty(404);
        }
        creates++;
        return Promise.reject(new TypeError('Failed to fetch'));
    });
    const opened = await proposed({ fetch });
    await confirmed(opened);
    assert.ok(textNodes(opened.root).includes('The run stopped at step 1: openHAB did not answer the create.'));
    assert.ok(textNodes(opened.root).some((words) => words.startsWith('It could not be looked up afterwards')));
});

test('wizard: the run lists its steps grouped by Thing, and the end links each new Thing to its page in openHAB', async () => {
    const opened = await proposed();
    const { root } = opened;
    await confirmed(opened);
    assert.deepEqual(
        root.querySelectorAll('.step-group').map((group) => [group.querySelector('h4').textContent, group.querySelectorAll('li').length]),
        [
            ['Location groups', 1],
            [`Kitchen ${THING}`, 3],
        ],
    );
    const link = only(root, '.main-ui');
    assert.equal(link.tagName, 'A');
    assert.equal(link.getAttribute('href'), `/oh/settings/things/${THING}`);
    assert.equal(link.getAttribute('target'), '_blank');
    assert.equal(link.textContent, `Open ${THING} in openHAB`);
    await click(only(root, '.back-panel'));
    assert.deepEqual(opened.h.navigations, [{ view: 'group-addresses', bridge: BRIDGE.uid, query: {} }]);
});

test('wizard: the sheet\'s "Create" runs only the answer the sheet shows: after a change behind it nothing is made and the plan is shown', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [goodPreview(), morePreview()] }) });
    const { root, fetch } = opened;
    await click(createButton(root));
    const stale = only(root, '.confirm-create');
    only(row(root, 'Kitchen_Light'), '.part-read').click();
    await checked(opened);
    assert.equal(createButton(root).disabled, false);
    assert.equal(only(root, '.read-list').children.length, 2, 'the sheet still shows the answer it was drawn from');
    await click(stale);
    assert.equal(fetch.calls.length, 0);
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.ok(root.querySelector('.wizard-run') === null);
    assert.ok(opened.h.document.activeElement === createButton(root));
    assert.equal(only(root, '.check-state').textContent, STALE, 'the page says why nothing happened');
    assert.equal(only(root, '.check-state').getAttribute('role'), 'status');

    await click(createButton(root));
    assert.equal(only(root, '.check-state').textContent, '');
    assert.equal(only(root, '.read-list').children.length, 3);
    assert.match(only(root, '.question').textContent, /send 9 read telegrams/);
    await click(only(root, '.confirm-create'));
    assert.equal(fetch.calls.length, 12);
});

test('wizard: the sheet\'s "Create" makes nothing while a change behind it waits for its check', async () => {
    const opened = await proposed();
    const { root, fetch } = opened;
    await click(createButton(root));
    only(row(root, 'Kitchen_Light'), '.part-read').click();
    await click(only(root, '.confirm-create'));
    assert.equal(fetch.calls.length, 0);
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.ok(root.querySelector('.wizard-run') === null);
    assert.equal(only(root, '.check-state').textContent, STALE);
    await checked(opened);
    assert.equal(only(root, '.check-state').textContent, STALE, 'the answer of the check does not take the line away');
    only(row(root, 'Kitchen_Light'), '.part-read').click();
    assert.notEqual(only(root, '.check-state').textContent, STALE, 'the next change does');
});

test('wizard: what the last run made can still be undone after the wizard was left: the list is kept for the tab, the line says how many are left, and its use ends it', async () => {
    const keep = new Map();
    const first = await proposed({ keep });
    await confirmed(first);
    const created = first.fetch.calls.filter((call) => call.method === 'POST' || call.method === 'PUT').map((call) => (call.method === 'POST' ? `/oh/rest/things/${encodeURIComponent(THING)}` : call.url.pathname));
    assert.equal(created.length, 4);
    first.view.close();

    // with the kept plan: from the summary bar
    const second = await open({ keep });
    const lastRun = only(second.root, '.last-run');
    assert.equal(lastRun.textContent, 'The last run (4 left to undo)…');
    await click(lastRun);
    assert.ok(textNodes(second.root).includes('The last run made 4 objects that can still be undone here.'));
    assert.equal(only(second.root, '.created').querySelectorAll('li').length, 4);
    second.view.close();

    // opened for something else, without the plan: from the first screen
    const third = await open({ keep, query: { device: 'P-7_D-2' } });
    assert.ok(third.root.querySelector('.wizard-choose'));
    await click(only(third.root, '.last-run'));
    assert.ok(third.h.document.activeElement === only(third.root, '.wizard-run').querySelector('h3'));
    await click(only(third.root, '.undo'));
    assert.equal(third.h.questions.length, 1);
    assert.deepEqual(
        third.fetch.calls
            .filter((call) => call.method === 'DELETE')
            .map((call) => call.url.pathname)
            .sort(),
        [...created].sort(),
    );
    third.view.close();

    const fourth = await open({ keep });
    assert.ok(fourth.root.querySelector('.last-run') === null, 'used: nothing is kept');
});

test('wizard: a run that ends after the wizard was left keeps its undo list too; after a sign-out emptied the store it keeps nothing', async () => {
    for (const signedOut of [false, true]) {
        const keep = new Map();
        const opened = await proposed({ keep });
        await click(createButton(opened.root));
        only(opened.root, '.confirm-create').click();
        opened.view.close();
        if (signedOut) {
            keep.clear();
        }
        await settle();
        const again = await open({ keep, query: { device: 'P-7_D-2' } });
        assert.equal(again.root.querySelectorAll('.last-run').length, signedOut ? 0 : 1, String(signedOut));
    }
});

test('wizard: a click on the sheet\'s text leaves the focus on the sheet, and Tab from there stays in it', async () => {
    const opened = await proposed();
    const { root } = opened;
    await click(createButton(root));
    const sheet = only(root, '.confirm-sheet');
    const cancel = only(root, '.confirm-cancel');
    assert.equal(sheet.getAttribute('tabindex'), '-1');
    assert.equal(sheet.getAttribute('aria-modal'), 'true');
    sheet.focus();
    assert.equal(sheet.fire('keydown', { key: 'Tab' }).defaultPrevented, false, 'the browser goes on to the first button itself');
    assert.equal(sheet.fire('keydown', { key: 'Tab', shiftKey: true }).defaultPrevented, true);
    assert.ok(opened.h.document.activeElement === cancel);
});

test('wizard: after "Back to the plan" the last run is reachable from the summary bar, and its undo deletes exactly what the run made', async () => {
    const opened = await proposed();
    const { root, fetch, h } = opened;
    assert.ok(root.querySelector('.last-run') === null);
    await confirmed(opened);
    assert.ok(opened.h.document.activeElement === only(root, '.wizard-run').querySelector('h3'));
    await click(only(root, '.back-plan'));
    assert.ok(root.querySelector('.undo') === null);
    const lastRun = only(root, '.last-run');
    assert.equal(lastRun.textContent, 'The last run (4 left to undo)…');
    await click(lastRun);
    assert.ok(textNodes(root).includes('Done: 4 created.'));
    await click(only(root, '.undo'));
    assert.equal(h.questions.length, 1);
    const created = fetch.calls.filter((call) => call.method === 'POST' || call.method === 'PUT').map((call) => (call.method === 'POST' ? `/oh/rest/things/${encodeURIComponent(THING)}` : call.url.pathname));
    assert.equal(created.length, 4);
    assert.deepEqual(
        fetch.calls
            .filter((call) => call.method === 'DELETE')
            .map((call) => call.url.pathname)
            .sort(),
        [...created].sort(),
    );
    await click(only(root, '.back-plan'));
    assert.ok(root.querySelector('.last-run') === null, 'nothing is left to undo');
});

test('wizard: a new run while the last one can still be undone asks first; "no" opens no sheet and keeps the undo', async () => {
    const opened = await proposed();
    const { root, fetch, h } = opened;
    await confirmed(opened);
    await click(only(root, '.back-plan'));
    await click(only(root, '.recheck'));
    assert.equal(createButton(root).disabled, false);
    h.answer = false;
    await click(createButton(root));
    assert.deepEqual(h.questions, ['The last run created 4 Things, Items, links and groups that can still be undone. A new run drops that undo: they stay in openHAB until you delete them there. Go on?']);
    assert.ok(root.querySelector('.confirm-sheet') === null);
    assert.equal(only(root, '.last-run').disabled, false);
    h.answer = true;
    await click(createButton(root));
    assert.equal(h.questions.length, 2);
    only(root, '.confirm-sheet');
    assert.equal(fetch.calls.length, 12);
});

/** The answer of call 22 when openHAB has the house's group already: the room's group lies in it and is the only one made. */
function intoHouse() {
    const answer = goodPreview();
    answer.existingGroups = ['gHouse'];
    answer.steps[0].item.groupNames = ['gHouse'];
    return answer;
}

test('wizard: the sheet lists each location group with its label, tag and the group it lies in, and the ones openHAB has already', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [intoHouse()] }) });
    await click(createButton(opened.root));
    const sheet = only(opened.root, '.confirm-sheet');
    assert.ok(textNodes(sheet).includes('Location groups: 1 new'));
    assert.deepEqual(
        only(sheet, '.group-list').children.map((entry) => entry.textContent),
        ['gKitchen Kitchen Room in gHouse', 'gHouse is in openHAB already, used as it is'],
    );
});

test('wizard: a run into a group of openHAB looks that the group is there, never writes it, and its undo leaves it', async () => {
    const fetch = fakeCore((call) => {
        if (call.method === 'GET') {
            return empty(call.url.pathname === '/oh/rest/items/gHouse' ? 200 : 404);
        }
        if (call.method === 'DELETE') {
            return empty(call.url.pathname.startsWith('/oh/rest/things/') ? 202 : 200);
        }
        return empty(call.url.pathname.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    const opened = await proposed({ api: fakeApi({ previews: [intoHouse()] }), fetch });
    await confirmed(opened);
    const calls = () => fetch.calls.map((call) => `${call.method} ${decodeURIComponent(call.url.pathname)}`);
    assert.equal(calls()[0], 'GET /oh/rest/items/gHouse');
    assert.ok(textNodes(opened.root).includes('Done: 4 created.'));
    await click(only(opened.root, '.undo'));
    assert.deepEqual(
        calls().filter((line) => line.endsWith('/items/gHouse')),
        ['GET /oh/rest/items/gHouse'],
        'the group of openHAB is looked at once and never written or deleted',
    );
    assert.equal(calls().at(-1), 'DELETE /oh/rest/items/gKitchen');
});

test('wizard: a run stops before anything is made when the group of openHAB it builds on is gone', async () => {
    const opened = await proposed({ api: fakeApi({ previews: [intoHouse()] }) });
    await confirmed(opened);
    assert.deepEqual(
        opened.fetch.calls.map((call) => `${call.method} ${call.url.pathname}`),
        ['GET /oh/rest/items/gHouse'],
    );
    assert.ok(textNodes(opened.root).includes('The run stopped: A group of openHAB the plan builds on is gone. Check the plan again.'));
});

const GONE =
    'The last run made 4 Things, Items, links and groups that were not undone. The page was loaded again or signed out since, so its undo list is gone: they stay in openHAB until you delete them there. Understood';

test('wizard: the kept undo list is the bridge\'s own: the wizard of another bridge has no last run', async () => {
    const keep = new Map();
    const first = await proposed({ keep });
    await confirmed(first);
    first.view.close();
    const other = await open({ keep, bridge: { ...BRIDGE, uid: 'knx:ip:other-line', label: 'Line 8' } });
    assert.ok(other.root.querySelector('.last-run') === null);
    assert.ok(other.root.querySelector('.created') === null);
    other.view.close();
    const same = await open({ keep, query: { device: 'P-7_D-2' } });
    assert.equal(only(same.root, '.last-run').textContent, 'The last run (4 left to undo)…');
});

test('wizard: after a reload of the page the undo list is gone and the wizard says so plainly, once, until "Understood"', async () => {
    const stored = tabStore();
    const first = await proposed({ keep: new Map(), tab: stored });
    assert.ok(first.root.querySelector('.run-gone') === null);
    await confirmed(first);
    assert.deepEqual(JSON.parse(stored.text.value), { 'wizard.run:knx:ip:made-up': 4 }, 'a count only: no id, no name');
    first.view.close();

    // the page loaded again: the tab's memory is empty, the tab's store is what the browser kept
    const reloaded = await open({ keep: new Map(), tab: tabStore(stored.text) });
    assert.equal(only(reloaded.root, '.run-gone').textContent, GONE);
    assert.ok(reloaded.root.querySelector('.last-run') === null, 'no undo is offered that cannot be done');
    reloaded.view.close();
    const again = await open({ keep: new Map(), tab: tabStore(stored.text) });
    await click(only(again.root, '.run-gone-ok'));
    assert.ok(again.root.querySelector('.run-gone') === null);
    again.view.close();
    const after = await open({ keep: new Map(), tab: tabStore(stored.text) });
    assert.ok(after.root.querySelector('.run-gone') === null);
    // the wizard of another bridge never has the line
    const stored2 = tabStore();
    const run = await proposed({ keep: new Map(), tab: stored2 });
    await confirmed(run);
    const other = await open({ keep: new Map(), tab: tabStore(stored2.text), bridge: { ...BRIDGE, uid: 'knx:ip:other-line' } });
    assert.ok(other.root.querySelector('.run-gone') === null);
});

test('wizard: while the undo list is kept the line is not shown, and an undo that removes everything leaves nothing to say after a reload', async () => {
    const stored = tabStore();
    const keep = new Map();
    const first = await proposed({ keep, tab: stored });
    await confirmed(first);
    first.view.close();
    const second = await open({ keep, tab: stored });
    assert.ok(second.root.querySelector('.run-gone') === null);
    await click(only(second.root, '.last-run'));
    await click(only(second.root, '.undo'));
    assert.deepEqual(JSON.parse(stored.text.value), { 'wizard.run:knx:ip:made-up': null });
    second.view.close();
    const reloaded = await open({ keep: new Map(), tab: tabStore(stored.text) });
    assert.ok(reloaded.root.querySelector('.run-gone') === null);
});

test('wizard: a run still going at sign-out writes nothing into the store of the next visit, also when a wizard of that visit tried a run by then', async () => {
    const keep = new Map();
    const stored = tabStore();
    let release;
    const held = new Promise((resolve) => (release = resolve));
    // the first create waits until the test lets it go
    const fetch = fakeCore(async (call) => {
        if (call.method === 'GET') {
            return empty(404);
        }
        await held;
        return empty(call.url.pathname.startsWith('/oh/rest/links/') ? 200 : 201);
    });
    const opened = await proposed({ keep, tab: stored, fetch });
    await click(createButton(opened.root));
    only(opened.root, '.confirm-create').click();
    await settle();
    opened.view.close();
    // signed out, signed in again, and the wizard of the same bridge opened before the old run ends
    keep.clear();
    const next = await proposed({ keep, tab: stored });
    assert.ok(next.root.querySelector('.last-run') === null);
    // a run of the new visit while the old one still goes makes nothing, and leaves its own (empty) mark in the store
    await confirmed(next);
    assert.ok(textNodes(next.root).includes('Another run or undo is going.'));
    assert.equal(next.fetch.calls.length, 0);
    release();
    await settle();
    assert.equal(keep.has('wizard.run:knx:ip:made-up') && keep.get('wizard.run:knx:ip:made-up') !== null, false, 'the old run left no list in the new store');
    next.view.close();
    const later = await open({ keep, tab: stored, query: { device: 'P-7_D-2' } });
    assert.ok(later.root.querySelector('.last-run') === null);
    assert.equal(only(later.root, '.run-gone').textContent, GONE, 'what it made is said, since it can no longer be undone here');
});
