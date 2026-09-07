/**
 * Diese Datei enthält die Konfiguration für die Code Coverage in den E2E-Tests des Projekts.
 */
import playwrightCoverage, { PlaywrightCoverage } from '@soroush.tech/playwright-coverage';

export const e2eCoverage: PlaywrightCoverage = playwrightCoverage({
  enabled: true,
  include: ['src/app/**/*.ts'],
  exclude: ['**/*.spec.ts'],
  report: {
    name: 'E2E Coverage',
    outputDir: './coverage/e2e',
    lcov: true,
    reports: ['console-summary', 'v8'],
  },
});
