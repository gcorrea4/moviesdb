package fiap.com.br.moviedb.dto;

/**
 * "Card" do filme: só o que a tela de listagem precisa.
 * Em vez de devolver o gênero inteiro (objeto), devolve só o nome (genreName).
 */
public record MovieSummaryDTO(String title, Integer releaseYear, Double rating, String genreName) {
}
