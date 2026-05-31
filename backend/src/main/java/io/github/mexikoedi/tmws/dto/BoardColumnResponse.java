/** Diese Klasse repräsentiert die Antwort für eine Spalte eines Boards. */
package io.github.mexikoedi.tmws.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardColumnResponse {
  private long id;
  private String title;
  private int position;
  private long boardId;
  private List<TaskResponse> tasks;
}
