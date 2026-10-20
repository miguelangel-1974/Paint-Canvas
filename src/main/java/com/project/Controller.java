package com.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

// Controlador de la vista layout.fxml: gestiona los eventos del ratón,
// las herramientas, la lista de objetos y el redibujado del canvas.
public class Controller implements Initializable {

    // ---- Controles de la vista ----
    // Cada uno se enlaza con el elemento del .fxml que tiene el mismo fx:id
    @FXML private Canvas canvas;              // área de dibujo
    @FXML private Canvas colorsCanvas;        // paleta de lápices
    @FXML private ToggleButton freehandRadio; // herramienta: traç lliure
    @FXML private ToggleButton lineRadio;     // herramienta: línia
    @FXML private ToggleButton rectRadio;     // herramienta: rectangle
    @FXML private ColorPicker strokePicker;   // color de la línia / contorn
    @FXML private ColorPicker fillPicker;     // color d'emplenament (solo rectángulos)
    @FXML private Slider widthSlider;         // ample de la línia
    @FXML private ListView<Shape> shapesList; // lista de objetos dibujados
    @FXML private Button deleteButton;        // botón eliminar

    private GraphicsContext gc;               // "pincel" para dibujar en el canvas
    private ColorPalette colorPalette;        // la paleta de lápices

    // ---- Datos del dibujo ----
    // Todos los objetos terminados, en orden de creación (el último queda encima)
    private final List<Shape> shapes = new ArrayList<>();
    // Objeto que se está dibujando ahora mismo (previsualización).
    // Todavía no está en la lista: se añade al soltar el ratón.
    private Shape currentShape = null;
    // Objeto seleccionado en la lista (null si no hay ninguno)
    private Shape selectedShape = null;

    // Herramienta actual: "freehand", "line" o "rect"
    private String tool = "freehand";

    // ---- Estado del movimiento de un objeto ----
    private boolean dragging = false;  // true mientras se arrastra un objeto para moverlo
    private double lastX;              // última posición del ratón (para calcular cuánto se ha movido)
    private double lastY;

    // Se ejecuta una sola vez al cargar la vista: aquí se prepara todo
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        // Extremos y uniones redondeados para que los trazos se vean suaves
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        // Valores iniciales del formulario
        strokePicker.setValue(Color.BLACK);
        fillPicker.setValue(Color.LIGHTBLUE);
        widthSlider.setMin(1);     // el ancho tiene que ser mayor que 0
        widthSlider.setMax(50);
        widthSlider.setValue(3);

        // Paleta de lápices. Al empezar marca el lápiz que coincide con el color del contorno
        colorPalette = new ColorPalette(colorsCanvas);
        colorPalette.setSelectedColor(strokePicker.getValue());
        // Clic en la paleta -> onPaletteClicked
        colorsCanvas.setOnMouseClicked(this::onPaletteClicked);
        // Si el usuario cambia el color con el ColorPicker, la paleta levanta el lápiz
        // que coincida (o ninguno si ese color no está en la paleta)
        strokePicker.setOnAction(event -> colorPalette.setSelectedColor(strokePicker.getValue()));

        // Herramientas: al pulsar cada botón se cambia la herramienta actual
        freehandRadio.setSelected(true);
        freehandRadio.setOnAction(event -> chooseTool("freehand"));
        lineRadio.setOnAction(event -> chooseTool("line"));
        rectRadio.setOnAction(event -> chooseTool("rect"));

        // Lista de objetos y botón eliminar (desactivado hasta que haya algo seleccionado)
        shapesList.setOnMouseClicked(this::onListClicked);
        deleteButton.setDisable(true);
        deleteButton.setOnAction(event -> deleteSelected());

