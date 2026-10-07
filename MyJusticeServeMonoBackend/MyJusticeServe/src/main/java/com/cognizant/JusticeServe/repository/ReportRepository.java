package com.cognizant.JusticeServe.repository;

import com.cognizant.JusticeServe.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Repository
public interface ReportRepository  extends JpaRepository<Report,Long> {
    List<Report> findByScope(Report.ReportScope scope);

}
