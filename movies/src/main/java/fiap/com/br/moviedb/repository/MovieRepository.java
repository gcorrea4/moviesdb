package fiap.com.br.moviedb.repository;

import fiap.com.br.moviedb.dto.MovieAutocompleteDTO;
import fiap.com.br.moviedb.dto.MovieSummaryDTO;
import fiap.com.br.moviedb.model.Movie;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * @EntityGraph(attributePaths = {"genre", "director"}) diz ao JPA para trazer o filme
 * JUNTO com o gênero e o diretor, na MESMA consulta (um JOIN).
 * Sem isso, o JPA faria 1 consulta para os filmes + 1 para cada gênero/diretor
 * (o famoso problema N+1), o que deixa a API lenta.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Endpoint 2: busca pelo título (contém o texto, ignora maiúsculas/minúsculas)
    @EntityGraph(attributePaths = {"genre", "director"})
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Endpoints 1, 3, 4, 5, 6 e 7: uma única consulta com filtros OPCIONAIS.
    // Quando o parâmetro é null, a condição "(:param is null or ...)" fica verdadeira
    // e o filtro é ignorado. Assim os filtros podem ser combinados livremente.
    @EntityGraph(attributePaths = {"genre", "director"})
    @Query("""
            select m from Movie m
            left join m.genre g
            left join m.director d
            where (:genre is null or lower(g.name) = lower(:genre))
              and (:director is null or lower(d.name) like lower(concat('%', :director, '%')))
              and (:yearFrom is null or m.releaseYear >= :yearFrom)
              and (:yearTo is null or m.releaseYear <= :yearTo)
              and (:ratingAbove is null or m.rating > :ratingAbove)
            """)
    Page<Movie> filter(@Param("genre") String genre,
                       @Param("director") String director,
                       @Param("yearFrom") Integer yearFrom,
                       @Param("yearTo") Integer yearTo,
                       @Param("ratingAbove") Double ratingAbove,
                       Pageable pageable);

    // Endpoint 8: filmes de um diretor com nota acima de um valor
    // (o nome do método já descreve a consulta: DirectorId + RatingGreaterThan)
    @EntityGraph(attributePaths = {"genre", "director"})
    Page<Movie> findByDirectorIdAndRatingGreaterThan(Long directorId, Double rating, Pageable pageable);

    // Endpoint 9: autocomplete. Traz só id e title (no máximo 10 resultados).
    List<MovieAutocompleteDTO> findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc(String title);

    // Endpoint 10: resumo (card). "select new" monta o DTO direto na consulta,
    // então só as 4 colunas necessárias são lidas do banco.
    @Query("""
            select new fiap.com.br.moviedb.dto.MovieSummaryDTO(m.title, m.releaseYear, m.rating, g.name)
            from Movie m
            left join m.genre g
            """)
    Page<MovieSummaryDTO> findSummaries(Pageable pageable);

    // Endpoint 11: 5 maiores notas (desempata pelo título para a ordem ser sempre a mesma)
    @EntityGraph(attributePaths = {"genre", "director"})
    List<Movie> findTop5ByOrderByRatingDescTitleAsc();
}
