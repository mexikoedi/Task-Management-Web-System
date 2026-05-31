/**
 * E2E-Tests für Authentifizierungs-Flows und grundlegende Dashboard-Zugriffe.
 * Die Tests stellen sicher, dass die Authentifizierungsseiten korrekt funktionieren, einschließlich Client-Validierung,
 * Navigation zwischen Login/Registrierung/Reset, und dass erfolgreiche Logins zum Dashboard führen.
 */
import {
  expect,
  test,
  Page,
  Route,
  PlaywrightTestArgs,
  PlaywrightTestOptions,
  PlaywrightWorkerArgs,
  PlaywrightWorkerOptions,
  Locator,
} from '@playwright/test';

const api = {
  login: '**/api/auth/login',
  register: '**/api/auth/register',
  passwordResetRequest: '**/api/auth/password-reset',
  verifyEmail: '**/api/auth/verify-email**',
  resetPassword: '**/api/auth/reset-password**',
  me: '**/api/auth/me',
  boards: '**/api/boards',
  boardById: '**/api/boards/*',
};

const selectors = {
  loginEmail: '#login-email',
  loginPassword: '#login-password',
  loginSubmit: 'button[type="submit"]',
  loginRegisterLink: 'button:has-text("Neues Konto erstellen")',
  loginResetLink: 'button:has-text("Passwort vergessen?")',
  registerName: '#register-name',
  registerEmail: '#register-email',
  registerPassword: '#register-password',
  registerPasswordConfirm: '#register-password-confirm',
  registerSubmit: 'button[type="submit"]',
  registerLoginLink: 'button:has-text("Hier anmelden")',
  resetEmail: '#reset-email',
  resetSubmit: 'button[type="submit"]',
  resetLoginLink: 'button:has-text("Zurück zur Anmeldung")',
  resetPasswordInput: '#password',
  resetPasswordSubmit: 'button[type="submit"]',
  dashboardHeader: '.app-header',
};

const mockUser = {
  id: 1,
  name: 'Max Mustermann',
  email: 'max@test.de',
  image: null,
  emailVerified: true,
  emailChanged: false,
};

const mockBoard = {
  id: 1,
  owner: mockUser,
  title: 'Board A',
  background: null,
  columns: [],
  members: [mockUser],
};

async function mockAuthRoutes(page: Page): Promise<void> {
  await page.route(api.login, async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ token: 'fake-token' }),
    });
  });

  await page.route(api.register, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
  });

  await page.route(api.passwordResetRequest, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
  });

  await page.route(api.verifyEmail, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
  });

  await page.route(api.resetPassword, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
  });

  await page.route(api.me, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockUser) });
  });
}

async function mockBoardRoutes(page: Page): Promise<void> {
  await page.route(api.boards, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockBoard]) });
  });

  await page.route(api.boardById, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockBoard) });
  });
}

async function gotoLogin(page: Page): Promise<void> {
  await page.goto('/login');
  await expect(page).toHaveURL(/\/login$/);
}

test('Leitet von Root zur Anmeldeseite weiter und zeigt das Authentifizierungsformular an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('button', { name: /anmelden|login/i })).toBeVisible();
});

test('Zeigt Client-Validierung im Login an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.locator(selectors.loginEmail).focus();
  await page.locator(selectors.loginEmail).blur();
  await page.locator(selectors.loginPassword).focus();
  await page.locator(selectors.loginPassword).blur();
  await expect(page.locator('.error-text').first()).toBeVisible();
});

test('Wechselt zwischen Login, Registrierung und Reset.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.click(selectors.loginRegisterLink);
  await expect(page.getByRole('heading', { name: /konto erstellen/i })).toBeVisible();
  await page.click(selectors.registerLoginLink);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
  await page.click(selectors.loginResetLink);
  await expect(page.getByRole('heading', { name: /passwort zurücksetzen/i })).toBeVisible();
  await page.click(selectors.resetLoginLink);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('Zeigt Client-Validierung in der Registrierung an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.click(selectors.loginRegisterLink);
  await page.fill(selectors.registerName, 'A');
  await page.locator(selectors.registerName).blur();
  await page.fill(selectors.registerEmail, 'invalid-email');
  await page.locator(selectors.registerEmail).blur();
  await page.fill(selectors.registerPassword, 'abc');
  await page.locator(selectors.registerPassword).blur();
  await page.fill(selectors.registerPasswordConfirm, 'xyz');
  await page.locator(selectors.registerPasswordConfirm).blur();
  await expect(page.locator('.error-text').first()).toBeVisible();
});

