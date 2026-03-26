package in.cg.main.policy.repository;

import in.cg.main.policy.entity.PurchasedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PurchasedPolicyRepository extends JpaRepository<PurchasedPolicy, Long> {
    Optional<PurchasedPolicy> findByCustomerUsernameAndPolicyId(String customerUsername, Long policyId);
    List<PurchasedPolicy> findByCustomerUsername(String customerUsername);
}
