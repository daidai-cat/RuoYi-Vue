package com.ymsl.a1.nims.repository;

import com.ymsl.a1.nims.domain.entity.FlashcardItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardItemRepository extends JpaRepository<FlashcardItem, Long> {

    List<FlashcardItem> findByBookIdOrderByQuadrantTypeAscIdAsc(Long bookId);
}
