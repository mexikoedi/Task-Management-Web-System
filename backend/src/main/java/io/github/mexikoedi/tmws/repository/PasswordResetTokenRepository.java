package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.PasswordResetToken;
import io.github.mexikoedi.tmws.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);

  Optional<PasswordResetToken> findByUserAndUsedFalse(User user);
}
