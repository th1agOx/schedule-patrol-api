package br.com.schedule.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Document(collection = "rondas")
public class RondaDocument {

    @Id
    private String id;

    @Field("nome")
    private String nome;

    @Field("descricao")
    private String descricao;

    @Field("ativa")
    private boolean ativa;

    @Field("pontos")
    private List<PontoDocument> pontos;


    public RondaDocument() {
    }

    public RondaDocument(String id, String nome, String descricao, boolean ativa, List<PontoDocument> pontos) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativa = ativa;
        this.pontos = pontos;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public List<PontoDocument> getPontos() {
        return pontos;
    }
}
