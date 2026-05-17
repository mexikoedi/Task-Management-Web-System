package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.Board;
import io.github.mexikoedi.tmws.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
  List<Board> findAllByMembersContains(User user);
}

