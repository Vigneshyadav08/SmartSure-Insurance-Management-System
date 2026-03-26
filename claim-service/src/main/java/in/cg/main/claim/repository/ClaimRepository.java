package in.cg.main.claim.repository;
import in.cg.main.claim.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByCustomerUsername(String customerUsername);
}
