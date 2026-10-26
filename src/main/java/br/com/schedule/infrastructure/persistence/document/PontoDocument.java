package br.com.schedule.infrastructure.persistence.document;

import org.springframework.data.mongodb.core.mapping.Field;

public class PontoDocument {

    @Field("codigo")
    private String codigo;

    @Field("descricao")
    private String descricao;


    public PontoDocument() {
    }

    public PontoDocument(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}
