package com.duoc.seguridadcalidad.api.repository;

import com.duoc.seguridadcalidad.api.model.Banner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByActivoTrue();
}
