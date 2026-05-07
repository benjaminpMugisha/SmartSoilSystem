package com.rab.smartsoil.repository;

import com.rab.smartsoil.model.Advisory;
import com.rab.smartsoil.model.DiseaseAlert;
import com.rab.smartsoil.model.SoilSample;
import com.rab.smartsoil.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer — Spring Data JPA Repositories
 * One repository per aggregate root (SRP applied at data layer)
 */

@Repository
public interface SoilSampleRepository extends JpaRepository<SoilSample, String> {
    List<SoilSample> findByPlotIdOrderBySampleDateDesc(String plotId);
    List<SoilSample> findByDistrictOrderBySampleDateDesc(String district);
}

@Repository
public interface AdvisoryRepository extends JpaRepository<Advisory, String> {
    List<Advisory> findByPlotIdOrderByGeneratedDateDesc(String plotId);
    List<Advisory> findByDistrictOrderByGeneratedDateDesc(String district);
}

@Repository
public interface AlertRepository extends JpaRepository<DiseaseAlert, String> {
    List<DiseaseAlert> findByDistrictAndIsTreatedFalse(String district);
    List<DiseaseAlert> findByFarmerEmailOrderByDetectedDateDesc(String email);
    List<DiseaseAlert> findBySeverityAndIsTreatedFalse(DiseaseAlert.Severity severity);
}

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
