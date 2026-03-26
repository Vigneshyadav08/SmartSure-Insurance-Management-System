package in.cg.main.auth.repository;
import in.cg.main.auth.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<UserApp, Long> {
    Optional<UserApp> findByUsername(String username);
}
