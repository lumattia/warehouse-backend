package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.TenantDtos;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.TenantService;
import com.demo.warehouse.service.UserService;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    private Tenant tenant;
    private User user;
    private TenantDtos.TenantResponse tenantResponse;

    @BeforeEach
    void setUp() {
        this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");
        tenant.setModules(Set.of(ModuleType.DRESS));
        user = new User(null, "testuser", null, "", UserRole.SUPERADMIN, new HashSet<>());

        tenantResponse = TenantDtos.TenantResponse.builder()
                .id(tenant.getId())
                .name("Test Tenant")
                .modules(Set.of(ModuleType.DRESS))
                .createdAt(Instant.now())
                .expiresAt(null)
                .build();

        when(userRepository.findByAuth0Sub(anyString())).thenReturn(java.util.Optional.of(user));
        TestFactory.setUserContextHolder(user);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @WithMockUser
    void page_ShouldReturnPageOfTenants() throws Exception {
        Page<TenantDtos.TenantResponse> page = new PageImpl<>(Collections.singletonList(tenantResponse), PageRequest.of(0, 10), 1);
        when(tenantService.page(any())).thenReturn(page);

        mockMvc.perform(get("/tenants/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(tenantService, times(1)).page(any());
    }

    @Test
    @WithMockUser
    void list_ShouldReturnListOfTenants() throws Exception {
        IdName<UUID> idName = new IdName<>() {
            @Override
            public UUID getId() {
                return tenant.getId();
            }

            @Override
            public String getName() {
                return tenant.getName();
            }
        };
        List<IdName<UUID>> list = Collections.singletonList(idName);
        when(tenantService.list()).thenReturn(list);

        mockMvc.perform(get("/tenants/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(tenant.getId().toString()));

        verify(tenantService, times(1)).list();
    }

    @Test
    @WithMockUser
    void detail_ShouldReturnTenant() throws Exception {
        when(tenantService.detail(tenant.getId())).thenReturn(tenantResponse);

        mockMvc.perform(get("/tenants/" + tenant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId().toString()));

        verify(tenantService, times(1)).detail(tenant.getId());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SUPERADMIN"})
    void create_ShouldReturnCreatedTenant() throws Exception {
        TenantDtos.TenantCreateRequest request = new TenantDtos.TenantCreateRequest("New Tenant", Set.of(ModuleType.DRESS));
        when(tenantService.create(any())).thenReturn(tenantResponse);

        mockMvc.perform(post("/tenants/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId().toString()));

        verify(tenantService, times(1)).create(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SUPERADMIN"})
    void update_ShouldReturnUpdatedTenant() throws Exception {
        TenantDtos.TenantUpdateRequest request = new TenantDtos.TenantUpdateRequest(tenant.getId(), "Updated Tenant", Set.of(ModuleType.DRESS));
        when(tenantService.update(any())).thenReturn(tenantResponse);

        mockMvc.perform(put("/tenants/update/" + tenant.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId().toString()));

        verify(tenantService, times(1)).update(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_SUPERADMIN"})
    void delete_ShouldDeleteTenant() throws Exception {
        doNothing().when(tenantService).delete(tenant.getId());

        mockMvc.perform(delete("/tenants/delete/" + tenant.getId())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(tenantService, times(1)).delete(tenant.getId());
    }

    @Test
    @WithMockUser
    void switchTenant_ShouldSwitchTenant() throws Exception {
        UserDto.LoggedUserDto loggedUserDto = new UserDto.LoggedUserDto(1L, "testuser", "auth0sub", UserRole.ADMIN, tenant);
        when(userService.switchTenant(tenant.getId())).thenReturn(loggedUserDto);

        mockMvc.perform(post("/tenants/switch/" + tenant.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService, times(1)).switchTenant(tenant.getId());
    }
}
