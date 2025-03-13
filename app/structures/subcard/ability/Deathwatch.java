package structures.subcard.ability;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * 死亡守望能力接口
 * 当任何单位死亡时触发
 */
public interface Deathwatch {
    /**
     * 当任何单位死亡时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 具有死亡守望能力的单位
     */
    void onUnitDeath(ActorRef out, GameState gameState, Unit unit);
}