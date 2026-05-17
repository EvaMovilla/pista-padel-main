package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.ReservaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PistaServiceTest {

    @Mock
    private PistaRepositorio pistaRepositorio;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @InjectMocks
    private PistaService pistaService;

    @Test
    void crearPista_lanza409_siNombreYaExiste() {

        PistaRequest req = new PistaRequest();
        req.setNombre("Central");
        req.setUbicacion("Madrid");
        req.setPrecioHora(20.0);
        req.setActiva(true);

        when(pistaRepositorio.existsByNombre("Central")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> pistaService.crearPista(req)
        );

        assertEquals(409, ex.getStatusCode().value());

        verify(pistaRepositorio, never()).save(any());
    }

    @Test
    void crearPista_guardaYDevuelvePista_siNombreNoExiste() {

        PistaRequest req = new PistaRequest();
        req.setNombre("Central");
        req.setUbicacion("Madrid");
        req.setPrecioHora(20.0);
        req.setActiva(true);

        when(pistaRepositorio.existsByNombre("Central")).thenReturn(false);

        when(pistaRepositorio.save(any(Pista.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Pista res = pistaService.crearPista(req);

        assertNotNull(res);
        assertEquals("Central", res.getNombre());
        assertEquals("Madrid", res.getUbicacion());
        assertEquals(20.0, res.getPrecioHora());
        assertNotNull(res.getFechaAlta());

        ArgumentCaptor<Pista> captor = ArgumentCaptor.forClass(Pista.class);

        verify(pistaRepositorio).save(captor.capture());

        assertEquals("Central", captor.getValue().getNombre());
    }

    @Test
    void listarPistas_siActivaEsNull_usaFindAll() {

        when(pistaRepositorio.findAll()).thenReturn(List.of());

        List<Pista> res = pistaService.listarPistas(null);

        assertNotNull(res);

        verify(pistaRepositorio).findAll();

        verify(pistaRepositorio, never()).findByActiva(anyBoolean());
    }

    @Test
    void listarPistas_siActivaNoEsNull_usaFindByActiva() {

        when(pistaRepositorio.findByActiva(true)).thenReturn(List.of());

        List<Pista> res = pistaService.listarPistas(true);

        assertNotNull(res);

        verify(pistaRepositorio).findByActiva(true);

        verify(pistaRepositorio, never()).findAll();
    }

    @Test
    void obtenerPista_lanza404_siNoExiste() {

        when(pistaRepositorio.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> pistaService.obtenerPista(1L)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void obtenerPista_devuelvePista_siExiste() {

        Pista p = new Pista();

        when(pistaRepositorio.findById(1L)).thenReturn(Optional.of(p));

        Pista res = pistaService.obtenerPista(1L);

        assertSame(p, res);
    }

    @Test
    void patch_actualizaNombreCorrectamente() {

        Pista pista = new Pista();
        pista.setNombre("Vieja");

        PistaUpdateRequest req = new PistaUpdateRequest();
        req.setNombre("Nueva");

        when(pistaRepositorio.findById(1L))
                .thenReturn(Optional.of(pista));

        when(pistaRepositorio.existsByNombre("Nueva"))
                .thenReturn(false);

        Pista resultado = pistaService.patch(1L, req);

        assertEquals("Nueva", resultado.getNombre());
    }

    @Test
    void patch_lanza409_siNombreDuplicado() {

        Pista pista = new Pista();
        pista.setNombre("Central");

        PistaUpdateRequest req = new PistaUpdateRequest();
        req.setNombre("Premium");

        when(pistaRepositorio.findById(1L))
                .thenReturn(Optional.of(pista));

        when(pistaRepositorio.existsByNombre("Premium"))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> pistaService.patch(1L, req)
        );

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void delete_siTieneReservas_haceBorradoLogico() {

        Pista pista = new Pista();
        pista.setActiva(true);

        when(pistaRepositorio.findById(1L))
                .thenReturn(Optional.of(pista));

        when(reservaRepositorio.existsByPista_IdPista(1L))
                .thenReturn(true);

        pistaService.delete(1L);

        assertFalse(pista.getActiva());

        verify(pistaRepositorio).save(pista);

        verify(pistaRepositorio, never()).delete(any());
    }

    @Test
    void delete_siNoTieneReservas_borraFisicamente() {

        Pista pista = new Pista();

        when(pistaRepositorio.findById(1L))
                .thenReturn(Optional.of(pista));

        when(reservaRepositorio.existsByPista_IdPista(1L))
                .thenReturn(false);

        pistaService.delete(1L);

        verify(pistaRepositorio).delete(pista);

        verify(pistaRepositorio, never()).save(any());
    }
}