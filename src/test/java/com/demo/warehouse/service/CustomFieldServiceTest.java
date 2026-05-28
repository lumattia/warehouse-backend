package com.demo.warehouse.service;

import com.demo.warehouse.domain.CustomFieldDefinition;
import com.demo.warehouse.domain.CustomFieldGroup;
import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.FieldValidations;
import com.demo.warehouse.mapper.CustomFieldDtos;
import com.demo.warehouse.repository.CustomFieldDefinitionRepository;
import com.demo.warehouse.repository.CustomFieldGroupRepository;
import com.demo.warehouse.repository.CustomFieldValueRepository;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import com.demo.warehouse.testutils.TestFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CustomFieldServiceTest {

    @Mock
    private CustomFieldGroupRepository groupRepository;
    
    @Mock
    private CustomFieldDefinitionRepository definitionRepository;
    
    @Mock
    private CustomFieldValueRepository valueRepository;
    
    @InjectMocks
    private CustomFieldService customFieldService;

    private Tenant tenant;
    private CustomFieldGroup group;
    private CustomFieldDefinition definition;

    @BeforeEach
    void setUp() {
        tenant = TestFactory.createDefaultTenant();
        tenant.setHasCustomFields(true);
        
        group = new CustomFieldGroup();
        group.setId(1L);
        group.setName("Test Group");
        group.setGroupOrder(0);
        group.setModule(ModuleType.DRESS);
        group.setTenantId(tenant.getId());
        group.setDefinitions(new ArrayList<>());
        
        definition = new CustomFieldDefinition();
        definition.setId(1L);
        definition.setGroup(group);
        definition.setLabel("Test Field");
        definition.setType(com.demo.warehouse.domain.CustomFieldType.TEXT);
        definition.setFieldOrder(0);
        definition.setValidations(new FieldValidations(false, null, null, null, null));
        
        group.getDefinitions().add(definition);
        
        TestFactory.setUserContextHolder(TestFactory.createDefaultUser(tenant));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getFormStructureForEntity_WhenHasCustomFieldsFalse_ReturnsEmpty() {
        tenant.setHasCustomFields(false);
        
        List<CustomFieldDtos.CustomFieldGroupResponse> result = customFieldService.getFormStructureForEntity(ModuleType.DRESS, 1L);
        
        assertTrue(result.isEmpty());
        verify(groupRepository, never()).findByTenantIdAndModuleOrderByGroupOrderAsc(any(), any());
    }

    @Test
    void getFormStructureForEntity_WhenHasCustomFieldsTrue_ReturnsStructure() {
        when(groupRepository.findByTenantIdAndModuleOrderByGroupOrderAsc(tenant.getId(), ModuleType.DRESS))
            .thenReturn(List.of(group));
        
        List<CustomFieldDtos.CustomFieldGroupResponse> result = customFieldService.getFormStructureForEntity(ModuleType.DRESS, 1L);
        
        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).name());
        assertEquals(1, result.get(0).definitions().size());
        assertEquals("Test Field", result.get(0).definitions().get(0).label());
    }

    @Test
    void validateAndSaveValues_WhenRequiredFieldEmpty_ThrowsException() {
        definition.setValidations(new FieldValidations(true, null, null, null, null));
        when(groupRepository.findByTenantIdAndModuleOrderByGroupOrderAsc(tenant.getId(), ModuleType.DRESS))
            .thenReturn(List.of(group));
        
        Map<Long, String> customFields = new HashMap<>();
        customFields.put(1L, "");
        
        assertThrows(RuntimeException.class, () -> 
            customFieldService.validateAndSaveValues(ModuleType.DRESS, 1L, customFields));
    }

    @Test
    void validateAndSaveValues_WhenNumberFieldInvalid_ThrowsException() {
        definition.setType(com.demo.warehouse.domain.CustomFieldType.NUMBER);
        definition.setValidations(new FieldValidations(false, 10.0, null, null, null));
        when(groupRepository.findByTenantIdAndModuleOrderByGroupOrderAsc(tenant.getId(), ModuleType.DRESS))
            .thenReturn(List.of(group));
        
        Map<Long, String> customFields = new HashMap<>();
        customFields.put(1L, "5");
        
        assertThrows(RuntimeException.class, () -> 
            customFieldService.validateAndSaveValues(ModuleType.DRESS, 1L, customFields));
    }

    @Test
    void updateOrders_WhenDuplicateGroupOrders_ThrowsException() {
        List<CustomFieldDtos.OrderUpdateDTO> groupOrders = List.of(
            new CustomFieldDtos.OrderUpdateDTO(1L, 1),
            new CustomFieldDtos.OrderUpdateDTO(2L, 1)
        );
        
        assertThrows(RuntimeException.class, () -> 
            customFieldService.updateOrders(ModuleType.DRESS, groupOrders, List.of()));
    }

    @Test
    void updateOrders_WhenDuplicateFieldOrders_ThrowsException() {
        List<CustomFieldDtos.OrderUpdateDTO> fieldOrders = List.of(
            new CustomFieldDtos.OrderUpdateDTO(1L, 1),
            new CustomFieldDtos.OrderUpdateDTO(2L, 1)
        );
        
        assertThrows(RuntimeException.class, () -> 
            customFieldService.updateOrders(ModuleType.DRESS, List.of(), fieldOrders));
    }

    @Test
    void updateOrders_WhenValid_UpdatesOrders() {
        List<CustomFieldDtos.OrderUpdateDTO> groupOrders = List.of(
            new CustomFieldDtos.OrderUpdateDTO(1L, 1)
        );
        List<CustomFieldDtos.OrderUpdateDTO> fieldOrders = List.of(
            new CustomFieldDtos.OrderUpdateDTO(1L, 1)
        );
        
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(definitionRepository.findById(1L)).thenReturn(Optional.of(definition));
        
        customFieldService.updateOrders(ModuleType.DRESS, groupOrders, fieldOrders);
        
        assertEquals(1, group.getGroupOrder());
        assertEquals(1, definition.getFieldOrder());
        verify(groupRepository).save(group);
        verify(definitionRepository).save(definition);
    }
}
