package com.demo.warehouse.controller;

import com.demo.warehouse.domain.Dress;
import com.demo.warehouse.domain.DressMovement;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.DressMovementDtos;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.DressMovementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DressMovementController.class)
class DressMovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private DressMovementService dressMovementService;
    @MockitoBean
    private UserRepository userRepository;
    private DressMovement dressMovement;
    private Dress dress;

    @BeforeEach
    void setUp() {
        this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        var mockUser = new User();
        when(userRepository.findByAuth0Sub(anyString())).thenReturn(java.util.Optional.of(mockUser));
        dress = new Dress();
        dress.setId(1L);
        dress.setTitle("Test Dress");
        dress.setSku("SKU001");
        dress.setSize("M");
        dress.setColor("#FF0000");
        dress.setStock(10);
        dress.setPrice(new BigDecimal("100.00"));

        dressMovement = new DressMovement();
        dressMovement.setId(1L);
        dressMovement.setDress(dress);
        dressMovement.setQuantity(5);
        dressMovement.setInstant(Instant.now());
    }

    @Test
    @WithMockUser
    void page_ShouldReturnPageOfDressMovements() throws Exception {
        Page<DressMovement> page = new PageImpl<>(Collections.singletonList(dressMovement), PageRequest.of(0, 10), 1);
        when(dressMovementService.page(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/dress-movements/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(dressMovementService, times(1)).page(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void detail_ShouldReturnDressMovement() throws Exception {
        when(dressMovementService.detail(1L)).thenReturn(dressMovement);

        mockMvc.perform(get("/dress-movements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(5));

        verify(dressMovementService, times(1)).detail(1L);
    }

    @Test
    @WithMockUser
    void create_ShouldReturnCreatedDressMovement() throws Exception {
        DressMovementDtos.DressMovementCreateRequest request = new DressMovementDtos.DressMovementCreateRequest(
                1L, 5, Instant.now()
        );
        when(dressMovementService.create(any())).thenReturn(dressMovement);

        mockMvc.perform(post("/dress-movements/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(dressMovementService, times(1)).create(any());
    }

    @Test
    @WithMockUser
    void update_ShouldReturnUpdatedDressMovement() throws Exception {
        DressMovementDtos.DressMovementUpdateRequest request = new DressMovementDtos.DressMovementUpdateRequest(
                1L, 1L, 10, Instant.now()
        );
        when(dressMovementService.update(any())).thenReturn(dressMovement);

        mockMvc.perform(put("/dress-movements/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(dressMovementService, times(1)).update(any());
    }

    @Test
    @WithMockUser
    void delete_ShouldDeleteDressMovement() throws Exception {
        doNothing().when(dressMovementService).delete(1L);

        mockMvc.perform(delete("/dress-movements/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(dressMovementService, times(1)).delete(1L);
    }
}
