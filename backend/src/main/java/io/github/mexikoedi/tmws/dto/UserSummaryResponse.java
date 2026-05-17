package io.github.mexikoedi.tmws.dto;

public class UserSummaryResponse {
  private Long id;
  private String name;
  private String email;
  private boolean emailChanged;
  private String image;

  public UserSummaryResponse(Long id, String name, String email, boolean emailChanged, String image) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.emailChanged = emailChanged;
    this.image = image;
  }

  // Getter / Setter
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public boolean isEmailChanged() { return emailChanged; }
  public void setEmailChanged(boolean emailChanged) { this.emailChanged = emailChanged; }
  public String getImage() { return image; }
  public void setImage(String image) { this.image = image; }
}
