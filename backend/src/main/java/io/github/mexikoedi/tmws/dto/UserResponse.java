/**
 * Diese Klasse repräsentiert die Antwort, die zurückgegeben wird, wenn Informationen über einen Benutzer angefordert werden.
 */
package io.github.mexikoedi.tmws.dto;

import io.github.mexikoedi.tmws.model.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private String image;
  private boolean emailVerified;
  private boolean emailChanged;

  public UserResponse(User user) {
    this.id = user.getId();
    this.name = user.getName();
    this.email = user.getEmail();
    this.image = user.getImage();
    this.emailVerified = user.isEmailVerified();
    this.emailChanged = user.isEmailChanged();
  }
}
