/** Diese Klasse ist ein globaler Exception-Handler für die gesamte Anwendung. */
package io.github.mexikoedi.tmws.service.exception;

import io.github.mexikoedi.tmws.service.dto.ErrorResponse;
import io.github.mexikoedi.tmws.security.exception.InvalidTokenException;
import io.github.mexikoedi.tmws.security.exception.JwtExpiredException;
import io.github.mexikoedi.tmws.security.exception.JwtInvalidException;
import io.github.mexikoedi.tmws.security.exception.JwtMalformedException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * Diese Methode behandelt die UserDeactivatedException, die ausgelöst wird, wenn ein
   * Benutzerkonto deaktiviert ist.
   *
   * @param ex Die ausgelöste UserDeactivatedException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 403 (Forbidden).
   */
  @ExceptionHandler(UserDeactivatedException.class)
  public ResponseEntity<ErrorResponse> handleUserDeactivated(
      UserDeactivatedException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(build(HttpStatus.FORBIDDEN, "USER_DEACTIVATED", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die EmailAlreadyExistsException, die ausgelöst wird, wenn versucht
   * wird, eine E-Mail-Adresse zu registrieren, die bereits existiert.
   *
   * @param ex Die ausgelöste EmailAlreadyExistsException, die Informationen über den Fehler
   *     enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
      EmailAlreadyExistsException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT, "EMAIL_EXISTS", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die ResourceNotFoundException, die ausgelöst wird, wenn eine
   * angeforderte Ressource nicht gefunden wird.
   *
   * @param ex Die ausgelöste ResourceNotFoundException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 404 (Not Found).
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die InvalidPasswordException, die ausgelöst wird, wenn ein ungültiges
   * Passwort eingegeben wird.
   *
   * @param ex Die ausgelöste InvalidPasswordException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 400 (Bad Request).
   */
  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPassword(
      InvalidPasswordException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(build(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die InvalidTokenException, die ausgelöst wird, wenn ein ungültiges
   * Token verwendet wird,
   *
   * @param ex Die ausgelöste InvalidTokenException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 401 (Unauthorized).
   */
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidToken(
      InvalidTokenException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(build(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die JwtExpiredException, die ausgelöst wird, wenn ein JWT-Token
   * abgelaufen ist.
   *
   * @param ex Die ausgelöste JwtExpiredException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 401 (Unauthorized).
   */
  @ExceptionHandler(JwtExpiredException.class)
  public ResponseEntity<ErrorResponse> handleExpiredJwt(
      JwtExpiredException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(build(HttpStatus.UNAUTHORIZED, "EXPIRED_JWT", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die JwtInvalidException, die ausgelöst wird, wenn ein ungültiges
   * JWT-Token verwendet wird.
   *
   * @param ex Die ausgelöste JwtInvalidException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 401 (Unauthorized).
   */
  @ExceptionHandler(JwtInvalidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidJwt(
      JwtInvalidException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(build(HttpStatus.UNAUTHORIZED, "INVALID_JWT", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die JwtMalformedException, die ausgelöst wird, wenn ein JWT-Token nicht
   * korrekt formatiert ist.
   *
   * @param ex Die ausgelöste JwtMalformedException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 401 (Unauthorized).
   */
  @ExceptionHandler(JwtMalformedException.class)
  public ResponseEntity<ErrorResponse> handleMalformedJwt(
      JwtMalformedException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(build(HttpStatus.UNAUTHORIZED, "MALFORMED_JWT", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die PasswordResetTokenAlreadyUsedException, die ausgelöst wird, wenn
   * ein Passwort-Reset-Token bereits verwendet wurde.
   *
   * @param ex Die ausgelöste PasswordResetTokenAlreadyUsedException, die Informationen über den
   *     Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(PasswordResetTokenAlreadyUsedException.class)
  public ResponseEntity<ErrorResponse> handlePasswordResetTokenAlreadyUsed(
      PasswordResetTokenAlreadyUsedException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            build(
                HttpStatus.CONFLICT,
                "PASSWORD_RESET_TOKEN_ALREADY_USED",
                ex.getMessage(),
                request));
  }

  /**
   * Diese Methode behandelt die PasswordResetTokenExpiredException, die ausgelöst wird, wenn ein
   * Passwort-Reset-Token abgelaufen ist.
   *
   * @param ex Die ausgelöste PasswordResetTokenExpiredException, die Informationen über den Fehler
   *     enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 410 (Gone).
   */
  @ExceptionHandler(PasswordResetTokenExpiredException.class)
  public ResponseEntity<ErrorResponse> handlePasswordResetTokenExpired(
      PasswordResetTokenExpiredException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.GONE)
        .body(build(HttpStatus.GONE, "PASSWORD_RESET_TOKEN_EXPIRED", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die VerificationTokenAlreadyUsedException, die ausgelöst wird, wenn ein
   * Verifizierungs-Token bereits verwendet wurde.
   *
   * @param ex Die ausgelöste VerificationTokenAlreadyUsedException, die Informationen über den
   *     Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(VerificationTokenAlreadyUsedException.class)
  public ResponseEntity<ErrorResponse> handleVerificationTokenAlreadyUsed(
      VerificationTokenAlreadyUsedException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            build(
                HttpStatus.CONFLICT, "VERIFICATION_TOKEN_ALREADY_USED", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die VerificationTokenExpiredException, die ausgelöst wird, wenn ein
   * Verifizierungs-Token abgelaufen ist.
   *
   * @param ex Die ausgelöste VerificationTokenExpiredException, die Informationen über den Fehler
   *     enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 410 (Gone).
   */
  @ExceptionHandler(VerificationTokenExpiredException.class)
  public ResponseEntity<ErrorResponse> handleVerificationTokenExpired(
      VerificationTokenExpiredException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.GONE)
        .body(build(HttpStatus.GONE, "VERIFICATION_TOKEN_EXPIRED", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die UserAlreadyMemberException, die ausgelöst wird, wenn versucht wird,
   * einen Benutzer einem Projektboard hinzuzufügen, der bereits Mitglied ist.
   *
   * @param ex Die ausgelöste UserAlreadyMemberException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(UserAlreadyMemberException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyMember(
      UserAlreadyMemberException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT, "USER_ALREADY_MEMBER", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die UserInviteNotPossibleException, die ausgelöst wird, wenn versucht
   * wird, einen Benutzer einzuladen, der nicht eingeladen werden kann, da er der Besitzer des
   * Projektboards.
   *
   * @param ex Die ausgelöste UserInviteNotPossibleException, die Informationen über den Fehler
   *     enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(UserInviteNotPossibleException.class)
  public ResponseEntity<ErrorResponse> handleUserInviteNotPossible(
      UserInviteNotPossibleException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT, "USER_INVITE_NOT_POSSIBLE", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die ColumnAlreadyExistsException, die ausgelöst wird, wenn versucht
   * wird, eine Spalte hinzuzufügen, die bereits existiert.
   *
   * @param ex Die ausgelöste ColumnAlreadyExistsException, die Informationen über den Fehler
   *     enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(ColumnAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleColumnAlreadyExists(
      ColumnAlreadyExistsException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT, "COLUMN_ALREADY_EXISTS", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die TaskAlreadyExistsException, die ausgelöst wird, wenn versucht wird,
   * eine Aufgabe hinzuzufügen, die bereits existiert.
   *
   * @param ex Die ausgelöste TaskAlreadyExistsException, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 409 (Conflict).
   */
  @ExceptionHandler(TaskAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleTaskAlreadyExists(
      TaskAlreadyExistsException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(build(HttpStatus.CONFLICT, "TASK_ALREADY_EXISTS", ex.getMessage(), request));
  }

  /**
   * Diese Methode behandelt die MethodArgumentNotValidException, die ausgelöst wird, wenn die
   * Validierung von Methodenargumenten fehlschlägt.
   *
   * @param ex Die ausgelöste MethodArgumentNotValidException, die Informationen über den Fehler
   *     enthält, einschließlich der Validierungsfehler.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, einschließlich der Validierungsfehler, und einem HTTP-Statuscode von 400 (Bad
   *     Request).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request));
  }

  /**
   * Diese Methode behandelt alle anderen Ausnahmen, die nicht spezifisch von den vorherigen
   * Methoden behandelt werden.
   *
   * @param ex Die ausgelöste Ausnahme, die Informationen über den Fehler enthält.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Eine ResponseEntity mit einem ErrorResponse-Objekt, das die Details des Fehlers
   *     enthält, und einem HTTP-Statuscode von 500 (Internal Server Error).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), request));
  }

  /**
   * Diese Hilfsmethode erstellt ein ErrorResponse-Objekt mit den angegebenen Parametern,
   * einschließlich des aktuellen Zeitstempels,
   *
   * @param status Der HTTP-Statuscode, der den Fehler beschreibt.
   * @param error Ein kurzer Fehlercode oder eine Fehlertypbeschreibung, die den Fehler
   *     kategorisiert.
   * @param message Eine detaillierte Fehlermeldung, die die Ursache des Fehlers beschreibt.
   * @param request Das HttpServletRequest-Objekt, das Informationen über die eingehende
   *     HTTP-Anfrage enthält, wie z.B. die URI, die Methode und die Header.
   * @return Ein ErrorResponse-Objekt, das die Details des Fehlers enthält, einschließlich des
   *     Zeitstempels, des Statuscodes,
   */
  private ErrorResponse build(
      HttpStatus status, String error, String message, HttpServletRequest request) {
    return new ErrorResponse(
        LocalDateTime.now(), status.value(), error, message, request.getRequestURI());
  }
}
