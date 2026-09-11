package com.abarrotes.sistema.usuario.infrastructure.rest;

import com.abarrotes.sistema.usuario.application.port.GestionarUsuarioUseCase;
import com.abarrotes.sistema.usuario.domain.model.Usuario;
import com.abarrotes.sistema.usuario.domain.model.Rol;

// --- IMPORTAMOS LOS MODELOS DEL CLIENTE PARA USAR DECOLECTA ---
import com.abarrotes.sistema.cliente.domain.port.ConsultaDocumentoPort;
import com.abarrotes.sistema.cliente.domain.model.Cliente;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
// NUEVO: Importamos el PasswordEncoder para encriptar la clave al editar
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final GestionarUsuarioUseCase gestionarUsuarioUseCase;
    private final JdbcTemplate jdbcTemplate; 
    
    // --- INYECTAMOS TU PUERTO DE DECOLECTA ---
    private final ConsultaDocumentoPort consultaDocumentoPort;
    
    // --- INYECTAMOS EL ENCRIPTADOR DE CONTRASEÑAS ---
    private final PasswordEncoder passwordEncoder;

    // DTO ampliado para recibir todos los datos del personal
    @Data
    public static class CrearUsuarioRequest {
        private String dni;
        private String nombre;
        private String apellidos;
        private String username;
        private String password; // Puede venir nulo desde el frontend
        private String email;
        private String telefono;
        private String rol;
        private String estado;
    }

    // --- NUEVO ENDPOINT: CONSULTAR DNI EN RENIEC DESDE EL BACKEND ---
    @GetMapping("/dni/{dni}")
    public ResponseEntity<?> consultarReniec(@PathVariable String dni) {
        // Usamos tu adaptador Decolecta que ya tienes configurado
        Optional<Cliente> resultado = consultaDocumentoPort.consultarDni(dni);
        
        if (resultado.isPresent()) {
            // Tu adaptador devuelve el nombre completo en el campo 'razonSocial'
            return ResponseEntity.ok(Map.of("nombreCompleto", resultado.get().getRazonSocial()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "DNI no encontrado en RENIEC"));
        }
    }

    // 1. LISTAR TODOS LOS TRABAJADORES (Para llenar la tabla en React)
    @GetMapping
    public List<Map<String, Object>> listarUsuarios() {
        return jdbcTemplate.queryForList("SELECT id, dni, nombre, apellidos, username, email, telefono, rol, estado FROM usuarios ORDER BY id DESC");
    }

    // 2. CREAR USUARIO 
    @PostMapping
    public ResponseEntity<String> crearUsuario(@RequestBody CrearUsuarioRequest request) {
        try {
            // Si el frontend no manda password, usamos el DNI por defecto
            String pwd = (request.getPassword() == null || request.getPassword().trim().isEmpty()) 
                         ? request.getDni() : request.getPassword().trim();

            String nombreRol = request.getRol() != null ? request.getRol() : "TRABAJADOR";
            
            // Llamamos al caso de uso pasándole el Enum convertido (Este caso de uso ya encripta por defecto)
            Usuario nuevoUsuario = gestionarUsuarioUseCase.crearUsuario(
                    request.getUsername(), 
                    pwd, 
                    Rol.valueOf(nombreRol) 
            );
            
            // ACTUALIZACIÓN INMEDIATA para guardar los datos personales extra
            jdbcTemplate.update("UPDATE usuarios SET dni = ?, nombre = ?, apellidos = ?, email = ?, telefono = ?, estado = 'ACTIVO' WHERE id = ?",
                request.getDni(), request.getNombre(), request.getApellidos(), request.getEmail(), request.getTelefono(), nuevoUsuario.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario creado exitosamente con ID: " + nuevoUsuario.getId());
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    // 3. EDITAR USUARIO 
    @PutMapping("/{id}")
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @RequestBody CrearUsuarioRequest request) {
        try {
            // Evaluamos si el administrador envió una nueva contraseña
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                // Si la envió, la ENCRIPTAMOS primero antes de guardarla a la BD
                String passwordEncriptada = passwordEncoder.encode(request.getPassword().trim());
                
                String sql = "UPDATE usuarios SET dni = ?, nombre = ?, apellidos = ?, username = ?, email = ?, telefono = ?, rol = ?, estado = ?, password = ? WHERE id = ?";
                jdbcTemplate.update(sql, 
                    request.getDni(), request.getNombre(), request.getApellidos(), request.getUsername(), 
                    request.getEmail(), request.getTelefono(), request.getRol(), 
                    request.getEstado() != null ? request.getEstado() : "ACTIVO",
                    passwordEncriptada, id
                );
            } else {
                // Si NO envió contraseña (campo vacío), actualizamos todo MENOS la contraseña
                String sql = "UPDATE usuarios SET dni = ?, nombre = ?, apellidos = ?, username = ?, email = ?, telefono = ?, rol = ?, estado = ? WHERE id = ?";
                jdbcTemplate.update(sql, 
                    request.getDni(), request.getNombre(), request.getApellidos(), request.getUsername(), 
                    request.getEmail(), request.getTelefono(), request.getRol(), 
                    request.getEstado() != null ? request.getEstado() : "ACTIVO",
                    id
                );
            }
            
            return ResponseEntity.ok(Map.of("mensaje", "Personal actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Error al actualizar: " + e.getMessage()));
        }
    }

    // 4. ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            jdbcTemplate.update("DELETE FROM usuarios WHERE id = ?", id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Error al eliminar. Verifique que no tenga viajes asignados."));
        }
    }
}