package in.cg.main.admin.repository;
import in.cg.main.admin.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReportRepository extends JpaRepository<Report, Long> {
}
