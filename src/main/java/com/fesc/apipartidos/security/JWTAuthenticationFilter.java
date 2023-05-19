package com.fesc.apipartidos.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesc.apipartidos.services.IUsuarioService;
import com.fesc.apipartidos.shared.UsuarioDto;
import com.fesc.apipartidos.utils.AppContexto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        UsuarioSignupRequestModel usuarioSignupRequestModel = new UsuarioSignupRequestModel();

        try {
            usuarioSignupRequestModel = new ObjectMapper().readValue(request.getInputStream(),
                    UsuarioSignupRequestModel.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(
                usuarioSignupRequestModel.getUsername(),
                usuarioSignupRequestModel.getPassword(),
                Collections.emptyList());

        return getAuthenticationManager().authenticate(upat);

    }

    @Autowired
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        UserDetailsImpl userDatails = (UserDetailsImpl) authResult.getPrincipal();

        String token = TokenUtils.createToken(userDatails.getNombre(), userDatails.getUsername());

        IUsuarioService iUsuarioService = (IUsuarioService) AppContexto.getBean("usuarioService");
        UsuarioDto usuarioDto = iUsuarioService.leerUsuario(userDatails.getUsername());

        response.addHeader("Access-Control-Expose-Headers", "Authorization, idUsuario");
        response.addHeader("idUsuario", usuarioDto.getIdUsuario());
        response.addHeader(ConstantesSecurity.HEADER_STRING, ConstantesSecurity.TOKEN_PREFIJO + token);
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, authResult);

    }

}
