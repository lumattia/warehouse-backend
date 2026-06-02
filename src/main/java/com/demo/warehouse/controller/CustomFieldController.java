package com.demo.warehouse.controller;

import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.mapper.CustomFieldDtos;
import com.demo.warehouse.service.CustomFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/custom-fields")
@RequiredArgsConstructor
public class CustomFieldController {

    private final CustomFieldService customFieldService;

    @GetMapping("/form-structure/{module}/{targetId}")
    public List<CustomFieldDtos.CustomFieldGroupResponse> getFormStructure(
            @PathVariable ModuleType module,
            @PathVariable Long targetId) {
        return customFieldService.getFormStructureForEntity(module, targetId);
    }

    @PostMapping("/values/{module}/{targetId}")
    public void saveValues(
            @PathVariable ModuleType module,
            @PathVariable Long targetId,
            @RequestBody CustomFieldDtos.CustomFieldValueSaveRequest request) {
        customFieldService.validateAndSaveValues(module, targetId, request.customFields());
    }

    @PutMapping("/orders/{module}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public void updateOrders(
            @PathVariable ModuleType module,
            @RequestBody CustomFieldDtos.OrderUpdateRequest request) {
        customFieldService.updateOrders(module, request.groupOrders(), request.fieldOrders());
    }

    @GetMapping("/groups/{module}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public List<CustomFieldDtos.CustomFieldGroupResponse> getGroupsByModule(
            @PathVariable ModuleType module) {
        return customFieldService.getGroupsByModule(module);
    }

    @PostMapping("/groups")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public CustomFieldDtos.CustomFieldGroupResponse createGroup(
            @RequestBody CustomFieldDtos.CustomFieldGroupCreateRequest request) {
        return customFieldService.createGroup(request);
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public CustomFieldDtos.CustomFieldGroupResponse updateGroup(@PathVariable Long id,
            @RequestBody CustomFieldDtos.CustomFieldGroupUpdateRequest request) {
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("El ID de la URL no coincide con el ID del cuerpo");
        }
        return customFieldService.updateGroup(request);
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public void deleteGroup(@PathVariable Long id) {
        customFieldService.deleteGroup(id);
    }

    @PostMapping("/definitions")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public CustomFieldDtos.CustomFieldDefinitionResponse createDefinition(
            @RequestBody CustomFieldDtos.CustomFieldDefinitionCreateRequest request) {
        return customFieldService.createDefinition(request);
    }

    @PutMapping("/definitions/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public CustomFieldDtos.CustomFieldDefinitionResponse updateDefinition(@PathVariable Long id,
            @RequestBody CustomFieldDtos.CustomFieldDefinitionUpdateRequest request) {
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("El ID de la URL no coincide con el ID del cuerpo");
        }
        return customFieldService.updateDefinition(request);
    }

    @DeleteMapping("/definitions/{id}")
    @PreAuthorize("@securityService.isAtLeast('ADMIN')")
    public void deleteDefinition(@PathVariable Long id) {
        customFieldService.deleteDefinition(id);
    }
}
