package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.domain.UserRole;
import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.UserDto;
import com.demo.warehouse.mapper.UserMapper;
import com.demo.warehouse.repository.DressMovementRepository;
import com.demo.warehouse.repository.DressRepository;
import com.demo.warehouse.repository.TenantRepository;
import com.demo.warehouse.repository.UserRepository;
import com.demo.warehouse.service.Auth0ManagementService;
import com.demo.warehouse.service.UserService;
import com.demo.warehouse.tenantFilter.UserContext;
import com.demo.warehouse.tenantFilter.UserContextHolder;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TenantRepository tenantRepository;
    @MockitoBean
    private DressRepository dressRepository;
    @MockitoBean
    private DressMovementRepository dressMovementRepository;
    @MockitoBean
    private Auth0ManagementService auth0ManagementService;
    private User user;
    private Tenant tenant;
    private UserDto.UserResponse userResponse;
    private UserDto.LoggedUserDto loggedUserDto;

    @BeforeEach
    void setUp() {
        this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        this.objectMapper.findAndRegisterModules();

        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");
        tenant.setModules(Set.of(ModuleType.DRESS));

        user = new User(null, "testuser", null, "", UserRole.SUPERADMIN, new HashSet<>());

        user.setTenant(tenant);

        userResponse = UserDto.UserResponse.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.ADMIN)
                .tenant(tenant)
                .isEditable(true)
                .build();

        loggedUserDto = new UserDto.LoggedUserDto(1L, "testuser", "auth0sub", UserRole.ADMIN, tenant);

        when(userRepository.findByAuth0Sub(anyString())).thenReturn(java.util.Optional.of(user));
        UserContext context = UserContext.builder().realUser(user).build();
        UserContextHolder.set(context);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @WithMockUser
    void getCurrentUser_ShouldReturnCurrentUser() throws Exception {
        when(userMapper.toLogged(user)).thenReturn(loggedUserDto);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userMapper, times(1)).toLogged(user);
    }

    @Test
    @WithMockUser
    void page_ShouldReturnPageOfUsers() throws Exception {
        Page<UserDto.UserResponse> page = new PageImpl<>(Collections.singletonList(userResponse), PageRequest.of(0, 10), 1);
        when(userService.page(any(), any())).thenReturn(page);

        mockMvc.perform(get("/users/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService, times(1)).page(any(), any());
    }

    @Test
    @WithMockUser
    void list_ShouldReturnListOfUsers() throws Exception {
        List<IdName<Long>> list = Collections.singletonList(new IdNameImpl(1L, "testuser"));
        when(userService.list()).thenReturn(list);

        mockMvc.perform(get("/users/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("testuser"));

        verify(userService, times(1)).list();
    }

    @Test
    @WithMockUser
    void detail_ShouldReturnUserForSelf() throws Exception {
        when(userService.detail(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService, times(1)).detail(1L);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void create_ShouldReturnCreatedUser() throws Exception {
        UserDto.UserCreateRequest request = new UserDto.UserCreateRequest("newuser", UserRole.USER, null);
        when(userService.create(any())).thenReturn(userResponse);

        mockMvc.perform(post("/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService, times(1)).create(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void update_ShouldReturnUpdatedUser() throws Exception {
        UserDto.UserUpdateRequest request = new UserDto.UserUpdateRequest(1L, "updateduser", UserRole.ADMIN, null);
        when(userService.update(any())).thenReturn(userResponse);

        mockMvc.perform(put("/users/update/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService, times(1)).update(any());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void delete_ShouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/delete/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(1L);
    }
}
