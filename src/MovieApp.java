import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class MovieApp extends Application {

   
    private ObservableList<Movie> movies = FXCollections.observableArrayList();
    private final String FILE_NAME = "movies.txt";

    // --- Guardar referencias a la ventana de lista y la tabla ---
    private Stage listStage;
    private TableView<Movie> movieTable;


    @Override
    public void start(Stage primaryStage) {
        loadMovies();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Button addBtn = new Button("Agregar Película");
        addBtn.setOnAction(e -> addMovie());

        Button searchBtn = new Button("Buscar Película");
        searchBtn.setOnAction(e -> searchMovie());

        Button sortBtn = new Button("Ordenar Películas");
        sortBtn.setOnAction(e -> sortMovies());

        Button listBtn = new Button("Listar Todas");
        listBtn.setOnAction(e -> listMovies());

        Button deleteBtn = new Button("Eliminar Película");
        deleteBtn.setOnAction(e -> deleteMovie());

        Button exitBtn = new Button("Salir");
        exitBtn.setOnAction(e -> {
            saveMovies();
            primaryStage.close();
            if (listStage != null) { 
                listStage.close();
            }
        });

        root.getChildren().addAll(addBtn, searchBtn, sortBtn, listBtn, deleteBtn, exitBtn);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Registro de Películas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addMovie() {
        String title = getInput("Ingrese el título:");
        if (title == null || title.isEmpty()) return;

        // Verificar si existe
        for (Movie m : movies) {
            if (m.getTitle().equalsIgnoreCase(title)) {
                showAlert(Alert.AlertType.WARNING, "La película ya existe.");
                return;
            }
        }

        String director = getInput("Ingrese el director:");
        String costStr = getInput("Ingrese el costo (float):");
        float cost;
        try {
            cost = Float.parseFloat(costStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Costo inválido.");
            return;
        }
        String duration = getInput("Ingrese la duración:");
        String language = getInput("Ingrese el idioma:");
        String country = getInput("Ingrese el país de origen:");

        Movie movie = new Movie(title, director, cost, duration, language, country);
        movies.add(movie); 
        showAlert(Alert.AlertType.INFORMATION, "Película agregada.");
    }

    private void searchMovie() {
        String title = getInput("Ingrese el título a buscar:");
        if (title == null || title.isEmpty()) return;

        for (Movie m : movies) {
            if (m.getTitle().equalsIgnoreCase(title)) {
                showAlert(Alert.AlertType.INFORMATION, m.toString());
                return;
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "No se encontró la película.");
    }

    private void sortMovies() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Título", "Título", "Director", "Costo", "Duración", "Idioma", "País");
        dialog.setTitle("Ordenar Por");
        dialog.setHeaderText("Seleccione el campo para ordenar:");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get() == null) return;

        String field = result.get();
        Comparator<Movie> comparator = null;

        switch (field) {
            case "Título":
                comparator = Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Director":
                comparator = Comparator.comparing(Movie::getDirector, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Costo":
                comparator = Comparator.comparingDouble(Movie::getCost);
                break;
            case "Duración":
                comparator = Comparator.comparing(Movie::getDuration, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Idioma":
                comparator = Comparator.comparing(Movie::getLanguage, String.CASE_INSENSITIVE_ORDER);
                break;
            case "País":
                comparator = Comparator.comparing(Movie::getCountry, String.CASE_INSENSITIVE_ORDER);
                break;
        }

        if (comparator != null) {
            // --- Ordenar la lista observable. La tabla se actualiza sola. ---
            movies.sort(comparator);
            showAlert(Alert.AlertType.INFORMATION, "Películas ordenadas por " + field + ".");

        }
    }

    // --- Lógica para mostrar/crear la ventana de lista ---
    private void listMovies() {
        if (movies.isEmpty() && listStage == null) { // Solo mostrar si está vacía Y NUNCA se ha abierto
             showAlert(Alert.AlertType.INFORMATION, "No hay películas registradas.");
             return;
        }

        // Crear la ventana solo si no existe
        if (listStage == null) {
            listStage = new Stage();
            listStage.setTitle("Lista de Películas");

            movieTable = new TableView<>();
            
            // --- Enlazar la tabla directamente a la lista observable ---
            movieTable.setItems(movies);

            // --- Usar lambdas ---
            TableColumn<Movie, String> titleCol = new TableColumn<>("Título");
            titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

            TableColumn<Movie, String> directorCol = new TableColumn<>("Director");
            directorCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDirector()));

            TableColumn<Movie, Float> costCol = new TableColumn<>("Costo");
            costCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCost()));

            TableColumn<Movie, String> durationCol = new TableColumn<>("Duración");
            durationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDuration()));

            TableColumn<Movie, String> languageCol = new TableColumn<>("Idioma");
            languageCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLanguage()));

            TableColumn<Movie, String> countryCol = new TableColumn<>("País");
            countryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCountry()));

            movieTable.getColumns().addAll(titleCol, directorCol, costCol, durationCol, languageCol, countryCol);
            movieTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            ScrollPane scrollPane = new ScrollPane(movieTable);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            VBox vbox = new VBox(scrollPane);
            Scene scene = new Scene(vbox, 800, 600);
            listStage.setScene(scene);
        }

        // Mostrar la ventana (nueva o existente) y traerla al frente
        listStage.show();
        listStage.toFront();
    }

    private void deleteMovie() {
        String title = getInput("Ingrese el título a eliminar:");
        if (title == null || title.isEmpty()) return;

        Iterator<Movie> iterator = movies.iterator();
        while (iterator.hasNext()) {
            Movie m = iterator.next();
            if (m.getTitle().equalsIgnoreCase(title)) {
                iterator.remove(); // La TableView se actualizará sola
                showAlert(Alert.AlertType.INFORMATION, "Película eliminada.");
                return;
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "No se encontró la película.");
    }

    private String getInput(String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(header);
        dialog.setTitle(null); // Quitar el título de la ventana de diálogo
        dialog.setContentText(null);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null); // Devolver null si se cancela
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Información");
        alert.setContentText(message);
        alert.show();
    }

    private void loadMovies() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    try {
                        String title = parts[0];
                        String director = parts[1];
                        float cost = Float.parseFloat(parts[2]);
                        String duration = parts[3];
                        String language = parts[4];
                        String country = parts[5];
                        movies.add(new Movie(title, director, cost, duration, language, country));
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear costo en línea: " + line);
                    }
                }
            }
        } catch (IOException | SecurityException e) { 
            // Archivo no encontrado, error de permisos o de lectura
            System.err.println("No se pudo cargar el archivo, iniciando vacío: " + e.getMessage());
        }
    }

    private void saveMovies() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Movie m : movies) {
                bw.write(m.getTitle() + "|" + m.getDirector() + "|" + m.getCost() + "|" + m.getDuration() + "|" + m.getLanguage() + "|" + m.getCountry());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al guardar el archivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Movie {
    private String title;
    private String director;
    private float cost;
    private String duration;
    private String language;
    private String country;

    public Movie(String title, String director, float cost, String duration, String language, String country) {
        this.title = title;
        this.director = director;
        this.cost = cost;
        this.duration = duration;
        this.language = language;
        this.country = country;
    }

    // Getters públicos (necesarios para el CellValueFactory)
    public String getTitle() { return title; }
    public String getDirector() { return director; }
    public float getCost() { return cost; }
    public String getDuration() { return duration; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }

    @Override
    public String toString() {
        return "Título: " + title + "\nDirector: " + director + "\nCosto: " + cost + 
               "\nDuración: " + duration + "\nIdioma: " + language + "\nPaís: " + country;
    }
}