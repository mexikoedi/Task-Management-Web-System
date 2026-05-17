package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.Task;
import io.github.mexikoedi.tmws.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  List<Task> findByTitleContainingIgnoreCase(String title);

  List<Task> findAllByAssigneesContains(User user);
}
