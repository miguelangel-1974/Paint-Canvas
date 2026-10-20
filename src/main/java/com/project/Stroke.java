package com.project;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Trazo libre: una lista ordenada de puntos (x, y) con su propio estilo.
// Aunque tenga muchos puntos, es UN solo objeto.
public class Stroke extends Shape {

    // Puntos por los que ha pasado el ratón, en orden
    private final List<Point2D> points = new ArrayList<>();

    public Stroke(Color color, double width) {
        super(color, width);    // el color y el ancho los guarda la clase Shape
    }

    // Añade un punto al final del trazo
    public void addPoint(double x, double y) {
        points.add(new Point2D(x, y));
    }

    // En un trazo libre, "actualizar el final" es añadir un punto nuevo
    @Override
    public void setEnd(double x, double y) {
        addPoint(x, y);
    }

    @Override
    public String getTypeName() {
        return "Traç lliure";
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Si todavía no hay puntos, no hay nada que dibujar
        if (points.isEmpty()) return;

        gc.setStroke(color);
        gc.setLineWidth(width);

        // Empezamos en el primer punto y unimos cada punto con el siguiente
        gc.beginPath();
        gc.moveTo(points.get(0).getX(), points.get(0).getY());
        for (Point2D p : points) {
            gc.lineTo(p.getX(), p.getY());
        }
        gc.stroke();
    }

    // El rectángulo que contiene el trazo se calcula con los valores
    // mínimos y máximos de x e y de todos sus puntos
    @Override
    public Rectangle2D getBounds() {
        if (points.isEmpty()) return new Rectangle2D(0, 0, 0, 0);

        // Empezamos con valores "imposibles" para que el primer punto los sustituya
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    // Mover el trazo = mover TODOS sus puntos
    @Override
    public void move(double dx, double dy) {
        // Point2D no se puede modificar (es inmutable), así que cada punto
        // se sustituye por uno nuevo ya desplazado
        for (int i = 0; i < points.size(); i++) {
            Point2D p = points.get(i);
            points.set(i, new Point2D(p.getX() + dx, p.getY() + dy));
        }
    }
}