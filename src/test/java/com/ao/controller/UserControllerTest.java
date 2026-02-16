package com.ao.controller;

import com.ao.dto.UserRequest;
import com.ao.dto.UserResponse;
import com.ao.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private UserController controller;

    @Test
    void shouldReturnAllUsers() {
        List<UserResponse> data = List.of(
                new UserResponse(1L, "u1@test.com", "Reda", "Jamaleddine", true)
        );

        when(userAdminService.findAll()).thenReturn(data);

        ResponseEntity<List<UserResponse>> response = controller.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(data, response.getBody());
        verify(userAdminService).findAll();
    }

    @Test
    void shouldCreateUser() {
        UserRequest request = new UserRequest("u1@test.com", "Reda", "Jamaleddine", true);
        UserResponse saved = new UserResponse(1L, "u1@test.com", "Reda", "Jamaleddine", true);

        when(userAdminService.upsert(request)).thenReturn(saved);

        ResponseEntity<UserResponse> response = controller.upsert(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
        verify(userAdminService).upsert(request);
    }

    @Test
    void shouldReturnNoContentWhenDeleteSucceeded() {
        when(userAdminService.deleteById(10L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteById(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userAdminService).deleteById(10L);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteMissing() {
        when(userAdminService.deleteById(10L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteById(10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userAdminService).deleteById(10L);
    }
}
