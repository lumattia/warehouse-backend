package com.demo.warehouse.controller;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.IdNameImpl;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.DressService;
import com.demo.warehouse.testutils.TestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(DressController.class)
class DressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private DressService dressService;
    @MockitoBean
    private UserRepository userRepository;
    private DressDtos.DressResponse dressResponse;
    private DressDtos.DressCreateRequest createRequest;
    private DressDtos.DressUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        this.objectMapper.findAndRegisterModules();
        var mockUser = new User();
        when(userRepository.findByAuth0Sub(anyString())).thenReturn(java.util.Optional.of(mockUser));
        dressResponse = TestFactory.createDefaultDressResponse();
        createRequest = new DressDtos.DressCreateRequest(
                "Test Dress", "SKU001", "M", "#FF0000", new BigDecimal("100.00")
        );
        updateRequest = new DressDtos.DressUpdateRequest(
                1L, "Updated Dress", "SKU002", "L", "#00FF00", new BigDecimal("150.00")
        );
    }

    @Test
    @WithMockUser
    void page_ShouldReturnPageOfDresses() throws Exception {
        Page<DressDtos.DressResponse> page = new PageImpl<>(Collections.singletonList(dressResponse), PageRequest.of(0, 10), 1);
        when(dressService.page(any(), any())).thenReturn(page);

        mockMvc.perform(get("/dresses/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(dressService, times(1)).page(any(), any());
    }

    @Test
    @WithMockUser
    void list_ShouldReturnListOfDresses() throws Exception {
        List<IdName<Long>> list = Collections.singletonList(new IdNameImpl<>(1L, "Test Dress"));
        when(dressService.list()).thenReturn(list);

        mockMvc.perform(get("/dresses/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Dress"));

        verify(dressService, times(1)).list();
    }

    @Test
    @WithMockUser
    void detail_ShouldReturnDress() throws Exception {
        when(dressService.detail(1L)).thenReturn(dressResponse);

        mockMvc.perform(get("/dresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Dress"));

        verify(dressService, times(1)).detail(1L);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void create_ShouldReturnCreatedDress() throws Exception {
        when(dressService.create(any())).thenReturn(dressResponse);

        mockMvc.perform(post("/dresses/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(dressService, times(1)).create(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void update_ShouldReturnUpdatedDress() throws Exception {
        when(dressService.update(any())).thenReturn(dressResponse);

        mockMvc.perform(put("/dresses/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(dressService, times(1)).update(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void delete_ShouldDeleteDress() throws Exception {
        doNothing().when(dressService).delete(1L);

        mockMvc.perform(delete("/dresses/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(dressService, times(1)).delete(1L);
    }
}
