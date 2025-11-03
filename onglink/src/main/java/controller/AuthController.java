package controller;

import model.AuthModel;

public class AuthController {
    private final AuthModel model;

    public AuthController() {
        this.model = new AuthModel();
    }

    public boolean autenticar(String email, String senha) {
        return model.autenticar(email, senha);
    }

    /**
     * Permite o acesso ao sistema somente se o status for ADMIN.
     */
    public boolean isAdmin(String email) {
        return model.isAdmin(email);
    }
    
    /**
     * Retorna o status do usuário (ADMIN, USER, ONG).
     */
    public String getStatus(String email) {
        return model.getStatus(email);
    }
}