package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.domain.exception.AgendamentoInvalidoException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.document.PeriodoDocument;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoRondaMongoRepositoryTest {

    private static final Instant INICIO = Instant.parse("2026-11-05T10:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-05T12:00:00Z");
    private static final Periodo PERIODO = new Periodo(INICIO, FIM);

    @Mock
    private MongoTemplate mongoTemplate;

    private AgendamentoRondaMongoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AgendamentoRondaMongoRepository(mongoTemplate);
    }

    @Test
    void shouldSaveAndMapSavedDocument() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", AGENDADA);
        when(mongoTemplate.save(any(AgendamentoRondaDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRonda salvo = repository.salvar(agendamento);

        assertThat(salvo.getId()).isEqualTo("agendamento-1");
        assertThat(salvo.getIdRonda()).isEqualTo("ronda-1");
        assertThat(salvo.getResponsavel()).isEqualTo("Maria");
        assertThat(salvo.getPeriodoPrevisto()).isEqualTo(PERIODO);
        assertThat(salvo.getStatus()).isEqualTo(AGENDADA);
        assertThat(salvo.getMotivoCancelamento()).isEmpty();

        ArgumentCaptor<AgendamentoRondaDocument> captor =
                ArgumentCaptor.forClass(AgendamentoRondaDocument.class);
        verify(mongoTemplate).save(captor.capture());
        AgendamentoRondaDocument document = captor.getValue();
        assertThat(document.getId()).isEqualTo("agendamento-1");
        assertThat(document.getIdRonda()).isEqualTo("ronda-1");
        assertThat(document.getResponsavel()).isEqualTo("Maria");
        assertThat(document.getPeriodoPrevisto().getInicio()).isEqualTo(INICIO);
        assertThat(document.getPeriodoPrevisto().getFim()).isEqualTo(FIM);
        assertThat(document.getStatus()).isEqualTo("AGENDADA");
        assertThat(document.getMotivoCancelamento()).isNull();
    }

    @Test
    void shouldPersistCancellationReason() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", CANCELADA);
        when(mongoTemplate.save(any(AgendamentoRondaDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRonda salvo = repository.salvar(agendamento);

        assertThat(salvo.getStatus()).isEqualTo(CANCELADA);
        assertThat(salvo.getMotivoCancelamento()).contains("Sem equipe");

        ArgumentCaptor<AgendamentoRondaDocument> captor =
                ArgumentCaptor.forClass(AgendamentoRondaDocument.class);
        verify(mongoTemplate).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("CANCELADA");
        assertThat(captor.getValue().getMotivoCancelamento()).isEqualTo("Sem equipe");
    }

    @Test
    void shouldRoundTripAllStatuses() {
        when(mongoTemplate.save(any(AgendamentoRondaDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        for (StatusAgendamento status : StatusAgendamento.values()) {
            AgendamentoRonda original = agendamento("agendamento-1", status);

            AgendamentoRonda salvo = repository.salvar(original);

            assertThat(salvo.getStatus()).as("status %s", status).isEqualTo(status);
            assertThat(salvo.getMotivoCancelamento()).isEqualTo(original.getMotivoCancelamento());
        }
    }

    @Test
    void shouldFindById() {
        when(mongoTemplate.findById("agendamento-1", AgendamentoRondaDocument.class))
                .thenReturn(document("agendamento-1", "Maria", EM_ANDAMENTO));

        AgendamentoRonda agendamento = repository.buscarPorId("agendamento-1").orElseThrow();

        assertThat(agendamento.getId()).isEqualTo("agendamento-1");
        assertThat(agendamento.getIdRonda()).isEqualTo("ronda-1");
        assertThat(agendamento.getResponsavel()).isEqualTo("Maria");
        assertThat(agendamento.getPeriodoPrevisto()).isEqualTo(PERIODO);
        assertThat(agendamento.getStatus()).isEqualTo(EM_ANDAMENTO);
        assertThat(agendamento.getMotivoCancelamento()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(repository.buscarPorId("inexistente")).isEmpty();

        verify(mongoTemplate).findById("inexistente", AgendamentoRondaDocument.class);
    }

    @Test
    void shouldRejectUnknownStatus() {
        when(mongoTemplate.findById("agendamento-1", AgendamentoRondaDocument.class))
                .thenReturn(new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                        new PeriodoDocument(INICIO, FIM), "PAUSADA", null));

        assertThatThrownBy(() -> repository.buscarPorId("agendamento-1"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAUSADA")
                .hasMessageContaining("agendamento-1");
    }

    @Test
    void shouldRejectMissingStatus() {
        when(mongoTemplate.findById("agendamento-1", AgendamentoRondaDocument.class))
                .thenReturn(new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                        new PeriodoDocument(INICIO, FIM), null, null));

        assertThatThrownBy(() -> repository.buscarPorId("agendamento-1"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não foi informado")
                .hasMessageContaining("agendamento-1");
    }

    @Test
    void shouldRejectDocumentWithoutPeriod() {
        when(mongoTemplate.findById("agendamento-1", AgendamentoRondaDocument.class))
                .thenReturn(new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                        null, "AGENDADA", null));

        assertThatThrownBy(() -> repository.buscarPorId("agendamento-1"))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("período");
    }

    @Test
    void shouldListWithoutFilters() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class)))
                .thenReturn(List.of(document("agendamento-1", "Maria", AGENDADA)));

        List<AgendamentoRonda> agendamentos = repository.listar(null, null, null);

        assertThat(agendamentos).extracting(AgendamentoRonda::getId).containsExactly("agendamento-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject()).isEmpty();
    }

    @Test
    void shouldListWithAllFilters() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class)))
                .thenReturn(List.of());

        repository.listar(EM_ANDAMENTO, "Maria", "ronda-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject())
                .containsOnlyKeys("status", "responsavel", "idRonda")
                .containsEntry("status", "EM_ANDAMENTO")
                .containsEntry("responsavel", "Maria")
                .containsEntry("idRonda", "ronda-1");
    }

    @Test
    void shouldListWithOnlyStatusFilter() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class)))
                .thenReturn(List.of());

        repository.listar(CANCELADA, null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject())
                .containsOnlyKeys("status")
                .containsEntry("status", "CANCELADA");
    }

    @Test
    void shouldFindActiveByResponsavel() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class)))
                .thenReturn(List.of(document("agendamento-1", "Maria", AGENDADA)));

        List<AgendamentoRonda> agendamentos = repository.buscarAtivosPorResponsavel("Maria");

        assertThat(agendamentos).extracting(AgendamentoRonda::getId).containsExactly("agendamento-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject()).containsEntry("responsavel", "Maria");
        assertThat(statusIn(captor.getValue()))
                .containsExactly("AGENDADA", "EM_ANDAMENTO");
    }

    @Test
    void shouldReturnTrueWhenThereIsActiveAgendamentoForRonda() {
        when(mongoTemplate.exists(any(Query.class), eq(AgendamentoRondaDocument.class))).thenReturn(true);

        assertThat(repository.existeAtivoParaRonda("ronda-1")).isTrue();

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).exists(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject()).containsEntry("idRonda", "ronda-1");
        assertThat(statusIn(captor.getValue()))
                .containsExactly("AGENDADA", "EM_ANDAMENTO");
    }

    @Test
    void shouldReturnFalseWhenThereIsNoActiveAgendamentoForRonda() {
        when(mongoTemplate.exists(any(Query.class), eq(AgendamentoRondaDocument.class))).thenReturn(false);

        assertThat(repository.existeAtivoParaRonda("ronda-1")).isFalse();
    }

    @Test
    void shouldRejectNullAgendamentoOnSave() {
        assertThatThrownBy(() -> repository.salvar(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessageContaining("agendamento");

        verifyNoInteractions(mongoTemplate);
    }

    private static List<String> statusIn(Query query) {
        Document status = (Document) query.getQueryObject().get("status");
        return status.getList("$in", String.class);
    }

    private static AgendamentoRonda agendamento(String id, StatusAgendamento status) {
        return AgendamentoRonda.reconstituir(id, "ronda-1", "Maria", PERIODO, status,
                status == CANCELADA ? "Sem equipe" : null);
    }

    private static AgendamentoRondaDocument document(String id, String responsavel, StatusAgendamento status) {
        return new AgendamentoRondaDocument(id, "ronda-1", responsavel, new PeriodoDocument(INICIO, FIM),
                status.name(), status == CANCELADA ? "Sem equipe" : null);
    }
}
