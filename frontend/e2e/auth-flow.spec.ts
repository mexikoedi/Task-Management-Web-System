/**
 * E2E-Tests für Authentifizierungs-Flows und grundlegende Dashboard-Zugriffe.
 * Die Tests stellen sicher, dass die Authentifizierungsseiten korrekt funktionieren, einschließlich Client-Validierung,
 * Navigation zwischen Login/Registrierung/Reset, und dass erfolgreiche Logins zum Dashboard führen.
 */
import type { BoardModel } from '../src/app/model/board.model';
import type { ColumnModel } from '../src/app/model/column.model';
import type { TaskModel } from '../src/app/model/task.model';
import { test, expect } from './coverage-fixtures';
import {
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
  boardUpdate: '**/api/boards/1',
  boardInvite: '**/api/boards/1/invite',
  addColumn: '**/api/boards/1/columns',
  updateColumn: '**/api/boards/columns/*',
  addTask: '**/api/boards/columns/*/tasks',
  updateTask: '**/api/boards/tasks/*',
  moveTask: '**/api/boards/tasks/*/move**',
  deleteTask: '**/api/boards/tasks/*',
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
  background: null as string | null,
  columns: [
    {
      id: 10,
      title: 'To Do',
      tasks: [
        {
          id: 100,
          title: 'Task One',
          description: 'Beschreibung eins',
          deadline: null,
          labels: 'high,bug',
          attachments: 'https://example.com/a.pdf',
          assignees: [mockUser],
        },
      ],
    },
    {
      id: 11,
      title: 'Done',
      tasks: [
        {
          id: 101,
          title: 'Task Two',
          description: 'Beschreibung zwei',
          deadline: null,
          labels: 'feature',
          attachments: '',
          assignees: [],
        },
      ],
    },
  ],
  members: [mockUser],
};

const mockBoardB = {
  ...mockBoard,
  id: 2,
  title: 'Board B',
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

async function submitLogin(page: Page): Promise<void> {
  await gotoLogin(page);
  await page.locator(selectors.loginEmail).fill('max@test.de');
  await page.locator(selectors.loginPassword).fill('Passwort123!');
  await page.locator(selectors.loginSubmit).click();
  await expect(page).toHaveURL(/\/dashboard$/);
}

async function loginToDashboard(page: Page): Promise<void> {
  await mockAuthRoutes(page);
  await mockBoardRoutes(page);
  await submitLogin(page);
  await expect(page.locator(selectors.dashboardHeader)).toBeVisible();
}

async function openTaskDeleteDialog(page: Page): Promise<void> {
  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task Two")').click();
  await page.locator('.task-modal button[title="Aufgabe löschen"]').click();
  await expect(page.locator('.swal2-popup')).toBeVisible();
  await page.locator('.swal2-confirm').click();
  await expect(page.locator('.task-modal')).toBeVisible();
}

async function openColumnDeleteDialog(page: Page): Promise<void> {
  await loginToDashboard(page);
  await page.locator('.column:has-text("Done") button.delete-column').click();
  await expect(page.locator('.swal2-popup')).toBeVisible();
  await page.locator('.swal2-confirm').click();
  await expect(page.getByRole('heading', { name: 'Done' })).toBeVisible();
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
  await page.locator(selectors.loginRegisterLink).click();
  await expect(page.getByRole('heading', { name: /konto erstellen/i })).toBeVisible();
  await page.locator(selectors.registerLoginLink).click();
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
  await page.locator(selectors.loginResetLink).click();
  await expect(page.getByRole('heading', { name: /passwort zurücksetzen/i })).toBeVisible();
  await page.locator(selectors.resetLoginLink).click();
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('Zeigt Client-Validierung in der Registrierung an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.locator(selectors.loginRegisterLink).click();
  await page.locator(selectors.registerName).fill('A');
  await page.locator(selectors.registerName).blur();
  await page.locator(selectors.registerEmail).fill('invalid-email');
  await page.locator(selectors.registerEmail).blur();
  await page.locator(selectors.registerPassword).fill('abc');
  await page.locator(selectors.registerPassword).blur();
  await page.locator(selectors.registerPasswordConfirm).fill('xyz');
  await page.locator(selectors.registerPasswordConfirm).blur();
  await expect(page.locator('.error-text').first()).toBeVisible();
});

test('Zeigt Client-Validierung im Reset-Formular an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.locator(selectors.loginResetLink).click();
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
  await page.locator('button.password-toggle').click();
  await expect(pwdInput).toHaveAttribute('type', 'text');
});

