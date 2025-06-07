import { FlatCompat } from "@eslint/eslintrc";
import js from "@eslint/js";
import tsParser from "@typescript-eslint/parser";
import regexp from "eslint-plugin-regexp";
import { globalIgnores } from "eslint/config";
import globals from "globals";
import path from "node:path";
import { fileURLToPath } from "node:url";
import ts from "typescript-eslint";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all,
});

export default [
    globalIgnores([
        "**/dist/**/*",
        "**/build/**/*",
        "**/forwards/**/*",
        "**/bin/*",
        "**/require/*",
        "docs/**/*",
        "**/.mocharc.cjs",
        "eslint.config.mjs",
        "webpack.config.js",
    ]),
    js.configs.recommended,
    ...ts.configs.recommendedTypeChecked,
    regexp.configs["flat/recommended"],

    {
        linterOptions: {
            reportUnusedDisableDirectives: true,
        },

        languageOptions: {
            globals: {
                ...globals.node,
            },

            parser: tsParser,
            ecmaVersion: 2022,
            sourceType: "module",

            parserOptions: {
                projectService: [],
            },
        },

        rules: {
            "@typescript-eslint/no-explicit-any": "off",
            "@typescript-eslint/no-empty-object-type": "off",
            "@typescript-eslint/require-await": "off",
            "@typescript-eslint/no-unsafe-argument": "off",
            "@typescript-eslint/no-unsafe-assignment": "off",
            "@typescript-eslint/no-unsafe-member-access": "off",
            "@typescript-eslint/no-unsafe-return": "off",
            "@typescript-eslint/no-unsafe-call": "off",
            "@typescript-eslint/restrict-template-expressions": "off",
            "@typescript-eslint/no-base-to-string": "off",
            "no-constant-condition": [
                "error",
                {
                    checkLoops: false,
                },
            ],
            "@typescript-eslint/no-namespace": "off",
            "no-inner-declarations": "off",
            "no-case-declarations": "off",
            "@typescript-eslint/no-implied-eval": "off",
            "@typescript-eslint/no-this-alias": "off",
            "import/default": "off",
            "import/export": "off",
            "import/no-named-as-default-member": "off",
            "@typescript-eslint/no-unsafe-enum-comparison": "off",
            "@typescript-eslint/no-unused-vars": "off",
            "@typescript-eslint/unbound-method": "off",
            "no-ex-assign": "off",
            "@typescript-eslint/no-redundant-type-constituents": "off",
            "import/no-unresolved": "off",
            "regexp/optimal-quantifier-concatenation": "off",
        },
    }
];
