package br.com.schedule.infrastructure.persistence.adapter;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.document.PeriodoDocument;
import br.com.schedule.infrastructure.persistence.mapper.AgendamentoRondaMapper;
import br.com.schedule.infrastructure.persistence.repository.AgendamentoRondaMongoRepository;
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
import java.util.Optional;

import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoRondaRepositoryAdapterTest {

    private static final Instant INICIO = Instant.parse("2026-11-05T10:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-05T12:00:00Z");
    private static final Periodo PERIODO = new Periodo(INICIO, FIM);
    private static final List<String> STATUS_ATIVOS = List.of("AGENDADA", "EM_ANDAMENTO");

    @Mock
    private AgendamentoRondaMongoRepository mongoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private AgendamentoRondaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AgendamentoRondaRepositoryAdapter(mongoRepository, mongoTemplate);
    }

    @Test
    void shouldSaveAndMapSavedDocument() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", AGENDADA, null);
        when(mongoRepository.save(any(AgendamentoRondaDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRonda salvo = adapter.salvar(agendamento);

        assertThat(salvo).isEqualTo(agendamento);
        assertThat(salvo.getStatus()).isEqualTo(AGENDADA);

        ArgumentCaptor<AgendamentoRondaDocument> captor =
                ArgumentCaptor.forClass(AgendamentoRondaDocument.class);
        verify(mongoRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("agendamento-1");
        assertThat(captor.getValue().getStatus()).isEqualTo("AGENDADA");
    }

    @Test
    void shouldFindById() {
        AgendamentoRondaDocument document =
                AgendamentoRondaMapper.toDocument(agendamento("agendamento-1", AGENDADA, null));
        when(mongoRepository.findById("agendamento-1")).thenReturn(Optional.of(document));

        Optional<AgendamentoRonda> encontrado = adapter.buscarPorId("agendamento-1");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.orElseThrow().getResponsavel()).isEqualTo("Maria");
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(mongoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorId("inexistente")).isEmpty();
    }

    @Test
    void shouldFindActiveByResponsavel() {
        when(mongoRepository.findByResponsavelAndStatusIn("Maria", STATUS_ATIVOS))
                .thenReturn(List.of(document("agendamento-1", "Maria", AGENDADA)));

        List<AgendamentoRonda> agendamentos = adapter.buscarAtivosPorResponsavel("Maria");

        assertThat(agendamentos).extracting(AgendamentoRonda::getId).containsExactly("agendamento-1");
        verify(mongoRepository).findByResponsavelAndStatusIn("Maria", STATUS_ATIVOS);
    }

    @Test
    void shouldCheckActiveByRonda() {
        when(mongoRepository.existsByIdRondaAndStatusIn("ronda-1", STATUS_ATIVOS)).thenReturn(true);

        assertThat(adapter.existeAtivoParaRonda("ronda-1")).isTrue();
        verify(mongoRepository).existsByIdRondaAndStatusIn("ronda-1", STATUS_ATIVOS);
    }

    @Test
    void shouldReturnFalseWhenThereIsNoActiveAgendamentoForRonda() {
        when(mongoRepository.existsByIdRondaAndStatusIn("ronda-1", STATUS_ATIVOS)).thenReturn(false);

        assertThat(adapter.existeAtivoParaRonda("ronda-1")).isFalse();
    }

    @Test
    void shouldListWithoutFilters() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class)))
                .thenReturn(List.of(document("agendamento-1", "Maria", AGENDADA)));

        List<AgendamentoRonda> agendamentos = adapter.listar(null, null, null);

        assertThat(agendamentos).extracting(AgendamentoRonda::getId).containsExactly("agendamento-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject()).isEmpty();
    }

    @Test
    void shouldListWithAllFilters() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class))).thenReturn(List.of());

        adapter.listar(EM_ANDAMENTO, "Maria", "ronda-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject())
                .containsEntry("status", "EM_ANDAMENTO")
                .containsEntry("responsavel", "Maria")
                .containsEntry("idRonda", "ronda-1");
    }

    @Test
    void shouldListWithOnlyStatusFilter() {
        when(mongoTemplate.find(any(Query.class), eq(AgendamentoRondaDocument.class))).thenReturn(List.of());

        adapter.listar(CANCELADA, null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AgendamentoRondaDocument.class));
        assertThat(captor.getValue().getQueryObject()).containsOnlyKeys("status");
    }

    private static AgendamentoRonda agendamento(String id, StatusAgendamento status, String motivo) {
        return AgendamentoRonda.reconstituir(id, "ronda-1", "Maria", PERIODO, status, motivo);
    }

    private static AgendamentoRondaDocument document(String id, String responsavel, StatusAgendamento status) {
        return new AgendamentoRondaDocument(id, "ronda-1", responsavel, new PeriodoDocument(INICIO, FIM),
                status.name(), status == CANCELADA ? "Sem equipe" : null);
    }
}
