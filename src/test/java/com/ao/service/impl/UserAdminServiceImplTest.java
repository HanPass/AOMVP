package com.ao.service.impl;

import com.ao.dto.UserRequest;
import com.ao.dto.UserResponse;
import com.ao.entity.UserEntity;
import com.ao.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminServiceImpl service;

    @Test
    void shouldCreateUserWhenEmailNotExisting() {
        UserRequest request = new UserRequest(" USER@TEST.COM ", "  Reda ", " Jamaleddine ", true);

        UserEntity saved = UserEntity.builder()
                .id(1L)
                .email("user@test.com")
                .firstName("Reda")
                .lastName("Jamaleddine")
                .enabled(true)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = service.upsert(request);

        assertEquals(1L, response.id());
        assertEquals("user@test.com", response.email());
        assertEquals("Reda", response.firstName());
        assertEquals("Jamaleddine", response.lastName());
        assertTrue(response.enabled());
    }

    @Test
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                UserEntity.builder().id(1L).email("u1@test.com").enabled(true).build(),
                UserEntity.builder().id(2L).email("u2@test.com").enabled(false).build()
        ));

        List<UserResponse> responses = service.findAll();

        assertEquals(2, responses.size());
        assertEquals("u1@test.com", responses.get(0).email());
        assertEquals("u2@test.com", responses.get(1).email());
    }

    @Test
    void shouldDeleteWhenIdExists() {
        when(userRepository.existsById(3L)).thenReturn(true);

        boolean deleted = service.deleteById(3L);

        assertTrue(deleted);
        verify(userRepository).deleteById(3L);
    }

    @Test
    void shouldNotDeleteWhenIdMissing() {
        when(userRepository.existsById(99L)).thenReturn(false);

        boolean deleted = service.deleteById(99L);

        assertFalse(deleted);
    }
}
