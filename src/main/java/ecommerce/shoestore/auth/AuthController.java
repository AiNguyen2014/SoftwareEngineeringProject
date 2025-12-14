package ecommerce.shoestore.auth;

import ecommerce.shoestore.auth.dto.*;
import ecommerce.shoestore.auth.user.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ================= LOGIN =================

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute LoginRequest request,
            HttpSession session,
            Model model
    ) {
        try {
            User user = authService.login(request);

            // ====== LƯU SESSION (DÙNG CHO VIEW) ======
            session.setAttribute("USER_ID", user.getUserId());
            session.setAttribute("FULLNAME", user.getFullname());
            session.setAttribute("ROLE", user.getAccount().getRole());
            session.setAttribute("AVATAR", user.getAvatar());

            // ====== TẠO AUTH TOKEN ======
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            AuthorityUtils.createAuthorityList(
                                    "ROLE_" + user.getAccount().getRole().name()
                            )
                    );

            // ====== 🔥 LƯU SECURITY CONTEXT VÀO SESSION (QUYẾT ĐỊNH 403) ======
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(token);
            SecurityContextHolder.setContext(context);

            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            // ====== REDIRECT THEO ROLE ======
            if (user.getAccount().getRole().name().equals("ADMIN")) {
                return "redirect:/admin";
            } else {
                return "redirect:/user/shoes";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    // ================= LOGOUT =================

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/auth/login?logout";
    }
}
