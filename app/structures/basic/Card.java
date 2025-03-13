package structures.basic;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import akka.actor.ActorRef;
import structures.GameState;

/**
 * 卡牌的基本抽象类，定义了所有卡牌共有的属性和行为。
 * 这个类被设计为可扩展的，允许不同类型的卡牌实现自己的特定逻辑。
 */
public abstract class Card {

    protected int id;
    protected String cardname;
    protected int manacost;
    protected MiniCard miniCard;
    protected BigCard bigCard;
    protected boolean isCreature;
    protected String unitConfig;
    
    // 攻击力和生命值（对于生物卡有意义）
    protected int attack;
    protected int health;

    public Card() {
    }

    /**
     * 当卡牌被打出时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param tile 目标格子
     */
    @JsonIgnore
    public abstract void onCardPlayed(ActorRef out, GameState gameState, Tile tile);

    /**
     * 返回此卡牌可以选择的有效目标格子
     * @param gameState 当前游戏状态
     * @return 可选择的格子列表
     */
    @JsonIgnore
    public abstract List<Tile> getValidTargets(GameState gameState);

    /**
     * 检查卡牌是否可以在当前游戏状态下使用
     * @param gameState 当前游戏状态
     * @return 如果可以使用则返回true，否则返回false
     */
    @JsonIgnore
    public boolean canPlay(GameState gameState) {
        // 默认检查法力值是否足够
        int playerMana = gameState.currentPlayer == 1 ? 
                         gameState.player1.getMana() : 
                         gameState.player2.getMana();
        return playerMana >= manacost && !getValidTargets(gameState).isEmpty();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public int getManacost() {
        return manacost;
    }

    public void setManacost(int manacost) {
        this.manacost = manacost;
    }

    public MiniCard getMiniCard() {
        return miniCard;
    }

    public void setMiniCard(MiniCard miniCard) {
        this.miniCard = miniCard;
    }

    public BigCard getBigCard() {
        return bigCard;
    }

    public void setBigCard(BigCard bigCard) {
        this.bigCard = bigCard;
    }

    public boolean isCreature() {
        return isCreature;
    }

    public boolean getIsCreature() {
        return isCreature;
    }

    public void setIsCreature(boolean isCreature) {
        this.isCreature = isCreature;
    }

    public String getUnitConfig() {
        return unitConfig;
    }

    public void setUnitConfig(String unitConfig) {
        this.unitConfig = unitConfig;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}