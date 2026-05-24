package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.UserDto.LoggedUserDto;
import com.demo.warehouse.mapper.UserDto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoggedUserDto toLogged(User entity);
    @Mapping(target = "isEditable", expression = "java(calculateIsEditable(entity))")
    UserResponse toResponse(User entity);

    default boolean calculateIsEditable(User targetUser) {
        try {
            User.validateCanModifyUser(targetUser);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}