        // Eventos del ratón sobre el canvas de dibujo
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);

        redraw();
    }

    // ---- Paleta ----

    // El usuario ha hecho clic en la paleta de lápices
    private void onPaletteClicked(MouseEvent event) {
        // La paleta averigua qué lápiz hay en esa posición
        colorPalette.selectColorAt(event.getX(), event.getY());

        // Si ha caído sobre un lápiz, ese color pasa a ser el color del contorno
        Color color = colorPalette.getSelectedColor();
        if (color != null) {
            strokePicker.setValue(color);
        }
    }

    // ---- Herramientas y lista ----

    // El usuario ha elegido una herramienta de dibujo
    private void chooseTool(String newTool) {
        tool = newTool;

        // Al elegir herramienta se deselecciona el objeto y se vuelve al modo dibujo
        selectedShape = null;
        shapesList.getSelectionModel().clearSelection();
        deleteButton.setDisable(true);
        redraw();
    }

    // El usuario ha hecho clic en la lista de objetos
    private void onListClicked(MouseEvent event) {
        Shape clicked = shapesList.getSelectionModel().getSelectedItem();
        if (clicked == null) return;   // clic en una zona vacía de la lista

        // Seleccionar un objeto activa el modo mover: se desmarcan las herramientas
        // para que arrastrar sobre el canvas mueva el objeto en vez de dibujar otro
        selectedShape = clicked;
        currentShape = null;
        freehandRadio.setSelected(false);
        lineRadio.setSelected(false);
        rectRadio.setSelected(false);
        deleteButton.setDisable(false);
        redraw();
    }

    // El usuario ha pulsado el botón eliminar
    private void deleteSelected() {
        if (selectedShape == null) return;

        // Se borra de los datos y de la lista visible
        shapes.remove(selectedShape);
        shapesList.getItems().remove(selectedShape);

        // Se limpia la selección (así desaparece el recuadro discontinuo)
        // y se vuelve al modo dibujo con la herramienta de traç lliure
        selectedShape = null;
        shapesList.getSelectionModel().clearSelection();
        deleteButton.setDisable(true);
        tool = "freehand";
        freehandRadio.setSelected(true);
        redraw();
    }

    // ---- Canvas de dibujo (ratón) ----

    // Se pulsa el botón del ratón sobre el canvas
    private void onMousePressed(MouseEvent event) {
        // Solo nos interesa el botón principal (el izquierdo)
        if (event.getButton() != MouseButton.PRIMARY) return;

        double x = event.getX();
        double y = event.getY();

        // MODO MOVER: hay un objeto seleccionado.
        // Solo se empieza a mover si el clic cae dentro de su recuadro de selección.
        if (selectedShape != null) {
            if (selectedShape.containsPoint(x, y)) {
                dragging = true;
                lastX = x;   // guardamos dónde ha empezado el arrastre
                lastY = y;
            }
            return;   // en modo mover nunca se crea un objeto nuevo
        }

        // MODO DIBUJO: se crea el objeto con el estilo que haya ahora en el formulario.
        // Cada objeto guarda su propio estilo, así que si luego se cambia el formulario,
        // los objetos ya dibujados no cambian.
        Color strokeColor = strokePicker.getValue();
        Color fillColor = fillPicker.getValue();
        double width = widthSlider.getValue();

        if (tool.equals("freehand")) {
            currentShape = new Stroke(strokeColor, width);
            currentShape.setEnd(x, y);   // en un trazo, esto guarda el primer punto
        }
        if (tool.equals("line")) {
            // Al principio el inicio y el final son el mismo punto
            currentShape = new Line(strokeColor, width, x, y, x, y);
        }
        if (tool.equals("rect")) {
            currentShape = new Rect(strokeColor, width, fillColor, x, y, x, y);
        }
        redraw();
    }

    // Se mueve el ratón con el botón pulsado
    private void onMouseDragged(MouseEvent event) {
        if (dragging) {
            // MODO MOVER: se calcula cuánto se ha movido el ratón desde el evento
            // anterior y se mueve el objeto esa distancia. Así el objeto no "salta"
            // a la posición del cursor: se desplaza igual que el ratón.
            double dx = event.getX() - lastX;
            double dy = event.getY() - lastY;
            selectedShape.move(dx, dy);
            lastX = event.getX();
            lastY = event.getY();
            redraw();
        } else if (currentShape != null) {
            // MODO DIBUJO: el objeto sigue al ratón (trazo: punto nuevo;
            // línea y rectángulo: se actualiza el punto final)
            currentShape.setEnd(event.getX(), event.getY());
            redraw();
        }
    }

    // Se suelta el botón del ratón
    private void onMouseReleased(MouseEvent event) {
        if (dragging) {
            // Fin del movimiento: el objeto se queda en su nueva posición y sigue seleccionado
            dragging = false;
            return;
        }
        if (currentShape == null) return;

        // Fin del dibujo: se guarda la posición final y el objeto se da por terminado.
        // Ahora sí se añade a los datos y a la lista.
        currentShape.setEnd(event.getX(), event.getY());
        shapes.add(currentShape);
        shapesList.getItems().add(currentShape);
        currentShape = null;
        redraw();
    }

    // ---- Redibujado ----

    // Borra el canvas y lo dibuja todo desde cero a partir de los datos.
    // Se llama cada vez que algo cambia (crear, mover, eliminar, seleccionar).
    private void redraw() {
        // Fondo blanco
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Objetos guardados, en orden de creación (los más recientes quedan encima)
        for (Shape s : shapes) {
            s.draw(gc);
        }

        // Objeto que se está dibujando ahora (previsualización), encima de los demás
        if (currentShape != null) {
            currentShape.draw(gc);
        }

        // Recuadro de selección discontinuo, lo último que se dibuja.
        // No es un objeto del dibujo: no se guarda ni sale en la lista.
        if (selectedShape != null) {
            Rectangle2D b = selectedShape.getSelectionBounds();
            gc.save();                      // guardamos la configuración del pincel...
            gc.setStroke(Color.DODGERBLUE);
            gc.setLineWidth(1);
            gc.setLineDashes(6, 4);         // línea discontinua: 6 px dibujados, 4 vacíos
            gc.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
            gc.restore();                   // ...y la recuperamos para no afectar a los objetos
        }
    }
}