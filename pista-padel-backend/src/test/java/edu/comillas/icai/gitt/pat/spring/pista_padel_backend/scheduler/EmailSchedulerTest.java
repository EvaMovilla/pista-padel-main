package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.scheduler;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSchedulerTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private ReservaRepositorio reservaRepo;
    @Mock
    private UsuarioRepositorio usuarioRepo;
    @Mock
    private PistaRepositorio pistaRepo;

    private EmailScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new EmailScheduler(mailSender, reservaRepo, usuarioRepo, pistaRepo);
        ReflectionTestUtils.setField(scheduler, "mailEnabled", false);
    }

    @Test
    void recordarReservasDelDia_mailDesactivado_noLlamaAlSender() {
        Pista pista = new Pista();
        pista.setIdPista(1L);
        pista.setNombre("Pista 1");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@test.com");

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setDuracionMinutos(60);

        when(pistaRepo.findAll()).thenReturn(List.of(pista));
        when(reservaRepo.findByPista_IdPistaAndFechaReservaAndEstado(
                eq(1L), any(LocalDate.class), eq(EstadoReserva.ACTIVA)))
                .thenReturn(List.of(reserva));

        scheduler.recordarReservasDelDia();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void enviarDisponibilidadMensual_mailDesactivado_noLlamaAlSender() {
        Pista pista = new Pista();
        pista.setNombre("Pista 1");
        pista.setActiva(true);
        pista.setPrecioHora(10.0);

        Usuario usuario = new Usuario();
        usuario.setEmail("user@test.com");

        when(pistaRepo.findAll()).thenReturn(List.of(pista));
        when(usuarioRepo.findAll()).thenReturn(List.of(usuario));

        scheduler.enviarDisponibilidadMensual();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void recordarReservasDelDia_mailActivado_llamaAlSenderUnaVezPorReserva() {
        ReflectionTestUtils.setField(scheduler, "mailEnabled", true);

        Pista pista = new Pista();
        pista.setIdPista(1L);
        pista.setNombre("Pista 1");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@test.com");

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setDuracionMinutos(60);

        when(pistaRepo.findAll()).thenReturn(List.of(pista));
        when(reservaRepo.findByPista_IdPistaAndFechaReservaAndEstado(
                eq(1L), any(LocalDate.class), eq(EstadoReserva.ACTIVA)))
                .thenReturn(List.of(reserva));

        scheduler.recordarReservasDelDia();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void enviarDisponibilidadMensual_mailActivado_enviaUnEmailPorUsuario() {
        ReflectionTestUtils.setField(scheduler, "mailEnabled", true);

        Pista pista = new Pista();
        pista.setNombre("Pista 1");
        pista.setActiva(true);
        pista.setPrecioHora(10.0);

        Usuario u1 = new Usuario();
        u1.setEmail("uno@test.com");
        Usuario u2 = new Usuario();
        u2.setEmail("dos@test.com");

        when(pistaRepo.findAll()).thenReturn(List.of(pista));
        when(usuarioRepo.findAll()).thenReturn(List.of(u1, u2));

        scheduler.enviarDisponibilidadMensual();

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}
