package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void obtenerUsuario_lanza404_siNoExiste() {

        when(usuarioRepositorio.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.obtenerUsuario(1L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void obtenerUsuario_devuelveUsuario_siExiste() {

        Usuario usuario = new Usuario();

        when(usuarioRepositorio.findById(1L))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.obtenerUsuario(1L);

        assertSame(usuario, resultado);
    }

    @Test
    void existeEmail_devuelveTrue_siExiste() {

        when(usuarioRepositorio.existsByEmail("test@test.com"))
                .thenReturn(true);

        boolean resultado = usuarioRepositorio.existsByEmail("test@test.com");

        assertTrue(resultado);
    }

    @Test
    void existeEmail_devuelveFalse_siNoExiste() {

        when(usuarioRepositorio.existsByEmail("test@test.com"))
                .thenReturn(false);

        boolean resultado = usuarioRepositorio.existsByEmail("test@test.com");

        assertFalse(resultado);
    }
}