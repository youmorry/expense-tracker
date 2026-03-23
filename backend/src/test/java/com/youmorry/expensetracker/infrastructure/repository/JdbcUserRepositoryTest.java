package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.model.user.CurrencyCode;
import com.youmorry.expensetracker.domain.model.user.User;
import com.youmorry.expensetracker.domain.model.user.UserId;
import com.youmorry.expensetracker.domain.model.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class JdbcUserRepositoryTest extends AbstractRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Test
  void save_newUser_assignsIdAndCreatedAt() {
    User newUser = User.createNew("google-123", "test@example.com", "Test User", CurrencyCode.JPY);

    User saved = userRepository.save(newUser);

    assertThat(saved.id()).isNotNull();
    assertThat(saved.googleId()).isEqualTo("google-123");
    assertThat(saved.email()).isEqualTo("test@example.com");
    assertThat(saved.displayName()).isEqualTo("Test User");
    assertThat(saved.currencyCode()).isEqualTo(CurrencyCode.JPY);

    User fetched = userRepository.findById(saved.id()).orElseThrow();
    assertThat(fetched.createdAt()).isNotNull();
  }

  @Test
  void findById_existingId_returnsUser() {
    User saved =
        userRepository.save(
            User.createNew("google-456", "user@example.com", "Found User", CurrencyCode.USD));

    Optional<User> found = userRepository.findById(saved.id());

    assertThat(found).isPresent();
    assertThat(found.get().googleId()).isEqualTo("google-456");
    assertThat(found.get().email()).isEqualTo("user@example.com");
  }

  @Test
  void findById_nonExistingId_returnsEmpty() {
    Optional<User> found = userRepository.findById(new UserId(9999));

    assertThat(found).isEmpty();
  }

  @Test
  void findByGoogleId_existingGoogleId_returnsUser() {
    userRepository.save(
        User.createNew("google-789", "google@example.com", "Google User", CurrencyCode.EUR));

    Optional<User> found = userRepository.findByGoogleId("google-789");

    assertThat(found).isPresent();
    assertThat(found.get().email()).isEqualTo("google@example.com");
  }

  @Test
  void findByGoogleId_nonExistingGoogleId_returnsEmpty() {
    Optional<User> found = userRepository.findByGoogleId("non-existing-google-id");

    assertThat(found).isEmpty();
  }

  @Test
  void deleteById_existingUser_removesUser() {
    User saved =
        userRepository.save(
            User.createNew("google-delete", "delete@example.com", "Delete User", CurrencyCode.GBP));

    userRepository.deleteById(saved.id());

    assertThat(userRepository.findById(saved.id())).isEmpty();
  }
}