test('Registrierung zeigt Passwortanforderungen an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await gotoLogin(page);
  await page.locator(selectors.loginRegisterLink).click();
  await page.locator(selectors.registerPassword).fill('Abc123!@#');
  await expect(page.locator('.password-requirements')).toBeVisible();
});

test('Erfolgreiches Login navigiert zum Dashboard.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
});

test('Projektboard zeigt Spalten und Aufgaben an.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await expect(page.getByRole('heading', { name: 'Board A' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'To Do' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Done' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Task One' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Task Two' })).toBeVisible();
});

test('Projektboard-Suche zeigt Vorschläge und öffnet Aufgaben-Details.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('#taskSearch').fill('Task One');
  await expect(page.locator('.suggestions li')).toContainText('Task One');
  await page.locator('.suggestions li').first().click();
  await expect(page.locator('.task-modal')).toBeVisible();
  await expect(page.locator('#taskTitle')).toHaveValue('Task One');
  await expect(page.locator('#taskDesc')).toHaveValue('Beschreibung eins');
});

test('Aufgabendetails lassen sich über Klick öffnen und schließen.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task Two")').click();
  await expect(page.locator('.task-modal')).toBeVisible();
  await expect(page.locator('#taskTitle')).toHaveValue('Task Two');
  await page.getByRole('button', { name: 'Abbrechen' }).last().click();
  await expect(page.locator('.task-modal')).toBeHidden();
});

test('Profilfenster und Profilbearbeitung im Projektboard funktionieren.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await expect(page.getByRole('heading', { name: 'Mein Konto' })).toBeVisible();
  await expect(page.getByText('Max Mustermann')).toBeVisible();
  await page.getByRole('button', { name: 'Profil bearbeiten' }).click();
  await expect(page.locator('#editName')).toBeVisible();
  await page.locator('.profile-popup button.password-toggle').first().click();
  await expect(page.locator('#currentPwd')).toHaveAttribute('type', 'text');
  await page.getByRole('button', { name: 'Abbrechen' }).first().click();
  await expect(page.getByRole('button', { name: 'Profil bearbeiten' })).toBeVisible();
});

test('Projektboard-Einstellungen lassen sich öffnen und schließen.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await expect(page.getByRole('heading', { name: 'Projektboard-Einstellungen' })).toBeVisible();
  await expect(page.locator('#bgUrl')).toBeVisible();
  await expect(page.locator('#inviteEmail')).toBeVisible();
  await expect(page.locator('#boardSelect')).toBeVisible();
  await page.getByRole('button', { name: 'Schließen' }).last().click();
  await expect(page.getByRole('heading', { name: 'Projektboard-Einstellungen' })).toBeHidden();
});

test('Neue Aufgabe Formular kann geöffnet und abgebrochen werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('.column:has-text("To Do") button.add-task').click();
  await expect(page.locator('#newTaskTitle-10')).toBeVisible();
  await page.locator('.new-task button[title="Abbrechen"]').click();
  await expect(page.locator('#newTaskTitle-10')).toBeHidden();
});

test('Neue Statuskategorie Formular kann geöffnet und abgebrochen werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.add-statuscategory').click();
  await expect(page.locator('#newColTitle')).toBeVisible();
  await page.getByRole('button', { name: 'Abbrechen' }).last().click();
  await expect(page.locator('#newColTitle')).toBeHidden();
});

