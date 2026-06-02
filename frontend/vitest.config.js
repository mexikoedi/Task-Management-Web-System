/**
 * @see https://vitest.dev/config
 */
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'json', 'lcov'],
      exclude: ['src/app/service/**', 'src/app/shared/**', '*.html'],
    },
  },
});
