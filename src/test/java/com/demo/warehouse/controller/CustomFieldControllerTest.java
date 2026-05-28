package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.FieldValidations;
import com.demo.warehouse.mapper.CustomFieldDtos;
import com.demo.warehouse.service.CustomFieldService;
import com.demo.warehouse.testutils.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(CustomFieldController.class)
class CustomFieldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomFieldService customFieldService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = TestFactory.createDefaultTenant();
        TestFactory.setUserContextHolder(TestFactory.createDefaultUser(tenant));
    }

    @AfterEach
    void tearDown() {
        com.demo.warehouse.tenantFilter.UserContextHolder.clear();
    }

    @Test
    void getFormStructure_ShouldReturnStructure() throws Exception {
        CustomFieldDtos.CustomFieldDefinitionResponse defResponse = new CustomFieldDtos.CustomFieldDefinitionResponse(
            1L, 1L, "Test Field", com.demo.warehouse.domain.CustomFieldType.TEXT, 0, new FieldValidations(false, null, null, null, null), ""
        );
        CustomFieldDtos.CustomFieldGroupResponse groupResponse = new CustomFieldDtos.CustomFieldGroupResponse(
            1L, "Test Group", 0, ModuleType.DRESS, List.of(defResponse)
        );
        
        when(customFieldService.getFormStructureForEntity(ModuleType.DRESS, 1L))
            .thenReturn(List.of(groupResponse));

        mockMvc.perform(get("/custom-fields/form-structure/DRESS/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Group"));
    }

    @Test
    void saveValues_ShouldSaveValues() throws Exception {
        mockMvc.perform(post("/custom-fields/values/DRESS/1")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"customFields\":{}}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrders_ShouldUpdateOrders() throws Exception {
        mockMvc.perform(put("/custom-fields/orders/DRESS")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"groupOrders\":[],\"fieldOrders\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    void getGroupsByModule_ShouldReturnGroups() throws Exception {
        CustomFieldDtos.CustomFieldGroupResponse groupResponse = new CustomFieldDtos.CustomFieldGroupResponse(
            1L, "Test Group", 0, ModuleType.DRESS, List.of()
        );
        
        when(customFieldService.getGroupsByModule(ModuleType.DRESS))
            .thenReturn(List.of(groupResponse));

        mockMvc.perform(get("/custom-fields/groups/DRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Group"));
    }

    @Test
    void createGroup_ShouldCreateGroup() throws Exception {
        CustomFieldDtos.CustomFieldGroupCreateRequest request = new CustomFieldDtos.CustomFieldGroupCreateRequest(
            "Test Group", 0, ModuleType.DRESS
        );
        CustomFieldDtos.CustomFieldGroupResponse response = new CustomFieldDtos.CustomFieldGroupResponse(
            1L, "Test Group", 0, ModuleType.DRESS, List.of()
        );
        
        when(customFieldService.createGroup(any())).thenReturn(response);

        mockMvc.perform(post("/custom-fields/groups")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Group\",\"groupOrder\":0,\"module\":\"DRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Group"));
    }

    @Test
    void updateGroup_ShouldUpdateGroup() throws Exception {
        CustomFieldDtos.CustomFieldGroupUpdateRequest request = new CustomFieldDtos.CustomFieldGroupUpdateRequest(
            1L, "Updated Group", 1, ModuleType.DRESS
        );
        CustomFieldDtos.CustomFieldGroupResponse response = new CustomFieldDtos.CustomFieldGroupResponse(
            1L, "Updated Group", 1, ModuleType.DRESS, List.of()
        );
        
        when(customFieldService.updateGroup(any())).thenReturn(response);

        mockMvc.perform(put("/custom-fields/groups")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"Updated Group\",\"groupOrder\":1,\"module\":\"DRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Group"));
    }

    @Test
    void deleteGroup_ShouldDeleteGroup() throws Exception {
        mockMvc.perform(delete("/custom-fields/groups/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void createDefinition_ShouldCreateDefinition() throws Exception {
        CustomFieldDtos.CustomFieldDefinitionCreateRequest request = new CustomFieldDtos.CustomFieldDefinitionCreateRequest(
            1L, "Test Field", com.demo.warehouse.domain.CustomFieldType.TEXT, 0, new FieldValidations(false, null, null, null, null)
        );
        CustomFieldDtos.CustomFieldDefinitionResponse response = new CustomFieldDtos.CustomFieldDefinitionResponse(
            1L, 1L, "Test Field", com.demo.warehouse.domain.CustomFieldType.TEXT, 0, new FieldValidations(false, null, null, null, null), null
        );
        
        when(customFieldService.createDefinition(any())).thenReturn(response);

        mockMvc.perform(post("/custom-fields/definitions")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"groupId\":1,\"label\":\"Test Field\",\"type\":\"TEXT\",\"fieldOrder\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Test Field"));
    }

    @Test
    void updateDefinition_ShouldUpdateDefinition() throws Exception {
        CustomFieldDtos.CustomFieldDefinitionUpdateRequest request = new CustomFieldDtos.CustomFieldDefinitionUpdateRequest(
            1L, 1L, "Updated Field", com.demo.warehouse.domain.CustomFieldType.TEXT, 1, new FieldValidations(false, null, null, null, null)
        );
        CustomFieldDtos.CustomFieldDefinitionResponse response = new CustomFieldDtos.CustomFieldDefinitionResponse(
            1L, 1L, "Updated Field", com.demo.warehouse.domain.CustomFieldType.TEXT, 1, new FieldValidations(false, null, null, null, null), null
        );
        
        when(customFieldService.updateDefinition(any())).thenReturn(response);

        mockMvc.perform(put("/custom-fields/definitions")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"groupId\":1,\"label\":\"Updated Field\",\"type\":\"TEXT\",\"fieldOrder\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Updated Field"));
    }

    @Test
    void deleteDefinition_ShouldDeleteDefinition() throws Exception {
        mockMvc.perform(delete("/custom-fields/definitions/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