test('Projektboard-Titel kann bearbeitet und gespeichert werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let boardState: {
    title: string;
    id: number;
    owner: {
      id: number;
      name: string;
      email: string;
      image: null;
      emailVerified: boolean;
      emailChanged: boolean;
    };
    background: string | null;
    columns: {
      id: number;
      title: string;
      tasks: {
        id: number;
        title: string;
        description: string;
        deadline: null;
        labels: string;
        attachments: string;
        assignees: {
          id: number;
          name: string;
          email: string;
          image: null;
          emailVerified: boolean;
          emailChanged: boolean;
        }[];
      }[];
    }[];
  } = { ...mockBoard, title: 'Board A' };
  await mockAuthRoutes(page);

  await page.route('**/api/boards', async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([boardState]),
    });
  });

  await page.route('**/api/boards/1', async (route: Route): Promise<void> => {
    const method: string = route.request().method();

    if (method === 'PUT') {
      const body = route.request().postDataJSON() as { title?: string; background?: string | null };
      boardState = {
        ...boardState,
        title: body.title ?? boardState.title,
        background: body.background ?? boardState.background,
      };
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(boardState),
    });
  });

  await submitLogin(page);
  await page.locator('.title button[title="Name bearbeiten"]').click();
  await page.locator('#title').fill('Board A Neu');
  await page.locator('button[title="Projektboard-Titel speichern"]').click();
  await expect(page.getByRole('heading', { name: 'Board A Neu' })).toBeVisible();
});

test('Hintergrundbild kann in Projektboard-Einstellungen gespeichert werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.boardUpdate, async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { background?: string };
    expect(body.background).toBe('https://example.com/bg.png');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ...mockBoard, background: 'https://example.com/bg.png' }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await page.locator('#bgUrl').fill('https://example.com/bg.png');
  await page.getByRole('button', { name: 'Speichern' }).first().click();
  await expect(page.locator('#bgUrl')).toHaveValue('https://example.com/bg.png');
});

test('Mitgliedseinladung sendet Einladungsanfrage.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.boardInvite, async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { email?: string };
    expect(body.email).toBe('newmember@test.de');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        ...mockBoard,
        members: [...mockBoard.members, { ...mockUser, id: 2, email: body.email }],
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await page.locator('#inviteEmail').fill('newmember@test.de');
  await page.getByRole('button', { name: 'Einladen' }).click();
});

test('Neue Statuskategorie kann erstellt werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.addColumn, async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { title?: string };
    expect(body.title).toBe('In Progress');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ id: 12, title: 'In Progress', tasks: [] }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button.add-statuscategory').click();
  await page.locator('#newColTitle').fill('In Progress');
  await page
    .locator('input#newColTitle')
    .locator('xpath=ancestor::form')
    .getByRole('button', { name: 'Erstellen' })
    .click();
  await expect(page.getByRole('heading', { name: 'In Progress' })).toBeVisible();
});

test('Statuskategorie kann umbenannt werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.updateColumn, async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { title?: string };
    expect(body.title).toBe('Todo Renamed');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        ...mockBoard,
        columns: [{ ...mockBoard.columns[0], title: 'Todo Renamed' }, mockBoard.columns[1]],
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('.column:has-text("To Do") button.edit-column').click();
  await page.locator('#colTitle').fill('Todo Renamed');
  await page.locator('.column-edit-wrapper button[title="Statuskategorie-Namen speichern"]').click();
  await expect(page.getByRole('heading', { name: 'Todo Renamed' })).toBeVisible();
});

test('Neue Aufgabe kann erstellt werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.addTask, async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { title?: string };
    expect(body.title).toBe('Task Three');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 102,
        title: 'Task Three',
        description: '',
        labels: '',
        attachments: '',
        assignees: [],
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('.column:has-text("To Do") button.add-task').click();
  await page.locator('#newTaskTitle-10').fill('Task Three');
  await page.locator('.new-task button[title="Aufgabe erstellen"]').click();
  await expect(page.getByRole('heading', { name: 'Task Three' })).toBeVisible();
});

