package edu.comillas.icai.gitt.pat.spring.pista_padel_backend.controller;

import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.MeResponse;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.dto.UserUpdateRequest;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Rol;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.Usuario;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.modelo.UsuarioRepositorio;
import edu.comillas.icai.gitt.pat.spring.pista_padel_backend.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/pistaPadel/users")
public class UsersController {

    private static final Logger log = LoggerFactory.getLogger(UsersController.class);

    private final UsuarioService usuarioService;
    private final UsuarioRepositorio usuarioRepo;

    public UsersController(UsuarioService usuarioService, UsuarioRepositorio usuarioRepo) {
        this.usuarioService = usuarioService;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public List<MeResponse> list(Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN) {
            log.warn("Acceso denegado a listado de usuarios para email={}", me.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
        log.debug("Admin listando todos los usuarios");
        return usuarioService.listAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{userId}")
    public MeResponse get(@PathVariable Long userId, Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN && !me.getIdUsuario().equals(userId)) {
            log.warn("Acceso denegado a usuario id={} para email={}", userId, me.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
        log.debug("Obteniendo usuario id={}", userId);
        return toResponse(usuarioService.getById(userId));
    }

    @PatchMapping("/{userId}")
    public MeResponse patch(@PathVariable Long userId,
                            @Valid @RequestBody UserUpdateRequest req,
                            Authentication auth) {
        Usuario me = me(auth);
        if (me.getRol() != Rol.ADMIN && !me.getIdUsuario().equals(userId)) {
            log.warn("Acceso denegado a modificar usuario id={} para email={}", userId, me.getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
        log.info("Actualizando usuario id={}", userId);
        return toResponse(usuarioService.patchUser(userId, req));
    }

    private Usuario me(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return usuarioRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
    }

    private MeResponse toResponse(Usuario u) {
        return new MeResponse(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellidos(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol(),
                u.isActivo()
        );
    }
}
