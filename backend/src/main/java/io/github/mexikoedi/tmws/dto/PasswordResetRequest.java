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
public class PasswordResetRequest {
  @NotBlank(message = "E-Mail ist erforderlich")
  @Size(max = 30, message = "E-Mail darf nicht länger als 30 Zeichen lang sein")
  @ValidEmail
  private String email;
}
