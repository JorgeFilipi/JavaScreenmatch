package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporadas;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Principal {

    private Scanner sc = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "http://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=52f8b848";

    public void exibeMenu() {

        System.out.println("Digite o nome da serie para busca: ");

        var nomeSerie = sc.nextLine();
        var json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        // Imprime os dados da série retornados pela API
        System.out.println(dados);

        List<DadosTemporadas> temporadas = new ArrayList<>();

        // Obtém os dados de cada temporada da série
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoApi.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporadas dadosTemporadas = conversor.obterDados(json, DadosTemporadas.class);
            temporadas.add(dadosTemporadas);
        }

        // Imprime todas as temporadas em um único bloco de JSON
        System.out.println(json);

        // Imprime os dados de cada temporada individualmente
        temporadas.forEach(System.out::println);

        // Percorre todas as temporadas e imprime os títulos de seus episódios
        // Essa estrutura pode ser substituída por um forEach para simplificar a lógica
//        for (int i = 0; i < dados.totalTemporadas(); i++) {
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++) {
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        // Percorre cada temporada e imprime os títulos dos episódios
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        System.out.println("\n*** Top 10 episodios ***\n");

        // Cria uma lista com todos os episódios, extraindo os dados de cada temporada
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        // Filtra episódios que possuem avaliação válida e os ordena em ordem decrescente
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(10)
                .forEach(System.out::println);

        // Cria uma lista de objetos Episodio, associando cada episódio à sua respectiva temporada
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        // Imprime todos os episódios com detalhes da avaliação e temporada
        episodios.forEach(System.out::println);

        // Seleciona os 10 melhores episódios com base na avaliação
        episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(10)
                .forEach(e -> System.out.println(
                        "Episódio: " + e.getTitulo().toUpperCase() +
                                " Avaliacao: " + e.getAvaliacao() +
                                " - Temporada: " + e.getTemporada()
                ));


        // Cria uma lista contendo os 10 melhores episódios
        List<Episodio> top10Episodios = episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Exibe os episódios do top 10, numerados de 1 a 10
        IntStream.range(0, top10Episodios.size())
                .forEach(i -> {
                    Episodio e = top10Episodios.get(i);
                    System.out.println(
                            (i + 1) + " - " +
                                    "Episódio: " + e.getTitulo().toUpperCase() +
                                    " Avaliação: " + e.getAvaliacao() +
                                    " - Temporada: " + e.getTemporada()
                    );
                });

        // Cria uma interação para exibir episódios a partir de um ano específico
        System.out.println("\nA partir de que ano você deseja ver os episódios? ");
        var ano = sc.nextLine();
        sc.nextLine();

        // Define um formatador para exibir a data no padrão brasileiro
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");


        // Filtra e exibe apenas episódios lançados após o ano informado
        LocalDate dataBusca = LocalDate.of(Integer.parseInt(ano), 1, 1);
        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada() +
                                " / Episódio: " + e.getTitulo() +
                                " - Data: " + e.getDataLancamento().format(formatador)
                ));
    }
}
