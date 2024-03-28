package wander.wise.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wander.wise.application.model.report.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
