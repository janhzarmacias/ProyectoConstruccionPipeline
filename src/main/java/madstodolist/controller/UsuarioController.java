package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.repository.UsuarioRepository;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/registrados")
    public String listarUsuarios(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            return "redirect:/login";
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null || !usuario.isAdministrador()) {
            return "redirect:/";  // o página 403
        }

        List<UsuarioData> usuarios = usuarioService.allUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioLogeado", true);

        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String verDetalleUsuario(@PathVariable Long id, Model model, Principal principal) {
        UsuarioData usuarioData = usuarioService.findById(id);

        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            return "redirect:/login";
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null || !usuario.isAdministrador()) {
            return "redirect:/";  // o página 403
        }

        if (usuarioData == null) {
            return "redirect:/registrados"; // o puedes redirigir a una página de error
        }

        model.addAttribute("usuarioData", usuarioData);

        usuario = null;

        boolean usuarioLogeado = false;

        if (principal != null) {
            usuario = usuarioService.findByEmail(principal.getName());
            usuarioLogeado = usuario != null;
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioLogeado", usuarioLogeado);


        return "UsuarioDetalles";
    }

    @GetMapping("/usuarios/bloquear/{id}")
    public String bloquearUsuario(@PathVariable Long id, Principal principal) {
        // Validar que el usuario actual es admin
        // Lógica para bloquear usuario (set bloqueado = true)
        usuarioService.bloquearUsuario(id);
        return "redirect:/registrados";
    }

    @GetMapping("/usuarios/habilitar/{id}")
    public String habilitarUsuario(@PathVariable Long id, Principal principal) {
        // Validar que el usuario actual es admin
        usuarioService.habilitarUsuario(id);
        return "redirect:/registrados";
    }

}
