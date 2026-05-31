/**
 * Diese Klasse repräsentiert die Antwort für eine Spalte eines Boards.
 */
package io.github.mexikoedi.tmws.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class BoardColumnResponse {
  private long id;
  private String title;
  private int position;
  private long boardId;
  private List<TaskResponse> tasks;
}