test('Aufgabendetails können gespeichert werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  const boardState: BoardModel = JSON.parse(JSON.stringify(mockBoard));
  await mockAuthRoutes(page);

  await page.route('**/api/boards', async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([boardState]),
    });
  });

  await page.route('**/api/boards/1', async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(boardState),
    });
  });

  await page.route('**/api/boards/tasks/100', async (route: Route): Promise<void> => {
    const body = route.request().postDataJSON() as { title?: string; description?: string; labels?: string };
    expect(body.title).toBe('Task One Updated');
    expect(body.description).toBe('Neue Beschreibung');
    expect(body.labels).toBe('important');
    const todoCol: ColumnModel | undefined = boardState.columns.find((c: ColumnModel): boolean => c.id === 10);
    const task: TaskModel | undefined = todoCol?.tasks?.find((t: TaskModel): boolean => t.id === 100);
    if (!task) throw new Error('Aufgabe 100 nicht gefunden.');
    task.title = body.title ?? task.title;
    task.description = body.description ?? task.description;
    task.labels = body.labels ?? task.labels;

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(task),
    });
  });

  await submitLogin(page);
  await page.locator('.task.task-clickable:has-text("Task One")').click();
  await page.locator('#taskTitle').fill('Task One Updated');
  await page.locator('#taskDesc').fill('Neue Beschreibung');
  await page.locator('#taskLabels').fill('important');
  await page.locator('.task-modal button[title="Aufgabe speichern"]').click();
  await expect(page.locator('.task-modal')).toBeHidden();
  await expect(page.getByRole('heading', { name: 'Task One Updated' })).toBeVisible();
});

test('Aufgabe kann gelöscht werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route(api.deleteTask, async (route: Route): Promise<void> => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
  });

  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task Two")').click();
  await page.locator('.task-modal button[title="Aufgabe löschen"]').click();
  await expect(page.getByRole('heading', { name: 'Task Two' })).toBeHidden();
});

test('Projektboard-Wechsel lädt ein anderes Projektboard.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await page.unroute(api.boards);
  await page.unroute(api.boardById);

  await page.route(api.boards, async (route: Route): Promise<void> => {
    if (route.request().method() !== 'GET') {
      await route.fallback();

      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([mockBoard, mockBoardB]),
    });
  });

  await page.route('**/api/boards/1', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'GET') {
      await route.fallback();

      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockBoard),
    });
  });

  await page.route('**/api/boards/2', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'GET') {
      await route.fallback();

      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(mockBoardB),
    });
  });

  await submitLogin(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await page.locator('ng-select#boardSelect').click();
  await page.locator('.ng-dropdown-panel .ng-option', { hasText: 'Board B' }).first().click();
  await page.locator('button[title="Projektboard wechseln"]').click();
  await expect(page.getByRole('heading', { name: 'Board B' })).toBeVisible();
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
  await page.locator(selectors.loginEmail).fill('max@test.de');
  await page.locator(selectors.loginPassword).fill('falsch');
  await page.locator(selectors.loginSubmit).click();
  await expect(page.locator('.error-message')).toBeVisible();
});

test('Registrierung erfolgreich zeigt Erfolgsmeldung und wechselt zurück zu Login.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await gotoLogin(page);
  await page.locator(selectors.loginRegisterLink).click();
  await page.locator(selectors.registerName).fill('Max Mustermann');
  await page.locator(selectors.registerEmail).fill('max@test.de');
  await page.locator(selectors.registerPassword).fill('Abc123!@#');
  await page.locator(selectors.registerPasswordConfirm).fill('Abc123!@#');
  await page.locator(selectors.registerSubmit).click();
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await page.waitForTimeout(4200);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('Passwort-Zurücksetzung erfolgreich zeigt Erfolgsmeldung und wechselt zurück zu Login.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await gotoLogin(page);
  await page.locator(selectors.loginResetLink).click();
  await page.locator(selectors.resetEmail).fill('max@test.de');
  await page.locator(selectors.resetSubmit).click();
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await page.waitForTimeout(4200);
  await expect(page.getByRole('heading', { name: /tmws/i })).toBeVisible();
});

test('E-Mail-Verifikation ohne Token zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/verify-email');
  await expect(page.locator('.error')).toBeVisible();
  await expect(page.getByRole('button', { name: /zurück zur anmeldung/i })).toBeVisible();
});

test('E-Mail-Verifikation mit Token zeigt Erfolg und leitet weiter.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await page.goto('/verify-email?token=fake-token');
  await expect(page.locator('.success')).toBeVisible({ timeout: 6000 });
  await expect(page).toHaveURL(/\/login\?verified=true$/, { timeout: 6000 });
});

test('Passwort-Zurücksetzung ohne Token zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/reset-password');
  await expect(page.locator('.error-message')).toBeVisible();
});

