package com.demo.warehouse.mapper;

import org.mapstruct.Mapper;
import com.demo.warehouse.domain.User;
import com.demo.warehouse.mapper.UserDto.LoggedUserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoggedUserDto toLogged(User entity);
}