package com.concursos.api_concursos.config;

import com.concursos.api_concursos.enums.UserRole;
import com.concursos.api_concursos.model.Usuario;
import com.concursos.api_concursos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Import necessário
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeedConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Injeta os valores definidos nas propriedades ou adota fallbacks seguros
    @Value("${ADMIN_EMAIL:admin@concursos.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:Admin}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            log.info("Banco de dados vazio. Gerando primeiro usuário administrador padrão...");
            
            Usuario admin = new Usuario();
            admin.setNome("Administrador Core");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.ADMIN);
            
            usuarioRepository.save(admin);
            log.info("Usuário inicial criado com sucesso utilizando variáveis de ambiente.");
        }
    }
}