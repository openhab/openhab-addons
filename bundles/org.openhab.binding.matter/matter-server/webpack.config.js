const path = require('path');

module.exports = {
    entry: './src/app.ts',
    target: 'node', 
    externals: {bufferutil: "bufferutil", "utf-8-validate": "utf-8-validate"},
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
