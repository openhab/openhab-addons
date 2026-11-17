// The Group Monitor's bar to read a group address or to write a value to it (call 24). It is the only part of the
// page that puts a telegram on the bus, so it sends on two presses alone: Read or Write, then Send in the line that
// says what goes out. Typing, choosing, Enter in a field, a suggestion, "Use selected", opening the panel or an
// address of the page send nothing, and nothing of the bar is ever written into the page's address. Without the
// bridge's setting the bar is one line that says how to allow it, and it makes no call.

import { add, attr, clear, el, place } from '../dom.js';
import { errorText, text } from '../texts.js';

/** The data point types offered in the short list; any other is typed as main.sub. */
export const COMMON_TYPES = Object.freeze(['1.001', '1.008', '1.009', '5.001', '5.010', '7.001', '9.001', '9.004', '9.007', '12.001', '13.010', '14.056', '16.001']);
/** How many names are offered at most while an address is typed. */
export const SUGGESTIONS = 8;
/** Under this name and the bridge's the shell's store of the tab keeps "do not ask again"; it is gone with the tab or a sign-out. */
export const NO_ASK_KEY = 'monitor.send.noAsk';

/** {@return the name under which "do not ask again" is kept for one bridge} */
export function noAskKey(bridgeUid) {
    return `${NO_ASK_KEY}:${bridgeUid}`;
}

const DPT = /^\d{1,3}\.\d{1,4}$/;
const GROUP_KEY = /^ga:(\d{1,5}(?:\/\d{1,4}){0,2})$/;
const NUMBER_MAINS = new Set([5, 6, 7, 8, 9, 12, 13, 14]);
const UNITS = Object.freeze({ '5.001': '%', '5.003': '°', '7.013': 'lx', '9.001': '°C', '9.004': 'lx', '9.005': 'm/s', '9.007': '%', '9.008': 'ppm', '13.010': 'Wh', '13.013': 'kWh', '14.019': 'A', '14.027': 'V', '14.056': 'W' });

function textOf(value) {
    return typeof value === 'string' ? value : '';
}

/**
 * {@return the 16-bit number of a group address written in any of the three styles (main/middle/sub, main/sub, the
 *     number itself), or null when the text is none of them}
 */
export function rawOf(address) {
    const parts = /^(\d{1,5})(?:\/(\d{1,4}))?(?:\/(\d{1,3}))?$/.exec(textOf(address));
    if (!parts) {
        return null;
    }
    const [first, second, third] = parts.slice(1).map((part) => (part === undefined ? null : Number(part)));
    if (second === null) {
        return first >= 1 && first <= 65535 ? first : null;
    }
    if (first > 31) {
        return null;
    }
    if (third === null) {
        return second <= 2047 && (first !== 0 || second !== 0) ? (first << 11) | second : null;
    }
    const raw = (first << 11) | (second << 8) | third;
    return second <= 7 && third <= 255 && raw !== 0 ? raw : null;
}

/** {@return the address as main/middle/sub, the one form the server takes; a text that is no address stays as it is} */
export function threeLevel(address) {
    const raw = rawOf(address);
    return raw === null ? textOf(address) : `${raw >> 11}/${(raw >> 8) & 7}/${raw & 255}`;
}

/** {@return the kind of editor a data point type gets: switch, number or text} */
export function editorKind(dpt) {
    const written = textOf(dpt).trim();
    if (!DPT.test(written)) {
        return 'text';
    }
    const main = Number(written.split('.')[0]);
    return main === 1 ? 'switch' : NUMBER_MAINS.has(main) ? 'number' : 'text';
}

/**
 * @param {Object} options
 * @param options.context the view's context: document, api, bridge, rows, keep, split
 * @param {() => (Object|null)} options.selected the telegram selected in the list, or null
 * @returns {{node: Element, close: () => void}}
 */
