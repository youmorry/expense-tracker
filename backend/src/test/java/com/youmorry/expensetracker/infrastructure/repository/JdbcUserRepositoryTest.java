package com.youmorry.expensetracker.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.youmorry.expensetracker.domain.user.User;
import com.youmorry.expensetracker.domain.user.UserId;
import com.youmorry.expensetracker.domain.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class JdbcUserRepositoryTest extends AbstractRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Test
  void save_newUser_assignsId() {
    User newUser = User.createNew("google-123", "test@example.com", "Test User");

    User saved = userRepository.save(newUser);

    assertThat(saved.id()).isNotNull();
  }

  @Test
  void findById_existingId_returnsUser() {
    User saved =
        userRepository.save(User.createNew("google-456", "user@example.com", "Found User"));

    Optional<User> found = userRepository.findById(saved.id());

    assertThat(found).hasValueSatisfying(user -> assertThat(user.id()).isEqualTo(saved.id()));
  }

  @Test
  void findById_nonExistingId_returnsEmpty() {
    Optional<User> found = userRepository.findById(new UserId(9999));

    assertThat(found).isEmpty();
  }

  @Test
  void findByGoogleId_existingGoogleId_returnsUser() {
    userRepository.save(User.createNew("google-789", "google@example.com", "Google User"));

    Optional<User> found = userRepository.findByGoogleId("google-789");

    assertThat(found)
        .hasValueSatisfying(user -> assertThat(user.googleId()).isEqualTo("google-789"));
  }

  @Test
  void findByGoogleId_nonExistingGoogleId_returnsEmpty() {
    Optional<User> found = userRepository.findByGoogleId("non-existing-google-id");

    assertThat(found).isEmpty();
  }

  @Test
  void deleteById_existingUser_removesUser() {
    User saved =
        userRepository.save(User.createNew("google-delete", "delete@example.com", "Delete User"));

    userRepository.deleteById(saved.id());

    assertThat(userRepository.findById(saved.id())).isEmpty();
  }
}
