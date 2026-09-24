package fiap.com.br.moviedb.service;

import fiap.com.br.moviedb.dto.MovieAutocompleteDTO;
import fiap.com.br.moviedb.dto.MovieSummaryDTO;
import fiap.com.br.moviedb.model.Movie;
import fiap.com.br.moviedb.repository.DirectorRepository;
import fiap.com.br.moviedb.repository.MovieRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Regras da API: valida os parâmetros e chama o repository. */
@Service
public class MovieService {

    // Campos pelos quais é permitido ordenar. Qualquer outro vira erro 400
    // (sem isso, ?sort=abc causaria um erro 500 dentro do JPA).
    private static final Set<String> SORT_FIELDS = Set.of("id", "title", "releaseYear", "rating");

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;

    public MovieService(MovieRepository movieRepository, DirectorRepository directorRepository) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
    }

    // Endpoints 1, 3, 4, 5, 6, 7
    public Page<Movie> filter(String genre, String director, Integer yearFrom, Integer yearTo,
                              Double ratingAbove, Pageable pageable) {
        validateSort(pageable);
        if (yearFrom != null && yearTo != null && yearFrom > yearTo) {
            throw new IllegalArgumentException("yearFrom deve ser menor ou igual a yearTo");
        }
        // texto vazio (?genre=) é tratado como "sem filtro"
        return movieRepository.filter(blankToNull(genre), blankToNull(director),
                yearFrom, yearTo, ratingAbove, pageable);
    }

    // Endpoint 2
    public Page<Movie> searchByTitle(String title, Pageable pageable) {
        validateSort(pageable);
        return movieRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
    }

    // Endpoint 8
    public Page<Movie> topByDirector(Long directorId, Double ratingAbove, Pageable pageable) {
        validateSort(pageable);
        if (!directorRepository.existsById(directorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Diretor não encontrado");
        }
        double min = ratingAbove == null ? 0.0 : ratingAbove;
        return movieRepository.findByDirectorIdAndRatingGreaterThan(directorId, min, pageable);
    }

    // Endpoint 9
    public List<MovieAutocompleteDTO> autocomplete(String q) {
        if (q == null || q.isBlank()) {
            return List.of(); // sem texto, sem sugestões
        }
        return movieRepository.findTop10ByTitleContainingIgnoreCaseOrderByTitleAsc(q.trim());
    }

    // Endpoint 10
    public Page<MovieSummaryDTO> summary(Pageable pageable) {
        validateSort(pageable);
        return movieRepository.findSummaries(pageable);
    }

    // Endpoint 11
    public List<Movie> top5() {
        return movieRepository.findTop5ByOrderByRatingDescTitleAsc();
    }

    // ---------- auxiliares ----------

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + order.getProperty()
                        + ". Use um destes: " + SORT_FIELDS);
            }
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
