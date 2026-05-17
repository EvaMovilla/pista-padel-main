package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.BadRequestException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.ConflictException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.ForbiddenException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaServiceTest {

    @Mock private ReservaRepositorio reservaRepositorio;
    @Mock private PistaService pistaService;

    private ReservaService reservaService;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaService(reservaRepositorio, pistaService);
    }

    @Test
    void crearReserva_lanza409_siHaySolapamiento() {
        Usuario usuario = crearUsuario(1L, Rol.USER);
        Pista pista = crearPista(10L, true);
        LocalDate fecha = LocalDate.now().plusDays(10);

        ReservaRequest req = crearRequest(10L, fecha, LocalTime.of(18, 0), 90);

        when(pistaService.obtenerPista(10L)).thenReturn(pista);
        when(reservaRepositorio.existeSolapamiento(
                eq(pista), eq(fecha), eq(LocalTime.of(18, 0)), eq(LocalTime.of(19, 30))
        )).thenReturn(true);

        assertThrows(ConflictException.class, () -> reservaService.crearReserva(req, usuario));
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void crearReserva_lanza400_siPistaInactiva() {
        Usuario usuario = crearUsuario(1L, Rol.USER);
        Pista pista = crearPista(10L, false);

        ReservaRequest req = crearRequest(10L, LocalDate.now().plusDays(10), LocalTime.of(18, 0), 60);

        when(pistaService.obtenerPista(10L)).thenReturn(pista);

        assertThrows(BadRequestException.class, () -> reservaService.crearReserva(req, usuario));
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void crearReserva_lanza400_siFechaPasada() {
        Usuario usuario = crearUsuario(1L, Rol.USER);
        Pista pista = crearPista(10L, true);

        ReservaRequest req = crearRequest(10L, LocalDate.now().minusDays(1), LocalTime.of(18, 0), 60);

        when(pistaService.obtenerPista(10L)).thenReturn(pista);

        assertThrows(BadRequestException.class, () -> reservaService.crearReserva(req, usuario));
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void crearReserva_guardaReserva_sinSolapamiento() {
        Usuario usuario = crearUsuario(1L, Rol.USER);
        Pista pista = crearPista(10L, true);

        ReservaRequest req = crearRequest(10L, LocalDate.now().plusDays(10), LocalTime.of(18, 0), 60);

        Reserva saved = new Reserva();
        saved.setIdReserva(1L);

        when(pistaService.obtenerPista(10L)).thenReturn(pista);
        when(reservaRepositorio.existeSolapamiento(any(), any(), any(), any())).thenReturn(false);
        when(reservaRepositorio.save(any())).thenReturn(saved);

        Reserva result = reservaService.crearReserva(req, usuario);

        assertNotNull(result);
        assertEquals(1L, result.getIdReserva());
        verify(reservaRepositorio).save(any());
    }

    @Test
    void cancelarReserva_lanza403_siNoTienePermisos() {
        Usuario dueno = crearUsuario(1L, Rol.USER);
        Usuario otro = crearUsuario(2L, Rol.USER);
        Reserva reserva = crearReserva(100L, dueno, crearPista(10L, true));

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        assertThrows(ForbiddenException.class, () -> reservaService.cancelarReserva(100L, otro));
        verify(reservaRepositorio, never()).save(any());
    }

    @Test
    void cancelarReserva_permiteCancelar_siEsElDueno() {
        Usuario usuario = crearUsuario(1L, Rol.USER);
        Reserva reserva = crearReserva(100L, usuario, crearPista(10L, true));

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        reservaService.cancelarReserva(100L, usuario);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepositorio).save(reserva);
    }

    @Test
    void cancelarReserva_permiteCancelar_siEsAdmin() {
        Usuario dueno = crearUsuario(1L, Rol.USER);
        Usuario admin = crearUsuario(99L, Rol.ADMIN);
        Reserva reserva = crearReserva(100L, dueno, crearPista(10L, true));

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        reservaService.cancelarReserva(100L, admin);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        verify(reservaRepositorio).save(reserva);
    }

    @Test
    void obtenerReserva_lanza403_siNoTienePermisos() {
        Usuario dueno = crearUsuario(1L, Rol.USER);
        Usuario otro = crearUsuario(2L, Rol.USER);
        Reserva reserva = crearReserva(100L, dueno, crearPista(10L, true));

        when(reservaRepositorio.findById(100L)).thenReturn(Optional.of(reserva));

        assertThrows(ForbiddenException.class, () -> reservaService.obtenerReserva(100L, otro));
    }

    @Test
    void listarMisReservasFiltradas_lanza400_siFromEsPosteriorATo() {
        Usuario usuario = crearUsuario(1L, Rol.USER);

        assertThrows(BadRequestException.class,
                () -> reservaService.listarMisReservasFiltradas(
                        usuario,
                        LocalDate.now().plusDays(30),
                        LocalDate.now().plusDays(1)
                ));
    }

    private ReservaRequest crearRequest(Long courtId, LocalDate fecha, LocalTime hora, int duracion) {
        ReservaRequest req = new ReservaRequest();
        req.setCourtId(courtId);
        req.setDate(fecha);
        req.setStartTime(hora);
        req.setDurationMinutes(duracion);
        return req;
    }

    private Usuario crearUsuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setRol(rol);
        u.setActivo(true);
        u.setNombre("Usuario");
        u.setEmail("test" + id + "@mail.com");
        return u;
    }

    private Pista crearPista(Long id, boolean activa) {
        Pista p = new Pista();
        p.setIdPista(id);
        p.setNombre("Pista " + id);
        p.setActiva(activa);
        p.setFechaAlta(LocalDateTime.now());
        return p;
    }

    private Reserva crearReserva(Long id, Usuario usuario, Pista pista) {
        Reserva r = new Reserva();
        r.setIdReserva(id);
        r.setUsuario(usuario);
        r.setPista(pista);
        r.setFechaReserva(LocalDate.now().plusDays(10));
        r.setHoraInicio(LocalTime.of(18, 0));
        r.setHoraFin(LocalTime.of(19, 30));
        r.setDuracionMinutos(90);
        r.setEstado(EstadoReserva.ACTIVA);
        r.setFechaCreacion(LocalDateTime.now());
        return r;
    }
}