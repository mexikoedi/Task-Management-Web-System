// Karma configuration file for Angular CLI project
import { join } from "node:path";
import karmaJasmine from "karma-jasmine";
import karmaChromeLauncher from "karma-chrome-launcher";
import karmaJasmineHtmlReporter from "karma-jasmine-html-reporter";
import karmaCoverage from "karma-coverage";
import angularDevkitKarma from "@angular-devkit/build-angular/plugins/karma.js";

export default function (config) {
  config.set({
    basePath: "",
    frameworks: ["jasmine", "@angular-devkit/build-angular"],
    plugins: [
      karmaJasmine,
      karmaChromeLauncher,
      karmaJasmineHtmlReporter,
      karmaCoverage,
      angularDevkitKarma
    ],
    client: {
      jasmine: {},
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: join(process.cwd(), "./coverage/frontend"),
      subdir: ".",
      reporters: [
        { type: "html" },
        { type: "text-summary" }
      ]
    },
    reporters: ["progress"],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ["ChromeHeadlessNoSandbox"],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: "ChromeHeadless",
        flags: ["--no-sandbox", "--disable-gpu"]
      }
    },
    singleRun: true,
    restartOnFileChange: false
  });
}
