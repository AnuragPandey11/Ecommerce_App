package com.ecom.repository;


import com.ecom.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(
            Image.ImageEntityType entityType, 
            Long entityId
    );

    Optional<Image> findByEntityTypeAndEntityIdAndIsPrimaryTrue(
            Image.ImageEntityType entityType, 
            Long entityId
    );

    @Modifying
    @Query("UPDATE Image i SET i.isPrimary = false WHERE i.entityType = :entityType AND i.entityId = :entityId")
    void clearPrimaryImages(@Param("entityType") Image.ImageEntityType entityType, @Param("entityId") Long entityId);

    @Modifying
    @Query("DELETE FROM Image i WHERE i.entityType = :entityType AND i.entityId = :entityId")
    void deleteByEntityTypeAndEntityId(@Param("entityType") Image.ImageEntityType entityType, @Param("entityId") Long entityId);

    long countByEntityTypeAndEntityId(Image.ImageEntityType entityType, Long entityId);
}
