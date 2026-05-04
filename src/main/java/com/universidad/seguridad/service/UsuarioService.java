package com.universidad.seguridad.service;

import com.universidad.seguridad.model.Usuario;
import com.universidad.seguridad.repository.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public void registrar(Usuario usuario) {
        if (repo.existsByEmail(usuario.getEmail()))
            throw new RuntimeException("El correo ya está registrado");

        usuario.setContrasenia(encoder.encode(usuario.getContrasenia()));
        usuario.setRol("ROLE_USER");
        repo.save(usuario);
    }

    // Solo ADMIN puede listar todos los usuarios
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    // ADMIN o el propio usuario pueden buscar por email
    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public Optional<Usuario> buscarPorEmail(String email) {
        return repo.findByEmail(email);
    }

    // Solo ADMIN puede cambiar roles
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cambiarRol(Long id, String nuevoRol) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setRol(nuevoRol);
    }

    // Un usuario solo puede actualizar sus propios datos o ADMIN
    @PreAuthorize("#usuario.email == authentication.name or hasRole('ADMIN')")
    @Transactional
    public void actualizarNombre(Usuario usuario) {
        Usuario existente = repo.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        existente.setNombre(usuario.getNombre());
    }
}