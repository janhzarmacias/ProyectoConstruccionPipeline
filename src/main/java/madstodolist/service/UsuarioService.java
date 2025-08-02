package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public boolean existeAdministrador() {
        return usuarioRepository.existsByAdministradorTrue();
    }

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");

        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");

        if (usuario.isAdministrador()) {
            if (usuarioRepository.existsByAdministradorTrue()) {
                throw new UsuarioServiceException("Ya existe un usuario administrador");
            }
        }

        Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
        // Forzamos el valor administrador
        usuarioNuevo.setAdministrador(usuario.isAdministrador());

        usuarioNuevo = usuarioRepository.save(usuarioNuevo);

        return modelMapper.map(usuarioNuevo, UsuarioData.class);
    }


    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;

        UsuarioData usuarioData = modelMapper.map(usuario, UsuarioData.class);

        return usuarioData;
    }
    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    public List<UsuarioData> allUsuarios() {
        List<Usuario> usuarios = (List<Usuario>) usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> {
                    UsuarioData dto = modelMapper.map(usuario, UsuarioData.class);
                    dto.setBloqueado(usuario.isBloqueado());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void bloquearUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        usuario.setBloqueado(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void habilitarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        usuario.setBloqueado(false);
        usuarioRepository.save(usuario);
    }



}
