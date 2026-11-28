import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';

const TESTS = new URL('./', import.meta.url);

const GROUP = /(?<![\w./])\d{1,5}(?:\/\d{1,5}){1,2}(?![\w/])/g;
const INDIVIDUAL = /(?<![\w.])\d{1,3}\.\d{1,3}\.\d{1,3}(?![\w.])/g;

function allowedGroup(address) {
    const parts = address.split('/').map(Number);
    if (parts[0] !== 31) {
        return false;
    }
    return parts.length === 3 ? parts[1] <= 7 && parts[2] <= 255 : parts[1] <= 2047;
}

function allowedIndividual(address) {
    const parts = address.split('.').map(Number);
    return (parts[0] === 7 || parts[0] === 31) && parts[1] <= 15 && parts[2] <= 255;
}

test('fixtures: every address written in a test is an individual address of area 7 or 31 or a group address of main group 31', () => {
    const found = [];
    for (const name of readdirSync(TESTS).filter((file) => file.endsWith('.js')).sort()) {
        const lines = readFileSync(new URL(name, TESTS), 'utf8').split('\n');
        lines.forEach((line, at) => {
            for (const [address] of line.matchAll(GROUP)) {
                if (!allowedGroup(address)) {
                    found.push(`${name}:${at + 1} ${address}`);
                }
            }
            for (const [address] of line.matchAll(INDIVIDUAL)) {
                if (!allowedIndividual(address)) {
                    found.push(`${name}:${at + 1} ${address}`);
                }
            }
        });
    }
    assert.deepEqual(found, []);
});
