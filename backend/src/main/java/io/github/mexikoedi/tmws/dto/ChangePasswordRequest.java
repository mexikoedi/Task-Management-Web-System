package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
  @NotBlank(message = "Aktuelles Passwort ist erforderlich")
  private String currentPassword;

  @NotBlank(message = "Neues Passwort ist erforderlich")
  private String newPassword;

  @NotBlank(message = "Passwortbestätigung ist erforderlich")
  private String newPasswordConfirm;

  public ChangePasswordRequest() {}

  public ChangePasswordRequest(String currentPassword, String newPassword, String newPasswordConfirm) {
    this.currentPassword = currentPassword;
    this.newPassword = newPassword;
    this.newPasswordConfirm = newPasswordConfirm;
  }

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getNewPasswordConfirm() {
    return newPasswordConfirm;
  }

  public void setNewPasswordConfirm(String newPasswordConfirm) {
    this.newPasswordConfirm = newPasswordConfirm;
  }
}

