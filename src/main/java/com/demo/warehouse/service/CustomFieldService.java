package com.demo.warehouse.service;

import com.demo.warehouse.domain.CustomFieldDefinition;
import com.demo.warehouse.domain.CustomFieldGroup;
import com.demo.warehouse.domain.ModuleType;
import com.demo.warehouse.domain.CustomFieldValue;
import com.demo.warehouse.domain.Tenant;
import com.demo.warehouse.domain.FieldValidations;
import com.demo.warehouse.mapper.CustomFieldDtos;
import com.demo.warehouse.repository.CustomFieldDefinitionRepository;
import com.demo.warehouse.repository.CustomFieldGroupRepository;
import com.demo.warehouse.repository.CustomFieldValueRepository;
import com.demo.warehouse.specification.CustomFieldGroupSpecification;
import com.demo.warehouse.tenantFilter.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomFieldService {

    private final CustomFieldGroupRepository groupRepository;
    private final CustomFieldDefinitionRepository definitionRepository;
    private final CustomFieldValueRepository valueRepository;
    @Transactional(readOnly = true)
    public List<CustomFieldDtos.CustomFieldGroupResponse> getFormStructureForEntity(ModuleType module, Long targetId) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();
        
        if (!tenant.getHasCustomFields()) {
            return Collections.emptyList();
        }

        List<CustomFieldGroup> groups = groupRepository.listBySpec(CustomFieldGroupSpecification.getAllWithModule(module));

        Map<Long, String> existingValues = valueRepository.findByTargetId(targetId).stream()
                .collect(Collectors.toMap(
                        v -> v.getDefinition().getId(),
                        CustomFieldValue::getValue
                ));

        return groups.stream().map(group -> {
            List<CustomFieldDtos.CustomFieldDefinitionResponse> definitions = group.getDefinitions().stream()
                    .map(def -> {
                        FieldValidations validations = def.getValidations();
                        String value = existingValues.getOrDefault(def.getId(), "");

                        return new CustomFieldDtos.CustomFieldDefinitionResponse(
                                def.getId(), def.getGroup().getId(), def.getLabel(), def.getType(),
                                def.getFieldOrder(), validations, value
                        );
                    })
                    .collect(Collectors.toList());

            return new CustomFieldDtos.CustomFieldGroupResponse(
                    group.getId(), group.getName(), group.getGroupOrder(), group.getModule(), definitions
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void validateAndSaveValues(ModuleType module, Long targetId, Map<Long, String> customFields) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        if (!tenant.getHasCustomFields()) {
            return;
        }

        List<CustomFieldGroup> groups = groupRepository.listBySpec(CustomFieldGroupSpecification.getAllWithModule(module));
        List<CustomFieldDefinition> allDefinitions = groups.stream()
                .flatMap(g -> g.getDefinitions().stream())
                .collect(Collectors.toList());

        for (CustomFieldDefinition definition : allDefinitions) {
            String value = customFields.get(definition.getId());
            FieldValidations validations = definition.getValidations();
            boolean isRequired = validations != null && validations.required();

            if (isRequired && (value == null || value.trim().isEmpty())) {
                throw new RuntimeException("Field '" + definition.getLabel() + "' is required");
            }

            if (value != null && !value.trim().isEmpty()) {
                switch (definition.getType()) {
                    case NUMBER:
                        try {
                            Double numValue = Double.parseDouble(value);
                            if (validations != null && validations.min() != null && numValue < validations.min()) {
                                throw new RuntimeException("Field '" + definition.getLabel() + "' must be at least " + validations.min());
                            }
                            if (validations != null && validations.max() != null && numValue > validations.max()) {
                                throw new RuntimeException("Field '" + definition.getLabel() + "' must be at most " + validations.max());
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Field '" + definition.getLabel() + "' must be a valid number");
                        }
                        break;
                    case TEXT:
                        if (validations != null && validations.maxLength() != null && value.length() > validations.maxLength()) {
                            throw new RuntimeException("Field '" + definition.getLabel() + "' must not exceed " + validations.maxLength() + " characters");
                        }
                        break;
                    case SELECT:
                        List<String> options = validations != null ? validations.options() : Collections.emptyList();
                        if (!options.isEmpty() && !options.contains(value)) {
                            throw new RuntimeException("Field '" + definition.getLabel() + "' must be one of the allowed options");
                        }
                        break;
                    case DATE:
                        try {
                            java.time.LocalDate.parse(value);
                        } catch (Exception e) {
                            throw new RuntimeException("Field '" + definition.getLabel() + "' must be a valid date (YYYY-MM-DD)");
                        }
                        break;
                    case CHECKBOX:
                        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                            throw new RuntimeException("Field '" + definition.getLabel() + "' must be true or false");
                        }
                        break;
                }
            }

            Optional<CustomFieldValue> existingValue = valueRepository.findByDefinitionIdAndTargetId(definition.getId(), targetId);
            CustomFieldValue targetValue = existingValue.orElseGet(() -> {
                CustomFieldValue newValue = new CustomFieldValue();
                newValue.setDefinition(definition);
                newValue.setTargetId(targetId);
                return newValue;
            });

            targetValue.setValue(value != null ? value : "");
            valueRepository.save(targetValue);
        }
    }

    @Transactional
    public void updateOrders(ModuleType module, List<Long> groupOrders, List<CustomFieldDtos.FieldOrderUpdate> fieldOrders) {
        for (int i = 0; i < groupOrders.size(); i++) {
            Long groupId = groupOrders.get(i);
            CustomFieldGroup group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

            if (!group.getModule().equals(module)) {
                throw new RuntimeException("Unauthorized or invalid module for group: " + groupId);
            }

            group.setGroupOrder(i);
            groupRepository.save(group);
        }

        for (int i = 0; i < fieldOrders.size(); i++) {
            CustomFieldDtos.FieldOrderUpdate fieldOrderUpdate = fieldOrders.get(i);
            Long fieldId = fieldOrderUpdate.fieldId();
            Long newGroupId = fieldOrderUpdate.groupId();
            
            CustomFieldDefinition definition = definitionRepository.findById(fieldId)
                    .orElseThrow(() -> new RuntimeException("Definition not found: " + fieldId));

            CustomFieldGroup newGroup = groupRepository.findById(newGroupId)
                    .orElseThrow(() -> new RuntimeException("Group not found: " + newGroupId));

            if (!newGroup.getModule().equals(module)) {
                throw new RuntimeException("Unauthorized or invalid module for group: " + newGroupId);
            }

            definition.setFieldOrder(i);
            definition.setGroup(newGroup);
            definitionRepository.save(definition);
        }
    }
    @Transactional(readOnly = true)
    public List<CustomFieldDtos.CustomFieldGroupResponse> getGroupsByModule(ModuleType module) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        if (!tenant.getHasCustomFields()) {
            return Collections.emptyList();
        }
        return groupRepository.listBySpec(CustomFieldGroupSpecification.getAllWithModule(module)).stream()
                .map(group -> {
                    List<CustomFieldDtos.CustomFieldDefinitionResponse> definitions = group.getDefinitions().stream()
                            .map(def -> {
                                FieldValidations validations = def.getValidations();
                                return new CustomFieldDtos.CustomFieldDefinitionResponse(
                                        def.getId(), def.getGroup().getId(), def.getLabel(), def.getType(),
                                        def.getFieldOrder(), validations, null
                                );
                            })
                            .collect(Collectors.toList());

                    return new CustomFieldDtos.CustomFieldGroupResponse(
                            group.getId(), group.getName(), group.getGroupOrder(), group.getModule(), definitions
                    );
                }).collect(Collectors.toList());
    }

    @Transactional
    public CustomFieldDtos.CustomFieldGroupResponse createGroup(CustomFieldDtos.CustomFieldGroupCreateRequest request) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        if (!tenant.getHasCustomFields()) {
            throw new RuntimeException("Custom fields are not enabled for this tenant");
        }

        CustomFieldGroup group = new CustomFieldGroup();
        group.setName(request.name());
        group.setGroupOrder(request.groupOrder() != null ? request.groupOrder() : 0);
        group.setModule(request.module());
        group.setTenant(tenant);
        group = groupRepository.save(group);

        return new CustomFieldDtos.CustomFieldGroupResponse(
                group.getId(), group.getName(), group.getGroupOrder(), group.getModule(), Collections.emptyList()
        );
    }

    @Transactional
    public CustomFieldDtos.CustomFieldGroupResponse updateGroup(CustomFieldDtos.CustomFieldGroupUpdateRequest request) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        CustomFieldGroup group = groupRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Group not found: " + request.id()));

        group.setName(request.name());
        group.setGroupOrder(request.groupOrder() != null ? request.groupOrder() : group.getGroupOrder());
        group = groupRepository.save(group);

        List<CustomFieldDtos.CustomFieldDefinitionResponse> definitions = group.getDefinitions().stream()
                .map(def -> {
                    FieldValidations validations = def.getValidations();
                    return new CustomFieldDtos.CustomFieldDefinitionResponse(
                            def.getId(), def.getGroup().getId(), def.getLabel(), def.getType(),
                            def.getFieldOrder(), validations, null
                    );
                })
                .collect(Collectors.toList());

        return new CustomFieldDtos.CustomFieldGroupResponse(
                group.getId(), group.getName(), group.getGroupOrder(), group.getModule(), definitions
        );
    }

    @Transactional
    public void deleteGroup(Long id) {
        CustomFieldGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        groupRepository.delete(group);
    }

    @Transactional
    public CustomFieldDtos.CustomFieldDefinitionResponse createDefinition(CustomFieldDtos.CustomFieldDefinitionCreateRequest request) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        if (!tenant.getHasCustomFields()) {
            throw new RuntimeException("Custom fields are not enabled for this tenant");
        }

        CustomFieldGroup group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("Group not found: " + request.groupId()));

        CustomFieldDefinition definition = new CustomFieldDefinition();
        definition.setGroup(group);
        definition.setLabel(request.label());
        definition.setType(request.type());
        definition.setFieldOrder(request.fieldOrder() != null ? request.fieldOrder() : 0);
        definition.setValidations(request.validations() != null ? request.validations() : new FieldValidations(null, null, null, null, null));

        definition = definitionRepository.save(definition);

        return new CustomFieldDtos.CustomFieldDefinitionResponse(
                definition.getId(), definition.getGroup().getId(), definition.getLabel(), definition.getType(),
                definition.getFieldOrder(), definition.getValidations(), null
        );
    }

    @Transactional
    public CustomFieldDtos.CustomFieldDefinitionResponse updateDefinition(CustomFieldDtos.CustomFieldDefinitionUpdateRequest request) {
        Tenant tenant = UserContextHolder.get().getUser().getTenant();

        CustomFieldDefinition definition = definitionRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("Definition not found: " + request.id()));

        CustomFieldGroup group = definition.getGroup();
        if (!group.getTenant().getId().equals(tenant.getId())) {
            throw new RuntimeException("Unauthorized to update definition: " + request.id());
        }

        definition.setGroup(groupRepository.findById(request.groupId())
                .orElseThrow(() -> new RuntimeException("Group not found: " + request.groupId())));
        definition.setLabel(request.label());
        definition.setType(request.type());
        definition.setFieldOrder(request.fieldOrder() != null ? request.fieldOrder() : definition.getFieldOrder());
        definition.setValidations(request.validations() != null ? request.validations() : definition.getValidations());

        definition = definitionRepository.save(definition);

        return new CustomFieldDtos.CustomFieldDefinitionResponse(
                definition.getId(), definition.getGroup().getId(), definition.getLabel(), definition.getType(),
                definition.getFieldOrder(), definition.getValidations(), null
        );
    }

    @Transactional
    public void deleteDefinition(Long id) {
        CustomFieldDefinition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Definition not found: " + id));

        definitionRepository.delete(definition);
    }

}