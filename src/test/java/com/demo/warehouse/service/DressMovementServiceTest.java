package com.demo.warehouse.service;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.mapper.DressMovementDtos;
import com.demo.warehouse.repository.DressMovementRepository;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.testutils.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DressMovementServiceTest {

    @Mock
    private DressMovementRepository dressMovementRepository;

    @Mock
    private DressRepository dressRepository;

    @InjectMocks
    private DressMovementService dressMovementService;

    private Dress dress;
    private DressMovement dressMovement;

    @BeforeEach
    void setUp() {
        dress = TestFactory.createDefaultDress();
        dressMovement = TestFactory.createDefaultDressMovement(dress);
    }

    @Test
    void page_ShouldReturnPageOfDressMovements() {
        Page<DressMovement> page = new PageImpl<>(java.util.Collections.singletonList(dressMovement), PageRequest.of(0, 10), 1);
        when(dressMovementRepository.getBySpec(any(), any(Pageable.class))).thenReturn(page);
        Specification<DressMovement> dummySpec = (root, query, criteriaBuilder) -> null;
        Page<DressMovement> result = dressMovementService.page(dummySpec, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(dressMovementRepository, times(1)).getBySpec(any(), any(Pageable.class));
    }

    @Test
    void detail_ShouldReturnDressMovement() {
        when(dressMovementRepository.getByIdOrThrow(1L)).thenReturn(dressMovement);

        DressMovement result = dressMovementService.detail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(dressMovementRepository, times(1)).getByIdOrThrow(1L);
    }

    @Test
    void create_ShouldCreateDressMovementAndUpdateStock() {
        DressMovementDtos.DressMovementCreateRequest request = new DressMovementDtos.DressMovementCreateRequest(
                1L, 5, Instant.now()
        );
        when(dressRepository.findById(1L)).thenReturn(Optional.of(dress));
        when(dressMovementRepository.save(any(DressMovement.class))).thenReturn(dressMovement);

        DressMovement result = dressMovementService.create(request);

        assertNotNull(result);
        assertEquals(15, dress.getStock()); // 10 + 5
        verify(dressRepository, times(1)).findById(1L);
        verify(dressMovementRepository, times(1)).save(any(DressMovement.class));
    }

    @Test
    void create_ShouldThrowWhenDressNotFound() {
        DressMovementDtos.DressMovementCreateRequest request = new DressMovementDtos.DressMovementCreateRequest(
                1L, 5, Instant.now()
        );
        when(dressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> dressMovementService.create(request));
        verify(dressRepository, times(1)).findById(1L);
        verify(dressMovementRepository, never()).save(any(DressMovement.class));
    }

    @Test
    void update_ShouldUpdateDressMovementAndAdjustStock() {
        DressMovementDtos.DressMovementUpdateRequest request = new DressMovementDtos.DressMovementUpdateRequest(
                1L, 1L, 10, Instant.now()
        );
        when(dressMovementRepository.getByIdOrThrow(1L)).thenReturn(dressMovement);
        when(dressRepository.findById(1L)).thenReturn(Optional.of(dress));
        when(dressMovementRepository.save(any(DressMovement.class))).thenReturn(dressMovement);

        DressMovement result = dressMovementService.update(request);

        assertNotNull(result);
        assertEquals(15, dress.getStock()); // 10 - 5 + 10
        verify(dressMovementRepository, times(1)).getByIdOrThrow(1L);
        verify(dressRepository, times(1)).findById(1L);
        verify(dressMovementRepository, times(1)).save(any(DressMovement.class));
    }

    @Test
    void delete_ShouldDeleteDressMovementAndRestoreStock() {
        when(dressMovementRepository.getByIdOrThrow(1L)).thenReturn(dressMovement);
        when(dressRepository.save(any(Dress.class))).thenReturn(dress);
        doNothing().when(dressMovementRepository).deleteById(1L);

        dressMovementService.delete(1L);

        assertEquals(5, dress.getStock()); // 10 - 5
        verify(dressMovementRepository, times(1)).getByIdOrThrow(1L);
        verify(dressRepository, times(1)).save(any(Dress.class));
        verify(dressMovementRepository, times(1)).deleteById(1L);
    }
}
