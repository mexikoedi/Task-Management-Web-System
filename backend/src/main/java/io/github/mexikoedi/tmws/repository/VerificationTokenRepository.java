package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.model.VerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
  Optional<VerificationToken> findByToken(String token);

  Optional<VerificationToken> findByUserAndUsedFalse(User user);

  void deleteAllByUser(User user);
}
