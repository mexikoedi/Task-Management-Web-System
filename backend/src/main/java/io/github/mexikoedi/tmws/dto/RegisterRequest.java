package io.github.mexikoedi.tmws.dto;

import io.github.mexikoedi.tmws.util.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "Name ist erforderlich")
  @Size(min = 2, max = 30, message = "Name muss zwischen 2 und 30 Zeichen lang sein")
  private String name;

  @NotBlank(message = "E-Mail ist erforderlich")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein")
  @ValidEmail
  private String email;

  @NotBlank(message = "Passwort ist erforderlich")
  @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
  private String password;

  @NotBlank(message = "Passwortbestätigung ist erforderlich")
  private String passwordConfirm;
}