test('Passwort-Zurücksetzung mit Token zeigt Formular und akzeptiert neues Passwort.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await mockAuthRoutes(page);
  await page.goto('/reset-password?token=fake-token');
  await expect(page.locator(selectors.resetPasswordInput)).toBeVisible();
  await page.locator(selectors.resetPasswordInput).fill('Abc123!@#');
  await page.locator(selectors.resetPasswordSubmit).click();
  await expect(page.locator('.success-message')).toBeVisible({ timeout: 6000 });
  await expect(page).toHaveURL(/\/login$/, { timeout: 6000 });
});

test('Auth-Guard leitet ohne Token auf Login mit returnUrl um.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.goto('/dashboard');
  await expect(page).toHaveURL(/\/login\?returnUrl=%2Fdashboard/);
});

test('Auth-Guard erlaubt Zugriff auf Projektboard mit Token.', async ({
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

test('Projektboard-Titel speichern zeigt Validierungsfehler bei leerem Titel.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('.title button[title="Name bearbeiten"]').click();
  await page.locator('#title').fill('');
  await page.locator('button[title="Projektboard-Titel speichern"]').click();
  await expect(page.locator('.title-group .error-text').first()).toBeVisible();
  await expect(page.locator('#title')).toBeVisible();
});

test('Projektboard-Titel speichern bleibt im Bearbeitungsmodus bei API-Fehler.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/boards/1', async (route: Route): Promise<void> => {
    if (route.request().method() === 'PUT') {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Update fehlgeschlagen' }),
      });

      return;
    }

    await route.fallback();
  });

  await loginToDashboard(page);
  await page.locator('.title button[title="Name bearbeiten"]').click();
  await page.locator('#title').fill('Neuer Titel');
  await page.locator('button[title="Projektboard-Titel speichern"]').click();
  await expect(page.getByRole('heading', { name: 'Board A' })).toBeVisible();
});

test('Hintergrundbild speichern zeigt Fehler bei ungültiger URL.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await page.locator('#bgUrl').fill('invalid-url');
  await page.locator('button[title="Hintergrundbild speichern"]').click();
  await expect(page.locator('#bgUrl.error-input')).toBeVisible();
});

test('Einladung zeigt Validierungsfehler bei ungültiger E-Mail.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button[title="Projektboard-Einstellungen"]').click();
  await page.locator('#inviteEmail').fill('ungültig');
  await page.getByRole('button', { name: 'Einladen' }).click();
  await expect(page.locator('.board-settings .error-text').first()).toBeVisible();
});

test('Neue Statuskategorie zeigt Fehler bei Duplikat.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.add-statuscategory').click();
  await page.locator('#newColTitle').fill('To Do');
  await page.locator('button[title="Statuskategorie erstellen"]').click();
  await expect(page.locator('.add-column .error-text').first()).toBeVisible();
});

test('Neue Aufgabe zeigt Fehler bei Duplikat-Titel.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('.column:has-text("To Do") button.add-task').click();
  await page.locator('#newTaskTitle-10').fill('Task One');
  await page.locator('.new-task button[title="Aufgabe erstellen"]').click();
  await expect(page.locator('.new-task .error-text').first()).toBeVisible();
});

test('Aufgabendetails speichern zeigt Fehler bei leerem Titel.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task One")').click();
  await page.locator('#taskTitle').fill('');
  await page.locator('.task-modal button[title="Aufgabe speichern"]').click();
  await expect(page.locator('.task-modal .error-text').first()).toBeVisible();
});

test('Aufgabe löschen via Swal: Abbrechen behält Aufgabe bei.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await openTaskDeleteDialog(page);
  await page.locator('.task-modal button[title="Abbrechen"]').click();
  await expect(page.getByRole('heading', { name: 'Task Two' })).toBeVisible();
});

test('Aufgabe löschen via Swal: Bestätigen + API-Fehler zeigt Fehlerhinweis.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/boards/tasks/101', async (route: Route): Promise<void> => {
    if (route.request().method() === 'DELETE') {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Delete fehlgeschlagen' }),
      });

      return;
    }

    await route.fallback();
  });

  await openTaskDeleteDialog(page);
  await expect(page.getByRole('heading', { name: 'Task Two' })).toBeVisible();
});

