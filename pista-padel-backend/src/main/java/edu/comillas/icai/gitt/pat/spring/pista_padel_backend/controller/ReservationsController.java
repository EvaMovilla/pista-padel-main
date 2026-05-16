package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.controller;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.NotFoundException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.ReservaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.*;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.ReservaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pistaPadel")
public class ReservationsController {

    private static final Logger log = LoggerFactory.getLogger(ReservationsController.class);

    private final ReservaService reservaService;
    private final UsuarioRepositorio usuarioRepo;

    public ReservationsController(ReservaService reservaService, UsuarioRepositorio usuarioRepo) {
        this.reservaService = reservaService;
        this.usuarioRepo = usuarioRepo;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Reserva> createReservation(@RequestBody ReservaRequest request, Authentication auth) {
        Usuario actual = me(auth);
        log.info("Creando reserva para usuario={}, pista={}, fecha={}", actual.getEmail(), request.getCourtId(), request.getDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(request, actual));
    }

    @GetMapping("/reservations")
    public List<Reserva> getMyReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        Usuario actual = me(auth);
        log.debug("Listando reservas de usuario={}, from={}, to={}", actual.getEmail(), from, to);
        return reservaService.listarMisReservasFiltradas(actual, from, to);
    }

    @GetMapping("/reservations/{reservationId}")
    public Reserva get(@PathVariable Long reservationId, Authentication auth) {
        Usuario me = me(auth);
        log.debug("Obteniendo reserva id={} para usuario={}", reservationId, me.getEmail());
        Reserva r = reservaService.get(reservationId);

        boolean admin = me.getRol() == Rol.ADMIN;
        boolean owner = r.getUsuario().getIdUsuario().equals(me.getIdUsuario());
        if (!admin && !owner) {
            log.warn("Acceso denegado a reserva id={} para usuario={}", reservationId, me.getEmail());
            throw new AccessDeniedException("Forbidden");
        }
        return r;
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, Authentication auth) {
        Usuario actual = me(auth);
        log.info("Cancelando reserva id={} por usuario={}", id, actual.getEmail());
        reservaService.cancelarReserva(id, actual);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{reservationId}")
    public Reserva patch(@PathVariable Long reservationId, @Valid @RequestBody ReservaUpdateRequest req, Authentication auth) {
        Usuario me = me(auth);
        log.info("Reprogramando reserva id={} por usuario={}", reservationId, me.getEmail());
        return reservaService.reprogramar(reservationId, req, me);
    }

    @GetMapping("/admin/reservations")
    public List<Reserva> adminReservations(
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "courtId", required = false) Long courtId,
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication auth
    ) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN) {
            log.warn("Acceso denegado a /admin/reservations para usuario={}", me.getEmail());
            throw new AccessDeniedException("Forbidden");
        }
        log.debug("Admin consultando reservas: fecha={}, courtId={}, userId={}", date, courtId, userId);
        return reservaService.adminReservas(date, courtId, userId);
    }

    private Usuario me(Authentication auth) {
        return usuarioRepo.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new NotFoundException("Usuario no existe"));
    }
}
