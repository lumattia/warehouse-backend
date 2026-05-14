package com.demo.warehouse.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.warehouse.mapper.IdName;
import com.demo.warehouse.mapper.DressDtos;
import com.demo.warehouse.service.DressService;
import com.demo.warehouse.specification.DressSpecification;

@RestController
@RequestMapping("/dresses")
@RequiredArgsConstructor
public class DressController {
    private final DressService dressService;

    @GetMapping("/page")
    public Page<DressDtos.DressResponse> page(DressDtos.DressFilterRequest request, Pageable pageable) {
        return dressService.page(DressSpecification.filterBy(request), pageable);
    }
    @GetMapping("/list")
    public List<IdName> list() {
        return dressService.list();
        }
    @GetMapping("/{id}")
    public DressDtos.DressResponse detail(@NotNull Long id) {
        return dressService.detail(id);
    }

    @PostMapping("/create")
    public DressDtos.DressResponse create(@Valid @RequestBody DressDtos.DressCreateRequest request) {
        return dressService.create(request);
    }
    @PutMapping("/update")
    public DressDtos.DressResponse update(@Valid @RequestBody DressDtos.DressUpdateRequest request) {
        return dressService.update(request);
    }
    @DeleteMapping("/delete")
    public void delete(@NotNull Long toDeleteId) {
        dressService.delete(toDeleteId);
    }
}
