package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.NotBlank;
import io.github.mexikoedi.tmws.util.ValidEmail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    @NotBlank(message = "E-Mail ist erforderlich")
    @ValidEmail
    private String email;
}

