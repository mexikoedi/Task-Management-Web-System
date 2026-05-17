package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
  List<Board> findAllByMembersContains(User user);
}
