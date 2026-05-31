/**
 * Diese Datei enthält Unit-Tests für die DashboardComponent der Angular-Anwendung.
 * Sie verwendet das Angular-Testframework und Vitest, um eine Test-Suite zu erstellen, die verschiedene Aspekte der
 * DashboardComponent überprüft.
 * Die Tests umfassen die Überprüfung der Initialisierung, der Interaktion mit dem BoardService und AuthService, der
 * Formularvalidierung, der Handhabung von Benutzerinteraktionen und der Fehlerbehandlung.
 */
import { describe, it, expect, beforeEach, afterEach, vi, Mock, MockInstance, MockedFunction } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { Router } from '@angular/router';
import { of, throwError, Subject, type Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { FormUtilsShared } from '../../shared/form-utils.shared';
import { AuthService } from '../../service/auth.service';
import { BoardService } from '../../service/board.service';
import { WebsocketService } from '../../service/websocket.service';
import { UserModel } from '../../model/user.model';
import { BoardModel } from '../../model/board.model';
import { ColumnModel } from '../../model/column.model';
import { TaskModel } from '../../model/task.model';
import Swal, { SweetAlertIcon, SweetAlertOptions, SweetAlertResult } from 'sweetalert2';
import { ValidatorFn } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';

vi.mock(
  'sweetalert2',
  (): {
    default: {
      fire: MockInstance<() => Promise<SweetAlertResult>> & {
        (...args: []): Promise<SweetAlertResult>;
        new (...args: []): Promise<SweetAlertResult>;
      } & {};
    };
  } => ({
    default: {
      fire: vi.fn((): Promise<SweetAlertResult> => Promise.resolve({} as SweetAlertResult)),
    },
  })
);

describe('DashboardComponent', (): void => {
  let component: DashboardComponent;

  let authServiceMock: {
    currentUser$: Subject<UserModel | null>;
    getCurrentUserSnapshot: Mock;
    getCurrentUser: Mock;
    getEmailFromToken: Mock;
    logout: Mock;
    changePassword: Mock;
    updateProfile: Mock;
    deactivateAccount: Mock;
  };

  let boardServiceMock: {
    listBoards: Mock;
    getBoard: Mock;
    createBoard: Mock;
    updateBoard: Mock;
    updateColumn: Mock;
    deleteColumn: Mock;
    addColumn: Mock;
    addTask: Mock;
    updateTask: Mock;
    deleteTask: Mock;
    moveTask: Mock;
    moveColumn: Mock;
    invite: Mock;
  };

  let websocketMock: {
    userUpdates: Mock;
    boardUpdates: Mock;
    subscribeUser: Mock;
    subscribeBoard: Mock;
  };

  let routerMock: { navigate: Mock };
  let formUtilsMock: Partial<FormUtilsShared>;

  const userA: UserModel = {
    id: 1,
    name: 'Max Mustermann',
    email: 'max@test.de',
    image: null,
    emailVerified: true,
    emailChanged: false,
  };

  const userB: UserModel = {
    id: 2,
    name: 'Erika Musterfrau',
    email: 'erika@test.de',
    image: null,
    emailVerified: true,
    emailChanged: false,
  };

  const taskA: TaskModel = {
    id: 10,
    title: 'Task A',
    description: 'Beschreibung',
    deadline: null,
    labels: null,
    attachments: null,
    assignees: [userB],
  };

  const columnA: ColumnModel = {
    id: 100,
    title: 'Todo',
    position: 0,
    boardId: 1,
    tasks: [taskA],
  };

  const columnB: ColumnModel = {
    id: 101,
    title: 'Done',
    position: 1,
    boardId: 1,
    tasks: [],
  };

  const boardA: BoardModel = {
    id: 1,
    owner: userA,
    title: 'Board A',
    background: null,
    columns: [columnA, columnB],
    members: [userA, userB],
  };

  const boardB: BoardModel = {
    id: 2,
    owner: userA,
    title: 'Board B',
    background: null,
    columns: [],
    members: [userA],
  };

  const createComponent: () => void = (): void => {
    component = TestBed.createComponent(DashboardComponent).componentInstance;
    component.ngOnInit();
  };

  beforeEach((): void => {
    authServiceMock = {
      currentUser$: new Subject<UserModel | null>(),
      getCurrentUserSnapshot: vi.fn((): UserModel | null => userA),
      getCurrentUser: vi.fn((): Observable<UserModel> => of(userA)),
      getEmailFromToken: vi.fn((): string | null => userA.email),
      logout: vi.fn(),
      changePassword: vi.fn((): Observable<void> => of(void 0)),
      updateProfile: vi.fn((): Observable<UserModel> => of({ ...userA, emailChanged: false })),
      deactivateAccount: vi.fn((): Observable<void> => of(void 0)),
    };

    boardServiceMock = {
      listBoards: vi.fn((): Observable<BoardModel[]> => of([boardA, boardB])),
      getBoard: vi.fn((id: number): Observable<BoardModel> => of(id === boardA.id ? boardA : boardB)),
      createBoard: vi.fn((): Observable<BoardModel> => of(boardA)),
      updateBoard: vi.fn((): Observable<BoardModel> => of(boardA)),
      updateColumn: vi.fn((): Observable<BoardModel> => of(boardA)),
      deleteColumn: vi.fn((): Observable<void> => of(void 0)),
      addColumn: vi.fn((): Observable<ColumnModel> => of(columnB)),
      addTask: vi.fn((): Observable<TaskModel> => of({ id: 99, title: 'Neu' })),
      updateTask: vi.fn((): Observable<void> => of(void 0)),
      deleteTask: vi.fn((): Observable<void> => of(void 0)),
      moveTask: vi.fn((): Observable<void> => of(void 0)),
      moveColumn: vi.fn((): Observable<void> => of(void 0)),
      invite: vi.fn((): Observable<void> => of(void 0)),
    };

    websocketMock = {
      userUpdates: vi.fn((): number => 0),
      boardUpdates: vi.fn((): number => 0),
      subscribeUser: vi.fn(),
      subscribeBoard: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    formUtilsMock = {
      emailValidator: vi.fn((): ValidatorFn => (): null => null),
      passwordStrengthValidator: vi.fn((): ValidatorFn => (): null => null),
      setupAutoClearErrors: vi.fn(),
      updateFieldErrors: vi.fn(),
      getErrorMessage: vi.fn((): string => 'Fehler'),
      markFormGroupTouched: vi.fn(),
      getPasswordRequirements: vi.fn((): { text: string; met: boolean }[] => []),
    };

    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: BoardService, useValue: boardServiceMock },
        { provide: WebsocketService, useValue: websocketMock },
        { provide: Router, useValue: routerMock },
        { provide: FormUtilsShared, useValue: formUtilsMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    });

    sessionStorage.clear();
  });

  afterEach((): void => {
    vi.clearAllMocks();
    TestBed.resetTestingModule();
  });

  it('Sollte korrekt initialisiert werden', (): void => {
    createComponent();
    expect(component).toBeTruthy();
    expect(component.board?.id).toBe(boardA.id);
  });

  it('Sollte aktive Board-ID aus SessionStorage verwenden', (): void => {
    sessionStorage.setItem(`activeBoardId:${userA.email}`, String(boardB.id));
    boardServiceMock.listBoards.mockReturnValue(of([boardA, boardB]));
    boardServiceMock.getBoard.mockReturnValue(of(boardB));
    createComponent();
    expect(component.board?.id).toBe(boardB.id);
    expect(component.boardSwitchForm.controls.selectedBoardId.value).toBe(boardB.id);
  });

  it('Sollte neues Board erstellen, wenn keine Boards existieren', (): void => {
    boardServiceMock.listBoards.mockReturnValue(of([]));
    createComponent();
    expect(boardServiceMock.createBoard).toHaveBeenCalled();
  });

  it('Sollte User-Updates abonnieren, wenn User vorhanden ist', (): void => {
    createComponent();
    authServiceMock.currentUser$.next(userA);
    expect(websocketMock.subscribeUser).toHaveBeenCalledWith(userA.id);
  });

  it('Sollte Vorschläge aufbauen', (): void => {
    createComponent();
    component.buildSuggestions();
    expect(component.suggestions().length).toBe(1);
    expect(component.suggestions()[0]?.title).toBe('Task A');
  });

  it('Sollte Suche filtern', (): void => {
    createComponent();
    component.onSearchChange('task');
    expect(component.suggestions().length).toBe(1);
    component.onSearchChange('xyz');
    expect(component.suggestions().length).toBe(0);
  });

  it('Sollte Suche zurücksetzen', (): void => {
    createComponent();
    component.searchForm.controls.query.setValue('Task');
    component.clearSearch();
    expect(component.searchForm.controls.query.value).toBe('');
  });

  it('Sollte Profil umschalten', (): void => {
    createComponent();
    component.toggleProfile();
    expect(component.profileOpen()).toBe(true);
    component.toggleProfile();
    expect(component.profileOpen()).toBe(false);
  });

  it('Sollte Settings umschalten', (): void => {
    createComponent();
    component.toggleSettings();
    expect(component.settingsOpen()).toBe(true);
    component.toggleSettings();
    expect(component.settingsOpen()).toBe(false);
  });

  it('Sollte Edit-Titel toggeln', (): void => {
    createComponent();
    component.toggleEditTitle();
    expect(component.editingTitle()).toBe(true);
    expect(component.boardTitleForm.controls.title.value).toBe(boardA.title);
  });

  it('Sollte Titel speichern, wenn Formular gültig ist', (): void => {
    createComponent();
    component.board = boardA;
    component.boardTitleForm.controls.title.setValue('Neu');
    component.saveTitle();
    expect(boardServiceMock.updateBoard).toHaveBeenCalled();
  });

  it('Sollte Titel nicht speichern, wenn Formular ungültig ist', (): void => {
    createComponent();
    component.board = boardA;
    component.boardTitleForm.controls.title.setValue('');
    component.saveTitle();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
  });

  it('Sollte Board nicht wechseln, wenn gleiche Auswahl', (): void => {
    createComponent();
    boardServiceMock.getBoard.mockClear();
    component.board = boardA;
    component.boardSwitchForm.controls.selectedBoardId.setValue(boardA.id);
    component.switchBoard();
    expect(boardServiceMock.getBoard).not.toHaveBeenCalled();
  });

  it('Sollte Board wechseln, wenn Auswahl anders ist', (): void => {
    createComponent();
    boardServiceMock.getBoard.mockClear();
    component.board = boardA;
    component.boardSwitchForm.controls.selectedBoardId.setValue(boardB.id);
    component.switchBoard();
    expect(boardServiceMock.getBoard).toHaveBeenCalledWith(boardB.id);
  });

  it('Sollte Profil in Edit-Modus setzen', (): void => {
    createComponent();
    component.profileName = userA.name;
    component.profileEmail = userA.email;
    component.profileImage = userA.image ?? '';
    component.enterProfileEditMode();
    expect(component.profileEditMode()).toBe(true);
    expect(component.profileForm.controls.name.value).toBe(userA.name);
  });

  it('Sollte Profil-Edit abbrechen', (): void => {
    createComponent();
    component.enterProfileEditMode();
    component.cancelProfileEdit();
    expect(component.profileEditMode()).toBe(false);
  });

  it('Sollte Profil-Popup schliessen', (): void => {
    createComponent();
    component.enterProfileEditMode();
    component.closeProfilePopup();
    expect(component.profileOpen()).toBe(false);
    expect(component.profileEditMode()).toBe(false);
  });

  it('Sollte Profil-Update abbrechen, wenn Formular ungültig ist', async (): Promise<void> => {
    createComponent();
    component.profileForm.controls.name.setValue('');
    await component.updateProfile();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
  });

  it('Sollte Profil-Update abbrechen, wenn Passwortfelder fehlen', async (): Promise<void> => {
    createComponent();
    component.profileForm.controls.name.setValue(userA.name);
    component.profileForm.controls.email.setValue(userA.email);
    component.profileForm.controls.currentPassword.setValue('Alt123!');
    component.profileForm.controls.newPassword.setValue('');
    component.profileForm.controls.confirmPassword.setValue('');
    await component.updateProfile();
    expect(component.profileFieldErrors['newPassword']).toBeDefined();
    expect(component.profileFieldErrors['confirmPassword']).toBeDefined();
  });

  it('Sollte Profil aktualisieren ohne Passwortwechsel', async (): Promise<void> => {
    createComponent();
    const validateSpy: Mock<(url: string) => Promise<boolean>> = vi
      .spyOn(component as unknown as { validateImageUrl: (url: string) => Promise<boolean> }, 'validateImageUrl')
      .mockResolvedValue(true);
    component.profileForm.controls.name.setValue(userA.name);
    component.profileForm.controls.email.setValue(userA.email);
    component.profileForm.controls.image.setValue('');
    await component.updateProfile();
    expect(authServiceMock.updateProfile).toHaveBeenCalled();
    validateSpy.mockRestore();
  });

  it('Sollte Profil aktualisieren mit Passwortwechsel', async (): Promise<void> => {
    createComponent();
    const validateSpy: Mock<(url: string) => Promise<boolean>> = vi
      .spyOn(component as unknown as { validateImageUrl: (url: string) => Promise<boolean> }, 'validateImageUrl')
      .mockResolvedValue(true);
    component.profileForm.controls.name.setValue(userA.name);
    component.profileForm.controls.email.setValue(userA.email);
    component.profileForm.controls.currentPassword.setValue('Alt123!');
    component.profileForm.controls.newPassword.setValue('Neu123!@#');
    component.profileForm.controls.confirmPassword.setValue('Neu123!@#');
    await component.updateProfile();
    expect(authServiceMock.changePassword).toHaveBeenCalled();
    expect(authServiceMock.updateProfile).toHaveBeenCalled();
    validateSpy.mockRestore();
  });

  it('Sollte Profil-Update Fehlerstatus behandeln', async (): Promise<void> => {
    createComponent();
    const validateSpy: Mock<(url: string) => Promise<boolean>> = vi
      .spyOn(component as unknown as { validateImageUrl: (url: string) => Promise<boolean> }, 'validateImageUrl')
      .mockResolvedValue(true);

    authServiceMock.updateProfile.mockReturnValue(
      throwError(
        (): HttpErrorResponse =>
          new HttpErrorResponse({
            status: 409,
            error: { message: 'E-Mail existiert bereits' },
          })
      )
    );

    component.profileForm.controls.name.setValue(userA.name);
    component.profileForm.controls.email.setValue(userA.email);
    await component.updateProfile();
    expect(component.profileFieldErrors['email']).toBeDefined();
    validateSpy.mockRestore();
  });

  it('Sollte Background-Update abbrechen, wenn Formular ungültig', async (): Promise<void> => {
    createComponent();
    component.board = boardA;
    component.backgroundForm.controls.background.setValue('x'.repeat(260));
    await component.updateBackground();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
  });

  it('Sollte Background-Update abbrechen, wenn URL ungültig', async (): Promise<void> => {
    createComponent();
    component.board = boardA;
    const validateSpy: Mock<(url: string) => Promise<boolean>> = vi
      .spyOn(component as unknown as { validateImageUrl: (url: string) => Promise<boolean> }, 'validateImageUrl')
      .mockResolvedValue(false);
    component.backgroundForm.controls.background.setValue('https://example.com/x.png');
    await component.updateBackground();
    expect(component.backgroundFieldErrors['background']).toBeDefined();
    validateSpy.mockRestore();
  });

  it('Sollte Background aktualisieren', async (): Promise<void> => {
    createComponent();
    component.board = boardA;
    const validateSpy: Mock<(url: string) => Promise<boolean>> = vi
      .spyOn(component as unknown as { validateImageUrl: (url: string) => Promise<boolean> }, 'validateImageUrl')
      .mockResolvedValue(true);
    component.backgroundForm.controls.background.setValue('https://example.com/bg.png');
    await component.updateBackground();
    expect(boardServiceMock.updateBoard).toHaveBeenCalled();
    validateSpy.mockRestore();
  });

  it('Sollte Invite abbrechen, wenn Formular ungültig', (): void => {
    createComponent();
    component.board = boardA;
    component.inviteForm.controls.email.setValue('');
    component.inviteMember();
    expect(formUtilsMock.markFormGroupTouched).toHaveBeenCalled();
  });

  it('Sollte Invite erfolgreich ausführen', (): void => {
    createComponent();
    component.board = boardA;
    component.inviteForm.controls.email.setValue('invite@test.de');
    component.inviteMember();
    expect(boardServiceMock.invite).toHaveBeenCalledWith(boardA.id, 'invite@test.de');
  });

  it('Sollte Invite-Fehler 404 behandeln', (): void => {
    createComponent();
    component.board = boardA;

    boardServiceMock.invite.mockReturnValue(
      throwError(
        (): HttpErrorResponse =>
          new HttpErrorResponse({
            status: 404,
            error: { message: 'Nicht gefunden' },
          })
      )
    );

    component.inviteForm.controls.email.setValue('invite@test.de');
    component.inviteMember();
    expect(component.inviteFieldErrors['email']).toBeDefined();
  });

  it('Sollte Invite-Fehler 409 behandeln', (): void => {
    createComponent();
    component.board = boardA;

    boardServiceMock.invite.mockReturnValue(
      throwError(
        (): HttpErrorResponse =>
          new HttpErrorResponse({
            status: 409,
            error: { message: 'Konflikt' },
          })
      )
    );

    component.inviteForm.controls.email.setValue('invite@test.de');
    component.inviteMember();
    expect(component.inviteFieldErrors['email']).toBeDefined();
  });

  it('Sollte Task-Erstellung starten und abbrechen', (): void => {
    createComponent();
    component.startCreateTask(columnA.id);
    expect(component.creatingTaskColumnId).toBe(columnA.id);
    component.cancelCreateTask();
    expect(component.creatingTaskColumnId).toBeNull();
  });

  it('Sollte Task-Erstellung leeren Titel ablehnen', (): void => {
    createComponent();
    component.board = boardA;
    component.creatingTaskColumnId = columnA.id;
    component.taskCreateForm.controls.title.setValue('');
    component.createTask();
    expect(component.taskCreateFieldErrors['title']).toBeDefined();
  });

  it('Sollte Task-Erstellung doppelte Titel ablehnen', (): void => {
    createComponent();
    component.board = boardA;
    component.creatingTaskColumnId = columnA.id;
    component.taskCreateForm.controls.title.setValue('Task A');
    component.createTask();
    expect(component.taskCreateFieldErrors['title']).toBeDefined();
  });

  it('Sollte Task-Erstellung ausführen', (): void => {
    createComponent();
    component.board = boardA;
    component.creatingTaskColumnId = columnA.id;
    component.taskCreateForm.controls.title.setValue('Neu');
    component.createTask();
    expect(boardServiceMock.addTask).toHaveBeenCalled();
  });

  it('Sollte Column-Edit starten und abbrechen', (): void => {
    createComponent();
    component.startEditColumn(columnA);
    expect(component.editingColumnId).toBe(columnA.id);
    component.cancelEditColumn();
    expect(component.editingColumnId).toBeNull();
  });

  it('Sollte Column-Edit doppelten Titel ablehnen', (): void => {
    createComponent();
    component.board = boardA;
    component.startEditColumn(columnA);
    component.columnEditForm.controls.title.setValue(columnB.title);
    component.saveColumnTitle(columnA);
    expect(component.columnEditFieldErrors['title']).toBeDefined();
  });

  it('Sollte Column-Edit speichern', (): void => {
    createComponent();
    component.board = boardA;
    component.startEditColumn(columnA);
    component.columnEditForm.controls.title.setValue('Neu');
    component.saveColumnTitle(columnA);
    expect(boardServiceMock.updateColumn).toHaveBeenCalled();
  });

  it('Sollte Column-Erstellung leeren Titel ablehnen', (): void => {
    createComponent();
    component.board = boardA;
    component.newColumnForm.controls.title.setValue('');
    component.addColumn();
    expect(component.newColumnFieldErrors['title']).toBeDefined();
  });

  it('Sollte Column-Erstellung doppelte Titel ablehnen', (): void => {
    createComponent();
    component.board = boardA;
    component.newColumnForm.controls.title.setValue('Todo');
    component.addColumn();
    expect(component.newColumnFieldErrors['title']).toBeDefined();
  });

  it('Sollte Column-Erstellung ausführen', (): void => {
    createComponent();
    component.board = boardA;
    component.newColumnForm.controls.title.setValue('Neu');
    component.addColumn();
    expect(boardServiceMock.addColumn).toHaveBeenCalled();
  });

  it('Sollte Task-Detail öffnen und schliessen', (): void => {
    createComponent();
    component.openTaskDetail(taskA);
    expect(component.taskDetailOpen()).toBe(true);
    component.closeTaskDetail();
    expect(component.taskDetailOpen()).toBe(false);
  });

  it('Sollte Task-Details speichern', (): void => {
    createComponent();
    component.selectedTask = { ...taskA, id: 10 };
    component.taskDetailForm.controls.title.setValue('Neu');
    component.taskDetailForm.controls.deadline.setValue(null);
    component.saveTaskDetails();
    expect(boardServiceMock.updateTask).toHaveBeenCalled();
  });

  it('Sollte Task-Details Deadline in Vergangenheit ablehnen', (): void => {
    createComponent();
    component.selectedTask = { ...taskA, id: 10 };
    component.taskDetailForm.controls.title.setValue('Neu');
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    component.taskDetailForm.controls.deadline.setValue(yesterday);
    component.saveTaskDetails();
    expect(component.taskDetailFieldErrors['deadline']).toBeDefined();
  });

  it('Sollte Task löschen nach Bestätigung', async (): Promise<void> => {
    createComponent();
    component.selectedTask = { ...taskA, id: 10 };
    const swalFireMock: MockedFunction<{
      <T = never>(options: SweetAlertOptions): Promise<SweetAlertResult<Awaited<T>>>;
      <T = never>(title?: string, html?: string, icon?: SweetAlertIcon): Promise<SweetAlertResult<Awaited<T>>>;
    }> = vi.mocked(Swal.fire);
    swalFireMock.mockResolvedValueOnce({ isDenied: false, isDismissed: false } as SweetAlertResult);
    component.deleteSelectedTask();
    await Promise.resolve();
    expect(boardServiceMock.deleteTask).toHaveBeenCalledWith(10);
  });

  it('Sollte Task nicht löschen bei Abbruch', async (): Promise<void> => {
    createComponent();
    component.selectedTask = { ...taskA, id: 10 };
    const swalFireMock: MockedFunction<{
      <T = never>(options: SweetAlertOptions): Promise<SweetAlertResult<Awaited<T>>>;
      <T = never>(title?: string, html?: string, icon?: SweetAlertIcon): Promise<SweetAlertResult<Awaited<T>>>;
    }> = vi.mocked(Swal.fire);
    swalFireMock.mockResolvedValueOnce({ isDenied: true, isDismissed: false } as SweetAlertResult);
    component.deleteSelectedTask();
    await Promise.resolve();
    expect(boardServiceMock.deleteTask).not.toHaveBeenCalled();
  });

  it('Sollte Column löschen nach Bestätigung', async (): Promise<void> => {
    createComponent();
    component.board = boardA;
    const swalFireMock: MockedFunction<{
      <T = never>(options: SweetAlertOptions): Promise<SweetAlertResult<Awaited<T>>>;
      <T = never>(title?: string, html?: string, icon?: SweetAlertIcon): Promise<SweetAlertResult<Awaited<T>>>;
    }> = vi.mocked(Swal.fire);
    swalFireMock.mockResolvedValueOnce({ isDenied: false, isDismissed: false } as SweetAlertResult);
    component.confirmDeleteColumn(columnA);
    await Promise.resolve();
    expect(boardServiceMock.deleteColumn).toHaveBeenCalledWith(columnA.id);
  });

  it('Sollte Column nicht löschen bei Abbruch', async (): Promise<void> => {
    createComponent();
    component.board = boardA;
    const swalFireMock: MockedFunction<{
      <T = never>(options: SweetAlertOptions): Promise<SweetAlertResult<Awaited<T>>>;
      <T = never>(title?: string, html?: string, icon?: SweetAlertIcon): Promise<SweetAlertResult<Awaited<T>>>;
    }> = vi.mocked(Swal.fire);
    swalFireMock.mockResolvedValueOnce({ isDenied: true, isDismissed: false } as SweetAlertResult);
    component.confirmDeleteColumn(columnA);
    await Promise.resolve();
    expect(boardServiceMock.deleteColumn).not.toHaveBeenCalled();
  });

  it('Sollte DragStart Task setzen', (): void => {
    createComponent();

    const mockEvent: DragEvent = {
      dataTransfer: {
        setData: vi.fn(),
      } as unknown as DataTransfer,
    } as DragEvent;

    component.onDragStart(mockEvent, 1);
    expect(component.isTaskDrag()).toBe(true);
  });

  it('Sollte Drop Task ausführen', (): void => {
    createComponent();

    component.board = {
      ...boardA,
      columns: [{ ...columnA, tasks: [] }, columnB],
    };

    const mockEvent: DragEvent = {
      dataTransfer: {
        getData: vi.fn((): string => '10'),
      } as unknown as DataTransfer,
      preventDefault: vi.fn(),
      stopPropagation: vi.fn(),
    } as unknown as DragEvent;

    component.onDrop(mockEvent, columnA.id);
    expect(boardServiceMock.moveTask).toHaveBeenCalled();
  });

  it('Sollte Column-Drop ausführen', (): void => {
    createComponent();
    component.board = boardA;

    const mockEvent: DragEvent = {
      dataTransfer: {
        getData: vi.fn((): string => '0'),
      } as unknown as DataTransfer,
      preventDefault: vi.fn(),
      stopPropagation: vi.fn(),
    } as unknown as DragEvent;

    component.onColumnDrop(mockEvent, 1);
    expect(boardServiceMock.moveColumn).toHaveBeenCalled();
  });

  it('Sollte Forms deaktivieren und aktivieren', (): void => {
    createComponent();

    const disableSpy: Mock<(opts?: { onlySelf?: boolean; emitEvent?: boolean }) => void> = vi.spyOn(
      component.profileForm,
      'disable'
    );

    const enableSpy: Mock<(opts?: { onlySelf?: boolean; emitEvent?: boolean }) => void> = vi.spyOn(
      component.profileForm,
      'enable'
    );

    component.isSaving.set(true);
    (component as unknown as { handleForms: () => void }).handleForms();
    expect(disableSpy).toHaveBeenCalled();
    component.isSaving.set(false);
    (component as unknown as { handleForms: () => void }).handleForms();
    expect(enableSpy).toHaveBeenCalled();
  });

  it('Sollte Account deaktivieren nach Bestätigung', async (): Promise<void> => {
    createComponent();
    const swalFireMock: MockedFunction<{
      <T = never>(options: SweetAlertOptions): Promise<SweetAlertResult<Awaited<T>>>;
      <T = never>(title?: string, html?: string, icon?: SweetAlertIcon): Promise<SweetAlertResult<Awaited<T>>>;
    }> = vi.mocked(Swal.fire);
    swalFireMock.mockResolvedValueOnce({ isDenied: false, isDismissed: false } as SweetAlertResult);
    component.deactivateAccount();
    await Promise.resolve();
    expect(authServiceMock.deactivateAccount).toHaveBeenCalledWith(userA.email);
  });

  it('Sollte Initialen vom aktuellen User liefern', (): void => {
    createComponent();
    expect(component.getCurrentUserInitials()).toBe('MM');
  });

  it('Sollte Initialen vom Benutzer liefern', (): void => {
    createComponent();
    expect(component.getUserInitials(userB)).toBe('EM');
  });
});
