package structures.subcard.ability;

import java.util.List;

import structures.GameState;
import structures.basic.Tile;

/**
 * 空投能力接口
 * 可以被召唤到棋盘上的任何空格
 */
public interface Airdrop {
    /**
     * 获取单位可以被召唤到的格子列表
     * @param gameState 当前游戏状态
     * @return 可以召唤到的格子列表
     */
    List<Tile> getSummonableTiles(GameState gameState);
}