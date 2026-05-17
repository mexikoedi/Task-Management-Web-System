package io.github.mexikoedi.tmws.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {
  @NotBlank(message = "Name ist erforderlich")
  private String name;
  @Email
  private String newEmail; // optional neue Email
  private String image; // optionales Profilbild

  // Optional Passwortfelder
  private String currentPassword;
  private String newPassword;
  private String newPasswordConfirm;

  // Getter & Setter für alle Felder
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getNewEmail() {return newEmail;}
  public void setNewEmail(String newEmail) {this.newEmail = newEmail;}

  public String getImage() { return image; }
  public void setImage(String image) { this.image = image; }

  public String getCurrentPassword() { return currentPassword; }
  public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

  public String getNewPassword() { return newPassword; }
  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

  public String getNewPasswordConfirm() { return newPasswordConfirm; }
  public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }

  public UpdateProfileRequest() {}

  public UpdateProfileRequest(String name) {
    this.name = name;
  }
}

