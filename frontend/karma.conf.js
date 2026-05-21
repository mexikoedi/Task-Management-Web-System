// Karma configuration file for Angular CLI project.
import { join } from 'node:path';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
import karmaJasmine from 'karma-jasmine';
import karmaChromeLauncher from 'karma-chrome-launcher';
import karmaJasmineHtmlReporter from 'karma-jasmine-html-reporter';
import karmaCoverage from 'karma-coverage';
const angularDevkitKarma = require('@angular-devkit/build-angular/plugins/karma');

const config = {
  basePath: '',
  frameworks: ['jasmine', '@angular-devkit/build-angular'],
  plugins: [karmaJasmine, karmaChromeLauncher, karmaJasmineHtmlReporter, karmaCoverage, angularDevkitKarma],
  client: {
    jasmine: {},
    clearContext: false,
  },
  jasmineHtmlReporter: {
    suppressAll: true,
  },
  coverageReporter: {
    dir: join(process.cwd(), './coverage/frontend'),
    subdir: '.',
    reporters: [{ type: 'html' }, { type: 'text-summary' }],
  },
  reporters: ['progress'],
  port: 9876,
  colors: true,
  logLevel: 'INFO',
  autoWatch: false,
  browsers: ['ChromeHeadlessNoSandbox'],
  customLaunchers: {
    ChromeHeadlessNoSandbox: {
      base: 'ChromeHeadless',
      flags: ['--no-sandbox', '--disable-gpu'],
    },
  },
  singleRun: true,
  restartOnFileChange: false,
};

export default function (karmaConfig) {
  karmaConfig.set(config);
}
