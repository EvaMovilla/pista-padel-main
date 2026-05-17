package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void existeEmail_devuelveTrue_siExiste() {

        when(usuarioRepositorio.existsByEmail("test@test.com"))
                .thenReturn(true);

        boolean resultado = usuarioRepositorio.existsByEmail("test@test.com");

        assertTrue(resultado);
        verify(usuarioRepositorio).existsByEmail("test@test.com");
    }

    @Test
    void existeEmail_devuelveFalse_siNoExiste() {

        when(usuarioRepositorio.existsByEmail("test@test.com"))
                .thenReturn(false);

        boolean resultado = usuarioRepositorio.existsByEmail("test@test.com");

        assertFalse(resultado);
        verify(usuarioRepositorio).existsByEmail("test@test.com");
    }

    @Test
    void existeEmail_llamaRepositorioConEmailCorrecto() {

        String email = "usuario@gmail.com";

        when(usuarioRepositorio.existsByEmail(email))
                .thenReturn(true);

        boolean resultado = usuarioRepositorio.existsByEmail(email);

        assertTrue(resultado);
        verify(usuarioRepositorio, times(1)).existsByEmail(email);
    }
}