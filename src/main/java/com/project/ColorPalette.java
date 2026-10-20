package com.project;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Colores predefinidos dibujados como lápices en su propio Canvas.
// El lápiz seleccionado se dibuja más arriba que los demás de su fila.
public class ColorPalette {

    // Los colores de la paleta, en orden (de izquierda a derecha y de arriba a abajo)
    private static final Color[] PENCIL_COLORS = {
        Color.BLACK, Color.GRAY, Color.RED, Color.ORANGE, Color.YELLOW,
        Color.GREEN, Color.CYAN, Color.BLUE, Color.PURPLE, Color.PINK
    };

    private static final int PENCILS_PER_ROW = 5;          // lápices en cada fila
    private static final int NUMBER_OF_ROWS = 2;           // filas de lápices
    private static final double SIDE_MARGIN = 4;           // espacio libre a cada lado de un lápiz
    private static final double SELECTED_LIFT = 14;        // cuánto más arriba sale el lápiz seleccionado
    private static final double WOOD_TIP_HEIGHT = 22;      // altura de la punta de madera (el triángulo)

    private final Canvas canvas;
    private final GraphicsContext gc;
    private Color selectedColor = null;     // color seleccionado (null = ninguno)

    public ColorPalette(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        draw();
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    // Cambia el color seleccionado y redibuja la paleta.
    // Si el color no coincide con ningún lápiz, no se levanta ninguno.
    public void setSelectedColor(Color color) {
        selectedColor = color;
        draw();
    }

    // Selecciona el lápiz que hay en la posición (mouseX, mouseY) del canvas.
    // Lo llama el Controller cuando el usuario hace clic en la paleta.
    public void selectColorAt(double mouseX, double mouseY) {
        // Cada lápiz tiene asignada una "celda": un hueco rectangular del canvas
        double cellWidth = canvas.getWidth() / PENCILS_PER_ROW;
        double cellHeight = canvas.getHeight() / NUMBER_OF_ROWS;

        for (int i = 0; i < PENCIL_COLORS.length; i++) {
            // Esquina de arriba a la izquierda de la celda del lápiz i
            double cellLeft = (i % PENCILS_PER_ROW) * cellWidth;   // % da la columna
            double cellTop = (i / PENCILS_PER_ROW) * cellHeight;   // / da la fila

            // ¿El clic cae dentro de esta celda?
            // Se usa la celda entera (no el lápiz) para que el área de clic
            // no cambie cuando un lápiz sube.
            boolean clickInsideCell = mouseX >= cellLeft && mouseX < cellLeft + cellWidth
                    && mouseY >= cellTop && mouseY < cellTop + cellHeight;

            if (clickInsideCell) {
                selectedColor = PENCIL_COLORS[i];
                draw();
                return;
            }
        }
    }

    // Borra el canvas y dibuja todos los lápices
    private void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double cellWidth = canvas.getWidth() / PENCILS_PER_ROW;
        double cellHeight = canvas.getHeight() / NUMBER_OF_ROWS;

        for (int i = 0; i < PENCIL_COLORS.length; i++) {
            double pencilLeft = (i % PENCILS_PER_ROW) * cellWidth + SIDE_MARGIN;
            double pencilWidth = cellWidth - SIDE_MARGIN * 2;
            double rowTop = (i / PENCILS_PER_ROW) * cellHeight;   // parte de arriba de su fila
            double rowBottom = rowTop + cellHeight;               // parte de abajo de su fila

            // Por defecto el lápiz empieza un poco más abajo que el borde de su fila...
            double pencilTop = rowTop + SELECTED_LIFT;
            // ...pero si es el seleccionado, empieza en el borde: se ve "levantado"
            if (PENCIL_COLORS[i].equals(selectedColor)) {
                pencilTop = rowTop;
            }

            double pencilHeight = rowBottom - pencilTop;
            drawPencil(PENCIL_COLORS[i], pencilLeft, pencilTop, pencilWidth, pencilHeight);
        }
    }

    // Dibuja un lápiz entero. Se le da su esquina de arriba a la izquierda
    // (pencilLeft, pencilTop) y su tamaño total (pencilWidth x pencilHeight)
    private void drawPencil(Color pencilColor, double pencilLeft, double pencilTop,
                            double pencilWidth, double pencilHeight) {

        // El cuerpo (la parte rectangular) empieza justo debajo de la punta de madera
        double bodyTop = pencilTop + WOOD_TIP_HEIGHT;
        double bodyHeight = pencilHeight - WOOD_TIP_HEIGHT;
        double pencilCenterX = pencilLeft + pencilWidth / 2;

        // Cuerpo del lápiz, del color que toque
        gc.setFill(pencilColor);
        gc.fillRect(pencilLeft, bodyTop, pencilWidth, bodyHeight);

        // Franja clara semitransparente para dar sensación de volumen
        gc.setFill(Color.rgb(255, 255, 255, 0.35));
        gc.fillRect(pencilLeft + pencilWidth * 0.15, bodyTop, pencilWidth * 0.25, bodyHeight);

        // Borde del cuerpo
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(pencilLeft, bodyTop, pencilWidth, bodyHeight);

        // Punta de madera: un triángulo con tres puntos
        // (esquina izquierda, esquina derecha y vértice de arriba, en el centro)
        gc.setFill(Color.TAN);
        gc.fillPolygon(
            new double[] { pencilLeft, pencilLeft + pencilWidth, pencilCenterX },
            new double[] { bodyTop, bodyTop, pencilTop },
            3);

        // Mina: un triángulo pequeño del color del lápiz en el extremo de la punta
        double leadHeight = WOOD_TIP_HEIGHT * 0.35;
        double leadHalfWidth = (pencilWidth / 2) * 0.35;
        gc.setFill(pencilColor);
        gc.fillPolygon(
            new double[] { pencilCenterX - leadHalfWidth, pencilCenterX + leadHalfWidth, pencilCenterX },
            new double[] { pencilTop + leadHeight, pencilTop + leadHeight, pencilTop },
            3);
    }
}