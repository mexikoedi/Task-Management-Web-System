/**
 * Diese Klasse repräsentiert die Antwort, die zurückgegeben wird, wenn Informationen über ein Board
 * angefordert werden.
 */
package io.github.mexikoedi.tmws.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardResponse {
  private Long id;
  private UserResponse owner;
  private String title;
  private String background;
  private List<BoardColumnResponse> columns;
  private List<UserResponse> members;
}