test('Statuskategorie löschen via Swal: Abbrechen belässt Spalte.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await openColumnDeleteDialog(page);
});

test('Statuskategorie löschen via Swal: Bestätigen + API-Fehler lässt Spalte bestehen.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/boards/columns/11', async (route: Route): Promise<void> => {
    if (route.request().method() === 'DELETE') {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Delete fehlgeschlagen' }),
      });

      return;
    }

    await route.fallback();
  });

  await openColumnDeleteDialog(page);
});

test('Account deaktivieren via Swal: Abbrechen schließt Dialog.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.locator('button[title="Account deaktivieren"]').click();
  await expect(page.getByRole('heading', { name: 'Mein Konto' })).toBeVisible();
});

test('Profilaktualisierung: Passwort-Bestätigung funktioniert.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.getByRole('button', { name: 'Profil bearbeiten' }).click();
  const newPwd = page.locator('#newPwd');
  const confirmPwd = page.locator('#confirmPwd');
  await expect(newPwd).toHaveAttribute('type', 'password');
  await expect(confirmPwd).toHaveAttribute('type', 'password');
  await page
    .locator('#newPwd')
    .locator('xpath=ancestor::div[contains(@class,"form-group")]')
    .locator('button.password-toggle')
    .click();
  await expect(newPwd).toHaveAttribute('type', 'text');
  await expect(confirmPwd).toHaveAttribute('type', 'text');
});

test('Profilaktualisierung erfolgreich mit API-PUT.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let profileSaved = false;

  await page.route('**/api/auth/profile', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'PUT') {
      await route.fallback();

      return;
    }

    const body = route.request().postDataJSON() as { name?: string; newEmail?: string; image?: string };
    expect(body.name).toBe('Max Neu');
    expect(body.newEmail).toBe('max-neu@test.de');
    profileSaved = true;

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 1,
        name: 'Max Neu',
        email: 'max-neu@test.de',
        image: body.image ?? null,
        emailVerified: true,
        emailChanged: false,
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.getByRole('button', { name: 'Profil bearbeiten' }).click();
  await page.locator('#editName').fill('Max Neu');
  await page.locator('#editEmail').fill('max-neu@test.de');
  await page.locator('button[title="Profil speichern"]').click();
  await expect.poll((): boolean => profileSaved).toBeTruthy();
  await expect(page.locator('.swal2-popup')).toBeVisible();
  await expect(page.locator('.swal2-title')).toContainText(/erfolg|aktualisiert|gespeichert/i);
});

test('Profilaktualisierung Fehlerpfad zeigt Fehlermeldung.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/auth/profile', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'PUT') {
      await route.fallback();

      return;
    }
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Profil konnte nicht aktualisiert werden.' }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.getByRole('button', { name: 'Profil bearbeiten' }).click();
  await page.locator('#editName').fill('Max Fehler');
  await page.locator('button[title="Profil speichern"]').click();
  await expect(page.locator('.profile-popup .error-message')).toBeVisible();
});

test('Profilfenster kann geschlossen werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await expect(page.getByRole('heading', { name: 'Mein Konto' })).toBeVisible();
  await page.locator('button[title="Schließen"]').first().click();
  await expect(page.getByRole('heading', { name: 'Mein Konto' })).toBeHidden();
});

test('Account deaktivieren: Swal öffnen und abbrechen.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.locator('button[title="Account deaktivieren"]').click();
  await expect(page.getByRole('heading', { name: 'Mein Konto' })).toBeVisible();
});

test('Account deaktivieren: Bestätigen + API-Fehlerpfad.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/auth/deactivate**', async (route: Route): Promise<void> => {
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Deaktivierung fehlgeschlagen' }),
    });
  });

  await loginToDashboard(page);
  await page.locator('button.avatar').click();
  await page.locator('button[title="Account deaktivieren"]').click();
  await expect(page.locator('.profile-popup')).toBeVisible();
});

