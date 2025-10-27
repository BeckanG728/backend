package com.tpdteam3.backend.controller;


import com.tpdteam3.backend.dto.MessageLogin;
import com.tpdteam3.backend.dto.RequestLogin;
import com.tpdteam3.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public MessageLogin login(@RequestBody RequestLogin request) {
        boolean u = usuarioService.validarUsuario(request.getLogin(), request.getPassword());
        String[] message = {u ? "Ok" : "Error", u ? "Credenciales correctas" : "Credenciales invalidas"};
        return new MessageLogin(message[0], message[1]);
    }


}
