package com.example.homesee.repository;

import com.example.homesee.entity.HouseTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseTourRepository extends JpaRepository<HouseTour, Long> {
    
    // 根据标题查找房屋导览
    List<HouseTour> findByTitleContaining(String title);
    
    // 根据描述查找房屋导览
    List<HouseTour> findByDescriptionContaining(String description);
}