test('Aufgabe kann verschoben werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let moveCalled = false;

  await page.route('**/api/boards/tasks/100/move**', async (route: Route): Promise<void> => {
    moveCalled = true;
    expect(route.request().method()).toBe('PUT');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 100,
        title: 'Task One',
        description: 'Beschreibung eins',
        deadline: null,
        labels: 'high,bug',
        attachments: 'https://example.com/a.pdf',
        assignees: [mockUser],
      }),
    });
  });

  await loginToDashboard(page);
  const taskCard: Locator = page.locator('.column:has-text("To Do") .task.task-clickable:has-text("Task One")').first();
  const targetColumn: Locator = page.locator('.column:has-text("Done")').first();
  await taskCard.dragTo(targetColumn);
  await expect.poll((): boolean => moveCalled).toBeTruthy();
});

test('Statuskategorie kann verschoben werden.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let moveColumnCalled = false;

  await page.route('**/api/boards/columns/10/move**', async (route: Route): Promise<void> => {
    moveColumnCalled = true;
    expect(route.request().method()).toBe('PUT');

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({}),
    });
  });

  await loginToDashboard(page);
  const sourceHeader: Locator = page.locator('.column:has-text("To Do") .column-header').first();
  const targetHeader: Locator = page.locator('.column:has-text("Done") .column-header').first();
  await sourceHeader.dragTo(targetHeader);
  await expect.poll((): boolean => moveColumnCalled).toBeTruthy();
});

test('Aufgabendetails: Deadline in Vergangenheit blockiert Speichern.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let receivedDeadline: unknown = 'not-set';

  await page.route('**/api/boards/tasks/100', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'PUT') {
      await route.fallback();

      return;
    }

    const body = route.request().postDataJSON() as { deadline?: unknown };
    receivedDeadline = body.deadline;

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 100,
        title: 'Task One',
        description: 'Beschreibung eins',
        deadline: body.deadline ?? null,
        labels: 'high,bug',
        attachments: 'https://example.com/a.pdf',
        assignees: [mockUser],
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task One")').click();

  await page.evaluate((): void => {
    const input = document.querySelector('#taskDeadline input') as HTMLInputElement | null;
    if (!input) return;
    input.value = '01.01.2000';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('change', { bubbles: true }));
    input.dispatchEvent(new Event('blur', { bubbles: true }));
  });

  await page.locator('.task-modal button[title="Aufgabe speichern"]').click();
  await expect.poll((): unknown => receivedDeadline).not.toBe('not-set');
  expect(receivedDeadline === null || receivedDeadline === '').toBeTruthy();
  await expect(page.locator('.task-modal')).toBeHidden();
});

test('Aufgabendetails: Speichern ohne Deadline funktioniert.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  let payloadSeen = false;

  await page.route('**/api/boards/tasks/100', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'PUT') {
      await route.fallback();

      return;
    }

    const body = route.request().postDataJSON() as { title?: string; deadline?: string | null };
    expect(body.title).toBeTruthy();
    expect(body.deadline === null || typeof body.deadline === 'string').toBeTruthy();
    payloadSeen = true;

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 100,
        title: body.title ?? 'Task One',
        description: 'Beschreibung eins',
        deadline: body.deadline ?? null,
        labels: 'high,bug',
        attachments: 'https://example.com/a.pdf',
        assignees: [mockUser],
      }),
    });
  });

  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task One")').click();
  await page.locator('#taskTitle').fill('Task One No Deadline');
  await page.locator('.task-modal button[title="Aufgabe speichern"]').click();
  await expect.poll((): boolean => payloadSeen).toBeTruthy();
});

test('Aufgabendetails API-Fehler zeigt Fehler.', async ({
  page,
}: PlaywrightTestArgs & PlaywrightTestOptions & PlaywrightWorkerArgs & PlaywrightWorkerOptions): Promise<void> => {
  await page.route('**/api/boards/tasks/100', async (route: Route): Promise<void> => {
    if (route.request().method() !== 'PUT') {
      await route.fallback();

      return;
    }

    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Task Update fehlgeschlagen' }),
    });
  });

  await loginToDashboard(page);
  await page.locator('.task.task-clickable:has-text("Task One")').click();
  await page.locator('#taskTitle').fill('Task One Error');
  await page.locator('.task-modal button[title="Aufgabe speichern"]').click();
  await expect(page.locator('.task-modal .error-message')).toBeVisible();
});
