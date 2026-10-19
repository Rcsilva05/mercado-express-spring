package br.com.mercadoexpress.controller;

import br.com.mercadoexpress.repository.ProductRepository;
import br.com.mercadoexpress.service.AccountService;
import br.com.mercadoexpress.web.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final AccountService accountService;

    public HomeController(ProductRepository productRepository, AccountService accountService) {
        this.productRepository = productRepository;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productRepository.findByActiveTrueOrderByNameAsc().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String register(
            @Valid @ModelAttribute SignupForm signupForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
            if (bindingResult.hasErrors()) {
                return "signup";
            }
            try {
                accountService.registerCustomer(signupForm);
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("signupForm", new SignupForm());
                redirectAttributes.addFlashAttribute("signupError", ex.getMessage());
                return "redirect:/signup";
        }
        redirectAttributes.addFlashAttribute("registered", true);
        return "redirect:/login";
    }
}
