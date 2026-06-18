package com.concursos.api_concursos.controller;

import com.concursos.api_concursos.dto.LoginRequestDTO;
import com.concursos.api_concursos.dto.LoginResponseDTO;
import com.concursos.api_concursos.model.Usuario;
import com.concursos.api_concursos.repository.UsuarioRepository;
import com.concursos.api_concursos.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoint para login e geração de tokens de acesso")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login para obter o Token JWT")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos."));

        // Compara a senha digitada com o hash criptografado do banco
        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new RuntimeException("Usuário ou senha inválidos.");
        }

        String token = tokenService.gerarToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}