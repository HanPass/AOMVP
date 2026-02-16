package com.ao.service.impl;

import com.ao.dto.UserRequest;
import com.ao.dto.UserResponse;
import com.ao.entity.UserEntity;
import com.ao.repository.UserRepository;
import com.ao.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse upsert(UserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        UserEntity entity = userRepository.findByEmail(normalizedEmail)
                .orElseGet(UserEntity::new);

        entity.setEmail(normalizedEmail);
        entity.setFirstName(trimToNull(request.firstName()));
        entity.setLastName(trimToNull(request.lastName()));
        entity.setEnabled(request.enabled() == null || request.enabled());

        UserEntity saved = userRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        if (id == null || !userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.isEnabled()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
