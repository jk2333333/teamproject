package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.ActorRef;
import commands.BasicCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel
 * position
 * and a grid position. Tiles also have a width and height in pixels and a
 * series of urls
 * that point to the different renderable textures that a tile might have.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Tile {

	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java
																// objects from a file

	private List<String> tileTextures;
	private int xpos;
	private int ypos;
	private int width;
	private int height;
	private int tilex;
	private int tiley;

	private int highlightStatus;

	@JsonIgnore
	private Unit unit;

	public Tile() {
	}

	public Tile(String tileTexture, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		tileTextures = new ArrayList<String>(1);
		tileTextures.add(tileTexture);
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
		this.highlightStatus = 0;
	}

	public Tile(List<String> tileTextures, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		this.tileTextures = tileTextures;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
		this.highlightStatus = 0;
	}

	public List<String> getTileTextures() {
		return tileTextures;
	}

	public void setTileTextures(List<String> tileTextures) {
		this.tileTextures = tileTextures;
	}

	public int getXpos() {
		return xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getTilex() {
		return tilex;
	}

	public void setTilex(int tilex) {
		this.tilex = tilex;
	}

	public int getTiley() {
		return tiley;
	}

	public void setTiley(int tiley) {
		this.tiley = tiley;
	}

	/**
	 * Loads a tile from a configuration file
	 * parameters.
	 * 
	 * @param configFile
	 * @return
	 */
	public static Tile constructTile(String configFile) {

		try {
			Tile tile = mapper.readValue(new File(configFile), Tile.class);
			return tile;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

	public void setHighlightStatus(ActorRef out, int mode) {
		BasicCommands.drawTile(out, this, mode);
		// System.out.println("Set: " + this.getTilex() + this.getTiley() + mode);
		this.highlightStatus = mode;
	}

	public int getHighlightStatus() {
		return this.highlightStatus;
	}

	// Add getUnit() method.
	public Unit getUnit() {
		return unit;
	}

	// Add setUnit() method.
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

}