export function createSendBar({ context, selected }) {
    const { document, api, bridge, rows } = context;
    const node = el(document, 'div', { class: 'send-bar' });
    if (bridge?.sendAllowed !== true) {
        add(document, node, el(document, 'p', { class: 'note send-off' }, text('monitor.send.notAllowed', text('monitor.send.setting'))));
        return { node, close() {} };
    }

    let closed = false;
    let busy = false;
    let pending = null;
    let known = [];
    let turn = 0;
    // whether this bar shows the line that says it sends at once; a bar drawn before the tick of another panel does not
    let saidNoAsk = false;
    const keepKey = noAskKey(textOf(bridge.uid));

    const field = (name, key, more = {}) => el(document, 'input', { type: 'text', name, class: name, autocomplete: 'off', 'aria-label': text(key), placeholder: text(key), ...more });
    const button = (name, key) => el(document, 'button', { type: 'button', class: name }, text(key));
    const addressField = field('send-address', 'monitor.send.address');
    const suggestions = el(document, 'ul', { class: 'send-suggestions', 'aria-label': text('monitor.send.names') });
    const useButton = button('send-use', 'monitor.send.use');
    const dptField = field('send-dpt', 'monitor.send.dpt', { maxlength: 8 });
    const commonField = el(
        document,
        'select',
        { name: 'send-dpt-common', class: 'send-dpt-common', 'aria-label': text('monitor.send.common') },
        el(document, 'option', { value: '' }, text('monitor.send.common')),
        COMMON_TYPES.map((type) => el(document, 'option', { value: type }, type)),
    );
    const valueBox = el(document, 'span', { class: 'send-value-box' });
    const readButton = button('send-read', 'monitor.send.read');
    const writeButton = button('send-write', 'monitor.send.write');
    const sendButton = button('send-go', 'monitor.send.go');
    const cancelButton = button('send-cancel', 'monitor.send.cancel');
    const askAgainButton = button('send-ask-again', 'monitor.send.askAgain');
    const noAskTick = el(document, 'input', { type: 'checkbox', name: 'send-no-ask', class: 'send-no-ask' });
    const confirmWords = el(document, 'span', { class: 'send-words' });
    const confirmLine = el(document, 'p', { class: 'send-confirm', role: 'status' }, confirmWords, ' ', sendButton, cancelButton, ' ', el(document, 'label', { class: 'tick' }, noAskTick, ' ', text('monitor.send.noAsk')));
    const noAskLine = el(document, 'p', { class: 'note send-no-ask-on' }, text('monitor.send.noAskOn'), ' ', askAgainButton);
    const answerLine = el(document, 'p', { class: 'note send-answer', role: 'status' });
    const fields = el(document, 'div', { class: 'send-fields' }, el(document, 'span', { class: 'send-address-box' }, addressField, suggestions), useButton, dptField, commonField, valueBox, readButton, writeButton);

    let valueKind = '';
    let valueField = null;
    let unit = null;

    function noAsk() {
        return context.keep?.get(keepKey) === true;
    }

    /** Makes the value's editor fit the type; what was typed stays while the kind of editor does. */
    function drawValue() {
        const dpt = dptField.value.trim();
        const kind = editorKind(dpt);
        if (kind !== valueKind) {
            valueKind = kind;
            if (kind === 'switch') {
                valueField = el(document, 'select', { name: 'send-value', class: 'send-value', 'aria-label': text('monitor.send.value') }, el(document, 'option', { value: 'ON' }, text('monitor.send.valueOn')), el(document, 'option', { value: 'OFF' }, text('monitor.send.valueOff')));
                valueField.value = 'ON';
            } else {
                valueField = field('send-value', 'monitor.send.value', kind === 'number' ? { type: 'number' } : { maxlength: 200 });
            }
            unit = el(document, 'span', { class: 'unit' });
        }
        clear(unit);
        add(document, unit, kind === 'number' && Object.hasOwn(UNITS, dpt) ? UNITS[dpt] : null);
        place(valueBox, [valueField, unit]);
    }

    /** {@return the project's entry of an address, written in the project's style or in another} */
    function entryOf(address) {
        const raw = rawOf(address);
        return known.find((entry) => entry.address === address) ?? (raw === null ? undefined : known.find((entry) => entry.raw === raw));
    }

    function nameOf(address) {
        return textOf(entryOf(address)?.name);
    }

    function say(words, bad = false) {
        clear(answerLine);
        add(document, answerLine, words);
        answerLine.classList.toggle('warn', bad);
    }

    function draw() {
        attr(readButton, 'disabled', busy);
        attr(writeButton, 'disabled', busy);
        attr(sendButton, 'disabled', busy);
        saidNoAsk = noAsk();
        place(node, [fields, pending ? confirmLine : null, saidNoAsk ? noAskLine : null, answerLine].filter(Boolean));
    }

    /** Takes the question back: what it said may no longer be what the fields hold. */
    function drop() {
        if (pending !== null) {
            pending = null;
            draw();
        }
    }

    function closeSuggestions() {
        clear(suggestions);
    }

    /** Fills the address, and the type the project gives it; sends nothing. */
    function setAddress(address, dataType) {
        addressField.value = address;
        closeSuggestions();
        const type = textOf(dataType) !== '' ? textOf(dataType) : textOf(entryOf(address)?.dataType);
        if (DPT.test(type)) {
            dptField.value = type;
            commonField.value = COMMON_TYPES.includes(type) ? type : '';
            drawValue();
        }
        drop();
    }

    function suggest() {
        closeSuggestions();
        const typed = addressField.value.trim().toLowerCase();
        if (typed === '') {
            return;
        }
        const found = known.filter((entry) => entry.address.startsWith(typed) || entry.name.toLowerCase().includes(typed)).slice(0, SUGGESTIONS);
        if (found.length === 1 && found[0].address === typed) {
            return;
        }
        add(
            document,
            suggestions,
            found.map((entry) => el(document, 'li', {}, el(document, 'button', { type: 'button', class: 'suggestion', value: entry.address }, el(document, 'span', { class: 'address' }, entry.address), ' ', el(document, 'span', { class: 'name' }, entry.name)))),
        );
    }

    function useSelected() {
        const telegram = selected();
        if (textOf(telegram?.destination) !== '') {
            setAddress(telegram.destination, telegram.dataType);
            say('');
            return;
        }
        const other = context.split?.other?.() ?? null;
        const key = GROUP_KEY.exec(textOf(other?.query?.row)) ?? GROUP_KEY.exec(textOf(other?.query?.sel));
        if (key) {
            setAddress(key[1]);
            say('');
        } else {
            say(text('monitor.send.nothingSelected'), true);
        }
    }

    /** {@return what one press on Read or Write would send and its words, or the words of what is missing} */
    function wanted(action) {
        const address = addressField.value.trim();
        if (address === '') {
            return { problem: text('monitor.send.needAddress') };
        }
        const name = nameOf(address);
        const sent = threeLevel(address);
        const written = sent === address ? address : text('monitor.send.sameAs', address, sent);
        const named = name === '' ? written : text('monitor.send.named', written, name);
        const where = textOf(bridge.label) === '' ? textOf(bridge.uid) : bridge.label;
        if (action === 'read') {
            return { address, request: { address: sent, action }, words: text('monitor.send.confirmRead', named, where) };
        }
        const dpt = dptField.value.trim();
        if (!DPT.test(dpt)) {
            return { problem: text('monitor.send.needType') };
        }
        const value = textOf(valueField.value).trim();
        if (value === '') {
            return { problem: text('monitor.send.needValue') };
        }
        const shown = valueKind === 'switch' ? text(value === 'ON' ? 'monitor.send.valueOn' : 'monitor.send.valueOff') : [value, unit.textContent].filter((part) => part !== '').join(' ');
        return { address, request: { address: sent, action, dpt, value }, words: text('monitor.send.confirmWrite', named, dpt, shown, where) };
    }

    /** @param shown the address as the bar holds it, in the project's style */
    async function send(request, shown) {
        if (busy || closed) {
            return;
        }
        busy = true;
        const mine = ++turn;
        say(text('monitor.send.sending'));
        draw();
        let words;
        let bad = false;
        try {
            const answer = await api.groupTelegram(bridge.uid, request);
            words = text(answer?.state === 'queued' ? 'monitor.send.queued' : 'monitor.send.sent', shown);
        } catch (error) {
            bad = true;
            words = errorText(error?.code, error?.text);
        }
        busy = false;
        if (!closed && mine === turn) {
            say(words, bad);
            draw();
        }
    }

    function press(action) {
        if (busy) {
            return;
        }
        const want = wanted(action);
        if (want.problem) {
            pending = null;
            say(want.problem, true);
            draw();
            return;
        }
        say('');
        if (noAsk() && saidNoAsk) {
            pending = null;
            send(want.request, want.address);
            return;
        }
        pending = want;
        clear(confirmWords);
        add(document, confirmWords, want.words);
        noAskTick.checked = false;
        draw();
        cancelButton.focus();
    }

    const onClick = (event) => {
        let target = event.target;
        while (target && target !== node && target.tagName !== 'BUTTON') {
            target = target.parentNode;
        }
        // the second click of a double click would be a second telegram
        if (event.detail > 1 && (target === readButton || target === writeButton || target === sendButton)) {
            return;
        }
        if (target === readButton) {
            press('read');
        } else if (target === writeButton) {
            press('write');
        } else if (target === sendButton && pending !== null && !busy) {
            const { request, address } = pending;
            pending = null;
            if (noAskTick.checked) {
                context.keep?.set(keepKey, true);
            }
            send(request, address);
        } else if (target === cancelButton) {
            drop();
            say('');
        } else if (target === askAgainButton) {
            context.keep?.delete(keepKey);
            draw();
        } else if (target === useButton) {
            useSelected();
        } else if (target?.classList?.contains('suggestion')) {
            setAddress(textOf(target.getAttribute('value')));
            addressField.focus();
        }
    };
    const onInput = (event) => {
        if (event.target === noAskTick) {
            return;
        }
        if (event.target === addressField) {
            suggest();
        } else if (event.target === dptField) {
            commonField.value = COMMON_TYPES.includes(dptField.value.trim()) ? dptField.value.trim() : '';
            drawValue();
        } else if (event.target === commonField && commonField.value !== '') {
            dptField.value = commonField.value;
            drawValue();
        }
        drop();
    };
    const onChange = (event) => {
        if (event.target === addressField) {
            const address = addressField.value.trim();
            if (known.some((entry) => entry.address === address)) {
                setAddress(address);
                return;
            }
        }
        onInput(event);
    };
    const onKey = (event) => {
        if (event.key === 'Escape' && suggestions.children.length > 0) {
            event.stopPropagation();
            closeSuggestions();
        }
    };
    node.addEventListener('click', onClick);
    node.addEventListener('input', onInput);
    node.addEventListener('change', onChange);
    node.addEventListener('keydown', onKey);

    drawValue();
    draw();
    // the project's names, for the suggestions and the type of an address; without a project the fields are typed
    if (bridge.projectState === 'LOADED' && typeof rows?.groupAddresses === 'function') {
        Promise.resolve()
            .then(() => rows.groupAddresses())
            .then((list) => {
                if (!closed && Array.isArray(list)) {
                    known = list.filter((entry) => textOf(entry?.address) !== '').map((entry) => ({ address: entry.address, raw: Number.isInteger(entry.raw) ? entry.raw : rawOf(entry.address), name: textOf(entry.name), dataType: textOf(entry.dataType) }));
                }
            })
            .catch(() => {
                // the fields work without names
            });
    }

    return {
        node,
        close() {
            closed = true;
            node.removeEventListener('click', onClick);
            node.removeEventListener('input', onInput);
            node.removeEventListener('change', onChange);
            node.removeEventListener('keydown', onKey);
        },
    };
}
