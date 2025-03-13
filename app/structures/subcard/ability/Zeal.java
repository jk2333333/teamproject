package structures.subcard.ability;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

/**
 * 热情能力接口
 * 当特定条件满足时触发效果
 */
public interface Zeal {
    /**
     * 当头像受到伤害时执行的逻辑
     * @param out WebSocket通信通道
     * @param gameState 当前游戏状态
     * @param unit 拥有热情能力的单位
     */
    void onAvatarDamaged(ActorRef out, GameState gameState, Unit unit);
}