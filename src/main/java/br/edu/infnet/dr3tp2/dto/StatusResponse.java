package br.edu.infnet.dr3tp2.dto;

/**
 * Record DTO para resposta de status da API
 */
public record StatusResponse(
        String status,
        String versao
) {}
