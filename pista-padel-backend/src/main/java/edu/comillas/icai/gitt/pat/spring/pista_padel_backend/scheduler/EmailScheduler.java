package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.scheduler;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

    private final JavaMailSender mailSender;
    private final ReservaRepositorio reservaRepo;
    private final UsuarioRepositorio usuarioRepo;
    private final PistaRepositorio pistaRepo;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    public EmailScheduler(JavaMailSender mailSender,
                          ReservaRepositorio reservaRepo,
                          UsuarioRepositorio usuarioRepo,
                          PistaRepositorio pistaRepo) {
        this.mailSender = mailSender;
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.pistaRepo = pistaRepo;
    }

    // Todos los días a las 02:00
    @Scheduled(cron = "0 0 2 * * *")
    public void recordarReservasDelDia() {
        LocalDate hoy = LocalDate.now();
        List<Pista> pistas = pistaRepo.findAll();
        int enviados = 0;

        for (Pista pista : pistas) {
            List<Reserva> reservas = reservaRepo.findByPista_IdPistaAndFechaReservaAndEstado(
                    pista.getIdPista(), hoy, EstadoReserva.ACTIVA);

            for (Reserva r : reservas) {
                String subject = "Recordatorio reserva pádel - " + hoy;
                String text = "Tienes reserva en " + pista.getNombre()
                        + " a las " + r.getHoraInicio()
                        + " (duración " + r.getDuracionMinutos() + " min).";
                send(r.getUsuario().getEmail(), subject, text);
                enviados++;
            }
        }

        log.info("Recordatorios enviados: {}", enviados);
    }

    // Día 1 de cada mes a las 02:00
    @Scheduled(cron = "0 0 2 1 * *")
    public void enviarDisponibilidadMensual() {
        List<Usuario> usuarios = usuarioRepo.findAll();
        List<Pista> pistas = pistaRepo.findAll();

        String subject = "Disponibilidad pistas (mensual)";
        StringBuilder body = new StringBuilder("Pistas:\n");
        for (Pista p : pistas) {
            body.append("- ").append(p.getNombre())
                    .append(" (activa=").append(p.isActiva())
                    .append(", precio/h=").append(p.getPrecioHora())
                    .append(")\n");
        }

        for (Usuario u : usuarios) {
            send(u.getEmail(), subject, body.toString());
        }

        log.info("Email mensual enviado a {} usuarios", usuarios.size());
    }

    private void send(String to, String subject, String text) {
        if (!mailEnabled) {
            log.info("[MAIL MOCK] To={} | Subject={}", to, subject);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("No se pudo enviar email a {}: {}", to, e.getMessage());
        }
    }
}