test('Zeigt Client-Validierung im Reset-Formular an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.click(selectors.loginResetLink);
  await page.locator(selectors.resetEmail).focus();
  await page.locator(selectors.resetEmail).blur();
  await expect(page.locator('.error-text').first()).toBeVisible();
});

test('Login-Toggle für Passwortsichtbarkeit funktioniert.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  const pwdInput: Locator = page.locator(selectors.loginPassword);
  await expect(pwdInput).toHaveAttribute('type', 'password');
  await page.click('button.password-toggle');
  await expect(pwdInput).toHaveAttribute('type', 'text');
});

test('Registrierung zeigt Passwortanforderungen an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.click(selectors.loginRegisterLink);
  await page.fill(selectors.registerPassword, 'Abc123!@#');
  await expect(page.locator('.password-requirements')).toBeVisible();
});

test('Erfolgreiches Login navigiert zum Dashboard.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await mockBoardRoutes(page);
  await gotoLogin(page);
  await page.fill(selectors.loginEmail, 'max@test.de');
  await page.fill(selectors.loginPassword, 'Passwort123!');
  await page.click(selectors.loginSubmit);
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.locator(selectors.dashboardHeader)).toBeVisible();
});

test('Login-Fehler zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.login, async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Login fehlgeschlagen' }),
    });
  });

  await gotoLogin(page);
  await page.fill(selectors.loginEmail, 'max@test.de');
  await page.fill(selectors.loginPassword, 'falsch');
  await page.click(selectors.loginSubmit);
  await expect(page.locator('.error-message')).toBeVisible();
});

test('Registrierung erfolgreich zeigt Erfolgsmeldung und wechselt zurück zu Login.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await gotoLogin(page);
  await page.click(selectors.loginRegisterLink);
  await page.fill(selectors.registerName, 'Max Mustermann');
  await page.fill(selectors.registerEmail, 'max@test.de');
  await page.fill(selectors.registerPassword, 'Abc123!@#');
  await page.fill(selectors.registerPasswordConfirm, 'Abc123!@#');
  await page.click(selectors.registerSubmit);
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await page.waitForTimeout(4200);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('Passwort-Reset-Request erfolgreich zeigt Erfolgsmeldung und wechselt zurück zu Login.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await gotoLogin(page);
  await page.click(selectors.loginResetLink);
  await page.fill(selectors.resetEmail, 'max@test.de');
  await page.click(selectors.resetSubmit);
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await page.waitForTimeout(4200);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('Verify-Email ohne Token zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/verify-email');
  await expect(page.locator('.error')).toBeVisible();
  await expect(page.getByRole('button', { name: /zurück zur anmeldung/i })).toBeVisible();
});

test('Verify-Email mit Token zeigt Erfolg und leitet weiter.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await page.goto('/verify-email?token=fake-token');
  await expect(page.locator('.success')).toBeVisible({ timeout: 6000 });
  await expect(page).toHaveURL(/\/login\?verified=true$/, { timeout: 6000 });
});

test('Reset-Password ohne Token zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/reset-password');
  await expect(page.locator('.error-message')).toBeVisible();
});

test('Reset-Password mit Token zeigt Formular und akzeptiert Submit.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);

  await page.goto('/reset-password?token=fake-token');
  await expect(page.locator(selectors.resetPasswordInput)).toBeVisible();
  await page.fill(selectors.resetPasswordInput, 'Abc123!@#');
  await page.click(selectors.resetPasswordSubmit);
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await expect(page).toHaveURL(/\/login$/, { timeout: 6000 });
});

test('Auth-Guard leitet ohne Token auf Login mit returnUrl um.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/dashboard');
  await expect(page).toHaveURL(/\/login\?returnUrl=%2Fdashboard/);
});

test('Auth-Guard erlaubt Zugriff auf Dashboard mit Token.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await mockBoardRoutes(page);

  await page.addInitScript((): void => {
    sessionStorage.setItem('token', 'fake-token');
  });

  await page.goto('/dashboard');
  await expect(page.locator(selectors.dashboardHeader)).toBeVisible();
});
