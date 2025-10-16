package controller;

import model.AuthModel;

public class AuthController {
    private final AuthModel model;

    public AuthController() {
        this.model = new AuthModel();
    }

    public boolean autenticar(String usuario, String senha) {
        return model.autenticar(usuario, senha);
    }

    /**
     * Permite o acesso ao sistema somente se o perfil for ADMIN.
     */
    public boolean isAdmin(String usuario) {
        return model.isAdmin(usuario);
    }
    
    public String getPerfil(String usuario) {
        return model.getPerfil(usuario);
    }
}