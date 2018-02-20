import java.awt.Graphics;

public class TileGrid {
	public Tile[][] map; //Array of Tiles

	//Array size
	private static final int COLUMN = 24;
	private static final int ROW = 25;

	//Grid offset
	private float x = 42.5f;
	private float y;
	
	//Constructor that builds the array of tiles
	public TileGrid() {
		map = new Tile[COLUMN][ROW];  //Dimension of the Board
		for (int i = 0; i < map.length; i++) {
			y = 24f;
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = new Tile(x, y, j, i);
				y += 23;
			}

			x += 23;
		}
	}

	//Draw the grid tiles onto the map
	public void drawGrid(Graphics g) {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j].drawTile(g);
			}
		}
	}
}
