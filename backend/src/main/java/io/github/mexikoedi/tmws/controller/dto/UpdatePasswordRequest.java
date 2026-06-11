/** Diese Klasse repräsentiert die Anforderung zum Aktualisieren des Passworts eines Benutzers. */
package io.github.mexikoedi.tmws.controller.dto;

import io.github.mexikoedi.tmws.controller.validation.NewPasswordDifferent;
import io.github.mexikoedi.tmws.service.validation.PasswordConfirmation;
import io.github.mexikoedi.tmws.service.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@PasswordMatches
@NewPasswordDifferent
public class UpdatePasswordRequest implements PasswordConfirmation {
  @NotBlank(message = "Aktuelles Passwort ist erforderlich.")
  private String currentPassword;

  @NotBlank(message = "Neues Passwort ist erforderlich.")
  @Size(min = 8, max = 50, message = "Passwort muss zwischen 8 und 50 Zeichen lang sein.")
  @Pattern(
      regexp =
          "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
      message = "Passwort muss Groß-/Kleinbuchstaben, Zahl und Sonderzeichen enthalten.")
  private String newPassword;

  @NotBlank(message = "Passwortbestätigung ist erforderlich.")
  private String newPasswordConfirm;

  @Override
  public String getPassword() {
    return newPassword;
  }

  @Override
  public String getPasswordConfirm() {
    return newPasswordConfirm;
  }

  @Override
  public String getPasswordConfirmFieldName() {
    return "newPasswordConfirm";
  }
}
