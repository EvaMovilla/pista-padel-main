package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.controller;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.PistaUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Pista;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.PistaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pistaPadel/courts")
public class CourtsController {

    private static final Logger log = LoggerFactory.getLogger(CourtsController.class);

    private final PistaService pistaService;
    private final UsuarioRepositorio usuarioRepo;

    public CourtsController(PistaService pistaService, UsuarioRepositorio usuarioRepo) {
        this.pistaService = pistaService;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<Pista> list() {
        log.debug("Listando todas las pistas");
        return pistaService.list();
    }

    @GetMapping("/{courtId}")
    public Pista get(@PathVariable Long courtId) {
        log.debug("Obteniendo pista id={}", courtId);
        return pistaService.get(courtId);
    }

    @PostMapping
    public ResponseEntity<Pista> createCourt(@RequestBody PistaRequest request, Authentication auth) {
        requireAdmin(auth);
        log.info("Creando pista nombre={}", request.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(pistaService.crearPista(request));
    }

    @PatchMapping("/{courtId}")
    public Pista patch(@PathVariable Long courtId, @Valid @RequestBody PistaUpdateRequest req, Authentication auth) {
        requireAdmin(auth);
        log.info("Actualizando pista id={}", courtId);
        return pistaService.patch(courtId, req);
    }

    @DeleteMapping("/{courtId}")
    public ResponseEntity<Void> delete(@PathVariable Long courtId, Authentication auth) {
        requireAdmin(auth);
        log.info("Eliminando/desactivando pista id={}", courtId);
        pistaService.delete(courtId);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(Authentication auth) {
        Usuario me = usuarioRepo.findByEmailIgnoreCase(auth.getName()).orElseThrow();
        if (me.getRol() != Rol.ADMIN) {
            log.warn("Acceso denegado a operación de admin para usuario={}", auth.getName());
            throw new AccessDeniedException("Forbidden");
        }
    }
}
