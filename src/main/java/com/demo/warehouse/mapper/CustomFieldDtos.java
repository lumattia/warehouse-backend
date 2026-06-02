package com.demo.warehouse.mapper;

import com.demo.warehouse.domain.CustomFieldType;
import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.FieldValidations;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class CustomFieldDtos {

    public static record CustomFieldGroupResponse(
        Long id,
        String name,
        Integer groupOrder,
        ModuleType module,
        List<CustomFieldDefinitionResponse> definitions
    ) {}

    public static record CustomFieldGroupCreateRequest(
        @NotBlank
        String name,
        Integer groupOrder,
        @NotNull
        ModuleType module
    ) {}

    public static record CustomFieldGroupUpdateRequest(
        @NotNull
        Long id,
        @NotBlank
        String name,
        Integer groupOrder
    ) {}

    public static record CustomFieldDefinitionResponse(
        Long id,
        Long groupId,
        String label,
        CustomFieldType type,
        Integer fieldOrder,
        FieldValidations validations,
        String value
    ) {}

    public static record CustomFieldDefinitionCreateRequest(
        @NotNull
        Long groupId,
        @NotBlank
        String label,
        @NotNull
        CustomFieldType type,
        Integer fieldOrder,
        FieldValidations validations
    ) {}

    public static record CustomFieldDefinitionUpdateRequest(
        @NotNull
        Long id,
        @NotNull
        Long groupId,
        @NotBlank
        String label,
        @NotNull
        CustomFieldType type,
        Integer fieldOrder,
        FieldValidations validations
    ) {}

    public static record OrderUpdateRequest(
        List<Long> groupOrders,
        List<FieldOrderUpdate> fieldOrders
    ) {}

    public static record FieldOrderUpdate(
        Long fieldId,
        Long groupId
    ) {}

    public static record CustomFieldValueSaveRequest(
        @NotNull
        Map<Long, String> customFields
    ) {}
}
