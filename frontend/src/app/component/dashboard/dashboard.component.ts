/**
 * Diese Klasse repräsentiert die Dashboard-Komponente der Anwendung, die als zentrale Anlaufstelle für Benutzer
 * dient, um ihre Projektboards zu verwalten.
 */
import { Component, effect, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { NgOptionTemplateDirective, NgSelectComponent } from '@ng-select/ng-select';
import { AuthService } from '../../service/auth.service';
import { BoardService } from '../../service/board.service';
import { DateInput, NgxsmkDatepickerComponent } from 'ngxsmk-datepicker';
import Swal, { SweetAlertResult } from 'sweetalert2';
import { NgIcon } from '@ng-icons/core';
import { WebsocketService } from '../../service/websocket.service';
import { ColumnModel } from '../../model/column.model';
import { UserModel } from '../../model/user.model';
import { TaskModel } from '../../model/task.model';
import { BoardModel } from '../../model/board.model';
import { firstValueFrom } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { FormUtilsShared } from '../../shared/form-utils.shared';

interface SearchFormControls {
  query: FormControl<string>;
}

interface ProfileFormControls {
  name: FormControl<string>;
  email: FormControl<string>;
  image: FormControl<string>;
  currentPassword: FormControl<string>;
  newPassword: FormControl<string>;
  confirmPassword: FormControl<string>;
}

interface BoardTitleFormControls {
  title: FormControl<string>;
}

interface BackgroundFormControls {
  background: FormControl<string>;
}

interface InviteFormControls {
  email: FormControl<string>;
}

interface BoardSwitchFormControls {
  selectedBoardId: FormControl<number | null>;
}

interface ColumnEditFormControls {
  title: FormControl<string>;
}

interface NewColumnFormControls {
  title: FormControl<string>;
}

interface TaskCreateFormControls {
  title: FormControl<string>;
}

interface TaskDetailFormControls {
  title: FormControl<string>;
  description: FormControl<string>;
  deadline: FormControl<DateInput | null>;
  labels: FormControl<string>;
  attachments: FormControl<string>;
  assigneeId: FormControl<number | null>;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgxsmkDatepickerComponent,
    NgIcon,
    NgOptionTemplateDirective,
    NgSelectComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {
  formUtilsShared: FormUtilsShared = inject(FormUtilsShared);
  fb: FormBuilder = inject(FormBuilder);

  searchForm: FormGroup<SearchFormControls> = this.fb.nonNullable.group({
    query: '',
  });

  profileForm: FormGroup<ProfileFormControls> = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
    email: ['', [Validators.required, Validators.maxLength(30), this.formUtilsShared.emailValidator()]],
    image: ['', [Validators.maxLength(255)]],
    currentPassword: [''],
    newPassword: ['', [Validators.minLength(8), Validators.maxLength(50), this.optionalPasswordStrengthValidator()]],
    confirmPassword: ['', [this.profilePasswordMatchValidator()]],
  });

  boardTitleForm: FormGroup<BoardTitleFormControls> = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(30)]],
  });

  backgroundForm: FormGroup<BackgroundFormControls> = this.fb.nonNullable.group({
    background: ['', [Validators.maxLength(255)]],
  });

  inviteForm: FormGroup<InviteFormControls> = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.maxLength(30), this.formUtilsShared.emailValidator()]],
  });

  boardSwitchForm: FormGroup<BoardSwitchFormControls> = this.fb.group({
    selectedBoardId: new FormControl<number | null>(null, Validators.required),
  });

  columnEditForm: FormGroup<ColumnEditFormControls> = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(25)]],
  });

  newColumnForm: FormGroup<NewColumnFormControls> = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(25)]],
  });

  taskCreateForm: FormGroup<TaskCreateFormControls> = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(30)]],
  });

  taskDetailForm: FormGroup<TaskDetailFormControls> = this.fb.group({
    title: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(30)],
    }),
    description: new FormControl<string>('', { nonNullable: true, validators: [Validators.maxLength(1000)] }),
    deadline: new FormControl<DateInput | null>(null),
    labels: new FormControl<string>('', { nonNullable: true, validators: [Validators.maxLength(255)] }),
    attachments: new FormControl<string>('', { nonNullable: true, validators: [Validators.maxLength(255)] }),
    assigneeId: new FormControl<number | null>(null),
  });

  board: BoardModel | null = null;
  boards: BoardModel[] = [];
  selectedTask: TaskModel | null = null;
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;
  currentDay: number = new Date().getDate();
  today: DateInput = new Date();
  comingYear: DateInput = new Date(this.currentYear + 1, this.currentMonth - 1, this.currentDay);
  profileName = '';
  profileEmail = '';
  profileImage = '';
  editingColumnId: number | null = null;
  creatingTaskColumnId: number | null = null;
  profileFieldErrors: Record<string, string[]> = {};
  boardTitleFieldErrors: Record<string, string[]> = {};
  backgroundFieldErrors: Record<string, string[]> = {};
  inviteFieldErrors: Record<string, string[]> = {};
  boardSwitchFieldErrors: Record<string, string[]> = {};
  columnEditFieldErrors: Record<string, string[]> = {};
  newColumnFieldErrors: Record<string, string[]> = {};
  taskCreateFieldErrors: Record<string, string[]> = {};
  taskDetailFieldErrors: Record<string, string[]> = {};
  authService: AuthService = inject(AuthService);
  boardService: BoardService = inject(BoardService);
  websocket: WebsocketService = inject(WebsocketService);
  isSaving: WritableSignal<boolean> = signal(false);
  profileOpen: WritableSignal<boolean> = signal(false);
  settingsOpen: WritableSignal<boolean> = signal(false);
  taskDetailOpen: WritableSignal<boolean> = signal(false);
  showAddColumn: WritableSignal<boolean> = signal(false);
  editingTitle: WritableSignal<boolean> = signal(false);
  profileEditMode: WritableSignal<boolean> = signal(false);
  showPassword: WritableSignal<boolean> = signal(false);
  showPasswordConfirm: WritableSignal<boolean> = signal(false);
  isTaskDrag: WritableSignal<boolean> = signal(false);
  searchQuery: WritableSignal<string> = signal('');
  suggestions: WritableSignal<TaskModel[]> = signal([]);
  profileGeneralError: WritableSignal<string | null> = signal(null);
  backgroundGeneralError: WritableSignal<string | null> = signal(null);
  taskCreateGeneralError: WritableSignal<string | null> = signal(null);
  taskDetailGeneralError: WritableSignal<string | null> = signal(null);

  /**
   * Initialisiert die Dashboard-Komponente und richtet Effekte ein, um auf Websocket-Updates für Benutzer-
   * und Board-Änderungen zu reagieren.
   */
  constructor() {
    effect((): void => {
      const updates: number = this.websocket.userUpdates();
      if (updates === 0) return;
      const user: UserModel | null = this.authService.getCurrentUserSnapshot();
      if (!user) return;
      this.authService.getCurrentUser().subscribe();

      if (this.board?.id) {
        this.boardService.getBoard(this.board.id).subscribe((board: BoardModel): void => {
          this.board = { ...board };
          this.buildSuggestions();
        });
      }

      this.boardService.listBoards().subscribe((boards: BoardModel[]): void => {
        this.boards = boards;
      });
    });

    effect((): void => {
      const updates: number = this.websocket.boardUpdates();
      if (updates === 0) return;
      const id: number | undefined = this.board?.id;
      if (!id) return;

      this.boardService.getBoard(id).subscribe((board: BoardModel): void => {
        this.board = { ...board };

        this.boardService.listBoards().subscribe((allBoards: BoardModel[]): void => {
          this.boards = allBoards;
          this.boardSwitchForm.controls.selectedBoardId.setValue(board.id ?? null, { emitEvent: false });
        });

        this.buildSuggestions();
      });
    });
  }

  /**
   * Lädt die Daten des aktuell angemeldeten Benutzers und initialisiert die Profilinformationen im Dashboard.
   */
  ngOnInit(): void {
    this.loadCurrentUser();

    this.authService.currentUser$.subscribe((user: UserModel | null): void => {
      if (!user) return;
      this.websocket.subscribeUser(user.id!);
    });

    this.formUtilsShared.setupAutoClearErrors(this.profileForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.profileFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.boardTitleForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.boardTitleFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.backgroundForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.backgroundFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.inviteForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.inviteFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.boardSwitchForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.boardSwitchFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.columnEditForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.columnEditFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.newColumnForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.newColumnFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.taskCreateForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.taskCreateFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.formUtilsShared.setupAutoClearErrors(this.taskDetailForm, (form: FormGroup, field: string): void =>
      this.formUtilsShared.updateFieldErrors(
        form,
        field,
        this.taskDetailFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      )
    );

    this.searchForm.controls.query.valueChanges.subscribe((value: string): void => {
      const query: string = value ?? '';
      this.searchQuery.set(query);
      this.onSearchChange(query);
    });

    this.profileForm.controls.newPassword.valueChanges.subscribe((): void => {
      this.profileForm.controls.confirmPassword.updateValueAndValidity({ emitEvent: false });
    });

    this.boardService.listBoards().subscribe((boards: BoardModel[]): void => {
      this.boards = boards;
      const email: string | null = this.authService.getEmailFromToken();
      const key = `activeBoardId:${email}`;
      const savedId: string | null = sessionStorage.getItem(key);

      if (savedId) {
        const savedBoard: BoardModel | undefined = boards.find((b: BoardModel): boolean => b.id === Number(savedId));

        if (savedBoard) {
          this.board = savedBoard;
          this.boardSwitchForm.controls.selectedBoardId.setValue(savedBoard.id ?? null, { emitEvent: false });
          this.loadBoard();

          return;
        }
      }

      if (boards.length > 0) {
        this.board = boards[0]!;
        this.boardSwitchForm.controls.selectedBoardId.setValue(boards[0]!.id ?? null, { emitEvent: false });
        this.loadBoard();
      } else {
        this.boardService.createBoard({ title: 'Neues Projektboard' }).subscribe((board: BoardModel): void => {
          this.board = board;
          this.boardSwitchForm.controls.selectedBoardId.setValue(board.id ?? null, { emitEvent: false });
          sessionStorage.setItem(key, String(board.id));
          this.loadBoard();
        });
      }
    });
  }

  /**
   * Wechselt das aktuell angezeigte Projektboard basierend auf der Benutzerauswahl im Board-Switcher.
   * Lädt das ausgewählte Board und aktualisiert die Anzeige entsprechend.
   */
  switchBoard(): void {
    const selectedId: number | null = this.boardSwitchForm.controls.selectedBoardId.value;

    if (!selectedId || selectedId === this.board?.id) {
      this.settingsOpen.set(false);

      return;
    }

    this.isSaving.set(true);
    this.handleForms();

    this.boardService.getBoard(selectedId).subscribe({
      next: (board: BoardModel): void => {
        this.board = board;
        const email: string | null = this.authService.getEmailFromToken();
        const key = `activeBoardId:${email}`;
        sessionStorage.setItem(key, String(board.id));
        this.settingsOpen.set(false);
        this.isSaving.set(false);
        this.handleForms();
        this.buildSuggestions();

        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Projektboard wurde gewechselt.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });
      },
      error: (): void => {
        this.isSaving.set(false);
        this.handleForms();

        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Das ausgewählte Projektboard konnte nicht geladen werden.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });
      },
    });
  }

  /**
   * Loggt den aktuell angemeldeten Benutzer aus und leitet ihn zurück zur Login-Seite.
   */
  logout(): void {
    this.authService.logout();
  }

  /**
   * Lädt die Daten des aktuell ausgewählten Projektboards und aktualisiert die Anzeige.
   */
  buildSuggestions(): void {
    const items: TaskModel[] = [];

    if (!this.board || !this.board.columns) {
      this.suggestions.set([]);

      return;
    }

    for (const col of this.board.columns) {
      if (col.tasks) items.push(...col.tasks);
    }

    this.suggestions.set(items);
  }

  /**
   * Aktualisiert die Liste der Suchvorschläge basierend auf der aktuellen Suchanfrage des Benutzers.
   *
   * @param query Die aktuelle Suchanfrage, die der Benutzer eingegeben hat.
   */
  onSearchChange(query: string): void {
    const q: string = (query || '').toLowerCase();

    if (!q || !this.board) {
      this.buildSuggestions();

      return;
    }

    const items: TaskModel[] = [];

    for (const col of this.board.columns || []) {
      for (const t of col.tasks || []) {
        if (t.title?.toLowerCase().includes(q)) items.push(t);
      }
    }

    this.suggestions.set(items);
  }

  /**
   * Setzt die Suchanfrage zurück, löscht die Suchvorschläge und zeigt wieder alle Aufgaben an.
   */
  clearSearch(): void {
    this.searchForm.controls.query.setValue('');
    this.searchQuery.set('');
    this.buildSuggestions();
  }

  /**
   * Wechselt den Profil-Overlay, in dem der Benutzer seine Profilinformationen einsehen und bearbeiten kann.
   */
  toggleProfile(): void {
    if (this.isSaving() || this.settingsOpen()) return;
    this.profileOpen.set(!this.profileOpen());
    this.resetPasswordFields();
  }

  /**
   * Wechselt den Einstellungen-Overlay, in dem der Benutzer Projektboard-bezogene Einstellungen vornehmen kann.
   */
  toggleSettings(): void {
    this.backgroundFieldErrors = {};
    this.inviteFieldErrors = {};
    this.boardSwitchFieldErrors = {};
    this.backgroundGeneralError.set(null);
    const newState = !this.settingsOpen();
    this.settingsOpen.set(newState);

    if (newState) {
      this.backgroundForm.controls.background.setValue(this.board?.background || '');
      this.boardSwitchForm.controls.selectedBoardId.setValue(this.board?.id ?? null, { emitEvent: false });
    } else {
      this.resetPasswordFields();
    }
  }

  /**
   * Wechselt den Editiermodus für den Board-Titel, damit der Benutzer den Titel des Projektboards bearbeiten kann.
   */
  toggleEditTitle(): void {
    this.boardTitleFieldErrors = {};
    const newState = !this.editingTitle();
    this.editingTitle.set(newState);

    if (newState) {
      this.boardTitleForm.controls.title.setValue(this.board?.title || '');
    }
  }

  /**
   * Speichert den neuen Titel des Projektboards, nachdem der Benutzer den Editiermodus verlassen hat.
   */
  saveTitle(): void {
    this.boardTitleFieldErrors = {};
    if (!this.board) return;

    if (this.boardTitleForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.boardTitleForm,
        this.boardTitleFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );
      return;
    }

    const newTitle: string = (this.boardTitleForm.controls.title.value || '').trim();
    this.isSaving.set(true);
    this.handleForms();

    this.boardService
      .updateBoard(this.board.id!, { title: newTitle, background: this.board?.background || null })
      .subscribe((b: BoardModel): void => {
        this.board = b;
        const index: number = this.boards.findIndex((x: BoardModel): boolean => x.id === b.id);

        if (index !== -1) {
          this.boards[index] = b;
        }

        this.editingTitle.set(false);
        this.isSaving.set(false);
        this.handleForms();
      });
  }

  /**
   * Startet den Editiermodus für eine bestimmte Statuskategorie, damit der Benutzer den Titel der Kategorie bearbeiten
   * kann.
   *
   * @param col Die Statuskategorie, die bearbeitet werden soll.
   */
  startEditColumn(col: ColumnModel): void {
    this.columnEditFieldErrors = {};
    this.editingColumnId = col.id!;
    this.columnEditForm.controls.title.setValue(col.title);
  }

  /**
   * Beendet den Editiermodus für die Statuskategorie, ohne die Änderungen zu speichern.
   */
  cancelEditColumn(): void {
    this.editingColumnId = null;
    this.columnEditForm.reset({ title: '' });
    this.columnEditFieldErrors = {};
  }

  /**
   * Speichert den neuen Titel der Statuskategorie, nachdem der Benutzer den Editiermodus verlassen hat.
   */
  saveColumnTitle(col: ColumnModel): void {
    this.columnEditFieldErrors = {};
    if (!col || !this.board) return;

    if (this.columnEditForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.columnEditForm,
        this.columnEditFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    const newTitle: string = (this.columnEditForm.controls.title.value || '').trim();
    const exists: boolean = this.board!.columns!.some(
      (c: ColumnModel): boolean => c.id !== col.id && c.title.trim().toLowerCase() === newTitle.toLowerCase()
    );

    if (exists) {
      this.columnEditFieldErrors['title'] = ['Es gibt schon eine Statuskategorie mit diesem Namen.'];

      return;
    }

    this.isSaving.set(true);
    this.handleForms();

    this.boardService.updateColumn(col.id!, { title: newTitle }).subscribe({
      next: (updatedBoard: BoardModel): void => {
        this.board = updatedBoard;
        this.cancelEditColumn();
        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        this.columnEditFieldErrors['title'] = ['Fehler beim Speichern.'];
        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Setzt das Hintergrundbild des Projektboards auf die vom Benutzer angegebene URL, nachdem die URL validiert wurde
   * und das Bild existiert.
   *
   * @returns Ein Promise, das aufgelöst wird, wenn der Hintergrund erfolgreich aktualisiert wurde oder ein Fehler
   * aufgetreten ist.
   */
  async updateBackground(): Promise<void> {
    this.backgroundFieldErrors = {};
    this.backgroundGeneralError.set(null);
    if (!this.board) return;

    if (this.backgroundForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.backgroundForm,
        this.backgroundFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    const bg: string = (this.backgroundForm.controls.background.value || '').trim();
    const newBackground: string | null = bg || null;
    const imageValid: boolean = await this.validateImageUrl(bg);

    if (!imageValid) {
      this.backgroundFieldErrors['background'] = ['URL ungültig oder Hintergrundbild existiert nicht.'];

      return;
    }

    this.isSaving.set(true);
    this.handleForms();

    this.boardService
      .updateBoard(this.board.id!, { title: this.board?.title || '', background: newBackground })
      .subscribe({
        next: (b: BoardModel): void => {
          this.board = b;
          this.settingsOpen.set(false);
          this.isSaving.set(false);
          this.handleForms();

          void Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: 'Hintergrundbild wurde erfolgreich geändert.',
            showConfirmButton: false,
            timer: 2500,
            timerProgressBar: true,
          });
        },
        error: (): void => {
          this.isSaving.set(false);
          this.handleForms();
          this.backgroundGeneralError.set('Fehler beim Speichern des Hintergrundbilds. Bitte versuchen Sie es erneut.');
        },
      });
  }

  /**
   * Lädt einen Benutzer anhand seiner ID und initialisiert die Profilinformationen im Dashboard.
   */
  inviteMember(): void {
    this.inviteFieldErrors = {};
    if (!this.board) return;

    if (this.inviteForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.inviteForm,
        this.inviteFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    const email: string = (this.inviteForm.controls.email.value || '').trim();
    this.isSaving.set(true);
    this.handleForms();

    this.boardService.invite(this.board.id!, email).subscribe({
      next: (): void => {
        this.isSaving.set(false);
        this.handleForms();
        this.loadBoard();
        this.inviteForm.reset();

        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Nutzer ' + email + ' wurde erfolgreich eingeladen.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.settingsOpen.set(false);
      },
      error: (err: HttpErrorResponse): void => {
        this.isSaving.set(false);
        this.handleForms();
        this.inviteFieldErrors['email'] = [err.error.message || 'Fehler beim Einladen des Benutzers.'];
      },
    });
  }

  /**
   * Wechselt den Profil-Overlay in den Editiermodus, damit der Benutzer seine Profilinformationen bearbeiten kann.
   */
  enterProfileEditMode(): void {
    this.profileEditMode.set(true);
    this.profileFieldErrors = {};
    this.profileGeneralError.set(null);

    this.profileForm.patchValue({
      name: this.profileName,
      email: this.profileEmail,
      image: this.profileImage,
    });
  }

  /**
   * Beendet den Editiermodus für das Profil, ohne die Änderungen zu speichern, und setzt alle Fehlermeldungen zurück.
   */
  cancelProfileEdit(): void {
    this.profileEditMode.set(false);
    this.profileFieldErrors = {};
    this.profileGeneralError.set(null);
    this.resetPasswordFields();
  }

  /**
   * Schließt den Profil-Overlay, setzt alle Fehlermeldungen zurück und verlässt den Editiermodus, falls dieser aktiv ist.
   */
  closeProfilePopup(): void {
    this.profileEditMode.set(false);
    this.profileOpen.set(false);
    this.profileFieldErrors = {};
    this.profileGeneralError.set(null);
  }

  /**
   * Aktualisiert die Profilinformationen des Benutzers, einschließlich Name, E-Mail, Profilbild und optional Passwortänderung.
   *
   * @returns Ein Promise, das aufgelöst wird, wenn die Profilinformationen erfolgreich aktualisiert wurden oder ein
   * Fehler aufgetreten ist.
   */
  async updateProfile(): Promise<void> {
    this.profileFieldErrors = {};
    this.profileGeneralError.set(null);

    if (this.profileForm.invalid) {
      this.formUtilsShared.markFormGroupTouched(
        this.profileForm,
        this.profileFieldErrors,
        this.formUtilsShared.getErrorMessage.bind(this.formUtilsShared)
      );

      return;
    }

    const name: string = (this.profileForm.controls.name.value || '').trim();
    const newEmail: string = (this.profileForm.controls.email.value || '').trim();
    const currentPwd: string = (this.profileForm.controls.currentPassword.value || '').trim();
    const newPwd: string = (this.profileForm.controls.newPassword.value || '').trim();
    const confirmPwd: string = (this.profileForm.controls.confirmPassword.value || '').trim();
    const image: string = (this.profileForm.controls.image.value || '').trim() || '';
    const wantsPasswordChange: boolean = !!currentPwd || !!newPwd || !!confirmPwd;

    if (wantsPasswordChange) {
      if (!currentPwd) {
        this.profileFieldErrors['currentPassword'] = ['Aktuelles Passwort erforderlich.'];
      }

      if (!newPwd) {
        this.profileFieldErrors['newPassword'] = ['Neues Passwort erforderlich.'];
      }

      if (!confirmPwd) {
        this.profileFieldErrors['confirmPassword'] = ['Passwortbestätigung erforderlich.'];
      }

      if (currentPwd && newPwd && currentPwd === newPwd) {
        this.profileFieldErrors['newPassword'] = ['Neues Passwort muss sich vom aktuellen Passwort unterscheiden.'];
      }
    }

    if (Object.keys(this.profileFieldErrors).length > 0) return;
    const imageValid: boolean = await this.validateImageUrl(image);

    if (!imageValid) {
      this.profileFieldErrors['image'] = ['URL ungültig oder Profilbild existiert nicht.'];

      return;
    }

    this.isSaving.set(true);
    this.handleForms();

    try {
      let passwordChanged = false;

      if (wantsPasswordChange) {
        await firstValueFrom(this.authService.changePassword(currentPwd, newPwd, confirmPwd));
        passwordChanged = true;
      }

      const updatedUser: UserModel = await firstValueFrom(this.authService.updateProfile(name, newEmail, image));
      this.profileEditMode.set(false);
      this.resetPasswordFields();
      this.profileName = updatedUser.name || '';
      this.profileEmail = updatedUser.email || '';
      this.profileImage = updatedUser.image || '';
      const emailChanged: boolean = updatedUser.emailChanged;

      if (emailChanged || passwordChanged) {
        this.isSaving.set(false);
        this.handleForms();
        this.logout();

        await Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Profil erfolgreich aktualisiert. Bitte E-Mail prüfen und erneut einloggen.',
          showConfirmButton: false,
          timer: 5000,
          timerProgressBar: true,
        });
      } else {
        this.isSaving.set(false);
        this.handleForms();
        this.closeProfilePopup();

        await Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Profil wurde erfolgreich aktualisiert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });
      }
    } catch (err) {
      this.isSaving.set(false);
      this.handleForms();
      this.handleError(err as HttpErrorResponse);
    }
  }

  /**
   * Deaktiviert den Account des aktuell angemeldeten Benutzers nach einer Bestätigungsabfrage und loggt ihn anschließend aus.
   */
  deactivateAccount(): void {
    const email: string | null = this.authService.getEmailFromToken();
    if (!email) return;
    this.isSaving.set(true);
    this.handleForms();

    Swal.fire({
      title: 'Möchten Sie Ihren Account wirklich deaktivieren?',
      showDenyButton: true,
      confirmButtonText: 'Ja.',
      denyButtonText: `Nein.`,
      confirmButtonColor: '#dc3545',
      denyButtonColor: '#10b981',
    }).then((result: SweetAlertResult): void => {
      if (result.isDenied || result.isDismissed) {
        void Swal.fire('Account wurde nicht deaktiviert.', '', 'info');
        this.isSaving.set(false);
        this.handleForms();

        return;
      }

      this.authService.deactivateAccount(email).subscribe((): void => {
        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Account wurde erfolgreich deaktiviert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
        this.authService.logout();
      });
    });
  }

  /**
   * Gibt die Initialen des aktuell angemeldeten Benutzers zurück, basierend auf seinem Namen oder seiner E-Mail-Adresse.
   *
   * @return Ein String mit den Initialen des Benutzers.
   */
  getCurrentUserInitials(): string {
    const user: UserModel | null = this.authService.getCurrentUserSnapshot();

    if (user?.name) {
      const parts: string[] = user.name.split(' ');

      if (parts.length >= 2) {
        return (parts[0]![0]! + parts[1]![0]!).toUpperCase();
      }

      return parts[0]!.substring(0, 2).toUpperCase();
    }

    const email: string | null = this.authService.getEmailFromToken();

    if (email) {
      return email.substring(0, 2).toUpperCase();
    }

    return 'U';
  }

  /**
   * Gibt die Initialen eines Benutzers zurück, basierend auf seinem Namen oder seiner E-Mail-Adresse.
   *
   * @param user Der Benutzer, für den die Initialen generiert werden sollen.
   * @returns Ein String mit den Initialen des Benutzers.
   */
  getUserInitials(user: UserModel): string {
    const source: string = user.name || user.email || '';
    if (!source) return 'U';
    const parts: string[] = source.split(' ');

    if (parts.length >= 2) {
      return (parts[0]![0]! + parts[1]![0]!).toUpperCase();
    }

    return source.substring(0, 2).toUpperCase();
  }

  /**
   * Startet den Erstellungsmodus für eine neue Aufgabe in einer bestimmten Statuskategorie, damit der Benutzer die
   * Details der neuen Aufgabe eingeben kann.
   *
   * @param columnId Die ID der Statuskategorie, in der die neue Aufgabe erstellt werden soll.
   */
  startCreateTask(columnId?: number): void {
    if (this.creatingTaskColumnId === columnId) {
      this.cancelCreateTask();

      return;
    }

    this.creatingTaskColumnId = columnId ?? null;
    this.taskCreateForm.reset({ title: '' });
    this.taskCreateFieldErrors = {};
    this.taskCreateGeneralError.set(null);
  }

  /**
   * Beendet den Erstellungsmodus für eine neue Aufgabe, ohne die Aufgabe zu erstellen, und setzt alle Fehlermeldungen zurück.
   */
  cancelCreateTask(): void {
    this.creatingTaskColumnId = null;
    this.taskCreateForm.reset({ title: '' });
    this.taskCreateFieldErrors = {};
    this.taskCreateGeneralError.set(null);
  }

  /**
   * Erstellt eine neue Aufgabe in der aktuell ausgewählten Statuskategorie, nachdem die Eingaben des Benutzers validiert
   * wurden.
   */
  createTask(): void {
    if (!this.board || !this.creatingTaskColumnId) return;
    this.taskCreateFieldErrors = {};
    this.taskCreateGeneralError.set(null);
    const title: string = (this.taskCreateForm.controls.title.value || '').trim();

    if (!title) {
      this.taskCreateFieldErrors['title'] = ['Titel ist erforderlich.'];

      return;
    }

    const exists: boolean = this.board
      .columns!.flatMap((c: ColumnModel): TaskModel[] => c.tasks || [])
      .some((t: TaskModel): boolean => t.title?.trim().toLowerCase() === title.toLowerCase());

    if (exists) {
      this.taskCreateFieldErrors['title'] = ['Es existiert bereits eine Aufgabe mit diesem Titel.'];

      return;
    }

    const col: ColumnModel | undefined = this.board.columns!.find(
      (c: ColumnModel): boolean => c.id === this.creatingTaskColumnId
    );
    if (!col) return;
    const payload: Partial<TaskModel> = { title };
    this.isSaving.set(true);
    this.handleForms();

    this.boardService.addTask(this.creatingTaskColumnId!, payload).subscribe({
      next: (task: TaskModel): void => {
        col.tasks = col.tasks || [];
        col.tasks.push(task);
        this.taskCreateForm.reset({ title: '' });
        this.creatingTaskColumnId = null;
        this.taskCreateFieldErrors = {};
        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        this.taskCreateGeneralError.set('Fehler beim Erstellen der Aufgabe. Bitte versuchen Sie es erneut.');
        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Fragt den Benutzer nach einer Bestätigung, bevor eine Statuskategorie gelöscht wird, und löscht die Kategorie sowie
   * alle darin enthaltenen Aufgaben, wenn der Benutzer bestätigt.
   *
   * @param col Die Statuskategorie, die gelöscht werden soll.
   */
  confirmDeleteColumn(col: ColumnModel): void {
    this.isSaving.set(true);
    this.handleForms();

    Swal.fire({
      title: 'Möchtest du diese Statuskategorie wirklich löschen? Alle Aufgaben darin werden ebenfalls gelöscht.',
      showDenyButton: true,
      confirmButtonText: 'Ja.',
      denyButtonText: `Nein.`,
      confirmButtonColor: '#dc3545',
      denyButtonColor: '#10b981',
    }).then((result: SweetAlertResult): void => {
      if (result.isDenied || result.isDismissed) {
        void Swal.fire('Statuskategorie wurde nicht gelöscht.', '', 'info');
        this.isSaving.set(false);
        this.handleForms();

        return;
      }

      this.deleteColumn(col);
    });
  }

  /**
   * Löscht eine Statuskategorie und alle darin enthaltenen Aufgaben und aktualisiert die Anzeige.
   *
   * @param col Die Statuskategorie, die gelöscht werden soll.
   */
  deleteColumn(col: ColumnModel): void {
    this.boardService.deleteColumn(col.id).subscribe({
      next: (): void => {
        this.board!.columns = this.board!.columns!.filter((c: ColumnModel): boolean => c.id !== col.id);

        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Statuskategorie wurde erfolgreich gelöscht.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Löschen der Statuskategorie.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Öffnet den Detail-Overlay für eine bestimmte Aufgabe, damit der Benutzer die Details der Aufgabe einsehen und bearbeiten
   * kann.
   *
   * @param task Die Aufgabe, deren Details angezeigt werden sollen.
   */
  openTaskDetail(task: TaskModel): void {
    if (this.isSaving()) return;
    this.selectedTask = { ...task };
    this.taskDetailOpen.set(true);
    document.body.style.overflow = 'hidden';

    this.taskDetailForm.reset({
      title: task.title || '',
      description: task.description || '',
      deadline: this.parseDeadline(task.deadline),
      labels: task.labels || '',
      attachments: task.attachments || '',
      assigneeId: task.assignees && task.assignees.length > 0 ? task.assignees[0]!.id! : null,
    });

    this.taskDetailFieldErrors = {};
    this.taskDetailGeneralError.set(null);
    this.clearSearch();
  }

  /**
   * Schließt den Detail-Overlay für die aktuell ausgewählte Aufgabe, setzt alle Fehlermeldungen zurück und leert die
   * ausgewählten Aufgabendetails.
   */
  closeTaskDetail(): void {
    this.taskDetailOpen.set(false);
    this.selectedTask = null;
    this.taskDetailFieldErrors = {};
    this.taskDetailGeneralError.set(null);

    this.taskDetailForm.reset({
      title: '',
      description: '',
      deadline: null,
      labels: '',
      attachments: '',
      assigneeId: null,
    });

    document.body.style.overflow = '';
  }

  /**
   * Speichert die Änderungen an den Details der aktuell ausgewählten Aufgabe, nachdem die Eingaben des Benutzers validiert
   * wurden, und aktualisiert die Anzeige entsprechend.
   */
  saveTaskDetails(): void {
    if (!this.selectedTask || !this.selectedTask.id) return;
    this.taskDetailFieldErrors = {};
    this.taskDetailGeneralError.set(null);
    const title: string = (this.taskDetailForm.controls.title.value || '').trim();

    if (!title) {
      this.taskDetailFieldErrors['title'] = ['Titel ist erforderlich.'];

      return;
    }

    const deadlineValue: DateInput | null = this.taskDetailForm.controls.deadline.value;
    const normalizedDeadline: string | null = this.normalizeDeadline(deadlineValue);

    if (!this.isDeadlineValid(normalizedDeadline)) {
      this.taskDetailFieldErrors['deadline'] = ['Deadline darf nicht in der Vergangenheit liegen.'];

      return;
    }

    const assigneeId: number | null = this.taskDetailForm.controls.assigneeId.value;
    const assigneeIds: number[] = assigneeId ? [assigneeId] : [];
    const payload = {
      title,
      description: this.taskDetailForm.controls.description.value,
      deadline: normalizedDeadline,
      labels: this.taskDetailForm.controls.labels.value,
      attachments: this.taskDetailForm.controls.attachments.value,
      assigneeIds,
    };

    this.isSaving.set(true);
    this.handleForms();

    this.boardService.updateTask(this.selectedTask.id, payload).subscribe({
      next: (): void => {
        this.loadBoard();
        this.closeTaskDetail();

        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Aufgabe wurde erfolgreich aktualisiert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        this.taskDetailGeneralError.set('Fehler beim Aktualisieren der Aufgabe. Bitte versuchen Sie es erneut.');
        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Fragt den Benutzer nach einer Bestätigung, bevor eine Aufgabe gelöscht wird, und löscht die Aufgabe, wenn der Benutzer
   * bestätigt.
   */
  deleteSelectedTask(): void {
    if (!this.selectedTask || !this.selectedTask.id) return;
    const taskId: number = this.selectedTask.id;
    this.isSaving.set(true);
    this.handleForms();

    Swal.fire({
      title: 'Möchten Sie diese Aufgabe wirklich löschen?',
      showDenyButton: true,
      confirmButtonText: 'Ja.',
      denyButtonText: `Nein.`,
      confirmButtonColor: '#dc3545',
      denyButtonColor: '#10b981',
    }).then((result: SweetAlertResult): void => {
      if (result.isDenied || result.isDismissed) {
        void Swal.fire('Aufgabe wurde nicht gelöscht.', '', 'info');
        this.isSaving.set(false);
        this.handleForms();

        return;
      }

      this.boardService.deleteTask(taskId).subscribe({
        next: (): void => {
          this.loadBoard();
          this.closeTaskDetail();

          void Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: 'Aufgabe wurde erfolgreich gelöscht.',
            showConfirmButton: false,
            timer: 2500,
            timerProgressBar: true,
          });

          this.isSaving.set(false);
          this.handleForms();
        },
        error: (): void => {
          void Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'error',
            title: 'Fehler beim Löschen der Aufgabe.',
            showConfirmButton: false,
            timer: 2500,
            timerProgressBar: true,
          });

          this.isSaving.set(false);
          this.handleForms();
        },
      });
    });
  }

  /**
   * Startet den Erstellungsmodus für eine neue Statuskategorie, damit der Benutzer den Titel der neuen Kategorie eingeben
   * kann.
   */
  addColumn(): void {
    this.newColumnFieldErrors = {};
    if (!this.board) return;
    const title: string = (this.newColumnForm.controls.title.value || '').trim();

    if (!title) {
      this.newColumnFieldErrors['title'] = ['Titel ist erforderlich.'];

      return;
    }

    const exists: boolean = this.board.columns!.some(
      (c: ColumnModel) => c.title.trim().toLowerCase() === title.toLowerCase()
    );

    if (exists) {
      this.newColumnFieldErrors['title'] = ['Es gibt schon eine Statuskategorie mit diesem Namen.'];

      return;
    }

    this.isSaving.set(true);
    this.handleForms();

    this.boardService.addColumn(this.board.id!, title).subscribe({
      next: (col: ColumnModel): void => {
        col.tasks = col.tasks || [];
        this.board!.columns = [...(this.board!.columns || []), col].sort(
          (a: ColumnModel, b: ColumnModel): number => (a.position ?? 0) - (b.position ?? 0)
        );
        this.newColumnForm.reset({ title: '' });
        this.showAddColumn.set(false);
        this.newColumnFieldErrors = {};
        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        this.newColumnFieldErrors['title'] = ['Fehler beim Erstellen der Statuskategorie.'];
        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Beendet den Erstellungsmodus für eine neue Statuskategorie, ohne die Kategorie zu erstellen, und setzt alle
   * Fehlermeldungen zurück.
   */
  cancelAddColumn(): void {
    this.showAddColumn.set(false);
    this.newColumnForm.reset({ title: '' });
    this.newColumnFieldErrors = {};
  }

  /**
   * Startet den Drag-and-Drop-Vorgang für eine Aufgabe, damit der Benutzer die Aufgabe in eine andere Statuskategorie
   * verschieben kann.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn der Benutzer beginnt, eine Aufgabe zu ziehen.
   * @param taskId Die ID der Aufgabe, die gezogen wird, damit sie beim Drop korrekt identifiziert und verschoben werden kann.
   */
  onDragStart(event: DragEvent, taskId?: number): void {
    if (this.isSaving()) return;
    if (!event.dataTransfer || !taskId) return;
    this.isTaskDrag.set(true);
    event.dataTransfer.setData('task', String(taskId));
  }

  /**
   * Erlaubt das Ablegen einer Aufgabe in einer Statuskategorie, indem das Standardverhalten des Browsers verhindert wird,
   * wenn eine Aufgabe über einer Statuskategorie gezogen wird.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn eine Aufgabe über einer Statuskategorie gezogen wird,
   * damit das Ablegen der Aufgabe ermöglicht wird.
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Verschiebt eine Aufgabe in eine andere Statuskategorie, nachdem der Benutzer die Aufgabe über der Zielkategorie
   * fallen gelassen hat, und aktualisiert die Anzeige entsprechend.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn eine Aufgabe über einer Statuskategorie fallen gelassen wird,
   * damit die Aufgabe korrekt verschoben und die Anzeige aktualisiert werden kann.
   * @param targetColumnId Die ID der Statuskategorie, in die die Aufgabe verschoben werden soll, damit die Aufgabe
   * korrekt zugeordnet und die Anzeige aktualisiert werden kann.
   */
  onDrop(event: DragEvent, targetColumnId?: number): void {
    event.preventDefault();
    event.stopPropagation();
    const id = Number(event.dataTransfer?.getData('task'));
    if (!id || !targetColumnId) return;
    this.isTaskDrag.set(false);
    const targetCol: ColumnModel = this.board!.columns!.find((c: ColumnModel): boolean => c.id === targetColumnId)!;
    const pos: number = targetCol.tasks?.length ?? 0;
    this.isSaving.set(true);
    this.handleForms();

    this.boardService.moveTask(id, targetColumnId, pos).subscribe({
      next: (): void => {
        this.loadBoard();
        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Verschieben der Aufgabe.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Startet den Drag-and-Drop-Vorgang für eine Statuskategorie, damit der Benutzer die Kategorie an eine andere Position
   * innerhalb des Projektboards verschieben kann.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn der Benutzer beginnt, eine Statuskategorie zu ziehen.
   * @param index Der Index der Statuskategorie, die gezogen wird, damit sie beim Drop korrekt identifiziert und
   * verschoben werden kann.
   */
  onColumnDragStart(event: DragEvent, index: number): void {
    if (this.isSaving()) return;
    if (!event.dataTransfer || index < 0) return;
    this.isTaskDrag.set(false);
    event.dataTransfer?.setData('column', String(index));
  }

  /**
   * Erlaubt das Ablegen einer Statuskategorie an einer anderen Position innerhalb des Projektboards, indem das
   * Standardverhalten des Browsers verhindert wird, wenn eine Statuskategorie über einer anderen Statuskategorie gezogen
   * wird.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn eine Statuskategorie über einer anderen Statuskategorie gezogen
   * wird, damit das Ablegen der Statuskategorie ermöglicht wird.
   */
  onColumnDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Verschiebt eine Statuskategorie an eine andere Position innerhalb des Projektboards, nachdem der Benutzer die Kategorie
   * über der Zielposition fallen gelassen hat, und aktualisiert die Anzeige entsprechend.
   *
   * @param event Das DragEvent, das ausgelöst wird, wenn eine Statuskategorie über einer anderen Statuskategorie
   * fallen gelassen wird, damit die Kategorie korrekt verschoben und die Anzeige aktualisiert werden kann.
   * @param targetIndex Der Index, an den die Statuskategorie verschoben werden soll, damit die Kategorie korrekt
   * positioniert und die Anzeige aktualisiert werden kann.
   */
  onColumnDrop(event: DragEvent, targetIndex: number): void {
    event.preventDefault();
    event.stopPropagation();
    const fromIndex = Number(event.dataTransfer?.getData('column'));
    if (isNaN(fromIndex)) return;
    if (this.isTaskDrag()) return;
    if (fromIndex === targetIndex) return;
    const cols: ColumnModel[] = [...this.board!.columns!];
    const moved: ColumnModel | undefined = cols.splice(fromIndex, 1)[0];
    cols.splice(targetIndex, 0, moved!);
    cols.forEach((c: ColumnModel, i: number): number => (c.position = i));
    this.board!.columns = cols;
    const movedColumn: ColumnModel | undefined = moved;
    this.isSaving.set(true);
    this.handleForms();

    this.boardService.moveColumn(movedColumn!.id, movedColumn!.position!).subscribe({
      next: (): void => {
        this.loadBoard();
        this.isSaving.set(false);
        this.handleForms();
      },
      error: (): void => {
        void Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Verschieben der Statuskategorie.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true,
        });

        this.isSaving.set(false);
        this.handleForms();
      },
    });
  }

  /**
   * Wechselt die Sichtbarkeit der Passwortfelder im Profil-Overlay, damit der Benutzer sein Passwort während der Bearbeitung
   * seines Profils einsehen kann, um Tippfehler zu vermeiden, oder die Felder ausgeblendet lassen kann, um seine Privatsphäre
   * zu schützen.
   */
  togglePasswordVisibility(): void {
    this.showPassword.set(!this.showPassword());
  }

  /**
   * Wechselt die Sichtbarkeit des Passwortbestätigungsfelds im Profil-Overlay, damit der Benutzer die Bestätigung seines neuen
   * Passworts während der Bearbeitung seines Profils einsehen kann, um Tippfehler zu vermeiden, oder das Feld ausgeblendet
   * lassen kann, um seine Privatsphäre zu schützen.
   */
  togglePasswordConfirmVisibility(): void {
    this.showPasswordConfirm.set(!this.showPasswordConfirm());
  }

  /**
   * Lädt die Daten des aktuell ausgewählten Projektboards vom Server, aktualisiert die Anzeige mit den neuesten Informationen
   * und stellt sicher, dass die WebSocket-Verbindung für das Board korrekt eingerichtet ist, damit der Benutzer immer die
   * aktuellsten Informationen sieht und in Echtzeit mit anderen Benutzern zusammenarbeiten kann.
   */
  private loadBoard(): void {
    const id: number | undefined = this.board?.id;
    if (!id) return;

    this.boardService.getBoard(id).subscribe((board: BoardModel): void => {
      this.board = { ...board };

      this.boardService.listBoards().subscribe((allBoards: BoardModel[]): void => {
        this.boards = allBoards;
        this.boardSwitchForm.controls.selectedBoardId.setValue(board.id ?? null, { emitEvent: false });
      });

      this.boardTitleForm.controls.title.setValue(board.title || '');
      this.backgroundForm.controls.background.setValue(board.background || '');
      this.buildSuggestions();
    });

    this.websocket.subscribeBoard(id);
  }

  /**
   * Lädt die Daten des aktuell angemeldeten Benutzers vom Server, aktualisiert die Profilinformationen im Dashboard und
   * stellt sicher, dass die Profilinformationen des Benutzers im Board-Member-Objekt auf dem neuesten Stand sind,
   * damit der Benutzer immer die aktuellsten Informationen sieht und sein Profil korrekt im Dashboard und in der
   * Mitgliederliste des Boards angezeigt wird.
   */
  private loadCurrentUser(): void {
    this.authService.getCurrentUser().subscribe();

    this.authService.currentUser$.subscribe((user: UserModel | null): void => {
      if (user) {
        this.profileName = user.name || '';
        this.profileEmail = user.email || '';
        this.profileImage = user.image || '';

        if (this.board?.members) {
          const me: UserModel | undefined = this.board.members.find((m: UserModel): boolean => m.email === user.email);

          if (me) {
            me.image = user.image || '';
            me.name = user.name || me.name;
          }
        }
      }
    });
  }

  /**
   * Validiert eine gegebene URL, um sicherzustellen, dass sie entweder leer ist oder auf eine gültige Bilddatei
   * (PNG, JPG, JPEG, WEBP) verweist, und überprüft zusätzlich, ob das Bild tatsächlich existiert, indem es versucht,
   * es zu laden.
   *
   * @param url Die URL, die validiert werden soll.
   * @returns Ein Promise, das aufgelöst wird mit `true`, wenn die URL gültig ist oder leer ist, und mit `false`,
   * wenn die URL ungültig ist oder das Bild nicht existiert.
   */
  private validateImageUrl(url: string): Promise<boolean> {
    return new Promise((resolve: (value: boolean | PromiseLike<boolean>) => void): void => {
      if (!url) return resolve(true);
      const pattern = /^(|https?:\/\/.+\.(png|jpg|jpeg|webp)(\?.*)?)$/i;

      if (!pattern.test(url)) {
        return resolve(false);
      }

      const img = new Image();
      img.onload = (): void => resolve(true);
      img.onerror = (): void => resolve(false);
      img.src = url;
    });
  }

  /**
   * Überprüft, ob eine gegebene Deadline gültig ist.
   *
   * @param deadline Die Deadline, die validiert werden soll, um sicherzustellen, dass sie entweder leer ist oder auf
   * ein Datum in der Gegenwart oder Zukunft verweist.
   * @returns `true`, wenn die Deadline gültig ist (leer oder in der Gegenwart/Zukunft), und `false`, wenn die Deadline
   * ungültig ist (in der Vergangenheit).
   */
  private isDeadlineValid(deadline: string | null | undefined): boolean {
    if (!deadline) return true;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const deadlineDate = new Date(deadline);
    deadlineDate.setHours(0, 0, 0, 0);

    return deadlineDate >= today;
  }

  /**
   * Setzt die Werte der Passwortfelder im Profil-Formular zurück.
   */
  private resetPasswordFields(): void {
    this.profileForm.patchValue({
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    });
  }

  /**
   * Aktiviert oder deaktiviert alle Formulare im Dashboard basierend auf dem aktuellen Status des `isSaving`-Flags.
   */
  private handleForms(): void {
    if (this.isSaving()) {
      this.backgroundForm.disable();
      this.inviteForm.disable();
      this.boardSwitchForm.disable();
      this.profileForm.disable();
      this.boardTitleForm.disable();
      this.columnEditForm.disable();
      this.newColumnForm.disable();
      this.taskCreateForm.disable();
      this.taskDetailForm.disable();
      this.searchForm.disable();
    } else {
      this.backgroundForm.enable();
      this.inviteForm.enable();
      this.boardSwitchForm.enable();
      this.profileForm.enable();
      this.boardTitleForm.enable();
      this.columnEditForm.enable();
      this.newColumnForm.enable();
      this.taskCreateForm.enable();
      this.taskDetailForm.enable();
      this.searchForm.enable();
    }
  }

  /**
   * Normalisiert einen gegebenen Deadline-Wert, indem er entweder in ein standardisiertes Datumsformat (YYYY-MM-DD)
   * umgewandelt wird.
   *
   * @param value Der Deadline-Wert, der normalisiert werden soll.
   * @returns Ein String im Format YYYY-MM-DD, wenn der Wert ein gültiges Date-Objekt ist, oder der ursprüngliche
   * String-Wert, wenn er bereits im richtigen Format vorliegt, oder `null`, wenn der Wert ungültig ist oder nicht
   * verarbeitet werden kann.
   */
  private normalizeDeadline(value: DateInput | null): string | null {
    if (!value) return null;

    if (value instanceof Date && !isNaN(value.getTime())) {
      const year: number = value.getFullYear();
      const month: string = String(value.getMonth() + 1).padStart(2, '0');
      const day: string = String(value.getDate()).padStart(2, '0');

      return `${year}-${month}-${day}`;
    }

    if (typeof value === 'string' && value.length >= 10) {
      return value.substring(0, 10);
    }

    return null;
  }

  /**
   * Parst einen gegebenen Deadline-String in ein Date-Objekt.
   *
   * @param deadline Der Deadline-String, der geparst werden soll.
   * @returns Ein Date-Objekt, wenn der String ein gültiges Datum darstellt, oder `null`, wenn der String ungültig ist
   * oder nicht geparst werden kann.
   */
  private parseDeadline(deadline: string | null | undefined): DateInput | null {
    if (!deadline) return null;
    const date = new Date(deadline);
    if (isNaN(date.getTime())) return null;

    return date;
  }

  /**
   * Gibt einen optionalen Validator zurück, der die Stärke eines Passworts überprüft.
   *
   * @returns Ein ValidatorFn, der überprüft, ob ein Passwort mindestens einen Großbuchstaben, einen Kleinbuchstaben,
   * eine Zahl und ein Sonderzeichen enthält.
   */
  private optionalPasswordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password: string = control.value || '';
      if (!password) return null;
      const strong: boolean = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).*$/.test(
        password
      );

      return strong ? null : { weakPassword: true };
    };
  }

  /**
   * Gibt einen Validator zurück, der überprüft, ob das Bestätigungs-Passwort mit dem neuen Passwort übereinstimmt.
   *
   * @returns Ein ValidatorFn, der überprüft, ob das Bestätigungs-Passwort mit dem neuen Passwort übereinstimmt.
   */
  private profilePasswordMatchValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!this.profileForm) {
        return null;
      }

      const password: string | undefined = this.profileForm.get('newPassword')?.value;
      const confirm: string = control.value || '';
      if (!password && !confirm) return null;

      return password === confirm ? null : { passwordMismatch: true };
    };
  }

  /**
   * Behandelt Fehler, die während der Aktualisierung des Benutzerprofils auftreten können, indem sie entweder spezifische
   * Fehlermeldungen für bestimmte Felder setzt, basierend auf dem HTTP-Statuscode des Fehlers, oder eine allgemeine
   * Fehlermeldung anzeigt, wenn der Fehler nicht spezifisch behandelt werden kann.
   *
   * @param error Der HttpErrorResponse, der während der Aktualisierung des Benutzerprofils aufgetreten ist.
   */
  private handleError(error: HttpErrorResponse): void {
    this.profileFieldErrors = {};
    this.profileGeneralError.set(null);

    if (error.status === 400) {
      this.profileFieldErrors['currentPassword'] = [error.error.message || 'Aktuelles Passwort ist falsch.'];

      return;
    }

    if (error.status === 409) {
      this.profileFieldErrors['email'] = [error.error.message || 'E-Mail ist bereits vergeben.'];

      return;
    }

    this.profileGeneralError.set('Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
  }
}
