const path = require('path');

module.exports = {
  entry: './index.js',
  mode: 'development',
  externals: [
    {
      ["@runtime"]: {
        root: "@runtime",
        commonjs: '@runtime',
        commonjs2: '@runtime',
        amd: '@runtime',
      },
      ["@runtime/Defaults"]: {
        root: "@runtime/Defaults",
        commonjs: '@runtime/Defaults',
        commonjs2: '@runtime/Defaults',
        amd: '@runtime/Defaults',
      },
      ["@runtime/provider"]: {
        root: "@runtime/provider",
        commonjs: '@runtime/provider',
        commonjs2: '@runtime/provider',
        amd: '@runtime/provider',
      },
      ["@runtime/RuleSupport"]: {
        root: "@runtime/RuleSupport",
        commonjs: '@runtime/RuleSupport',
        commonjs2: '@runtime/RuleSupport',
        amd: '@runtime/RuleSupport',
      },
      ["@runtime/osgi"]: {
        root: "@runtime/osgi",
        commonjs: '@runtime/osgi',
        commonjs2: '@runtime/osgi',
        amd: '@runtime/osgi',
      }
    }
  ],
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'oh.js',
    library: {
      name: "oh",
      type: "umd"
    },
    globalObject: 'this',
  }
};

