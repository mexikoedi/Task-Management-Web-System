import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';
import { NgOptionTemplateDirective, NgSelectComponent } from '@ng-select/ng-select';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { BoardService } from '../../service/board.service';
import {DateInput, NgxsmkDatepickerComponent} from 'ngxsmk-datepicker';
import Swal from 'sweetalert2';
import {
  BoardModel,
  ColumnModel,
  TaskModel,
  UserSummary
} from '../../model/board.model';
import {NgIcon} from "@ng-icons/core";
import {WebsocketService} from "../../service/websocket.service";

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgxsmkDatepickerComponent,
    NgIcon,
    NgOptionTemplateDirective,
    NgSelectComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  isSaving = false;
  board: BoardModel | null = null;
  boards: BoardModel[] = [];
  selectedBoardId: number | null = null;
  profileOpen = false;
  settingsOpen = false;
  taskDetailOpen = false;
  selectedTask: TaskModel | null = null;
  showAddColumn = false;
  newColumnTitle = '';
  newColumnError = '';
  searchQuery = '';
  suggestions: TaskModel[] = [];
  editingTitle = false;
  editedTitle = '';
  bgInput = '';
  bgInputError = '';
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;
  currentDay: number = new Date().getDate();
  today: DateInput = new Date();
  comingYear: DateInput = new Date(this.currentYear + 1, this.currentMonth - 1, this.currentDay);

  // Profile update
  profileEditMode = false;
  profileName = '';
  profileNameTemp = '';
  profileEmail = '';
  profileEmailTemp = '';
  profilePasswordCurrent = '';
  profilePasswordNew = '';
  profilePasswordConfirm = '';
  profileImage = '';
  profileImageTemp = '';
  profileErrors: { [key: string]: string } = {};
  showPassword = false;
  showPasswordConfirm = false;

  // Column
  draggedColumnIndex: number | null = null;
  editingColumnId: number | null = null;
  editedColumnTitle: string = '';
  columnErrors: any = {};

  // Create task
  creatingTaskColumnId: number | null = null;
  creatingTask: Partial<TaskModel> = { title: '' };
  taskErrors: { [key: string]: string } = {};
  isTaskDrag = false;

  // Assignee
  selectedAssigneeId: number | null = null;

  constructor(
    public authService: AuthService,
    private router: Router,
    private boardService: BoardService,
    private cdr: ChangeDetectorRef,
    private websocket: WebsocketService,
  ) {}

  ngOnInit(): void {
    // User laden
    this.loadCurrentUser();

    // User-WebSocket
    this.authService.currentUser$.subscribe(user => {
      if (!user) return;

      this.websocket.subscribeUser(user.id!, () => {
        // Profil neu laden
        this.authService.loadCurrentUser(user.email).subscribe();

        // Boards neu laden (z.B. Einladung)
        if (this.board?.id) {
          this.boardService.get(this.board.id).subscribe(board => {
            this.board = { ...board };
            this.buildSuggestions();
            this.cdr.detectChanges();
          });
        }

        this.boardService.list().subscribe(boards => {
          this.boards = boards;
          this.cdr.detectChanges();
        });
      });
    });

    // Boards initial laden + aktives Board bestimmen
    this.boardService.list().subscribe(boards => {
      this.boards = boards;

      const email = this.authService.getEmailFromToken();
      const key = `activeBoardId:${email}`;

      const savedId = localStorage.getItem(key);

      if (savedId) {
        const savedBoard = boards.find(b => b.id === Number(savedId));
        if (savedBoard) {
          this.board = savedBoard;
          this.selectedBoardId = savedBoard.id!;
          this.loadBoard();
          this.cdr.detectChanges();
          return;
        }
      }

      if (boards.length > 0) {
        this.board = boards[0];
        this.selectedBoardId = boards[0].id!;
        this.loadBoard();
        this.cdr.detectChanges();
      } else {
        const ownerEmail = this.authService.getEmailFromToken()!;
        this.boardService.create({ title: 'Neues Projektboard' }, ownerEmail).subscribe(board => {
          this.board = board;
          this.selectedBoardId = board.id!;
          localStorage.setItem(key, String(board.id));
          this.loadBoard();
          this.cdr.detectChanges();
        });
      }
    });
  }

  switchBoard() {
    if (!this.selectedBoardId || this.selectedBoardId === this.board?.id) {
      // Gleiches Board → nichts tun
      this.settingsOpen = false;
      return;
    }

    this.isSaving = true;

    this.boardService.get(this.selectedBoardId).subscribe({
      next: (board) => {
        this.board = board;
        const email = this.authService.getEmailFromToken();
        const key = `activeBoardId:${email}`;
        localStorage.setItem(key, String(board.id));
        this.settingsOpen = false;
        this.isSaving = false;
        this.buildSuggestions();
        this.cdr.detectChanges();

        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Projektboard wurde gewechselt.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
      },
      error: () => {
        this.isSaving = false;
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Das ausgewählte Projektboard konnte nicht geladen werden.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
      }
    });
  }

  private loadCurrentUser() {
    const email = this.authService.getEmailFromToken();
    if (!email) return;
    // 1. User initial laden
    this.authService.loadCurrentUser(email).subscribe(); // ruft HTTP ab & updated BehaviorSubject

    // 2. Subscription, um alle Änderungen live zu übernehmen
    this.authService.currentUser$.subscribe((user) => {
      if (user) {
        this.profileName = user.name || '';
        this.profileEmail = user.email || email;
        this.profileImage = user.image || ''; // <-- lädt korrekt aus BehaviorSubject

        if (this.board?.members) {
          const me = this.board.members.find(m => m.email === user.email);
          if (me) {
            me.image = user.image || '';
            me.name = user.name || me.name;
          }
        }

        this.cdr.detectChanges(); // sicherstellen, dass Angular UI updated
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private loadBoard() {
    const id = this.board?.id;
    if (!id) return;

    // 1. Initiales Laden des Boards
    this.boardService.get(id).subscribe(board => {
      this.board = { ...board };

      this.boardService.list().subscribe(allBoards => {
        this.boards = allBoards;
        this.selectedBoardId = board.id!;
      });

      this.buildSuggestions();
      this.cdr.detectChanges();
    });

    // 2. Board-Realtime-Updates
    this.websocket.subscribeBoard(id, () => {
      this.boardService.get(id).subscribe(board => {
        this.board = { ...board };

        this.boardService.list().subscribe(allBoards => {
          this.boards = allBoards;
          this.selectedBoardId = board.id!;
        });

        this.buildSuggestions();
        this.cdr.detectChanges();
      });
    });
  }

  buildSuggestions() {
    this.suggestions = [];
    if (!this.board || !this.board.columns) return;
    for (const col of this.board.columns) {
      if (col.tasks) this.suggestions.push(...col.tasks);
    }
  }

  onSearchChange() {
    const q = this.searchQuery.toLowerCase();
    this.suggestions = [];
    if (!q || !this.board) return;
    for (const col of this.board.columns || []) {
      for (const t of col.tasks || []) {
        if (t.title.toLowerCase().includes(q)) this.suggestions.push(t);
      }
    }
  }

  clearSearch() {
    this.searchQuery = '';
    this.buildSuggestions();
  }

  toggleProfile() {
    if (this.isSaving) return;
    this.profileOpen = !this.profileOpen;
  }

  toggleSettings() {
    this.profileErrors = {};
    this.settingsOpen = !this.settingsOpen;
    if (this.settingsOpen) {
      this.bgInput = this.board?.background || '';
      this.editedTitle = this.board?.title || '';
    } else {
      this.resetPasswordFields();
      this.bgInputError = '';
    }
  }

  toggleEditTitle() {
    this.profileErrors = {};
    this.editingTitle = !this.editingTitle;
    this.editedTitle = this.board?.title || '';
  }

  saveTitle() {
    this.profileErrors = {};
    if (!this.board) return;
    const newTitle = (this.editedTitle || '').trim();
    if (!newTitle) {
      this.profileErrors['title'] = 'Titel darf nicht leer sein.';
      this.cdr.detectChanges();
      return;
    }
    this.boardService.update(this.board.id!, { title: newTitle }).subscribe((b) => {
      this.board = b;
      const index = this.boards.findIndex(x => x.id === b.id);
      if (index !== -1) {
        this.boards[index] = b;
      }
      this.editingTitle = false;
      this.cdr.detectChanges();
    });
  }

  startEditColumn(col: ColumnModel) {
    this.columnErrors = {};
    this.editingColumnId = col.id!;
    this.editedColumnTitle = col.title;
  }

  cancelEditColumn() {
    this.editingColumnId = null;
    this.editedColumnTitle = '';
    this.columnErrors = {};
  }

  saveColumnTitle(col: ColumnModel) {
    this.columnErrors = {};
    const newTitle = (this.editedColumnTitle || '').trim();

    if (!newTitle) {
      this.columnErrors['title'] = 'Titel darf nicht leer sein.';
      return;
    }

    // Titel muss eindeutig sein
    const exists = this.board!.columns!
      .some(c => c.id !== col.id && c.title.trim().toLowerCase() === newTitle.toLowerCase());

    if (exists) {
      this.columnErrors['title'] = 'Es gibt schon eine Statuskategorie mit diesem Namen.';

      return;
    }

    this.isSaving = true;

    this.boardService.updateColumn(col.id!, { title: newTitle }).subscribe(
      updatedBoard => {
        this.board = updatedBoard;
        this.cancelEditColumn();
        this.isSaving = false;
        this.cdr.detectChanges();
      },
      () => {
        this.columnErrors['title'] = 'Fehler beim Speichern.';
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    );
  }

  async updateBackground() {
    this.profileErrors = {};
    this.bgInputError = '';
    if (!this.board) return;
    const bg = (this.bgInput || '').trim();
    // Wenn Input leer → Hintergrund zurücksetzen
    const newBackground = bg || null;

    const imageValid = await this.validateImageUrl(bg);
    if (!imageValid) {
      this.profileErrors['backgroundImage'] = 'Dieses Hintergrundbild existiert nicht.';
      this.cdr.detectChanges();
      return;
    }

    this.isSaving = true;
    this.boardService.update(this.board.id!, { background: newBackground }).subscribe({
      next: (b) => {
        this.board = b;
        this.bgInput = '';
        this.settingsOpen = false;
        this.isSaving = false;
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Hintergrund wurde erfolgreich geändert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSaving = false;
        this.bgInputError = 'Fehler beim Speichern der Hintergrund-URL.';
        this.cdr.detectChanges();
      }
    });
  }

  inviteMember(emailInput: HTMLInputElement) {
    this.profileErrors = {};
    if (!this.board) return;
    const email = (emailInput.value || '').trim();

    if (!email) {
      this.profileErrors['email'] = 'E-Mail ist erforderlich';
      this.cdr.detectChanges();
      return;
    }

    if (email && !this.validateEmail(email)) {
      this.profileErrors['email'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein';
      this.cdr.detectChanges();
      return;
    }

    this.isSaving = true;
    this.boardService.invite(this.board.id!, email).subscribe({
      next: () => {
        this.isSaving = false;
        this.loadBoard();
        emailInput.value = '';
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Nutzer ' + email + ' wurde erfolgreich eingeladen.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.settingsOpen = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isSaving = false;
        this.profileErrors = {};

        if (err.status === 404) {
          this.profileErrors['email'] = 'Dieser Benutzer existiert nicht.';
          this.cdr.detectChanges();
          return;
        }

        if (err.status === 409) {
          this.profileErrors['email'] = 'Dieser Benutzer ist bereits Mitglied.';
          this.cdr.detectChanges();
          return;
        }

        this.profileErrors['general'] = 'Fehler beim Einladen des Benutzers.';
        this.cdr.detectChanges();
      }
    });
  }

  /* Profile management */
  enterProfileEditMode() {
    this.profileEditMode = true;
    this.profileErrors = {};
    this.profileNameTemp = this.profileName;
    this.profileEmailTemp = this.profileEmail;
    this.profileImageTemp = this.profileImage;
  }

  cancelProfileEdit() {
    this.profileEditMode = false;
    this.profileErrors = {};
    this.resetPasswordFields();
  }

  closeProfilePopup() {
    this.profileEditMode = false;
    this.profileOpen = false;
    this.profileErrors = {};
  }

  async updateProfile() {
    this.profileErrors = {};

    const name = (this.profileNameTemp || '').trim();
    const newEmail = (this.profileEmailTemp || '').trim();
    const currentPwd = (this.profilePasswordCurrent || '').trim();
    const newPwd = (this.profilePasswordNew || '').trim();
    const confirmPwd = (this.profilePasswordConfirm || '').trim();
    const image = (this.profileImageTemp || '').trim() || "";

    // Pflichtfelder prüfen
    if (!name || name.length < 2) this.profileErrors['name'] = 'Name ist erforderlich und muss mindestens 2 Zeichen lang sein ';
    if (!newEmail) this.profileErrors['email'] = 'E-Mail ist erforderlich';

    // E-Mail Format
    if (newEmail && !this.validateEmail(newEmail)) {
      this.profileErrors['email'] = 'Bitte geben Sie eine gültige E-Mail-Adresse ein';
    }

    // Passwortvalidierung
    if (currentPwd || newPwd || confirmPwd) {
      if (!currentPwd) this.profileErrors['currentPassword'] = 'Aktuelles Passwort erforderlich';
      if (!newPwd) this.profileErrors['newPassword'] = 'Neues Passwort erforderlich';
      if (!confirmPwd) this.profileErrors['newPasswordConfirm'] = 'Bestätigung erforderlich';
      if (newPwd && !this.validatePasswordStrength(newPwd)) {
        this.profileErrors['newPassword'] = 'Passwort erfüllt nicht alle Anforderungen';
      }
      if (newPwd && confirmPwd && newPwd !== confirmPwd && !this.validatePasswordMatch(newPwd, confirmPwd))
        this.profileErrors['newPasswordConfirm'] = 'Passwörter stimmen nicht überein';

      if (currentPwd == newPwd)
        this.profileErrors['newPassword'] = 'Neues Passwort muss sich vom aktuellen Passwort unterscheiden';
    }

    if (Object.keys(this.profileErrors).length > 0) return;

    const imageValid = await this.validateImageUrl(image);
    if (!imageValid) {
      this.profileErrors['profileImage'] = 'Dieses Profilbild existiert nicht.';
      this.cdr.detectChanges();
      return;
    }

    this.isSaving = true;
    this.authService
      .updateProfile(name, newEmail, image, currentPwd, newPwd, confirmPwd)
      .subscribe({
        next:(updatedUser: UserSummary) => {
          // BehaviorSubject wurde schon in AuthService aktualisiert
          this.profileEditMode = false;
          this.profilePasswordCurrent = '';
          this.profilePasswordNew = '';
          this.profilePasswordConfirm = '';

          // UI direkt aus BehaviorSubject aktualisieren
          this.profileName = updatedUser.name || '';
          this.profileEmail = updatedUser.email || '';
          this.profileImage = updatedUser.image || '';
          this.cdr.detectChanges();

          const emailChanged = updatedUser.emailChanged;
          const passwordChanged = !!newPwd;

          if (emailChanged || passwordChanged) {
            Swal.fire({
              toast: true,
              position: 'top-end',
              icon: 'success',
              title: 'Profil erfolgreich aktualisiert. Da E-Mail oder Passwort geändert wurde, bitte  E-Mail prüfen und erneut einloggen.',
              showConfirmButton: false,
              timer: 5000,
              timerProgressBar: true
            });
            this.isSaving = false;
            this.logout();
          } else {
            Swal.fire({
              toast: true,
              position: 'top-end',
              icon: 'success',
              title: 'Profil wurde erfolgreich aktualisiert.',
              showConfirmButton: false,
              timer: 2500,
              timerProgressBar: true
            });
            this.isSaving = false;
            this.closeProfilePopup();
            this.cdr.detectChanges();
          }
        },
          error: (err) => {
          this.isSaving = false;
          this.handleError(err);
          this.cdr.detectChanges();
        }
      });
  }

  deactivateAccount() {
    const email = this.authService.getEmailFromToken();
    if (!email) return;
    this.isSaving = true;
    Swal.fire({
      title: "Möchten Sie Ihren Account wirklich deaktivieren?",
      showDenyButton: true,
      confirmButtonText: "Ja.",
      denyButtonText: `Nein.`,
      confirmButtonColor: "#dc3545",
      denyButtonColor: "#10b981"
    }).then((result) => {
      if (result.isDenied || result.isDismissed) {
        Swal.fire("Account wurde nicht deaktiviert.", "", "info");
        this.isSaving = false;
        return;
      }

      this.authService.deactivateAccount(email).subscribe(() => {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Account wurde erfolgreich deaktiviert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
        this.authService.logout();
        this.router.navigate(['/login']);
      });
    });
  }

  getCurrentUserInitials(): string {
    const user = this.authService.getCurrentUserSnapshot();

    if (user?.name) {
      const parts = user.name.split(' ');
      if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
      }
      return parts[0].substring(0, 2).toUpperCase();
    }

    // fallback: email aus Token
    const email = this.authService.getEmailFromToken();
    if (email) {
      return email.substring(0, 2).toUpperCase();
    }

    return 'U';
  }

  getUserInitials(user: any): string {
    const source = user.name || user.email || '';
    if (!source) return 'U';

    const parts = source.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }

    // fallback: email prefix
    return source.substring(0, 2).toUpperCase();
  }


  /* Task management */
  startCreateTask(columnId?: number) {
    if (this.creatingTaskColumnId === columnId) {
      this.cancelCreateTask();
      return;
    }

    this.creatingTaskColumnId = columnId ?? null;
    this.creatingTask = { title: '' };
    this.taskErrors = {};
  }

  cancelCreateTask() {
    this.creatingTaskColumnId = null;
    this.creatingTask = { title: '' };
    this.taskErrors = {};
  }

  createTask() {
    if (!this.board || !this.creatingTaskColumnId) return;
    this.taskErrors = {};
    const title = (this.creatingTask.title || '').trim();
    if (!title) {
      this.taskErrors['title'] = 'Titel ist erforderlich';
    }

    const exists = this.board.columns!
      .flatMap(c => c.tasks || [])
      .some(t => t.title.trim().toLowerCase() === title.toLowerCase());

    if (exists) {
      this.taskErrors['title'] = 'Es existiert bereits eine Aufgabe mit diesem Titel.';
    }

    if (Object.keys(this.taskErrors).length > 0) return;

    const col = this.board.columns!.find((c) => c.id === this.creatingTaskColumnId);
    if (!col) return;
    const payload: Partial<TaskModel> = {
      title
    };
    this.boardService.addTask(this.creatingTaskColumnId!, payload).subscribe(
      (task) => {
        col.tasks = col.tasks || [];
        col.tasks.push(task);
        this.creatingTask = { title: '' };
        this.creatingTaskColumnId = null;
        this.taskErrors = {};
        this.cdr.detectChanges();
      },
      (err) => {
        this.taskErrors['general'] = 'Fehler beim Erstellen der Aufgabe.';
        this.cdr.detectChanges();
      }
    );
  }

  confirmDeleteColumn(col: ColumnModel) {
    this.isSaving = true;
    Swal.fire({
      title: "Möchtest du diese Statuskategorie wirklich löschen? Alle Aufgaben darin werden ebenfalls gelöscht.",
      showDenyButton: true,
      confirmButtonText: "Ja.",
      denyButtonText: `Nein.`,
      confirmButtonColor: "#dc3545",
      denyButtonColor: "#10b981"
    }).then((result) => {
      if (result.isDenied || result.isDismissed) {
        Swal.fire("Statuskategorie wurde nicht gelöscht.", "", "info");
        this.isSaving = false;
        return;
      }

      this.deleteColumn(col);
    });
  }

  deleteColumn(col: ColumnModel) {
    this.boardService.deleteColumn(col.id).subscribe(
      () => {
        this.board!.columns = this.board!.columns!.filter(c => c.id !== col.id);
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Statuskategorie wurde erfolgreich gelöscht.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
        this.cdr.detectChanges();
      },
      () => {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Löschen der Statuskategorie.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
      }
    );
  }

  openTaskDetail(task: TaskModel) {
    if (this.isSaving) return;
    this.selectedTask = { ...task };
    this.taskDetailOpen = true;
    document.body.style.overflow = 'hidden';

    // Assignee aus Task übernehmen
    if (task.assignees && task.assignees.length > 0) {
      this.selectedAssigneeId = task.assignees[0].id!;
    } else {
      this.selectedAssigneeId = null;
    }

    // delete search suggestions and searchQuery of search bar after opening task details
    this.clearSearch()
  }

  closeTaskDetail() {
    this.taskDetailOpen = false;
    this.selectedTask = null;
    this.taskErrors = {};
    document.body.style.overflow = '';
  }

  saveTaskDetails() {
    if (!this.selectedTask || !this.selectedTask.id) return;
    this.taskErrors = {};
    const title = (this.selectedTask.title || '').trim();
    if (!title) {
      this.taskErrors['title'] = 'Titel ist erforderlich';
    }

    if (Object.keys(this.taskErrors).length > 0) return;

    if (this.selectedAssigneeId) {
      const user = this.board?.members?.find(m => m.id === this.selectedAssigneeId);
      if (user) {
          const user = this.board?.members?.find(m => m.id === this.selectedAssigneeId);
          this.selectedTask.assignees = user ? [user] : [];
      }
    }  else {
      this.selectedTask.assignees = [];
    }

    // IDs extrahieren
    const assigneeIds = this.selectedTask.assignees?.map(a => a.id!) || [];

    const payload = {
      title: this.selectedTask.title,
      description: this.selectedTask.description,
      deadline: this.selectedTask.deadline,
      labels: this.selectedTask.labels,
      attachments: this.selectedTask.attachments,
      assigneeIds
    };

    this.isSaving = true;
    this.boardService.updateTask(this.selectedTask.id, payload).subscribe(
      () => {
        this.loadBoard();
        this.closeTaskDetail();
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'success',
          title: 'Aufgabe wurde erfolgreich aktualisiert.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
        this.cdr.detectChanges();
      },
      (err) => {
        this.taskErrors['general'] = 'Fehler beim Aktualisieren der Aufgabe.';
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    );
  }

  deleteSelectedTask() {
    if (!this.selectedTask || !this.selectedTask.id) return;

    const taskId = this.selectedTask.id;
    this.isSaving = true;
    Swal.fire({
      title: "Möchten Sie diese Aufgabe wirklich löschen?",
      showDenyButton: true,
      confirmButtonText: "Ja.",
      denyButtonText: `Nein.`,
      confirmButtonColor: "#dc3545",
      denyButtonColor: "#10b981"
    }).then((result) => {
      if (result.isDenied || result.isDismissed) {
        Swal.fire("Aufgabe wurde nicht gelöscht.", "", "info");
        this.isSaving = false;
        return;
      }

      this.boardService.deleteTask(taskId).subscribe(
        () => {
          this.loadBoard();
          this.closeTaskDetail();
          Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: 'Aufgabe wurde erfolgreich gelöscht.',
            showConfirmButton: false,
            timer: 2500,
            timerProgressBar: true
          });
          this.isSaving = false;
        },
        (err) => {
          Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'error',
            title: 'Fehler beim Löschen der Aufgabe.',
            showConfirmButton: false,
            timer: 2500,
            timerProgressBar: true
          });
          this.isSaving = false;
        }
      );
    });
  }

  addColumn() {
    this.newColumnError = '';
    if (!this.board) return;
    const title = (this.newColumnTitle || '').trim();

    if (!title) {
      this.newColumnError = 'Name für Statuskategorie erforderlich.';

      return;
    }

    const exists = this.board.columns!
      .some(c => c.title.trim().toLowerCase() === title.toLowerCase());

    if (exists) {
      this.newColumnError = 'Es gibt schon eine Statuskategorie mit diesem Namen.';

      return;
    }

    this.boardService.addColumn(this.board.id!, title).subscribe(
      (col: ColumnModel) => {
        // Tasks initialisieren
        col.tasks = col.tasks || [];

        // Column hinzufügen + sortieren
        this.board!.columns = [...(this.board!.columns || []), col]
          .sort((a, b) => (a.position ?? 0) - (b.position ?? 0));

        // UI zurücksetzen
        this.newColumnTitle = '';
        this.showAddColumn = false;
        this.newColumnError = '';

        // Change Detection
        this.cdr.markForCheck();
      },
      (err) => {
        this.newColumnError = 'Fehler beim Erstellen der Statuskategorie.';
        this.cdr.detectChanges();
      }
    );
  }

  cancelAddColumn() {
    this.showAddColumn = false;
    this.newColumnTitle = '';
    this.newColumnError = '';
  }

  onDragStart(event: DragEvent, taskId?: number) {
    if (this.isSaving) return;
    if (!event.dataTransfer || !taskId) return;
    this.isTaskDrag = true;
    event.dataTransfer.setData('task', String(taskId));
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
  }

  onDrop(event: DragEvent, targetColumnId?: number) {
    event.preventDefault();
    const id = Number(event.dataTransfer?.getData('task'));
    if (!id || !targetColumnId) return;
    this.isTaskDrag = false;
    const targetCol = this.board!.columns!.find((c) => c.id === targetColumnId)!;
    const pos = (targetCol.tasks?.length ?? 0);
    this.isSaving = true;
    this.boardService.moveTask(id, targetColumnId, pos).subscribe(
      () => {
        this.loadBoard();
        this.isSaving = false;
      },
      (err) => {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Verschieben der Aufgabe.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
      }
    );
  }

  onColumnDragStart(event: DragEvent, index: number) {
    if (this.isSaving) return;
    if (!event.dataTransfer || !index) return;
    this.isTaskDrag = false;
    this.draggedColumnIndex = index;
    event.dataTransfer?.setData('column', String(index));
  }

  onColumnDragOver(event: DragEvent) {
    event.preventDefault();
  }

  onColumnDrop(event: DragEvent, targetIndex: number) {
    event.preventDefault();
    const fromIndex = Number(event.dataTransfer?.getData('column'));
    if (isNaN(fromIndex)) return;
    if (this.isTaskDrag) return;

    if (fromIndex === targetIndex) return;

    // lokale Reihenfolge ändern
    const cols = [...this.board!.columns!];
    const moved = cols.splice(fromIndex, 1)[0];
    cols.splice(targetIndex, 0, moved);

    // neue Positionen setzen
    cols.forEach((c, i) => c.position = i);

    this.board!.columns = cols;

    // Backend informieren
    this.saveColumnOrder(cols);

    this.draggedColumnIndex = null;
  }

  saveColumnOrder(columns: ColumnModel[]) {
    const payload = columns.map(c => ({
      id: c.id,
      position: c.position
    }));

    this.isSaving = true;
    this.boardService.updateColumnOrder(this.board!.id!, payload).subscribe(
      () => {
        this.loadBoard();
        this.isSaving = false;
      },
      () => {
        Swal.fire({
          toast: true,
          position: 'top-end',
          icon: 'error',
          title: 'Fehler beim Verschieben der Statuskategorie.',
          showConfirmButton: false,
          timer: 2500,
          timerProgressBar: true
        });
        this.isSaving = false;
      }
    );
  }

  onDeadlineChange(rawDate: Date | null): void {
    if (!rawDate || !this.selectedTask) {
      this.selectedTask!.deadline = null;
      return;
    }

    const dateObj = new Date(rawDate);

    // Extract year, month, and day based on local browser time
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');

    // Combine components and attach noon time to prevent timezone roll-overs
    // This satisfies the backend LocalDateTime requirement: "YYYY-MM-DDT12:00:00"
    this.selectedTask.deadline = `${year}-${month}-${day}T12:00:00`;
  }

  private validateImageUrl(url: string): Promise<boolean> {
    return new Promise((resolve) => {
      if (!url) return resolve(true);

      const img = new Image();
      img.onload = () => resolve(true);
      img.onerror = () => resolve(false);
      img.src = url;
    });
  }

  private resetPasswordFields(): void {
    this.profilePasswordCurrent = '';
    this.profilePasswordNew = '';
    this.profilePasswordConfirm = '';
  }

  /**
   * Toggle Passwort-Sichtbarkeit
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  togglePasswordConfirmVisibility(): void {
    this.showPasswordConfirm = !this.showPasswordConfirm;
  }

  /**
   * Validiere Passwort-Stärke
   */
  private validatePasswordStrength(password: string): boolean {
    return /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$/.test(password);
  }
    /**
     * Validiere E-Mail-Format
     */
    private validateEmail(email: string): boolean {
      const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
      return pattern.test(email);
    }

    /**
     * Validiere Passwort-Übereinstimmung
     */
    private validatePasswordMatch(pwd: string, confirm: string): boolean {
      return pwd === confirm;
    }

    /**
     * Prüfe einzelne Passwort-Anforderungen
     */
    getPasswordRequirements(): Array<{ text: string; met: boolean }> {
      const pwd = this.profilePasswordNew || '';
      return [
        { text: 'Mindestens 8 Zeichen', met: pwd.length >= 8 },
        { text: 'Groß- und Kleinbuchstaben', met: /(?=.*[A-Z])(?=.*[a-z])/.test(pwd) },
        { text: 'Mindestens eine Ziffer (0-9)', met: /\d/.test(pwd) },
        { text: 'Mindestens ein Sonderzeichen (!@#$%^&*)', met: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(pwd) }
      ];
    }

    /**
     * Fehlerbehandlung
     */
    private handleError(error: any): void {
      this.profileErrors = {};

      // 400 → falsches aktuelles Passwort
      if (error.status === 400) {
        this.profileErrors['currentPassword'] = 'Aktuelles Passwort ist falsch';
        return;
      }

      // 409 → E-Mail existiert bereits
      if (error.status === 409) {
        this.profileErrors['email'] = 'Diese E-Mail wird bereits verwendet';
        return;
      }

      // 404 -> Profilbild existiert nicht
      if (error.status === 404) {
        this.profileErrors['profileImage'] = 'Dieses Profilbild existiert nicht.';
        return;
      }

      // Fallback
      this.profileErrors['general'] = 'Ein unbekannter Fehler ist aufgetreten';
    }
}
