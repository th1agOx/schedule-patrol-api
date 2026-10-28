package br.com.schedule.infrastructure.persistence.repository;

import br.com.schedule.domain.exception.PontosObrigatoriosException;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.infrastructure.persistence.document.PontoDocument;
import br.com.schedule.infrastructure.persistence.document.RondaDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RondaMongoRepositoryTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private RondaMongoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RondaMongoRepository(mongoTemplate);
    }

    @Test
    void shouldSaveAndMapSavedDocument() {
        Ronda ronda = ronda("ronda-1", true, List.of(
                new Ponto("P-01", "Portão principal"),
                new Ponto("P-02", "Casa de válvulas")));
        when(mongoTemplate.save(any(RondaDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ronda salva = repository.salvar(ronda);

        assertThat(salva.getId()).isEqualTo("ronda-1");
        assertThat(salva.getNome()).isEqualTo("Ronda Norte");
        assertThat(salva.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(salva.isAtiva()).isTrue();
        assertThat(salva.getPontos()).containsExactly(
                new Ponto("P-01", "Portão principal"),
                new Ponto("P-02", "Casa de válvulas"));

        ArgumentCaptor<RondaDocument> captor = ArgumentCaptor.forClass(RondaDocument.class);
        verify(mongoTemplate).save(captor.capture());
        RondaDocument document = captor.getValue();
        assertThat(document.getId()).isEqualTo("ronda-1");
        assertThat(document.getNome()).isEqualTo("Ronda Norte");
        assertThat(document.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(document.isAtiva()).isTrue();
        assertThat(document.getPontos()).extracting(PontoDocument::getCodigo)
                .containsExactly("P-01", "P-02");
        assertThat(document.getPontos()).extracting(PontoDocument::getDescricao)
                .containsExactly("Portão principal", "Casa de válvulas");
    }

    @Test
    void shouldPreserveNullDescriptionWhenSaving() {
        Ronda ronda = Ronda.reconstituir("ronda-1", "Ronda Norte", null, true,
                List.of(new Ponto("P-01", "Portão principal")));
        when(mongoTemplate.save(any(RondaDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ronda salva = repository.salvar(ronda);

        assertThat(salva.getDescricao()).isNull();
        ArgumentCaptor<RondaDocument> captor = ArgumentCaptor.forClass(RondaDocument.class);
        verify(mongoTemplate).save(captor.capture());
        assertThat(captor.getValue().getDescricao()).isNull();
    }

    @Test
    void shouldFindById() {
        RondaDocument document = document("ronda-1", true,
                List.of(new PontoDocument("P-01", "Portão principal")));
        when(mongoTemplate.findById("ronda-1", RondaDocument.class)).thenReturn(document);

        Ronda ronda = repository.buscarPorId("ronda-1").orElseThrow();

        assertThat(ronda.getId()).isEqualTo("ronda-1");
        assertThat(ronda.getNome()).isEqualTo("Ronda Norte");
        assertThat(ronda.getDescricao()).isEqualTo("Inspeção diária");
        assertThat(ronda.isAtiva()).isTrue();
        assertThat(ronda.getPontos()).containsExactly(new Ponto("P-01", "Portão principal"));
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(repository.buscarPorId("inexistente")).isEmpty();

        verify(mongoTemplate).findById("inexistente", RondaDocument.class);
    }

    @Test
    void shouldListAll() {
        when(mongoTemplate.findAll(RondaDocument.class)).thenReturn(List.of(
                document("ronda-1", true, List.of(new PontoDocument("P-01", "Portão principal"))),
                document("ronda-2", false, List.of(new PontoDocument("P-02", "Casa de válvulas")))));

        List<Ronda> rondas = repository.listarTodas();

        assertThat(rondas).extracting(Ronda::getId).containsExactly("ronda-1", "ronda-2");
        assertThat(rondas).extracting(Ronda::isAtiva).containsExactly(true, false);
    }

    @Test
    void shouldListByActive() {
        when(mongoTemplate.find(any(Query.class), eq(RondaDocument.class))).thenReturn(List.of(
                document("ronda-1", true, List.of(new PontoDocument("P-01", "Portão principal")))));

        List<Ronda> rondas = repository.listarPorAtiva(true);

        assertThat(rondas).extracting(Ronda::getId).containsExactly("ronda-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(RondaDocument.class));
        assertThat(captor.getValue().getQueryObject())
                .containsOnlyKeys("ativa")
                .containsEntry("ativa", true);
    }

    @Test
    void shouldDeleteUsingUnderscoreId() {
        repository.excluir("ronda-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).remove(captor.capture(), eq(RondaDocument.class));
        assertThat(captor.getValue().getQueryObject())
                .containsOnlyKeys("_id")
                .containsEntry("_id", "ronda-1");
    }

    @Test
    void shouldRejectDocumentWithoutPoints() {
        when(mongoTemplate.findById("ronda-1", RondaDocument.class))
                .thenReturn(new RondaDocument("ronda-1", "Ronda Norte", null, true, List.of()));

        assertThatThrownBy(() -> repository.buscarPorId("ronda-1"))
                .isExactlyInstanceOf(PontosObrigatoriosException.class);
    }

    @Test
    void shouldRejectDocumentWithNullPoints() {
        when(mongoTemplate.findById("ronda-1", RondaDocument.class))
                .thenReturn(new RondaDocument("ronda-1", "Ronda Norte", null, true, null));

        assertThatThrownBy(() -> repository.buscarPorId("ronda-1"))
                .isExactlyInstanceOf(PontosObrigatoriosException.class);
    }

    @Test
    void shouldRejectNullPointInsideDocument() {
        RondaDocument document = new RondaDocument("ronda-1", "Ronda Norte", null, true,
                Arrays.asList(new PontoDocument("P-01", "Portão principal"), null));
        when(mongoTemplate.findById("ronda-1", RondaDocument.class)).thenReturn(document);

        assertThatThrownBy(() -> repository.buscarPorId("ronda-1"))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ponto nulo");
    }

    @Test
    void shouldRejectNullRondaOnSave() {
        assertThatThrownBy(() -> repository.salvar(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessageContaining("ronda");

        verifyNoInteractions(mongoTemplate);
    }

    private static Ronda ronda(String id, boolean ativa, List<Ponto> pontos) {
        return Ronda.reconstituir(id, "Ronda Norte", "Inspeção diária", ativa, pontos);
    }

    private static RondaDocument document(String id, boolean ativa, List<PontoDocument> pontos) {
        return new RondaDocument(id, "Ronda Norte", "Inspeção diária", ativa, pontos);
    }
}
