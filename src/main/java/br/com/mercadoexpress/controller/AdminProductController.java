package br.com.mercadoexpress.controller;

import br.com.mercadoexpress.model.Product;
import br.com.mercadoexpress.repository.ProductRepository;
import br.com.mercadoexpress.web.ProductForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        model.addAttribute("activeCount", productRepository.countByActiveTrue());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("pageTitle", "Novo produto");
        model.addAttribute("formAction", "/admin/products");
        return "admin/product-form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Novo produto");
            model.addAttribute("formAction", "/admin/products");
            return "admin/product-form";
        }
        productRepository.save(toProduct(productForm, new Product()));
        redirectAttributes.addFlashAttribute("successMessage", "Produto cadastrado com sucesso.");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id).orElseThrow();
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setCategory(product.getCategory());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setImageUrl(product.getImageUrl());
        form.setActive(product.isActive());
        model.addAttribute("productForm", form);
        model.addAttribute("pageTitle", "Editar produto");
        model.addAttribute("formAction", "/admin/products/" + id);
        return "admin/product-form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductForm productForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editar produto");
            model.addAttribute("formAction", "/admin/products/" + id);
            return "admin/product-form";
        }
        Product product = productRepository.findById(id).orElseThrow();
        productRepository.save(toProduct(productForm, product));
        redirectAttributes.addFlashAttribute("successMessage", "Alterações salvas.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Produto removido.");
        return "redirect:/admin/products";
    }

    private Product toProduct(ProductForm form, Product product) {
        product.setName(form.getName().trim());
        product.setCategory(form.getCategory().trim());
        product.setDescription(form.getDescription().trim());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setImageUrl(form.getImageUrl() == null ? "" : form.getImageUrl().trim());
        product.setActive(form.isActive());
        return product;
    }
}
