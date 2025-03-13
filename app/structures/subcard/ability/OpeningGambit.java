package structures.subcard.ability;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * 开战效果能力接口
 * 当单位被召唤到场上时触发
 */
public interface OpeningGambit {
    /**
     * 当单位被召唤到场上时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 被召唤的单位
     * @param tile 单位所在的格子
     */
    void onUnitSummoned(ActorRef out, GameState gameState, Unit unit, Tile tile);
}