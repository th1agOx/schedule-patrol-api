package br.com.schedule.infrastructure.persistence.adapter;

import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.infrastructure.persistence.document.PontoDocument;
import br.com.schedule.infrastructure.persistence.document.RondaDocument;
import br.com.schedule.infrastructure.persistence.mapper.RondaMapper;
import br.com.schedule.infrastructure.persistence.repository.RondaMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RondaRepositoryAdapterTest {

    @Mock
    private RondaMongoRepository mongoRepository;

    private RondaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RondaRepositoryAdapter(mongoRepository);
    }

    @Test
    void shouldSaveAndMapSavedDocument() {
        Ronda ronda = ronda("ronda-1", true);
        when(mongoRepository.save(any(RondaDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ronda salva = adapter.salvar(ronda);

        assertThat(salva).isEqualTo(ronda);
        assertThat(salva.getNome()).isEqualTo("Ronda Norte");
        assertThat(salva.getPontos()).containsExactly(new Ponto("P-01", "Portão principal"));

        ArgumentCaptor<RondaDocument> captor = ArgumentCaptor.forClass(RondaDocument.class);
        verify(mongoRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("ronda-1");
        assertThat(captor.getValue().getPontos()).extracting(PontoDocument::getCodigo).containsExactly("P-01");
    }

    @Test
    void shouldFindById() {
        RondaDocument document = RondaMapper.toDocument(ronda("ronda-1", true));
        when(mongoRepository.findById("ronda-1")).thenReturn(Optional.of(document));

        Optional<Ronda> encontrada = adapter.buscarPorId("ronda-1");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.orElseThrow().getNome()).isEqualTo("Ronda Norte");
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(mongoRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorId("inexistente")).isEmpty();
    }

    @Test
    void shouldListAll() {
        when(mongoRepository.findAll()).thenReturn(List.of(
                RondaMapper.toDocument(ronda("ronda-1", true)),
                RondaMapper.toDocument(ronda("ronda-2", false))));

        List<Ronda> rondas = adapter.listarTodas();

        assertThat(rondas).extracting(Ronda::getId).containsExactly("ronda-1", "ronda-2");
    }

    @Test
    void shouldListByActive() {
        when(mongoRepository.findByAtiva(true)).thenReturn(List.of(RondaMapper.toDocument(ronda("ronda-1", true))));

        List<Ronda> rondas = adapter.listarPorAtiva(true);

        assertThat(rondas).extracting(Ronda::getId).containsExactly("ronda-1");
        verify(mongoRepository).findByAtiva(true);
    }

    @Test
    void shouldDeleteById() {
        adapter.excluir("ronda-1");

        verify(mongoRepository).deleteById("ronda-1");
    }

    private static Ronda ronda(String id, boolean ativa) {
        return Ronda.reconstituir(id, "Ronda Norte", "Inspeção diária", ativa,
                List.of(new Ponto("P-01", "Portão principal")));
    }
}
