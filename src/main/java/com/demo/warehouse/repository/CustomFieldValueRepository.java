package com.demo.warehouse.repository;

import com.demo.warehouse.domain.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    Optional<CustomFieldValue> findByDefinitionIdAndTargetId(Long definitionId, Long targetId);
    List<CustomFieldValue> findByTargetId(Long targetId);
    void deleteByTargetId(Long targetId);
}
