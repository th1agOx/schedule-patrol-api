package br.com.schedule.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "agendamentos")
public class AgendamentoRondaDocument {

    @Id
    private String id;

    @Field("idRonda")
    private String idRonda;

    @Field("responsavel")
    private String responsavel;

    @Field("periodoPrevisto")
    private PeriodoDocument periodoPrevisto;

    @Field("status")
    private String status;

    @Field("motivoCancelamento")
    private String motivoCancelamento;

    public AgendamentoRondaDocument() {
    }

    public AgendamentoRondaDocument(String id, String idRonda, String responsavel,
            PeriodoDocument periodoPrevisto, String status, String motivoCancelamento) {
        this.id = id;
        this.idRonda = idRonda;
        this.responsavel = responsavel;
        this.periodoPrevisto = periodoPrevisto;
        this.status = status;
        this.motivoCancelamento = motivoCancelamento;
    }

    public String getId() {
        return id;
    }

    public String getIdRonda() {
        return idRonda;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public PeriodoDocument getPeriodoPrevisto() {
        return periodoPrevisto;
    }

    public String getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }
}
