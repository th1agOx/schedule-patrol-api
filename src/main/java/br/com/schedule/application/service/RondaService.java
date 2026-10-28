package br.com.schedule.application.service;

import br.com.schedule.application.dto.request.RondaRequestDTO;
import br.com.schedule.application.dto.response.RondaResponseDTO;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.mapper.RondaDTOMapper;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.PoliticaDeExclusaoDeRonda;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Orquestra as operações da Ronda: busca, chama o comportamento do domínio, salva e responde.
 * Regra de negócio NÃO fica aqui: ela vive nas entidades e nos serviços de domínio.
 */
@Service
public class RondaService {

    private final RondaRepository rondaRepository;
    private final PoliticaDeExclusaoDeRonda politicaDeExclusao;

    public RondaService(RondaRepository rondaRepository, PoliticaDeExclusaoDeRonda politicaDeExclusao) {
        this.rondaRepository = rondaRepository;
        this.politicaDeExclusao = politicaDeExclusao;
    }

    public List<RondaResponseDTO> listar(Boolean ativa) {
        List<Ronda> rondas = ativa == null
                ? rondaRepository.listarTodas()
                : rondaRepository.listarPorAtiva(ativa);

        return rondas.stream().map(RondaDTOMapper::paraResponse).toList();
    }

    public RondaResponseDTO buscarPorId(String id) {
        return RondaDTOMapper.paraResponse(buscar(id));
    }

    public RondaResponseDTO criar(RondaRequestDTO request) {
        Ronda ronda = Ronda.criar(request.nome(), request.descricao(), RondaDTOMapper.paraPontos(request.pontos()));
        return RondaDTOMapper.paraResponse(rondaRepository.salvar(ronda));
    }

    public RondaResponseDTO atualizar(String id, RondaRequestDTO request) {
        Ronda ronda = buscar(id);
        ronda.atualizar(request.nome(), request.descricao(), RondaDTOMapper.paraPontos(request.pontos()));
        return RondaDTOMapper.paraResponse(rondaRepository.salvar(ronda));
    }

    public RondaResponseDTO ativar(String id) {
        Ronda ronda = buscar(id);
        ronda.ativar();
        return RondaDTOMapper.paraResponse(rondaRepository.salvar(ronda));
    }

    public RondaResponseDTO desativar(String id) {
        Ronda ronda = buscar(id);
        ronda.desativar();
        return RondaDTOMapper.paraResponse(rondaRepository.salvar(ronda));
    }

    public void excluir(String id) {
        Ronda ronda = buscar(id);
        politicaDeExclusao.verificarExclusao(ronda.getId());   // regra entre agregados (domínio)
        rondaRepository.excluir(ronda.getId());
    }

    private Ronda buscar(String id) {
        return Optional.ofNullable(id)
                .flatMap(rondaRepository::buscarPorId)
                .orElseThrow(() -> RecursoNaoEncontradoException.ronda(id));
    }
}
