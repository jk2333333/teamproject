package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import actors.GameActor;
import structures.GameState;

/**
 * Represents a unit on the game board.
 * Each unit has a unique ID, animation state, position, animation data, and
 * image correction information.
 * The newly added `hasMoved` and `hasAttacked` flags track whether the unit has
 * moved or attacked
 * during the current turn. These flags are reset at the beginning of each turn
 * and used in movement
 * and attack logic.
 *
 * @author Dr. Richard McCreadie
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson serialization tool

	int id;
	private int health;
	private int attack;
	@JsonIgnore
	private Tile tile;
	private int owner;

	private UnitAnimationType animation;
	private Position position;
	private UnitAnimationSet animations;
	private ImageCorrection correction;

	// 单位状态与行动相关的字段
	private int moves;
	private int maxMoves;
	private int attacks;
	private int maxAttacks;
	private boolean sleeping;
	private boolean stunned;
	private boolean isAvartar1;
	private boolean isAvartar2;

	public Unit() {
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
		this.stunned = false;
		this.isAvartar1 = false;
		this.isAvartar2 = false;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
		this.stunned = false;
		this.isAvartar1 = false;
		this.isAvartar2 = false;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(),
				currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
		this.tile = currentTile;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
		this.stunned = false;
		this.isAvartar1 = false;
		this.isAvartar2 = false;
	}

	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
		this.stunned = false;
		this.isAvartar1 = false;
		this.isAvartar2 = false;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	// Getter and Setter methods
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UnitAnimationType getAnimation() {
		return animation;
	}

	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	/**
	 * Sets the unit's owner (1 = Player, 2 = AI)
	 */
	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	/**
	 * Sets the unit's position based on the given tile.
	 * 
	 * @param tile The tile where the unit is placed.
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
		if (this.tile != null) {
			this.tile.setUnit(null);
		}
		this.tile = tile;
		tile.setUnit(this);
	}

	/**
	 * 返回单位所在的格子
	 * @return 单位所在的格子
	 */
	@JsonIgnore
	public Tile getTile() {
		return tile;
	}

	/**
	 * 设置单位所在的格子
	 * @param tile 单位所在的格子
	 */
	@JsonIgnore
	public void setTile(Tile tile) {
		setPositionByTile(tile);
	}

	// 眩晕状态相关方法
	@JsonIgnore
	public boolean isStunned() {
		return stunned;
	}

	@JsonIgnore
	public void setStunned(boolean stunned) {
		this.stunned = stunned;
	}

	// 移动次数相关方法
	@JsonIgnore
	public int getMoves() {
		return moves;
	}

	@JsonIgnore
	public void setMoves(int moves) {
		this.moves = moves;
	}

	@JsonIgnore
	public int getMaxMoves() {
		return maxMoves;
	}

	@JsonIgnore
	public void setMaxMoves(int maxMoves) {
		this.maxMoves = maxMoves;
	}

	// 攻击次数相关方法
	@JsonIgnore
	public int getAttacks() {
		return attacks;
	}

	@JsonIgnore
	public void setAttacks(int attacks) {
		this.attacks = attacks;
	}

	@JsonIgnore
	public int getMaxAttacks() {
		return maxAttacks;
	}

	@JsonIgnore
	public void setMaxAttacks(int maxAttacks) {
		this.maxAttacks = maxAttacks;
	}

	// 睡眠状态相关方法
	@JsonIgnore
	public boolean isSleeping() {
		return sleeping;
	}

	@JsonIgnore
	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
	}

	/**
	 * 重置单位的回合状态
	 * 在每个回合开始时调用
	 */
	@JsonIgnore
	public void resetTurnStatus() {
		this.moves = 0;
		this.attacks = 0;
		this.sleeping = false;
		// 如果单位被眩晕，在回合结束时解除眩晕状态
		this.stunned = false;
	}

	/**
	 * 检查单位是否可以移动
	 * @return 如果单位可以移动则返回true
	 */
	@JsonIgnore
	public boolean canMove() {
		return (!stunned && !sleeping && maxMoves - moves > 0);
	}

	/**
	 * 检查单位是否可以攻击
	 * @param gameState 当前游戏状态
	 * @return 如果单位可以攻击则返回true
	 */
	@JsonIgnore
	public boolean canAttack(GameState gameState) {
		// 检查周围是否有敌方单位
		// 这个实现与UnitManager中的方法类似

		// 如果单位已经攻击了或被眩晕或者处于睡眠状态，它不能攻击
		if (stunned || sleeping || attacks >= maxAttacks) {
			return false;
		}

		// 检查周围是否有敌方单位
		int[][] directions = new int[][] {
				{ 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent tiles (right, left, down, up)
				{ 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal tiles
		};

		int cx = position.getTilex();
		int cy = position.getTiley();

		// 检查周围格子
		for (int[] dir : directions) {
			int nx = cx + dir[0];
			int ny = cy + dir[1];

			// 检查坐标是否合法
			if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) {
				Tile targetTile = gameState.board[nx][ny];
				Unit targetUnit = targetTile.getUnit();

				// 如果格子有单位，且是敌方单位
				if (targetUnit != null && targetUnit.getOwner() != this.owner) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 记录单位移动一次
	 */
	@JsonIgnore
	public void addMoves() {
		this.moves++;
	}

	/**
	 * 记录单位攻击一次
	 */
	@JsonIgnore
	public void addAttacks() {
		this.attacks++;
	}

	/**
	 * 设置单位不能再移动
	 */
	@JsonIgnore
	public void cantMove() {
		this.moves = this.maxMoves;
	}

	/**
	 * 设置单位为指定玩家的化身
	 * @param player 玩家编号（1或2）
	 */
	@JsonIgnore
	public void setIsAvartar(int player) {
		if (player == 1) {
			this.isAvartar1 = true;
		} else if (player == 2) {
			this.isAvartar2 = true;
		} else {
			System.out.println("Player not exist. ");
		}
	}

	/**
	 * 检查单位是否为指定玩家的化身
	 * @param player 玩家编号（1或2）
	 * @return 如果是指定玩家的化身返回true
	 */
	@JsonIgnore
	public boolean getIsAvartar(int player) {
		if (player == 1) {
			return this.isAvartar1;
		} else if (player == 2) {
			return this.isAvartar2;
		}
		System.out.println("Player not exist. ");
		return false;
	}
	
	/**
	 * 获取单位的配置文件路径
	 * 这个方法用于卡牌效果中识别单位类型
	 * @return 单位的配置文件路径，如果没有则返回null
	 */
	@JsonIgnore
	public String getUnitConfig() {
		// 这个字段应该在单位创建时由UnitManager设置
		// 默认实现返回null
		return null;
	}
	
	/**
	 * 获取单位的卡牌名称
	 * 这个方法用于显示单位名称
	 * @return 单位的卡牌名称，如果没有则返回"Unknown Unit"
	 */
	@JsonIgnore
	public String getCardname() {
		// 这个字段应该在单位创建时由UnitManager设置
		// 默认实现返回一个通用名称
		return "Unknown Unit";
	}
}