package io.github.mexikoedi.tmws.repository;

import io.github.mexikoedi.tmws.model.VerificationToken;
import io.github.mexikoedi.tmws.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserAndUsedFalse(User user);
}

