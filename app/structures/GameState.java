package structures;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.OrderedCardLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Stores the game state, including all game-related data structures.
 * This class only maintains data storage and provides methods such as `loadDecks()`.
 * It does not contain logic for drawing cards or switching turns.
 */
public class GameState {

    public boolean gameInitalised = false; // Indicates if the game has been initialized

    // The game board (9x5 tiles)
    public Tile[][] board = new Tile[9][5];

    // Card decks and hands for both players (stored as Lists)
    public List<Card> player1Deck = new ArrayList<>();
    public List<Card> player2Deck = new ArrayList<>();
    public List<Card> player1Hand = new ArrayList<>();
    public List<Card> player2Hand = new ArrayList<>();

    // Current game turn information
    public int currentTurn = 1;
    public int currentPlayer = 1; // 1 for player 1, 2 for player 2

    // Player and their avatars (Hero Units)
    public Player player1;
    public Player player2;
    public Unit player1Avatar;
    public Unit player2Avatar;

    // List of active units for each player
    public List<Unit> playerUnits;

    // Tiles and cards related to user interactions (highlighted/movable/selected)
    public List<Tile> movableTiles;
    public List<Tile> attackableTiles;
    public List<Tile> summonableTiles;
    public Card selectedCard;
    public int selectedHandPosition = -1; // Default value when no card is selected
    public Unit selectedUnit;
    private int currentUnitId;

    public Tile selectedTile;  // 记录当前选中的棋子 Tile

    // 能力系统相关的数据结构
    private Map<Unit, List<Consumer<Unit>>> deathwatchListeners = new HashMap<>();
    private List<Unit> provokeUnits = new ArrayList<>();
    private Map<Unit, Integer> artifactDurability = new HashMap<>();

    /**
     * Initializes the game state, including player objects and empty lists for cards and units.
     */
    public GameState() {
        player1 = new Player(20, 0);
        player2 = new Player(20, 0);

        playerUnits = new ArrayList<>();

        movableTiles = new ArrayList<>();
        attackableTiles = new ArrayList<>();
        summonableTiles = new ArrayList<>();
        currentUnitId = 0;
    }

    /**
     * Loads the decks for both players.
     * Each player is assigned a deck of 20 cards.
     */
    public void loadDecks() {
        this.player1Deck = OrderedCardLoader.getPlayer1Cards(20);
        this.player2Deck = OrderedCardLoader.getPlayer2Cards(20);
    }

    /**
     * Sets the avatars (hero units) for both players.
     * @param p1 Avatar for player 1
     * @param p2 Avatar for player 2
     */
    public void setAvatars(Unit p1, Unit p2) {
        this.player1Avatar = p1;
        this.player2Avatar = p2;
        playerUnits.add(player1Avatar);
        playerUnits.add(player2Avatar);
    }

    /**
     * Retrieves a unit by its unique ID.
     * @param id The unit's unique identifier
     * @return The unit with the given ID, or `null` if not found
     */
    public Unit getUnitById(int id) {
        for (Unit u : playerUnits) {
            if (u.getId() == id)
                return u;
        }
        return null;
    }
    
    public int getCurrentUnitId() {
        int id = this.currentUnitId;
        ++this.currentUnitId;
        return id;
    }
    
    // 死亡守望相关方法
    
    /**
     * 为单位添加死亡守望监听器
     * @param unit 拥有死亡守望能力的单位
     * @param listener 当任何单位死亡时执行的回调
     */
    public void addDeathwatchListener(Unit unit, Consumer<Unit> listener) {
        if (!deathwatchListeners.containsKey(unit)) {
            deathwatchListeners.put(unit, new ArrayList<>());
        }
        deathwatchListeners.get(unit).add(listener);
    }

    /**
     * 触发所有死亡守望监听器
     * @param deadUnit 死亡的单位
     */
    public void triggerDeathwatch(Unit deadUnit) {
        for (Map.Entry<Unit, List<Consumer<Unit>>> entry : deathwatchListeners.entrySet()) {
            Unit unit = entry.getKey();
            if (unit.getHealth() <= 0) continue; // 忽略已死亡的单位
            
            List<Consumer<Unit>> listeners = entry.getValue();
            for (Consumer<Unit> listener : listeners) {
                listener.accept(deadUnit);
            }
        }
    }
    
    /**
     * 移除单位的所有死亡守望监听器
     * @param unit 要移除监听器的单位
     */
    public void removeDeathwatchListeners(Unit unit) {
        deathwatchListeners.remove(unit);
    }
    
    // 嘲讽相关方法
    
    /**
     * 注册一个具有嘲讽能力的单位
     * @param unit 具有嘲讽能力的单位
     */
    public void registerProvokeUnit(Unit unit) {
        if (!provokeUnits.contains(unit)) {
            provokeUnits.add(unit);
        }
    }
    
    /**
     * 取消注册嘲讽单位
     * @param unit 要取消注册的单位
     */
    public void unregisterProvokeUnit(Unit unit) {
        provokeUnits.remove(unit);
    }
    
    /**
     * 获取所有具有嘲讽能力的单位
     * @return 嘲讽单位列表
     */
    public List<Unit> getProvokeUnits() {
        return new ArrayList<>(provokeUnits);
    }
    
    /**
     * 检查单位是否被嘲讽
     * @param unit 要检查的单位
     * @return 如果单位被嘲讽则返回true
     */
    public boolean isProvoked(Unit unit) {
        if (provokeUnits.isEmpty()) return false;
        
        int x = unit.getPosition().getTilex();
        int y = unit.getPosition().getTiley();
        
        // 检查单位周围是否有敌方嘲讽单位
        for (Unit provokeUnit : provokeUnits) {
            if (provokeUnit.getOwner() == unit.getOwner()) continue; // 忽略同队的嘲讽单位
            
            int px = provokeUnit.getPosition().getTilex();
            int py = provokeUnit.getPosition().getTiley();
            
            // 如果嘲讽单位在相邻格子（包括对角线）
            if (Math.abs(x - px) <= 1 && Math.abs(y - py) <= 1) {
                return true;
            }
        }
        
        return false;
    }
    
    // 神器相关方法
    
    /**
     * 为单位设置神器耐久度
     * @param unit 拥有神器的单位
     * @param durability 初始耐久度
     */
    public void setArtifactDurability(Unit unit, int durability) {
        artifactDurability.put(unit, durability);
    }
    
    /**
     * 减少单位神器的耐久度
     * @param unit 拥有神器的单位
     * @return 剩余的耐久度，如果单位没有神器则返回0
     */
    public int decreaseArtifactDurability(Unit unit) {
        if (!artifactDurability.containsKey(unit)) return 0;
        
        int durability = artifactDurability.get(unit) - 1;
        artifactDurability.put(unit, durability);
        
        // 如果耐久度为0，移除神器
        if (durability <= 0) {
            artifactDurability.remove(unit);
        }
        
        return durability;
    }
    
    /**
     * 检查单位是否拥有神器
     * @param unit 要检查的单位
     * @return 如果单位拥有神器则返回true
     */
    public boolean hasArtifact(Unit unit) {
        return artifactDurability.containsKey(unit) && artifactDurability.get(unit) > 0;
    }
    
    /**
     * 获取单位神器的当前耐久度
     * @param unit 拥有神器的单位
     * @return 神器耐久度，如果单位没有神器则返回0
     */
    public int getArtifactDurability(Unit unit) {
        return artifactDurability.getOrDefault(unit, 0);
    }
}