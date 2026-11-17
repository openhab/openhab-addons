// The event-stream format: bytes in, events out. Lines end with LF, CR or CRLF, and a chunk may end anywhere, also
// inside a UTF-8 character or between CR and LF.

/** An event or a line longer than this is dropped whole; the server's events are far below it. */
export const MAX_EVENT_CHARS = 1024 * 1024;

const CR = 13;
const LF = 10;

/**
 * @param {(event: {event: string, data: string}) => void} onEvent called once per finished event that has data
 * @returns {{push: (bytes: Uint8Array) => void, end: () => void, held: () => number}}
 */
export function createSseParser(onEvent) {
    let decoder = new TextDecoder('utf-8');
    let line = '';
    let skipLF = false;
    let name = '';
    let data = [];
    let size = 0;
    let tooLarge = false;
    let lineTooLong = false;

    function reset() {
        name = '';
        data = [];
        size = 0;
        tooLarge = false;
    }

    function dispatch() {
        const finished = { event: name === '' ? 'message' : name, data: data.join('\n') };
        const send = data.length > 0 && !tooLarge;
        reset();
        if (send) {
            onEvent(finished);
        }
    }

    function field(text) {
        if (text === '') {
            dispatch();
            return;
        }
        if (text.charCodeAt(0) === 58) {
            return;
        }
        const colon = text.indexOf(':');
        const key = colon < 0 ? text : text.slice(0, colon);
        let value = colon < 0 ? '' : text.slice(colon + 1);
        if (value.charCodeAt(0) === 32) {
            value = value.slice(1);
        }
        if (key === 'event') {
            name = value;
        } else if (key === 'data' && !tooLarge) {
            size += value.length + 1;
            if (size > MAX_EVENT_CHARS) {
                tooLarge = true;
                data = [];
                size = 0;
            } else {
                data.push(value);
            }
        }
    }

    function feed(text) {
        let start = 0;
        if (skipLF && text.length > 0) {
            if (text.charCodeAt(0) === LF) {
                start = 1;
            }
            skipLF = false;
        }
        for (let at = start; at < text.length; at++) {
            const code = text.charCodeAt(at);
            if (code !== CR && code !== LF) {
                continue;
            }
            const whole = line + text.slice(start, at);
            line = '';
            if (lineTooLong || whole.length > MAX_EVENT_CHARS) {
                lineTooLong = false;
                tooLarge = true;
                data = [];
                size = 0;
            } else {
                field(whole);
            }
            if (code === CR) {
                if (at + 1 < text.length) {
                    if (text.charCodeAt(at + 1) === LF) {
                        at++;
                    }
                } else {
                    skipLF = true;
                }
            }
            start = at + 1;
        }
        line += text.slice(start);
        if (line.length > MAX_EVENT_CHARS) {
            line = '';
            lineTooLong = true;
        }
    }

    return {
        push(bytes) {
            feed(decoder.decode(bytes, { stream: true }));
        },
        end() {
            decoder = new TextDecoder('utf-8');
            line = '';
            skipLF = false;
            lineTooLong = false;
            reset();
        },
        /** {@return how many characters of an unfinished line and event the parser holds} */
        held() {
            return line.length + size;
        },
    };
}
