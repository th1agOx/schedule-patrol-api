package br.com.schedule.application.dto.response;

import java.util.List;

import br.com.schedule.application.dto.PontoDTO;

public record RondaResponseDTO(String id, String nome, String descricao, boolean ativa, List<PontoDTO> pontos) {
}
