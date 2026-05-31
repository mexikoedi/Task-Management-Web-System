/**
 * Diese Klasse bietet eine zentrale Sammlung von Funktionen zur Handhabung von Formularen und ihren Eigenschaften.
 */
import { Injectable } from '@angular/core';
import { AbstractControl, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';

@Injectable({ providedIn: 'root' })
export class FormUtilsShared {
  /**
   * Markiert alle Felder eines Formulars als "touched" und aktualisiert die Fehlernachrichten entsprechend.
   *
   * @param formGroup Das Formular, dessen Felder markiert werden sollen.
   * @param fieldErrors Ein Objekt, das die Fehlernachrichten für jedes Feld speichert.
   * @param getErrorMessage Eine Funktion, die eine benutzerfreundliche Fehlermeldung basierend auf dem Feldnamen,
   * dem Fehlerkey und den Fehlerdetails zurückgibt.
   */
  markFormGroupTouched(
    formGroup: FormGroup,
    fieldErrors: Record<string, string[]>,
    getErrorMessage: (field: string, errorKey: string, errorValue: ValidationErrors) => string
  ): void {
    Object.keys(formGroup.controls).forEach((key: string): void => {
      const control: AbstractControl | null = formGroup.get(key);
      control?.markAsTouched();

      if (control?.errors) {
        const errors: ValidationErrors = control.errors;
        fieldErrors[key] = Object.keys(control.errors).map((errorKey: string): string =>
          getErrorMessage(key, errorKey, errors[errorKey])
        );
      }
    });
  }

  /**
   * Richtet automatische Fehlerbereinigung ein, indem es auf Änderungen in den Formularfeldern hört und die
   * Fehler entsprechend aktualisiert.
   *
   * @param formGroup Das Formular, für das die automatische Fehlerbereinigung eingerichtet werden soll.
   * @param updateFieldErrors Eine Funktion, die die Fehler für ein bestimmtes Feld aktualisiert, wenn sich
   * dessen Wert ändert.
   */
  setupAutoClearErrors(
    formGroup: FormGroup,
    updateFieldErrors: (formGroup: FormGroup, fieldName: string) => void
  ): void {
    Object.keys(formGroup.controls).forEach((key: string): void => {
      const control: AbstractControl | null = formGroup.get(key);
      control?.valueChanges.subscribe((): void => {
        updateFieldErrors(formGroup, key);
      });
    });
  }

  /**
   * Aktualisiert die Fehler für ein bestimmtes Feld basierend auf dessen aktuellen Validierungsstatus.
   *
   * @param formGroup Das Formular, das das zu aktualisierende Feld enthält.
   * @param fieldName Der Name des Feldes, dessen Fehler aktualisiert werden sollen.
   * @param fieldErrors Ein Objekt, das die Fehlernachrichten für jedes Feld speichert.
   * @param getErrorMessage Eine Funktion, die eine benutzerfreundliche Fehlermeldung basierend auf dem Feldnamen,
   * dem Fehlerkey und den Fehlerdetails zurückgibt.
   */
  updateFieldErrors(
    formGroup: FormGroup,
    fieldName: string,
    fieldErrors: Record<string, string[]>,
    getErrorMessage: (field: string, errorKey: string, errorValue: ValidationErrors) => string
  ): void {
    const control: AbstractControl | null = formGroup.get(fieldName);

    if (!control) return;

    if (control.errors && control.touched) {
      const errors: ValidationErrors = control.errors;
      fieldErrors[fieldName] = Object.keys(control.errors).map((errorKey: string): string =>
        getErrorMessage(fieldName, errorKey, errors[errorKey])
      );
    } else {
      delete fieldErrors[fieldName];
    }
  }

  /**
   * Gibt eine benutzerfreundliche Fehlermeldung zurück, basierend auf dem Feldnamen, dem Fehlerkey und den
   * Fehlerdetails.
   *
   * @param fieldName Der Name des Feldes, für das die Fehlermeldung generiert werden soll.
   * @param errorType Der Schlüssel des Fehlers.
   * @param errorValue Die Details des Fehlers, die für die Generierung der Fehlermeldung verwendet werden können.
   * @return Eine benutzerfreundliche Fehlermeldung, die auf dem Fehler basiert.
   */
  getErrorMessage(fieldName: string, errorType: string, errorValue: ValidationErrors): string {
    const messages: Record<string, string> = {
      required: `${this.getFieldDisplayName(fieldName)} ist erforderlich.`,
      invalidEmail: 'Bitte geben Sie eine gültige E-Mail-Adresse ein.',
      minlength: `${this.getFieldDisplayName(fieldName)} muss mindestens ${errorValue?.['requiredLength']} Zeichen lang sein.`,
      maxlength: `${this.getFieldDisplayName(fieldName)} darf nicht mehr als ${errorValue?.['requiredLength']} Zeichen lang sein.`,
      weakPassword: 'Passwort erfüllt nicht alle Anforderungen.',
      passwordMismatch: 'Passwörter stimmen nicht überein.',
    };

    return messages[errorType] || `${this.getFieldDisplayName(fieldName)} ist ungültig.`;
  }

  /**
   * Gibt eine benutzerfreundliche Anzeige des Feldnamens zurück, basierend auf dem tatsächlichen Feldnamen.
   *
   * @param fieldName Der tatsächliche Name des Feldes, für das die Anzeige generiert werden soll.
   * @return Eine benutzerfreundliche Anzeige des Feldnamens.
   */
  getFieldDisplayName(fieldName: string): string {
    const fieldNames: Record<string, string> = {
      name: 'Name',
      email: 'E-Mail-Adresse',
      password: 'Passwort',
      passwordConfirm: 'Passwortbestätigung',
      query: 'Sucheingabe',
      image: 'Profilbild',
      currentPassword: 'Aktuelles Passwort',
      newPassword: 'Neues Passwort',
      confirmPassword: 'Passwortbestätigung',
      title: 'Titel',
      background: 'Hintergrundbild',
      selectedBoardId: 'Ausgewähltes Board',
      description: 'Beschreibung',
      deadline: 'Deadline',
      labels: 'Labels',
      attachments: 'Anhänge',
      assigneeId: 'Zugewiesene Person',
    };

    return fieldNames[fieldName] || this.capitalize(fieldName);
  }

  /**
   * Kapitalisiert den ersten Buchstaben eines Strings und gibt den modifizierten String zurück.
   *
   * @param str Der String, dessen erster Buchstabe kapitalisiert werden soll.
   * @return Der modifizierte String mit einem kapitalisierten ersten Buchstaben.
   */
  capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  /**
   * Validator-Funktion, die überprüft, ob ein Passwort den Anforderungen entspricht.
   *
   * @return Eine Validator-Funktion, die ein AbstractControl überprüft und entweder null zurückgibt, wenn
   * das Passwort stark ist, oder ein Objekt mit einem Fehlerkey, wenn das Passwort schwach ist.
   */
  passwordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password: string = control.value || '';
      const strong: boolean = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).*$/.test(
        password
      );
      return strong ? null : { weakPassword: true };
    };
  }

  /**
   * Validator-Funktion, die überprüft, ob eine E-Mail-Adresse gültig ist.
   *
   * @return Eine Validator-Funktion, die ein AbstractControl überprüft und entweder null zurückgibt, wenn die
   * E-Mail-Adresse gültig ist, oder ein Objekt mit einem Fehlerkey, wenn die E-Mail-Adresse ungültig ist.
   */
  emailValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const email: string = control.value || '';
      const pattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

      return pattern.test(email) ? null : { invalidEmail: true };
    };
  }

  /**
   * Gibt eine Liste von Anforderungen zurück, die ein Passwort erfüllen muss, sowie Informationen darüber,
   * ob diese Anforderungen erfüllt sind oder nicht.
   *
   * @param password Das Passwort, für das die Anforderungen überprüft werden sollen.
   * @return Eine Liste von Anforderungen, die das Passwort erfüllen muss, sowie Informationen darüber, ob
   * diese Anforderungen erfüllt sind oder nicht.
   */
  getPasswordRequirements(password: string): { text: string; met: boolean }[] {
    return [
      { text: 'Mindestens 8 Zeichen', met: password.length >= 8 },
      { text: 'Groß- und Kleinbuchstaben', met: /(?=.*[A-Z])(?=.*[a-z])/.test(password) },
      { text: 'Mindestens eine Ziffer (0-9)', met: /\d/.test(password) },
      { text: 'Mindestens ein Sonderzeichen (!@#$%^&*)', met: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password) },
    ];
  }
}
