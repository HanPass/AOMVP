package com.ao.service;

import com.ao.entity.UserEntity;
import com.ao.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrontendAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FrontendAuthService frontendAuthService;

    @Test
    void shouldCreateTokenAndDecodeEmail() {
        when(userRepository.findByEmail("test@aomvp.ma")).thenReturn(Optional.of(UserEntity.builder().email("test@aomvp.ma").build()));

        String token = frontendAuthService.login("test@aomvp.ma");

        assertNotNull(token);
        assertEquals("test@aomvp.ma", frontendAuthService.emailFromToken(token));
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findByEmail("new@aomvp.ma")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = frontendAuthService.register("new@aomvp.ma", "New Company");

        assertEquals("new@aomvp.ma", frontendAuthService.emailFromToken(token));
    }
}
