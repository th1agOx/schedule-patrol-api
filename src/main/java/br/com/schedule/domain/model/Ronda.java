package br.com.schedule.domain.model;

import br.com.schedule.domain.exception.IdentificadorObrigatorioException;
import br.com.schedule.domain.exception.NomeObrigatorioException;
import br.com.schedule.domain.exception.PontoDuplicadoException;
import br.com.schedule.domain.exception.PontoInvalidoException;
import br.com.schedule.domain.exception.PontosObrigatoriosException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public final class Ronda {

    private final String id;
    private String nome;
    private String descricao;
    private boolean ativa;
    private List<Ponto> pontos;

    private Ronda(String id, String nome, String descricao, boolean ativa, List<Ponto> pontos) {
        this.id = requireId(id);
        this.nome = requireNome(nome);
        this.descricao = normalizeDescricao(descricao);
        this.ativa = ativa;
        this.pontos = requirePontos(pontos, id);
    }

    public static Ronda criar(String nome, String descricao, List<Ponto> pontos) {
        return new Ronda(UUID.randomUUID().toString(), nome, descricao, true, pontos);
    }


    public static Ronda reconstituir(String id, String nome, String descricao, boolean ativa, List<Ponto> pontos) {
        return new Ronda(id, nome, descricao, ativa, pontos);
    }

    public void atualizar(String nome, String descricao, List<Ponto> pontos) {
        String novoNome = requireNome(nome);
        String novaDescricao = normalizeDescricao(descricao);
        List<Ponto> novosPontos = requirePontos(pontos, id);

        this.nome = novoNome;
        this.descricao = novaDescricao;
        this.pontos = novosPontos;
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public boolean podeSerAgendada() {
        return ativa;
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

    public List<Ponto> getPontos() {
        return pontos;
    }

    private static String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IdentificadorObrigatorioException("O identificador da ronda é obrigatório.");
        }
        return id;
    }

    private static String requireNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new NomeObrigatorioException();
        }
        return nome.trim();
    }

    private static String normalizeDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }

    private static List<Ponto> requirePontos(List<Ponto> pontos, String rondaId) {
        if (pontos == null || pontos.isEmpty()) {
            throw new PontosObrigatoriosException();
        }

        Set<String> codigos = new HashSet<>();
        for (Ponto ponto : pontos) {
            if (ponto == null) {
                throw new PontoInvalidoException("A lista de pontos não pode conter pontos nulos.");
            }
            if (!codigos.add(ponto.codigoNormalizado())) {
                throw new PontoDuplicadoException(ponto.codigo(), rondaId);
            }
        }

        return List.copyOf(pontos);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Ronda ronda)) {
            return false;
        }
        return id.equals(ronda.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
