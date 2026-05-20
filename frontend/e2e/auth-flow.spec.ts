import { expect, test } from '@playwright/test';

test('redirects root to login and shows auth form', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
  await expect(
    page.getByRole('button', { name: /anmelden|login/i })
  ).toBeVisible();
});
