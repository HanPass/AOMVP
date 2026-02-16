package com.ao.service;

import com.ao.dto.UserRequest;
import com.ao.dto.UserResponse;

import java.util.List;

public interface UserAdminService {
    UserResponse upsert(UserRequest request);

    List<UserResponse> findAll();

    boolean deleteById(Long id);
}
