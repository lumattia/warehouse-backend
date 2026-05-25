package com.demo.warehouse.service;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.mapper.DressMapper;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.repository.DressRepository;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DressServiceTest {

    @Mock
    private DressMapper dressMapper;

    @Mock
    private DressRepository dressRepository;

    @InjectMocks
    private DressService dressService;

    private Dress dress;
    private DressDtos.DressResponse dressResponse;

    @BeforeEach
    void setUp() {
        dress = new Dress();
        dress.setId(1L);
        dress.setTitle("Test Dress");
        dress.setSku("SKU001");
        dress.setSize("M");
        dress.setColor("#FF0000");
        dress.setStock(0);
        dress.setPrice(new BigDecimal("100.00"));

        dressResponse = DressDtos.DressResponse.builder()
                .id(1L)
                .title("Test Dress")
                .sku("SKU001")
                .size("M")
                .color("#FF0000")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();
    }

    @Test
    void page_ShouldReturnPageOfDresses() {
        Page<Dress> page = new PageImpl<>(Collections.singletonList(dress), PageRequest.of(0, 10), 1);
        when(dressRepository.getBySpec(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(dressMapper.toResponse(any(Dress.class))).thenReturn(dressResponse);
        Specification<Dress> dummySpec = (root, query, criteriaBuilder) -> null;

        Page<DressDtos.DressResponse> result = dressService.page(dummySpec, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(dressRepository, times(1)).getBySpec(any(Specification.class), any(Pageable.class));
        verify(dressMapper, times(1)).toResponse(any(Dress.class));
    }

    @Test
    void list_ShouldReturnListOfDresses() {
        List<IdName<Long>> list = Collections.singletonList(new IdNameImpl(1L, "Test Dress"));
        when(dressRepository.getAllAsIdName()).thenReturn(list);

        List<IdName<Long>> result = dressService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(dressRepository, times(1)).getAllAsIdName();
    }

    @Test
    void detail_ShouldReturnDress() {
        when(dressRepository.findById(1L)).thenReturn(Optional.of(dress));
        when(dressMapper.toResponse(dress)).thenReturn(dressResponse);

        DressDtos.DressResponse result = dressService.detail(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(dressRepository, times(1)).findById(1L);
        verify(dressMapper, times(1)).toResponse(dress);
    }

    @Test
    void detail_ShouldThrowWhenDressNotFound() {
        when(dressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> dressService.detail(1L));
        verify(dressRepository, times(1)).findById(1L);
        verify(dressMapper, never()).toResponse(any(Dress.class));
    }

    @Test
    void create_ShouldCreateDress() {
        DressDtos.DressCreateRequest request = new DressDtos.DressCreateRequest(
                "New Dress", "SKU002", "L", "#00FF00", new BigDecimal("150.00")
        );
        when(dressRepository.save(any(Dress.class))).thenReturn(dress);
        when(dressMapper.toResponse(dress)).thenReturn(dressResponse);

        DressDtos.DressResponse result = dressService.create(request);

        assertNotNull(result);
        assertEquals(0, dress.getStock());
        verify(dressRepository, times(1)).save(any(Dress.class));
        verify(dressMapper, times(1)).toResponse(dress);
    }

    @Test
    void update_ShouldUpdateDress() {
        DressDtos.DressUpdateRequest request = new DressDtos.DressUpdateRequest(
                1L, "Updated Dress", "SKU003", "XL", "#0000FF", new BigDecimal("200.00")
        );
        when(dressRepository.getByIdOrThrow(1L)).thenReturn(dress);
        when(dressRepository.save(any(Dress.class))).thenReturn(dress);
        when(dressMapper.toResponse(dress)).thenReturn(dressResponse);

        DressDtos.DressResponse result = dressService.update(request);

        assertNotNull(result);
        assertEquals("Updated Dress", dress.getTitle());
        verify(dressRepository, times(1)).getByIdOrThrow(1L);
        verify(dressRepository, times(1)).save(any(Dress.class));
        verify(dressMapper, times(1)).toResponse(dress);
    }

    @Test
    void delete_ShouldDeleteDress() {
        doNothing().when(dressRepository).deleteById(1L);

        dressService.delete(1L);

        verify(dressRepository, times(1)).deleteById(1L);
    }
}
