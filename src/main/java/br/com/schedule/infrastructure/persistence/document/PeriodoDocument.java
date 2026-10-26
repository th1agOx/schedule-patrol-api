package br.com.schedule.infrastructure.persistence.document;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;


public class PeriodoDocument {

    @Field("inicio")
    private Instant inicio;

    @Field("fim")
    private Instant fim;


    public PeriodoDocument() {
    }

    public PeriodoDocument(Instant inicio, Instant fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getFim() {
        return fim;
    }
}
