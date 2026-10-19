package br.com.mercadoexpress.service;

import br.com.mercadoexpress.model.AppUser;
import br.com.mercadoexpress.model.UserRole;
import br.com.mercadoexpress.repository.AppUserRepository;
import br.com.mercadoexpress.web.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerCustomer(SignupForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Este e-mail já possui cadastro.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas informadas não coincidem.");
        }

        AppUser user = new AppUser();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserRole.CLIENTE);
        userRepository.save(user);
    }
}
