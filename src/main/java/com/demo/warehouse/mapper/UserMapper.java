package com.demo.warehouse.mapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.UserDto.LoggedUserDto;
import com.demo.warehouse.mapper.UserDto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoggedUserDto toLogged(User entity);
    UserResponse toResponse(User entity);
    UserResponse toResponseWithTenants(User entity);
}