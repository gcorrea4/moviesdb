package fiap.com.br.moviedb.controller;

import fiap.com.br.moviedb.dto.MovieAutocompleteDTO;
import fiap.com.br.moviedb.dto.MovieSummaryDTO;
import fiap.com.br.moviedb.model.Movie;
import fiap.com.br.moviedb.service.MovieService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * O parâmetro "Pageable" é preenchido automaticamente pelo Spring com os
 * parâmetros da URL: page (começa em 0), size e sort (ex: sort=rating,desc).
 * O valor padrão (@PageableDefault) é usado quando o cliente não envia nada.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    // 01, 03, 04, 05, 06, 07 - lista com filtros opcionais e combináveis
    // GET /movies?page=0&size=10&sort=title
    // GET /movies?genre=Drama | director=Nolan | yearFrom=2000&yearTo=2010 | ratingAbove=8.0
    // GET /movies?genre=Action&yearFrom=2010
    @GetMapping
    public Page<Movie> list(@RequestParam(required = false) String genre,
                            @RequestParam(required = false) String director,
                            @RequestParam(required = false) Integer yearFrom,
                            @RequestParam(required = false) Integer yearTo,
                            @RequestParam(required = false) Double ratingAbove,
                            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return service.filter(genre, director, yearFrom, yearTo, ratingAbove, pageable);
    }

    // 02 - GET /movies/search?title=god
    @GetMapping("/search")
    public Page<Movie> search(@RequestParam String title,
                              @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return service.searchByTitle(title, pageable);
    }

    // 08 - GET /movies/director/1/top?ratingAbove=7.5&page=0&size=5
    @GetMapping("/director/{directorId}/top")
    public Page<Movie> topByDirector(@PathVariable Long directorId,
                                     @RequestParam(required = false) Double ratingAbove,
                                     @PageableDefault(size = 5, sort = "rating",
                                             direction = Sort.Direction.DESC) Pageable pageable) {
        return service.topByDirector(directorId, ratingAbove, pageable);
    }

    // 09 - GET /movies/autocomplete?q=god  (devolve só id e title)
    @GetMapping("/autocomplete")
    public List<MovieAutocompleteDTO> autocomplete(@RequestParam(required = false) String q) {
        return service.autocomplete(q);
    }

    // 10 - GET /movies/summary?page=0&size=10&sort=rating,desc
    @GetMapping("/summary")
    public Page<MovieSummaryDTO> summary(@PageableDefault(size = 10, sort = "rating",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return service.summary(pageable);
    }

    // 11 - GET /movies/top5
    @GetMapping("/top5")
    public List<Movie> top5() {
        return service.top5();
    }
}
