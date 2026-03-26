package in.cg.main.policy.repository;
import in.cg.main.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PolicyRepository extends JpaRepository<Policy, Long> {
}
