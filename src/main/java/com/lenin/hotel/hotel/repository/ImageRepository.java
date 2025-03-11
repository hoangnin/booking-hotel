package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.hotel.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByReferenceIdAndReferenceTableAndType(Integer referenceId, String referenceTable, ImageType type);
}
