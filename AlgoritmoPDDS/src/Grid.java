import java.util.ArrayList;
import java.util.List;

public class Grid {
    private Nodo[][] nodos;
    private int width, height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        nodos = new Nodo[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodos[y][x] = new Nodo(x, y);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public Nodo getNodoAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return nodos[y][x];
    }

    public List<Nodo> getNeighbors(Nodo Nodo) {
        List<Nodo> neighbors = new ArrayList<>();
        int[][] dirs = { {0, -1}, {-1, 0}, {1, 0}, {0, 1} }; 

        for (int[] dir : dirs) {
            Nodo neighbor = getNodoAt(Nodo.getPosX() + dir[0], Nodo.getPosY() + dir[1]);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
}
