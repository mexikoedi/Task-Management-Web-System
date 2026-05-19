/**
 * @see https://stylelint.io/user-guide/configure
 * @type {import('stylelint').Config}
 */
const config = {
  extends: ['stylelint-config-standard'],
  rules: {
    'block-no-empty': true,
  },
};

export default config;
