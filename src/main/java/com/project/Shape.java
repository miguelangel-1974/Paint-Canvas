package com.project;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Clase base de todos los objetos que se pueden dibujar (trazo libre, línea y rectángulo).
// Es "abstract": no se puede crear con "new Shape(...)", solo sus clases hijas
// (Stroke, Line y Rect). Sirve para que las tres tengan los mismos métodos
// y así poder guardarlas todas juntas en una misma lista.
public abstract class Shape {

    // Contador de identificadores. Es "static": hay UNA sola copia compartida
    // por todos los objetos, así cada objeto nuevo recibe el siguiente número
    // (1, 2, 3...). Sin "static" cada objeto tendría su propio contador
    // y todos tendrían el id 1.
    private static int nextId = 1;

    private int id;             // número que identifica al objeto (se muestra en la lista)
    protected Color color;      // color de la línea o del contorno
    protected double width;     // ancho de la línea

    // Constructor: guarda el estilo del objeto y le asigna su identificador
    public Shape(Color color, double width) {
        this.id = nextId;   // primero le damos el número actual...
        nextId++;           // ...y luego subimos el contador para el siguiente objeto
        this.color = color;
        this.width = width;
    }

    // ---- Métodos que cada clase hija implementa a su manera ----

    // Nombre del tipo de objeto, por ejemplo "Línia"
    public abstract String getTypeName();

    // Dibuja el objeto en el canvas
    public abstract void draw(GraphicsContext gc);

    // Rectángulo que contiene justo al objeto (sin margen)
    public abstract Rectangle2D getBounds();

    // Mueve el objeto entero sumando (dx, dy) a sus coordenadas
    public abstract void move(double dx, double dy);

    // Actualiza el punto final mientras se arrastra el ratón
    // (en un trazo libre se añade un punto nuevo a la lista)
    public abstract void setEnd(double x, double y);

    // ---- Métodos comunes a todos los objetos ----

    public int getId() {
        return id;
    }

    // Texto que se muestra en la lista, por ejemplo "Línia 2"
    public String getName() {
        return getTypeName() + " " + id;
    }

    // Rectángulo de selección: el de getBounds() más un margen por cada lado.
    // El margen tiene en cuenta el ancho de la línea, para que el recuadro
    // rodee todo el objeto y se vea también en líneas horizontales o verticales
    // (cuyo rectángulo tendría ancho o alto 0).
    public Rectangle2D getSelectionBounds() {
        Rectangle2D b = getBounds();
        double margin = width / 2 + 4;
        return new Rectangle2D(
            b.getMinX() - margin,
            b.getMinY() - margin,
            b.getWidth() + margin * 2,
            b.getHeight() + margin * 2
        );
    }

    // Devuelve true si el punto (x, y) está dentro del recuadro de selección.
    // Se usa para saber si el usuario ha pulsado sobre el objeto y quiere moverlo.
    public boolean containsPoint(double x, double y) {
        return getSelectionBounds().contains(x, y);
    }

    // El ListView llama a toString() para saber qué texto mostrar de cada elemento
    @Override
    public String toString() {
        return getName();
    }
}