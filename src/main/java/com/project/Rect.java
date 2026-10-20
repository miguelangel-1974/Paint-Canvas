package com.project;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Rectángulo definido por dos esquinas opuestas: la primera (x1, y1) donde se
// pulsa el ratón y la segunda (x2, y2) donde se suelta. Así se puede dibujar
// arrastrando en cualquier dirección (arriba, abajo, izquierda o derecha).
public class Rect extends Shape {

    private double x1, y1, x2, y2;
    private final Color fillColor;  // color de emplenament (solo lo tienen los rectángulos)

    public Rect(Color color, double width, Color fillColor, double x1, double y1, double x2, double y2) {
        super(color, width);    // el color del contorno y el ancho los guarda Shape
        this.fillColor = fillColor;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    // Cambia la esquina opuesta. Se llama mientras se arrastra el ratón,
    // así el rectángulo "sigue" al cursor (previsualización)
    @Override
    public void setEnd(double x, double y) {
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    public String getTypeName() {
        return "Rectangle";
    }

    @Override
    public void draw(GraphicsContext gc) {
        Rectangle2D b = getBounds();

        // Primero el relleno...
        gc.setFill(fillColor);
        gc.fillRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());

        // ...y encima el contorno, con su color y su ancho
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    // Calcula x, y, ancho y alto a partir de las dos esquinas.
    // x e y son los mínimos (la esquina de arriba a la izquierda) y el ancho
    // y el alto son valores absolutos, para que funcione en cualquier dirección.
    @Override
    public Rectangle2D getBounds() {
        double x = Math.min(x1, x2);
        double y = Math.min(y1, y2);
        return new Rectangle2D(x, y, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    // Mover el rectángulo = mover sus dos esquinas
    @Override
    public void move(double dx, double dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }
}