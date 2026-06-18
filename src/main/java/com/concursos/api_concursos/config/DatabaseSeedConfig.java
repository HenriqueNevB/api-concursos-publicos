package com.concursos.api_concursos.config;

import com.concursos.api_concursos.enums.UserRole;
import com.concursos.api_concursos.model.Usuario;
import com.concursos.api_concursos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeedConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            log.info("Banco de dados vazio. Gerando primeiro usuário administrador padrão...");
            
            Usuario admin = new Usuario();
            admin.setNome("Administrador Core");
            admin.setEmail("admin@concursos.com");
            admin.setSenha(passwordEncoder.encode("Admin")); // Senha inicial
            admin.setRole(UserRole.ADMIN);
            
            usuarioRepository.save(admin);
            log.info("Usuário inicial criado com sucesso: admin@concursos.com / Admin");
        }
    }
}