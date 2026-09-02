const path = require('path');
const webpack = require('webpack');

module.exports = {
    entry: './src/app.ts',
    target: 'node', 
    externals: [
        {bufferutil: "bufferutil", "utf-8-validate": "utf-8-validate"},
        // Exclude Bun-specific modules (we're targeting Node.js, not Bun)
        function({ request }, callback) {
            if (request && (request.startsWith('bun:') || request === 'bun')) {
                return callback(null, 'commonjs ' + request);
            }
            callback();
        }
    ],
    plugins: [
        // Ignore Bun-specific platform files since we're targeting Node.js
        new webpack.IgnorePlugin({
            resourceRegExp: /BunSqlite\.js$/,
            contextRegExp: /@matter\/nodejs\/dist\/cjs\/storage\/sqlite\/platform/
        })
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader'
            }
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js']
    },
    output: {
        filename: 'matter.js', // single output JS file
        path: path.resolve(__dirname, 'dist')
    }
};
