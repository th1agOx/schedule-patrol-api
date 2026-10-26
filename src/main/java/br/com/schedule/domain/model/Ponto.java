package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.PontoInvalidoException;

import java.util.Locale;


public record Ponto(String codigo, String descricao) {

    public Ponto {
        codigo = requireText(codigo, "código");
        descricao = requireText(descricao, "descrição");
    }

    String codigoNormalizado() {
        return codigo.toLowerCase(Locale.ROOT);
    }

    private static String requireText(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new PontoInvalidoException("O %s do ponto é obrigatório e não pode ser vazio.".formatted(campo));
        }
        return valor.trim();
    }
}
