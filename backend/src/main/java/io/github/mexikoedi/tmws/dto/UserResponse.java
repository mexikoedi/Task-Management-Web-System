package io.github.mexikoedi.tmws.dto;

public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private boolean emailVerified;
  private boolean emailChanged;
  private String image;

  public UserResponse() {}

  public UserResponse(
      Long id,
      String name,
      String email,
      boolean emailVerified,
      boolean emailChanged,
      String image) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.emailVerified = emailVerified;
    this.emailChanged = emailChanged;
    this.image = image;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public boolean isEmailChanged() {
    return emailChanged;
  }

  public void setEmailChanged(boolean emailChanged) {
    this.emailChanged = emailChanged;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }
}
