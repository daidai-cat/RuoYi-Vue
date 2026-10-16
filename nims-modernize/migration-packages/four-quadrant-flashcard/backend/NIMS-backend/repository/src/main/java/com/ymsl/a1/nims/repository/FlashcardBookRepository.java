package com.ymsl.a1.nims.repository;

import com.ymsl.a1.nims.domain.entity.FlashcardBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardBookRepository extends JpaRepository<FlashcardBook, Long> {

    List<FlashcardBook> findAllByOrderByCreateTimeDesc();
}
