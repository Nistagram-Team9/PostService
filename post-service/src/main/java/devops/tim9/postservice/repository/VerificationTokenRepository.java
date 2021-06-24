package devops.tim9.postservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import devops.tim9.postservice.security.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

	VerificationToken findByToken(String token);
}