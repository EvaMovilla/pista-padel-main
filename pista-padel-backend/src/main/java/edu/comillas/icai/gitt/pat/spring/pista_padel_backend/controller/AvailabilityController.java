package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.controller;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.Excepciones.NotFoundException;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.EstadoReserva;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.PistaRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.ReservaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pistaPadel")
public class AvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityController.class);

    private final PistaRepositorio pistaRepo;
    private final ReservaRepositorio reservaRepo;

    public AvailabilityController(PistaRepositorio pistaRepo, ReservaRepositorio reservaRepo) {
        this.pistaRepo = pistaRepo;
        this.reservaRepo = reservaRepo;
    }

    @GetMapping("/availability")
    public Map<String, Object> availability(
            @RequestParam("date") LocalDate date,
            @RequestParam(value = "courtId", required = false) Long courtId
    ) {
        log.debug("Consulta disponibilidad: fecha={}, courtId={}", date, courtId);

        List<Pista> pistas = (courtId == null)
                ? pistaRepo.findAll()
                : List.of(pistaRepo.findById(courtId)
                        .orElseThrow(() -> new NotFoundException("Pista no encontrada")));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("date", date);

        var courts = pistas.stream().map(p -> Map.of(
                "courtId", p.getIdPista(),
                "nombre", p.getNombre(),
                "activa", p.isActiva(),
                "reservas", reservaRepo
                        .findByPista_IdPistaAndFechaReservaAndEstado(p.getIdPista(), date, EstadoReserva.ACTIVA)
                        .stream()
                        .map(r -> Map.of("inicio", r.getHoraInicio(), "fin", r.getHoraFin()))
                        .toList()
        )).toList();

        res.put("courts", courts);
        log.debug("Disponibilidad calculada para {} pistas en fecha={}", courts.size(), date);
        return res;
    }

    @GetMapping("/courts/{courtId}/availability")
    public Map<String, Object> availabilityCourt(
            @PathVariable Long courtId,
            @RequestParam("date") LocalDate date
    ) {
        return availability(date, courtId);
    }
}
