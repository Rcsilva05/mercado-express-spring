package br.com.mercadoexpress.controller;

import br.com.mercadoexpress.model.Product;
import br.com.mercadoexpress.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkspaceController {

    private final ProductRepository productRepository;

    public WorkspaceController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/app")
    public String workspace(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin/products" : "redirect:/catalog";
    }

    @GetMapping("/catalog")
    public String catalog(Model model) {
        model.addAttribute("products", productRepository.findByActiveTrueOrderByNameAsc());
        return "catalog";
    }
}
