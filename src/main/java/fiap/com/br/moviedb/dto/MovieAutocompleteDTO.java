package fiap.com.br.moviedb.dto;

/**
 * Resposta do autocomplete: apenas id e title.
 * Como é um record e o repository só seleciona essas duas colunas,
 * o banco nem chega a ler as outras (mais leve e rápido).
 */
public record MovieAutocompleteDTO(Long id, String title) {
}
