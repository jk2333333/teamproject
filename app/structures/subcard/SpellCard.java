package structures.subcard;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;

/**
 * 法术卡牌的抽象类，继承自Card类，实现了法术卡共有的行为。
 * 所有的法术卡都应该继承此类，而不是直接继承Card类。
 */
public abstract class SpellCard extends Card {
    
    public SpellCard() {
        this.isCreature = false;
        this.unitConfig = null;
    }
    
    /**
     * 当法术被施放时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param tile 目标格子
     */
    @Override
    public abstract void onCardPlayed(ActorRef out, GameState gameState, Tile tile);
    
    /**
     * 获取法术的有效目标格子
     * 对于不同的法术，目标选择规则可能不同，需要被子类重写
     */
    @Override
    public abstract java.util.List<Tile> getValidTargets(GameState gameState);
}