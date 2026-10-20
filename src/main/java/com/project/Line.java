package com.project;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Línea recta entre dos puntos: (x1, y1) es el inicio y (x2, y2) es el final
public class Line extends Shape {

    private double x1, y1, x2, y2;

    public Line(Color color, double width, double x1, double y1, double x2, double y2) {
        super(color, width);    // el color y el ancho los guarda la clase Shape
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    // Cambia el punto final. Se llama mientras se arrastra el ratón,
    // así la línea "sigue" al cursor (previsualización)
    @Override
    public void setEnd(double x, double y) {
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    public String getTypeName() {
        return "Línia";
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeLine(x1, y1, x2, y2);
    }

    // Rectángulo que contiene la línea, a partir de los mínimos y máximos
    // de x e y de sus dos extremos
    @Override
    public Rectangle2D getBounds() {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        return new Rectangle2D(minX, minY, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    // Mover la línea = mover sus dos extremos
    @Override
    public void move(double dx, double dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
    }
}